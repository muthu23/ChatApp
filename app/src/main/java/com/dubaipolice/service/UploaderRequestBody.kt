package com.dubaipolice.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.dubaipolice.R
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.FileUtils.bytesIntoHumanReadable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream


class UploaderRequestBody(
    private val file: File,
    private val contentType: String,
    private val notificationId: Int,
    private val context: Context,
    private val isThumb: Boolean
) : RequestBody() {

    private lateinit var notificationManager: NotificationManager
    private var progress: Int = 0
    private var builder: NotificationCompat.Builder =
        NotificationCompat.Builder(context, AppConstants.notificationChannelId)
            .setContentTitle(file.name)
            .setContentText("Uploading your file...")
            .setSmallIcon(R.drawable.ic_logo_push_notification)
            .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
            .setPriority(NotificationCompat.PRIORITY_LOW)


    override fun contentType() = contentType.toMediaTypeOrNull()

    override fun contentLength() = file.length()

    override fun writeTo(sink: BufferedSink) {
        val channelId = AppConstants.uploadNotificationChannelId
        createNotificationChannel(channelId)
        val length = file.length()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val fileInputStream = FileInputStream(file)
        var uploaded = 0L
        fileInputStream.use { inputStream ->
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                uploaded += read
                sink.write(buffer, 0, read)
                if (!isThumb)
                    updateDownload(uploaded, length)
            }
        }
    }

    private fun updateDownload(uploaded: Long, total: Long) {
        progress = (100 * uploaded / total).toInt()
        builder.setProgress(100, progress, false)
        builder.setContentText(
            "$progress% complete " + bytesIntoHumanReadable(
                uploaded
            ) + " of " + bytesIntoHumanReadable(total)
        )
        Log.v("Upload progress", progress.toString())
        notificationManager.notify(notificationId, builder.build())
        if (progress == 100) {
            builder.setContentText("Upload complete.")
            builder.setProgress(0, 0, false)
            notificationManager.notify(notificationId, builder.build())
            notificationManager.cancel(notificationId)
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
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 2048
    }
}