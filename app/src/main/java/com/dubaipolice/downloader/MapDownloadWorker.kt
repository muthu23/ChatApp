package com.dubaipolice.downloader

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dubaipolice.R
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.SharedPref
import com.here.sdk.core.LanguageCode
import com.here.sdk.core.engine.SDKNativeEngine
import com.here.sdk.maploader.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException


@Suppress("BlockingMethodInNonBlockingContext", "SameParameterValue")
class MapDownloadWorker(val ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    private lateinit var notificationManager: NotificationManager
    private lateinit var builder: NotificationCompat.Builder
    private var progress: Int = 0
    val downloadRegion = "Tamil Nadu"

    private lateinit var mapUpdater: MapUpdater
    private lateinit var mapDownloader: MapDownloader

    private var downloadableRegions: List<Region> = emptyList()
    private val mapDownloaderTasks: MutableList<MapDownloaderTask> = mutableListOf()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            setUpMapRequirement()
            downloadMap()
            Result.success()
        } catch (error: Throwable) {
            error.printStackTrace()
            Result.failure()
        }
    }

    private fun setUpMapRequirement() {
        val sdkNativeEngine = SDKNativeEngine.getSharedInstance()
            ?: throw java.lang.RuntimeException("SDKNativeEngine not initialized.")
        mapDownloader = MapDownloader.fromEngine(sdkNativeEngine)
        mapUpdater = MapUpdater.fromEngine(sdkNativeEngine)
        // Note that the default storage path can be adapted when creating a new SDKNativeEngine.
        val storagePath = sdkNativeEngine.options.cachePath
        Log.d("TAG", "StoragePath: $storagePath")

    }

    private fun downloadMap() {
        try {
            createNotificationChannel()
            builder = NotificationCompat.Builder(applicationContext, AppConstants.notificationMapChannelId)
                    .setContentTitle("Downloading offline map for $downloadRegion")
                    .setSmallIcon(R.drawable.ic_logo_push_notification)
                    .setColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
                    .setPriority(NotificationCompat.PRIORITY_LOW)

            onDownloadRegion()

        } catch (e: IOException) {
            e.printStackTrace()
            updateDownload(0, true)
        } catch (e: Exception) {
            e.printStackTrace()
            updateDownload(0, true)
        }

    }


    private fun onDownloadRegion() {

        val languageCode: LanguageCode = if (SharedPref.readString(AppConstants.KEY_LANGUAGE)
                .equals(AppConstants.LANGUAGE_ARABIC)
        ) {
            LanguageCode.AR_SA
        } else LanguageCode.EN_US

        mapDownloader.getDownloadableRegions(
            languageCode
        ) { mapLoaderError, list ->
            if (mapLoaderError != null) {
                SharedPref.writeBoolean(AppConstants.KEY_IS_APP_MAP_DOWNLOADED, false)
            }
            downloadableRegions = list!!
            onDownloadMap()
        }

    }

    private fun onDownloadMap() {
        val region: Region? = findRegion(downloadRegion)
        region?.let {
            val regionIDs = listOf(region.regionId)
            val mapDownloaderTask = mapDownloader.downloadRegions(regionIDs,
                object : DownloadRegionsStatusListener {
                    override fun onDownloadRegionsComplete(
                        mapLoaderError: MapLoaderError?,
                        list: List<RegionId>?
                    ) {
                        if (mapLoaderError != null) {
                            updateDownload(100, false)
                            SharedPref.writeBoolean(AppConstants.KEY_IS_APP_MAP_DOWNLOADED, true)
                        }
                        onCancelMapDownloadClicked()
                    }

                    override fun onProgress(regionId: RegionId, percentage: Int) {
                        updateDownload(percentage, false)
                    }

                    override fun onPause(mapLoaderError: MapLoaderError?) {}

                    override fun onResume() {}
                })
            mapDownloaderTasks.add(mapDownloaderTask)
        }
    }

    private fun findRegion(localizedRegionName: String): Region? {
        var downloadableRegion: Region? = null
        for (regions in downloadableRegions) { // continental regions:
            for (country in regions.childRegions!!) { // country regions:
                if (country.name == "India") {
                    for (state in country.childRegions!!) { // state regions:
                        downloadableRegion = if (state.name == localizedRegionName) {
                            state
                        } else {
                            country
                        }
                    }
                }
            }

        }
        return downloadableRegion
    }

    private fun updateDownload(progress: Int, isFailed: Boolean) {
        if (!isFailed) {
            builder.setProgress(100, progress, false)
            builder.setContentText("$progress% completed ")
            notificationManager.notify(20, builder.build())

            if (progress == 100) {
                builder.setContentText("Download complete.")
                builder.setProgress(0, 0, false)
                notificationManager.notify(20, builder.build())
            }
        } else {
            builder.setContentText("Download failed.")
            builder.setProgress(0, 0, false)
            notificationManager.notify(20, builder.build())
        }

    }

    private fun createNotificationChannel() {
        val channelId = AppConstants.notificationMapChannelId
        val name = AppConstants.notificationMapChannelName
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

    private fun onCancelMapDownloadClicked() {
        for (mapDownloaderTask in mapDownloaderTasks) {
            mapDownloaderTask.cancel()
        }
        mapDownloaderTasks.clear()
    }



}