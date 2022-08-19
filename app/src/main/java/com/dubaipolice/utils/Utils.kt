package com.dubaipolice.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.*
import android.media.ExifInterface
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.METADATA_KEY_DURATION
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.loader.content.CursorLoader
import androidx.work.WorkManager
import com.dubaipolice.BuildConfig
import com.dubaipolice.MainApplication
import com.dubaipolice.R
import com.dubaipolice.api.ApiClient
import com.dubaipolice.api.ApiResponseCallback
import com.dubaipolice.api.GenricError
import com.dubaipolice.db.AppDatabase
import com.dubaipolice.model.CommonResponse
import com.dubaipolice.service.LocationUpdaterToServer
import com.dubaipolice.view.activity.LoginActivity
import com.dubaipolice.wrapper.Resource
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


object Utils {
    fun isNetConnected(context: Context): Boolean {
        try {
            val cm =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val n = cm.activeNetwork
            if (n != null) {
                val nc = cm.getNetworkCapabilities(n)
                return nc!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                ) || nc.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun showToast(context: Context, s: String) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show()
    }

    fun showToastLong(context: Context, s: String) {
        Toast.makeText(context, s, Toast.LENGTH_LONG).show()
    }

    //check and return true if permission is granted
    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean =
        permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * This function is used to compress image
     */
    fun compressImage(context: Context?, imageUri: String?): String {

        val filePath: String? = imageUri
        var scaledBitmap: Bitmap? = null
        val options = BitmapFactory.Options()

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true
        var bmp = BitmapFactory.decodeFile(filePath, options)
        var actualHeight = options.outHeight
        var actualWidth = options.outWidth

//      max Height and width values of the compressed image is taken as 816x612
        val maxHeight = 1024.0f //816.0f;
        val maxWidth = 1024.0f //612.0f;
        var imgRatio = (actualWidth / actualHeight).toFloat()
        val maxRatio = maxWidth / maxHeight

//      width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight
                actualWidth = (imgRatio * actualWidth).toInt()
                actualHeight = maxHeight.toInt()
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth
                actualHeight = (imgRatio * actualHeight).toInt()
                actualWidth = maxWidth.toInt()
            } else {
                actualHeight = maxHeight.toInt()
                actualWidth = maxWidth.toInt()
            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true
        options.inInputShareable = true
        options.inTempStorage = ByteArray(16 * 1024)
        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }
        val ratioX = actualWidth / options.outWidth.toFloat()
        val ratioY = actualHeight / options.outHeight.toFloat()
        val middleX = actualWidth / 2.0f
        val middleY = actualHeight / 2.0f
        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
        val canvas = Canvas(scaledBitmap!!)
        canvas.setMatrix(scaleMatrix)
        canvas.drawBitmap(
            bmp,
            middleX - bmp.width / 2,
            middleY - bmp.height / 2,
            Paint(Paint.FILTER_BITMAP_FLAG)
        )

//      check the rotation of the image and display it properly
        val exif: ExifInterface
        try {
            exif = ExifInterface(filePath!!)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, 0
            )
            Log.d("EXIF", "Exif: $orientation")
            val matrix = Matrix()
            if (orientation == 6) {
                matrix.postRotate(90f)
                Log.d("EXIF", "Exif: $orientation")
            } else if (orientation == 3) {
                matrix.postRotate(180f)
                Log.d("EXIF", "Exif: $orientation")
            } else if (orientation == 8) {
                matrix.postRotate(270f)
                Log.d("EXIF", "Exif: $orientation")
            }
            scaledBitmap = Bitmap.createBitmap(
                scaledBitmap, 0, 0,
                scaledBitmap.width, scaledBitmap.height, matrix,
                true
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val out: FileOutputStream?
        val filename: String = getFilename(context!!)
        try {
            out = FileOutputStream(filename)

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap!!.compress(Bitmap.CompressFormat.JPEG, 40, out)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            Log.e("FileNotFound", "FileNotFound")
        }
        return filename
    }

    /**
     * This function is used to create the image directory
     */
    private fun getFilename(context: Context): String {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val imageFileName = "JPEG_" + timeStamp + "_"
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val image = File.createTempFile(
                imageFileName,  /* prefix */".jpg",  /* suffix */storageDir /* directory */
            )
            image.absolutePath
        } catch (e: java.lang.Exception) {
            ""
        }
    }

