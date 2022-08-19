package com.dubaipolice.fcm

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.dubaipolice.MainApplication
import com.dubaipolice.R
import com.dubaipolice.callservices.CallHandler
import com.dubaipolice.db.AppDatabase
import com.dubaipolice.model.CallerData
import com.dubaipolice.model.HLSLink
import com.dubaipolice.model.NotificationTune
import com.dubaipolice.service.FCMUpdater
import com.dubaipolice.service.HeadsUpNotificationService
import com.dubaipolice.service.RtpService
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.DateUtils
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.view.activity.HomeEndUserActivity
import com.dubaipolice.view.activity.VideoStreamActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException


class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private val TAG = MyFirebaseMessagingService::class.java.simpleName
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (token != null) {
            Log.e("Firebase token", token)
            saveToken(token)
            sendTokenOnServer(token)
        }
    }

    /**
     * send firebase token to server
     */
    private fun sendTokenOnServer(token: String) {

        val sendLocationWorkRequest =
            OneTimeWorkRequest.Builder(FCMUpdater::class.java) //pass data to work manager
                .setInputData(
                    Data.Builder()
                        .putString(AppConstants.FCM_TOKEN, token)
                        .build()
                )
                .build()

        WorkManager
            .getInstance(applicationContext)
            .enqueueUniqueWork(
                AppConstants.FCM_TOKEN,
                ExistingWorkPolicy.REPLACE,
                sendLocationWorkRequest
            )

    }

    /**
     * save firebase to shared pref
     */
    private fun saveToken(token: String) {

        SharedPref.writeString(AppConstants.KEY_FIREBASE_TOKEN, token)

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.e("FCM", "FCM Received")
        Log.e("FCMData", remoteMessage.data.toString())
       /* Log.e("FCMData", remoteMessage.data.toString())
        Log.e("FCMData2", remoteMessage.notification?.title.toString())
        Log.e("FCMData3", remoteMessage.notification?.body.toString())
*/
        //if logged in then only show push notifications
        if (SharedPref.readBoolean(AppConstants.KEY_IS_LOGGEDIN)) {

            val notificationType= remoteMessage.data["notification_type"]

            if(notificationType != null)
            {

                //incoming call
                if(notificationType == "call")
                {

                    val groupJid= remoteMessage.data["room_jid"]
                    val groupId= remoteMessage.data["group_id"]
                    val groupName= remoteMessage.data["name"]
                    val senderName= remoteMessage.data["sender_Name"]
                    val senderId= remoteMessage.data["sender_id"]
                    val senderProfileImage= remoteMessage.data["sender_profile_image"]
                    val startCallTime= remoteMessage.data["timestamp"]
                    val isCallActive = remoteMessage.data["is_call_active"]
                    val soundFile = remoteMessage.data["sound_file"]

                    if (!isMyServiceRunning(HeadsUpNotificationService::class.java)) {

                       /* val seconds= DateUtils.getSecondsBetweenTimestamp(startCallTime!!)
                        Log.e("diffinseconds", seconds.toString())*/

                        //if time difference of call start and current system
                        //is less then 60 seconds then show call UI
                        //if (seconds < 60) {

                            wakeLockScreen(this)

                            updateGroupCallStatus(
                                true,
                                groupJid!!
                            )

                            val intent =
                                Intent(AppConstants.CALL_STARTED_BROADCAST)
                            intent.putExtra(AppConstants.IntentConstants.GROUP_JID, groupJid)
                            MainApplication.appContext.sendBroadcast(intent)

                            if(groupJid != null && groupName != null && senderName != null &&
                                senderId != null)
                            {

                                if(SharedPref.readInt(AppConstants.KEY_USER_ID).toString() != senderId)
                                {

                                    //sendNotification(groupName!!, senderName!!)

                                    var callerData= CallerData()
                                    callerData.groupName= groupName
                                    callerData.groupJid= groupJid
                                    callerData.groupId= groupId
                                    callerData.senderName= senderName
                                    callerData.senderProfileImage= senderProfileImage

                                    val serviceIntent = Intent(applicationContext, HeadsUpNotificationService::class.java)
                                    val mBundle = Bundle()
                                    mBundle.putString("inititator", senderName)
                                    mBundle.putString("call_type", "Audio")
                                    mBundle.putSerializable(AppConstants.IntentConstants.CALLER_DATA, callerData)
                                    serviceIntent.putExtras(mBundle)
                                    ContextCompat.startForegroundService(applicationContext, serviceIntent)

                                    /* var callerData= CallerData()
                                     callerData.groupName= groupName
                                     callerData.groupJid= groupJid
                                     callerData.groupId= groupId
                                     callerData.senderName= senderName

                                     initiateCallService(callerData)*/

                                }

                            }

                        //}

                    }
                    else
                    {
                        sendNotification("Missed Call",senderName + " (" + groupName + ")", null)
                    }

                }
                else if(notificationType == "call-end") {

                    val groupJid = remoteMessage.data["room_jid"]
                    val groupId = remoteMessage.data["group_id"]
                    val groupName = remoteMessage.data["name"]
                    val isCallActive = remoteMessage.data["is_call_active"]
                    val soundFile = remoteMessage.data["sound_file"]

                    MainApplication.activeCallGroupJid = null

                    updateGroupCallStatus(
                        false,
                        groupJid!!
                    )

                    val intent =
                        Intent(AppConstants.CALL_ENDED_BROADCAST)
                    intent.putExtra(AppConstants.IntentConstants.GROUP_JID, groupJid)
                    MainApplication.appContext.sendBroadcast(intent)

                    //disconnect call if disconnect call from other end
                    //and still ringing
                    Log.e("ServiceA", "outside")
                     if (isMyServiceRunning(HeadsUpNotificationService::class.java)) {
                         Log.e("ServiceA", "inside")
                         HeadsUpNotificationService.groupId.let {

                             Log.e("ServiceA", "groupIdnotnull")
                             Log.e("MyGroupId", HeadsUpNotificationService.groupId.toString())
                             Log.e("MyGroupId2", groupId.toString())
                             if (HeadsUpNotificationService.groupId == groupId) {
                                 Log.e("ServiceA", "groupidmatch")
                                 MainApplication.appContext.stopService(Intent(MainApplication.appContext, HeadsUpNotificationService::class.java))
                             }
                             else
                             {
                                 Log.e("ServiceA", "groupidnotmatched")
                             }

                         }
                     }


                    //sendNotification("Call Destroyed", groupName!!)

                    //save value to local db/refresh group

                }
                else if(notificationType == "live-stream-start")
                {

                    val title = remoteMessage.data["title"]
                    val body = remoteMessage.data["body"]
                    val streamLink = remoteMessage.data["stream_link"]
                    val soundFile = remoteMessage.data["sound_file"]

                    if(title != null && body != null && streamLink != null)
                    {
                        sendNotificationStream(title, body, streamLink, soundFile)
                    }


                }
                else if(notificationType == "group-member-removed")
                {
                    val title = remoteMessage.data["title"]
                    val body = remoteMessage.data["body"]

                    val groupId= remoteMessage.data["group_id"]
                    val groupName= remoteMessage.data["group_name"]
                    val groupJid= remoteMessage.data["room_jid"]
                    val userId= remoteMessage.data["user_id"]
                    val userFullName= remoteMessage.data["user_fullname"]
                    val userName= remoteMessage.data["username"] //email
                    val soundFile = remoteMessage.data["sound_file"]

                    if(title != null && body != null)
                    {
                        sendNotification(title, body, soundFile)
                    }

                    //hangup call if current user removed from group
                    if(SharedPref.readInt(AppConstants.KEY_USER_ID).toString() == userId)
                    {
                        Log.e("RemoveUuserTest", "RemoveUserIdMatched")
                        if (MainApplication.activeCallGroupJid != null && MainApplication.activeCallGroupJid == groupJid) {
                            Log.e("RemoveUuserTest", "ActiveGroupFround")
                            val hangupExistingCallBroadcastIntent = Intent("org.jitsi.meet.HANG_UP")
                            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(hangupExistingCallBroadcastIntent)

                            MainApplication.activeCallGroupJid = null

                        }
                    }

                }
                else if(notificationType == "group-deletion")
                {
                    val title = remoteMessage.data["title"]
                    val body = remoteMessage.data["body"]

                    val groupId= remoteMessage.data["group_id"]
                    val groupName= remoteMessage.data["group_name"]
                    val groupJid= remoteMessage.data["room_jid"]
                    val soundFile = remoteMessage.data["sound_file"]

                    if(title != null && body != null)
                    {
                        sendNotification(title, body, soundFile)
                    }

                    //hangup call if group deleted
                    if (MainApplication.activeCallGroupJid != null && MainApplication.activeCallGroupJid == groupJid) {

                        val hangupExistingCallBroadcastIntent = Intent("org.jitsi.meet.HANG_UP")
                        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(hangupExistingCallBroadcastIntent)

                        MainApplication.activeCallGroupJid = null

                    }

                }
                else
                {
                    val title = remoteMessage.data["title"]
                    val body = remoteMessage.data["body"]
                    val soundFile = remoteMessage.data["sound_file"]

                    if(title != null && body != null)
                    {
                        sendNotification(title, body, soundFile)
                    }
                    else
                    {

                        if (remoteMessage.notification == null ||
                            remoteMessage.notification!!.title == null ||
                            remoteMessage.notification!!.body == null) {
                            return
                        }

                        var title= remoteMessage.notification!!.title
                        var body= remoteMessage.notification!!.body
                        sendNotification(title!!, body!!, null)

                    }

                }

            }
            else
            {

                val title = remoteMessage.data["title"]
                val body = remoteMessage.data["body"]
                val soundFile = remoteMessage.data["sound_file"]

                if(title != null && body != null)
                {
                    sendNotification(title, body, soundFile)
                }
                else
                {

                    if (remoteMessage.notification == null ||
                        remoteMessage.notification!!.title == null ||
                        remoteMessage.notification!!.body == null) {
                        return
                    }

                    var title= remoteMessage.notification!!.title
                    var body= remoteMessage.notification!!.body
                    sendNotification(title!!, body!!, null)

                }

            }

            /* if (remoteMessage.notification == null ||
                 remoteMessage.notification!!.title == null ||
                 remoteMessage.notification!!.body == null) {
                 return
             }

             var title= remoteMessage.notification!!.title
             var body= remoteMessage.notification!!.body
             sendNotification(title!!, body!!)*/

        }

    }

    /**
     * wake up mobile screen
     *
     */
    @SuppressLint("InvalidWakeLockTag")
    private fun wakeLockScreen(context: Context) {

        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isScreenOn = pm.isScreenOn
        if (isScreenOn == false) {
            val wl = pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                "MyLock"
            )
            wl.acquire(10000)
            val wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyCpuLock")
            wl_cpu.acquire(10000)
        }

    }

    @Suppress("DEPRECATION")
    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun updateGroupCallStatus(isCallActive: Boolean, groupJid: String) {
        GlobalScope.launch {

            AppDatabase.getAppDatabase(MainApplication.appContext)
                ?.groupInfoTableDao()
                ?.updateGroupCallStatus(isCallActive.toString(), groupJid)

        }
    }

    private fun initiateCallService(callerData: CallerData) {

               val callManager = CallHandler(applicationContext)
               if (callManager.checkIsAccountEnabled()) {
                   callManager.init()
                   callManager.startIncomingCall(callerData)
               }

    }

    /**
     *
     * show push notification
     */
    private fun sendNotification(title: String, body: String, sound: String?) {

        val notificationIntent: Intent = Intent(this, HomeEndUserActivity::class.java)
        notificationIntent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        /*val channelId = getString(R.string.default_notification_channel_id)
        val channelName = getString(R.string.default_notification_channel_name)
*/
        val channelId = "Default_Channel_ID"
        val channelName: CharSequence = "Default_Channel_Name"
        val channelDescription = "Default_Channel_Description"

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        var isFound= false

        if(sound != null)
        {
            getAllFilesInAssetByExtension(this, "", ".mp3")?.forEach {
                if(sound==it){

                    isFound= true

                    return@forEach

                }
            }

        }


        /*var defaultSoundUri = RingtoneManager.getActualDefaultRingtoneUri(
            applicationContext, RingtoneManager.TYPE_NOTIFICATION);*/
//        val defaultSoundUri =
//            Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + applicationContext.packageName + "/" + R.raw.footer_click)
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_logo_push_notification)
                .setColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
                .setContentTitle(title)
                .setContentText(body)
                //.setSound(defaultSoundUri)
                //.setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = channelDescription
            //channel.enableLights(true)
            //channel.lightColor = Color.WHITE
            //channel.vibrationPattern = longArrayOf(0, 500 /*, 500, 1000*/)
            //channel.enableVibration(true)
            //channel.setShowBadge(true)
            val audioAttributes =
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
            //channel.setSound(defaultSoundUri, audioAttributes)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())

        if(isFound)
        {
            playSound(this, sound!!)
        }
        else
        {
            soundAndVibrate(defaultSoundUri)
        }

        //soundAndVibrate(defaultSoundUri)



    }

