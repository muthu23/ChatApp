package com.dubaipolice.service

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.dubaipolice.R
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.view.activity.HomeEndUserActivity
import com.google.android.gms.location.*

class LocationDetectorService: Service() {

    private val TAG: String = LocationDetectorService::class.java.getSimpleName()


    //location
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var locationRequest: LocationRequest? = null

    //get location updates after every 100 mtrs instead of time
    var minimumDistanceBetweenUpdatesInMeter = 100


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= 26) {

            val CHANNEL_ID = "channelidlocation"

            val input =
                getString(R.string.app_name) + " monitoring your location"
            createNotificationChannel()
            val notificationIntent = Intent(this, HomeEndUserActivity::class.java)
            val pendingIntent= PendingIntent.getActivity(
                this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            /*pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(
                    this,
                    0, notificationIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            } else {
                PendingIntent.getActivity(
                    this,
                    0, notificationIntent, 0
                )
            }*/

            val notification: Notification = NotificationCompat.Builder(
                this,
                CHANNEL_ID
            )
                .setContentTitle("Location Monitoring")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_logo_push_notification)
                .setColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setAutoCancel(false)
                .setContentIntent(pendingIntent)
                .build()

            startForeground(1, notification)

        }
    }

    private fun createNotificationChannel() {

        val CHANNEL_ID = "channelidlocation"
        val CHANNEL_NAME = "channelnamelocation"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //Multiple alarm service using pending intent different request code added
        //getAlarmData()
        startLocationServices()
        return super.onStartCommand(intent, flags, startId)
    }

    /*    private void testLocation() {

        for(int i=0; i<10; i++)
        {
            addDataToFirestoreDatabase(24.1254, i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }*/
    private fun startLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
        createLocationCallback()
        startLocationUpdates()
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create()
        locationRequest!!.interval = 5000
        locationRequest!!.fastestInterval = 2500
        //locationRequest!!.smallestDisplacement = minimumDistanceBetweenUpdatesInMeter.toFloat()
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {

                //stopLocationUpdates();
                val location = locationResult.lastLocation
                //currentLocation= myLocation;

                //send lat long to firebase server for navigation
                if (SharedPref.readBoolean(AppConstants.KEY_IS_LOGGEDIN)) {
                    SharedPref.writeString(AppConstants.FIELD.LATITUDE,location.latitude.toString())
                    SharedPref.writeString(AppConstants.FIELD.LONGITUDE,location.longitude.toString())
                    //save lat lng to server
                    startLocationUpdatesWorkManager(location.latitude, location.longitude)
                }
                Log.e("LocationFetched", "LocationFetched")
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient!!.requestLocationUpdates(
            locationRequest!!,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationClient?.removeLocationUpdates(locationCallback!!)
    }

    override fun onDestroy() {
        //removeNotification();
        stopLocationUpdates()
        super.onDestroy()
    }

    private fun startLocationUpdatesWorkManager(latitude: Double, longitude: Double) {
        val sendLocationWorkRequest = OneTimeWorkRequest.Builder(
            LocationUpdaterToServer::class.java
        ) //pass data to work manager
            .setInputData(
                Data.Builder()
                    .putDouble(AppConstants.LATITUDE, latitude)
                    .putDouble(AppConstants.LONGITUDE, longitude)
                    .build()
            )
            .build()
        WorkManager
            .getInstance(applicationContext)
            .enqueueUniqueWork(
                "LocationUpdates",
                ExistingWorkPolicy.REPLACE,
                sendLocationWorkRequest
            )
    }

    private fun stopLocationUpdatesWorkManager() {
        WorkManager
            .getInstance(applicationContext)
            .cancelAllWork()
    }

}