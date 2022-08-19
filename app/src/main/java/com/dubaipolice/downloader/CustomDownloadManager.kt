package com.dubaipolice.downloader

import android.content.Context
import androidx.work.*
import com.dubaipolice.utils.AppConstants
import com.google.gson.Gson
import kotlinx.coroutines.DelicateCoroutinesApi
import java.sql.Timestamp
import java.util.*


class CustomDownloadManager(
    private var notificationId: Int,
    private var messageId: String,
    private var fileName: String,
    private var url: String,
    private var groupJid: String,
    context: Context
) {
    private val workManager = WorkManager.getInstance(context)
    fun startDownload() {
        val downloadDetails = DownloadDetails(
            notificationId = notificationId,
            messageId = messageId,
            fileName = fileName,
            url = url,
            groupJid= groupJid,
            createdTime = Timestamp(Date().time).nanos
        )
        startWorker(downloadDetails)
    }

    private fun startWorker(downloadDetails: DownloadDetails) {
        val builder: Data.Builder = Data.Builder()
        builder.putString("downloadDetails", serializeToJson(downloadDetails = downloadDetails))
        val data = builder.build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val downloadRequest = OneTimeWorkRequest
            .Builder(DownloadWorker::class.java)
            .setConstraints(constraints)
            .setInputData(data)
            .build()
        workManager.enqueueUniqueWork(
            System.currentTimeMillis().toString(),
            ExistingWorkPolicy.APPEND,
            downloadRequest
        )
    }
    private fun serializeToJson(downloadDetails: DownloadDetails?): String? {
        val gson = Gson()
        return gson.toJson(downloadDetails)
    }

}