/*    *//**
     *
     * show push notification
     *//*
    private fun sendNotification(title: String, body: String, sound: String?) {

        val notificationIntent: Intent = Intent(this, HomeEndUserActivity::class.java)
        notificationIntent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        *//*val channelId = getString(R.string.default_notification_channel_id)
        val channelName = getString(R.string.default_notification_channel_name)
*//*
        val channelId = "Default_Channel_ID"
        val channelName: CharSequence = "Default_Channel_Name"
        val channelDescription = "Default_Channel_Description"

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        *//*var defaultSoundUri = RingtoneManager.getActualDefaultRingtoneUri(
            applicationContext, RingtoneManager.TYPE_NOTIFICATION);*//*
//        val defaultSoundUri =
//            Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + applicationContext.packageName + "/" + R.raw.footer_click)
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_logo_push_notification)
                .setColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
                .setContentTitle(title)
                .setContentText(body)
                .setSound(defaultSoundUri)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = channelDescription
            //channel.enableLights(true)
            //channel.lightColor = Color.WHITE
            channel.vibrationPattern = longArrayOf(0, 500 *//*, 500, 1000*//*)
            channel.enableVibration(true)
            channel.setShowBadge(true)
            val audioAttributes =
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
            channel.setSound(defaultSoundUri, audioAttributes)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())

        //soundAndVibrate(defaultSoundUri)



    }*/

    private fun sendNotificationStream(title: String, body: String, streamLink: String, sound: String?) {

        val hlsLink = HLSLink(
            streamLink,
            0,
            false
        )

        val notificationIntent: Intent = Intent(this, VideoStreamActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        notificationIntent.putExtra(AppConstants.IntentConstants.HLS_LINK_DATA, hlsLink)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        /*val channelId = getString(R.string.default_notification_channel_id)
        val channelName = getString(R.string.default_notification_channel_name)
*/
        val channelId = "Default_Channel_ID"
        val channelName: CharSequence = "Default_Channel_Name"
        val channelDescription = "Default_Channel_Description"

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        var isFound= false

        if(sound != null)
        {
            getAllFilesInAssetByExtension(this, "", ".mp3")?.forEach {
                if(sound==it){

                    isFound= true

                    return@forEach

                }
            }

        }

        /*var defaultSoundUri = RingtoneManager.getActualDefaultRingtoneUri(
            applicationContext, RingtoneManager.TYPE_NOTIFICATION);*/
//        val defaultSoundUri =
//            Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + applicationContext.packageName + "/" + R.raw.footer_click)
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_logo_push_notification)
                .setColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
                .setContentTitle(title)
                .setContentText(body)
                //.setSound(defaultSoundUri)
                //.setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = channelDescription
            //channel.enableLights(true)
            //channel.lightColor = Color.WHITE
            //channel.vibrationPattern = longArrayOf(0, 500 /*, 500, 1000*/)
            //channel.enableVibration(true)
            //channel.setShowBadge(true)
            val audioAttributes =
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
            //channel.setSound(defaultSoundUri, audioAttributes)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())

        if(isFound)
        {
            playSound(this, sound!!)
        }
        else
        {
            soundAndVibrate(defaultSoundUri)
        }


    }

    /**
     *
     * show push notification
     */
    private fun sendCustomNotification(title: String, body: String) {

        // Layouts for the custom notification
        val notificationLayout = RemoteViews(packageName, R.layout.notification_collapsed)
        //Optional
        //val notificationLayoutExpanded = RemoteViews(packageName, R.layout.notification_expanded)

        notificationLayout.setTextViewText(R.id.collapsed_notification_title, title)
        notificationLayout.setTextViewText(R.id.collapsed_notification_info, body)

        val notificationIntent: Intent = Intent(this, HomeEndUserActivity::class.java)
        notificationIntent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        /*val channelId = getString(R.string.default_notification_channel_id)
        val channelName = getString(R.string.default_notification_channel_name)
*/
        val channelId = "Default_Channel_ID"
        val channelName: CharSequence = "Default_Channel_Name"
        val channelDescription = "Default_Channel_Description"

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        /*var defaultSoundUri = RingtoneManager.getActualDefaultRingtoneUri(
            applicationContext, RingtoneManager.TYPE_NOTIFICATION);*/
//        val defaultSoundUri =
//            Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + applicationContext.packageName + "/" + R.raw.footer_click)
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_logo_push_notification)
                .setColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                //.setCustomBigContentView(notificationLayoutExpanded)
                //.setSound(defaultSoundUri)
                //.setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = channelDescription
            //channel.enableLights(true)
            //channel.lightColor = Color.WHITE
            //channel.vibrationPattern = longArrayOf(0, 500 /*, 500, 1000*/)
            //channel.enableVibration(true)
            //channel.setShowBadge(true)
            val audioAttributes =
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
            //channel.setSound(defaultSoundUri, audioAttributes)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())

        //soundAndVibrate(defaultSoundUri)



    }

    private fun soundAndVibrate(defaultSoundUri: Uri) {

        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager?
            if(audioManager  != null)
            {
                when (audioManager.ringerMode) {
                    AudioManager.RINGER_MODE_NORMAL -> {

                        var ringtone= RingtoneManager.getRingtone(applicationContext, defaultSoundUri)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            ringtone?.isLooping= false
                        }
                        ringtone.play()

                        vibrate()

                    }
                    AudioManager.RINGER_MODE_SILENT -> {

                    }
                    AudioManager.RINGER_MODE_VIBRATE -> {
                        vibrate()
                        Log.e("Service!!", "vibrate mode")
                    }
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }

    }

    private fun vibrate()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 500 /*, 500, 1000*/),
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        }
        else
        {
            var vibrator= getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            vibrator.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 500 /*, 500, 1000*/),
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        }
    }

    private fun playSound(context: Context, file: String) {
        try {

            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager?
            if (audioManager != null) {
                when (audioManager.ringerMode) {
                    AudioManager.RINGER_MODE_NORMAL -> {

                        val mediaPlayer = MediaPlayer()
                        val afd = context.assets.openFd(file)
                        mediaPlayer.setDataSource(
                            afd.fileDescriptor,
                            afd.startOffset,
                            afd.length
                        )
                        afd.close()
                        mediaPlayer.prepare()
                        mediaPlayer.start()

                        vibrate()

                    }
                    AudioManager.RINGER_MODE_SILENT -> {

                    }
                    AudioManager.RINGER_MODE_VIBRATE -> {
                        vibrate()
                        Log.e("Service!!", "vibrate mode")
                    }
                }
            }

        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }

    }


    private fun getAllFilesInAssetByExtension(
        context: Context,
        path: String?,
        extension: String?
    ): Array<String>? {
        try {
            val files = context.assets.list(path!!)
            val filesWithExtension: MutableList<String> = ArrayList()
            for (file in files!!) {
                if (file.endsWith(extension!!)) {
                    filesWithExtension.add(file)
                }
            }
            return filesWithExtension.toTypedArray()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

}