    /**
     * This function is used for image compression helper
     */
    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        val totalPixels = (width * height).toFloat()
        val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }
        return inSampleSize
    }

    fun hideKeyboard(context: Context) {
        try {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow((context as Activity).currentFocus!!.windowToken, 0)
            removeFocus(context)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun removeFocus(context: Context) {
        /*
         * Remove focus from any edittext
         * */
        try {
            val current = (context as Activity).currentFocus
            if (current != null) {
                current.clearFocus()
                current.isFocusableInTouchMode = false
                current.isFocusable = false
                current.isFocusableInTouchMode = true
                current.isFocusable = true
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun showKeyboard(context: Context, editText: EditText) {
        try {
            val inputMethodManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.toggleSoftInputFromWindow(
                editText.applicationWindowToken,
                InputMethodManager.SHOW_FORCED, 0
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun showKeyboard(context: Context) {
        try {
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .toggleSoftInput(
                    InputMethodManager.SHOW_FORCED,
                    InputMethodManager.HIDE_IMPLICIT_ONLY
                )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Prevent multiple button clicks at the same time
     *
     * @param view
     */
    fun preventMultipleClicks(view: View) {
        try {
            view.isEnabled = false
            view.postDelayed({ view.isEnabled = true }, 500)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun logout() {

        clearExistingData()

        val i = Intent(MainApplication.appContext, LoginActivity::class.java)
        i.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        MainApplication.appContext.startActivity(i)
    }

    fun clearExistingData() {

        WorkManager.getInstance(MainApplication.appContext).cancelAllWork()

        MainApplication.connection?.disconnectXmpp("dis")
        MainApplication.connection = null

        SharedPref.clearData()

        AppDatabase.getAppDatabase(MainApplication.appContext)
            ?.groupInfoTableDao()
            ?.deleteAllGroups()

        AppDatabase.getAppDatabase(MainApplication.appContext)
            ?.messageInfoTableDao()
            ?.deleteAllChats()

        AppDatabase.getAppDatabase(MainApplication.appContext)
            ?.memberInfoTableDao()
            ?.deleteAllMembers()

    }

    fun Activity.sessionExpired(mContext: Context) {
        runOnUiThread {
            val alertDialog = android.app.AlertDialog.Builder(this)
//                .setTitle(getString(R.string.text_session_expired))
                .setMessage(getString(R.string.session_expired))
                .setCancelable(false)
                .setPositiveButton(
                    getString(R.string.ok)
                ) { _, _ ->
                    SharedPref.clearData()
                    startActivity(Intent(
                        this@sessionExpired, LoginActivity::class.java
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }.create()
            alertDialog.show()
        }
    }

    fun sessionExpiredPopup() {
        try {

            val alertDialog = android.app.AlertDialog.Builder(MainApplication.appContext)
//                .setTitle(getString(R.string.text_session_expired))
                .setMessage(MainApplication.appContext.getString(R.string.session_expired))
                .setCancelable(false)
                .setPositiveButton(
                    MainApplication.appContext.getString(R.string.ok)
                ) { _, _ ->
                    logoutApi()
                }.create()
            alertDialog.show()

        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun logoutApi()
    {

        MyProgressDialog.show(MainApplication.appContext)

        val call: Call<CommonResponse> = ApiClient.request!!.logout(
            Utils.getSelectedLanguage()!!,
            SharedPref.readString(AppConstants.KEY_TOKEN)!!,
            SharedPref.readInt(AppConstants.KEY_USER_ID).toString()
        )
        call.enqueue(object : ApiResponseCallback<CommonResponse?>() {

            override fun onSuccess(response: CommonResponse?) {
                MyProgressDialog.dismiss()
                logout()
            }

            override fun onError(msg: GenricError?) {
                MyProgressDialog.dismiss()
                logout()
            }


        })

    }

    fun forceUpdate(mContext: Context) {
        val alertDialog = android.app.AlertDialog.Builder(mContext)
            .setTitle(mContext.getString(R.string.title_force_update))
            .setMessage(mContext.getString(R.string.message_force_update))
            .setCancelable(false)
            .setPositiveButton(
                mContext.getString(R.string.ok),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        //openUrl(mContext,"https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
                    }

                }).create()
        alertDialog.show()

        //Overriding the handler immediately after show is probably a better approach than OnShowListener as described below
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val wantToCloseDialog = false
            //Do stuff, possibly set wantToCloseDialog to true then...
            if (wantToCloseDialog)
                alertDialog.dismiss()
            //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.

            openUrl(
                mContext,
                "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
            )

        }

    }


    fun openUrl(mContext: Context, url: String) {
        mContext.startActivity(Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        })
    }

    fun getSelectedLanguage(): String? {

        var language = SharedPref.readString(AppConstants.KEY_LANGUAGE)

        if (!TextUtils.isEmpty(language)) {
            return language
        } else {
            return AppConstants.LANGUAGE_ENGLISH
        }

    }

    fun getAppVersion(mContext: Context): String? {
        return try {
            mContext.packageManager.getPackageInfo(mContext.packageName, 0).versionName
        } catch (e: java.lang.Exception) {
            //e.printStackTrace()();
            ""
        }
    }

    fun getJidFromFullJid(fullJid: String): String? {
        return try {
            fullJid.substringBefore("/")
        } catch (e: java.lang.Exception) {
            //e.printStackTrace()();
            ""
        }
    }

    fun getUserFromJid(jid: String): String? {
        return try {
            jid.substringBefore("@")
        } catch (e: java.lang.Exception) {
            //e.printStackTrace()();
            ""
        }
    }

    fun getGroupFromJid(jid: String): String? {
        return try {
            jid.substringBefore("@")
        } catch (e: java.lang.Exception) {
            //e.printStackTrace()();
            ""
        }
    }

    fun getFileExtension(fileName: String): String {
        return try {
            fileName.substringAfterLast(".")
        } catch (e: java.lang.Exception) {
            //e.printStackTrace()();
            ""
        }
    }

    fun generateRandomUniqueId(): String? {
        return try {
            //UUID.randomUUID().toString().replace("-", "")
            UUID.randomUUID().toString()
        } catch (e: java.lang.Exception) {
            //e.printStackTrace()();
            System.currentTimeMillis().toString()
        }
    }

    fun View.showSnack(mContext: Context, message: String) {
        Snackbar.make(
            this,
            message,
            Snackbar.LENGTH_LONG
        ).also { snack ->
            snack.setAction(mContext.getString(R.string.ok)) {
                snack.dismiss()
            }
        }.show()
    }

    fun ContentResolver.getFileName(fileUri: Uri, mContext: Context): String {
        var name = ""
        val proj = arrayOf(MediaStore.Video.Media.DATA)
        return try {
            val returnCursor = this.query(fileUri, null, null, null, null)
            if (returnCursor != null) {
                val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                returnCursor.moveToFirst()
                name = returnCursor.getString(nameIndex)
                returnCursor.close()
            }
            name
        } catch (e: Exception) {
            val cursorLoader = CursorLoader(
                mContext,
                fileUri, proj, null, null, null
            )
            val cursor: Cursor? = cursorLoader.loadInBackground()
            if (cursor != null) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                name = cursor.getString(nameIndex)
                cursor.close()
            }
            name

        }
    }


    /*fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
        return outputStream().use { out ->
            bitmap.compress(format, quality, out)
            out.flush()
        }
    }*/

    fun bitmapToFile(
        context: Context,
        bitmap: Bitmap,
        fileNameToSave: String
    ): File { // File name like "image.png"
        //create a file to write bitmap datazz
        Log.e("StartTime3", System.currentTimeMillis().toString())
        val file: File?
        var fileNameWithOutExt = "image.png"

        val pos = fileNameToSave.lastIndexOf(".")
        if (pos > 0) {
            fileNameWithOutExt = fileNameToSave.trim().substring(0, pos).trim().plus(".png")
        }
        fileNameWithOutExt = fileNameWithOutExt.replace(" ", "_")

        return try {
            val recorderDirectory = File(context.externalCacheDir, "/thumb/")
            recorderDirectory.mkdirs()
            file =
                File(recorderDirectory.path + "/" + File.separator + fileNameWithOutExt.trim())
            file.createNewFile()
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            val bitmapData = bos.toByteArray()
            val fos = FileOutputStream(file)
            fos.write(bitmapData)
            fos.flush()
            fos.close()
            Log.e("StartTime4", System.currentTimeMillis().toString())
            file
        } catch (e: Exception) {
            Utils.showToast(context, e.message!!)
            e.printStackTrace()
            File(fileNameWithOutExt)
        }
    }

    fun getMimeType(filename: String): String {
        val type: String?
        val extension: String = MimeTypeMap.getFileExtensionFromUrl(filename)
        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        return type.toString()
    }

    fun getBitmapFromRes(context: Context, @DrawableRes resId: Int): Bitmap? {
        val drawable = ResourcesCompat.getDrawable(context.resources, resId, context.theme)
        val canvas = Canvas()
        val bitmap = drawable?.intrinsicWidth?.let {
            val createBitmap = Bitmap.createBitmap(
                it,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            createBitmap
        }
        canvas.setBitmap(bitmap)
        drawable?.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable?.draw(canvas)
        return bitmap
    }

    fun File.getMediaDuration(context: Context): Long {
        if (!exists()) return 0
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, Uri.parse(absolutePath))
        val durationStr: String? = retriever.extractMetadata(METADATA_KEY_DURATION)
        retriever.release()
        return durationStr?.toLongOrNull() ?: 0
    }

    fun formatMilliSecond(milliseconds: Long): String {
        var finalTimerString = ""
        val secondsString: String
        val hours = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes = (milliseconds % (1000 * 60 * 60)).toInt() / (1000 * 60)
        val seconds = (milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()

        // Add hours if there
        if (hours > 0) {
            finalTimerString = "$hours:"
        }
        secondsString = if (seconds < 10) {
            "0$seconds"
        } else {
            "" + seconds
        }
        finalTimerString = "$finalTimerString$minutes:$secondsString"
        return finalTimerString
    }

    //check google play service availability
    //result code is SERVICE_MISSING, SERVICE_VERSION_UPDATE_REQUIRED, or SERVICE_DISABLED
    fun isGooglePlayServicesAvailable(activity: Activity, request_code: Int): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(activity)
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, request_code)?.show()
            }
            return false
        }
        return true
    }

    fun replaceNull(value: String?): String {
        if (value == null) {
            return ""
        }
        return value
    }


}
