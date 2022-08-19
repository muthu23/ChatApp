package com.dubaipolice.downloader

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.dubaipolice.MainApplication
import com.dubaipolice.R
import com.dubaipolice.db.AppDatabase
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.FileUtils
import com.dubaipolice.utils.Utils.showToast
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException


@Suppress("BlockingMethodInNonBlockingContext")
class DownloadWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    private lateinit var notificationManager: NotificationManager
    private lateinit var builder: NotificationCompat.Builder
    private var file: File? = null
    private lateinit var downloadDetails: DownloadDetails
    private var progress: Int = 0

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            val data: Data = inputData
            downloadDetails = deserializeFromJson(data.getString("downloadDetails"))
            downloadFile(downloadDetails)

            Result.success()
        } catch (error: Throwable) {
            error.printStackTrace()
            Result.failure()
        }
    }

    private fun downloadFile(downloadDetail: DownloadDetails) {
        try {
            Log.v("Working Thread downloadFile ", Thread.currentThread().name)
            val client = OkHttpClient()
            val request = Request.Builder().url(downloadDetail.url)
                .addHeader("Content-Type", "application/json")
                .build()
            val response = client.newCall(request).execute()

            if (response.body != null) {
                if (downloadDetail.filePath.isNotEmpty()) {
                    val file = File(downloadDetail.filePath)
                    if (file.exists()) {
                        file.delete()
                    }
                }
                file = FileUtils.createFile(
                    applicationContext,
                    downloadDetail.fileName
                )
                if (file != null) {
                    val channelId = AppConstants.notificationChannelId
                    createNotificationChannel(channelId)
                    builder =
                        NotificationCompat.Builder(
                            applicationContext,
                            AppConstants.notificationChannelId
                        )
                            .setContentTitle(downloadDetail.fileName)
                            .setContentText("Downloading your file...")
                            .setSmallIcon(R.drawable.ic_logo_push_notification)
                            .setColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
                            .setPriority(NotificationCompat.PRIORITY_LOW)

                    downloadDetail.filePath = file!!.absolutePath
                    val inputStream = response.body?.byteStream()
                    val length = response.body?.contentLength()
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var bytesCopied: Long = 0
                    var bytes: Int
                    inputStream?.use {
                        file!!.outputStream().use { output ->
                            bytes = it.read(buffer)
                            while (bytes >= 0) {
                                output.write(buffer, 0, bytes)
                                bytesCopied += bytes
                                bytes = it.read(buffer)
                                updateDownload(bytesCopied, length!!, false)
                            }
                        }
                    }
                } else {
                    updateDownload(0, 0, true)
                }
            } else {
                updateDownload(0, 0, true)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            updateDownload(0, 0, true)
        } catch (e: Exception) {
            e.printStackTrace()
            updateDownload(0, 0, true)
        }

    }

    private fun updateDownload(uploaded: Long, total: Long, isFailed: Boolean) {
            if (!isFailed) {
                progress = (100 * uploaded / total).toInt()
                builder.setProgress(100, progress, false)
                builder.setContentText(
                    "$progress% complete " + bytesIntoHumanReadable(
                        uploaded
                    ) + " of " + bytesIntoHumanReadable(total)
                )
                Log.v("download progress", progress.toString())
                notificationManager.notify(downloadDetails.notificationId, builder.build())

                if (progress == 100) {
                    builder.setContentText("Download complete.")
                    builder.setProgress(0, 0, false)
                    notificationManager.notify(downloadDetails.notificationId, builder.build())
                    updateLocalFilePath()
                }
            } else {
                builder.setContentText("Download failed.")
                builder.setProgress(0, 0, false)
                notificationManager.notify(downloadDetails.notificationId, builder.build())

            }

    }

    // Deserialize to single object.
    private fun deserializeFromJson(jsonString: String?): DownloadDetails {
        val gson = Gson()
        return gson.fromJson(jsonString, DownloadDetails::class.java)
    }

    private fun updateLocalFilePath() {
            builder.setContentText("Download complete.")
            builder.setProgress(0, 0, false)
            notificationManager.notify(downloadDetails.notificationId, builder.build())

        updateDownloadedMediaUrl(
            file!!.path, downloadDetails.messageId, downloadDetails.groupJid
        )

    }

    private fun updateDownloadedMediaUrl(mediaUrl: String, messageId: String, groupJid: String) {
        AppDatabase.getAppDatabase(MainApplication.appContext)
            ?.messageInfoTableDao()
            ?.updateDownloadedMediaUrl(mediaUrl, messageId)

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            showToast(applicationContext, "Download completed ")
            with(NotificationManagerCompat.from(applicationContext)) {
                cancel(downloadDetails.notificationId)
            }
        }, 1000)


        //broadcast for Media downloaded to ChatActivity to refresh item
        val intent =
            Intent(AppConstants.MEDIA_DOWNLOADED_BROADCAST)
        intent.putExtra(AppConstants.MEDIA_DOWNLOADED_MEDIA_PATH, mediaUrl)
        intent.putExtra(AppConstants.MEDIA_DOWNLOADED_MESSAGE_ID, messageId)
        intent.putExtra(AppConstants.MEDIA_DOWNLOADED_GROUP_JID, groupJid)
        MainApplication.appContext.sendBroadcast(intent)

    }

    private fun bytesIntoHumanReadable(bytes: Long): String {
        val kilobyte: Long = 1024
        val megabyte = kilobyte * 1024
        val gigabyte = megabyte * 1024
        val terabyte = gigabyte * 1024
        return when {
            bytes in 0 until kilobyte -> {
                "$bytes B"
            }
            bytes in kilobyte until megabyte -> {
                (bytes / kilobyte).toString() + " KB"
            }
            bytes in megabyte until gigabyte -> {
                (bytes / megabyte).toString() + " MB"
            }
            bytes in gigabyte until terabyte -> {
                (bytes / gigabyte).toString() + " GB"
            }
            bytes >= terabyte -> {
                (bytes / terabyte).toString() + " TB"
            }
            else -> {
                "$bytes Bytes"
            }
        }
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = AppConstants.notificationChannelName
            val channelDescription = AppConstants.notificationChannelDescription
            val importance = NotificationManager.IMPORTANCE_LOW

            val channel = NotificationChannel(channelId, name, importance)
            channel.apply {
                description = channelDescription
                setSound(null, null)
            }

            // Finally register the channel with system
             notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 2048
    }

}