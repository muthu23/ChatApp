package com.dubaipolice.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.*
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.VolumeProviderCompat
import com.dubaipolice.MainApplication
import com.dubaipolice.R
import com.dubaipolice.model.CallerData
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.view.activity.AudioConferenceActivity
import com.dubaipolice.view.activity.HomeEndUserActivity
import com.dubaipolice.view.activity.IncomingCallUIActivity
import java.util.*
import java.util.concurrent.TimeUnit


class HeadsUpNotificationService : Service(), OnPreparedListener {
    private val CHANNEL_ID: String =
        MainApplication.appContext.getString(R.string.app_name)
            .toString() + "CallChannel"
    private val CHANNEL_NAME: String =
        MainApplication.appContext.getString(R.string.app_name)
            .toString() + "Call Channel"
    var mediaPlayer: MediaPlayer? = null
    var mvibrator: Vibrator? = null
    var audioManager: AudioManager? = null
    var playbackAttributes: AudioAttributes? = null
    private var handler: Handler? = null
    var afChangeListener: OnAudioFocusChangeListener? = null
    private var status = false
    private var vstatus = false

    private var mediaSession: MediaSessionCompat? = null

    companion object{
        var groupId: String? = null
    }

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        //using media session to trigger volume button to silent call sound
        mediaSession = MediaSessionCompat(this, "PlayerService")
        /*mediaSession!!.setFlags(
            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                    MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        )*/
        mediaSession!!.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(
                    PlaybackStateCompat.STATE_PLAYING,
                    0,
                    0f
                ) //you simulate a player which plays something.
                .build()
        )

        mediaSession!!.setPlaybackToRemote(getVolumeProvider())
        mediaSession!!.isActive = true
    }

    private fun getVolumeProvider(): VolumeProviderCompat? {
        val audio = MainApplication.appContext.getSystemService(AUDIO_SERVICE) as AudioManager
        val STREAM_TYPE = AudioManager.STREAM_MUSIC
        val currentVolume = audio.getStreamVolume(STREAM_TYPE)
        val maxVolume = audio.getStreamMaxVolume(STREAM_TYPE)
        val VOLUME_UP = 1
        val VOLUME_DOWN = -1
        return object : VolumeProviderCompat(VOLUME_CONTROL_RELATIVE, maxVolume, currentVolume) {
            override fun onAdjustVolume(direction: Int) {

                if (mediaPlayer != null) {
                    if (mediaPlayer!!.isPlaying) {
                        mediaPlayer!!.pause()
                    }
                }
                Log.e("AdjustVolume", "AdjustVolume")

                // Up = 1, Down = -1, Release = 0
                // Replace with your action, if you don't want to adjust system volume
                if (direction == VOLUME_UP) {
                    audio.adjustStreamVolume(
                        STREAM_TYPE,
                        AudioManager.ADJUST_RAISE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
                    )
                } else if (direction == VOLUME_DOWN) {
                    audio.adjustStreamVolume(
                        STREAM_TYPE,
                        AudioManager.ADJUST_LOWER, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
                    )
                }
                setCurrentVolume(audio.getStreamVolume(STREAM_TYPE))
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var data: Bundle? = null
        var name: String? = ""
        var callerData: CallerData? = null
        var callType = ""
        val NOTIFICATION_ID = 120
        try {
            audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager?
            if (audioManager != null) {
                when (audioManager!!.ringerMode) {
                    AudioManager.RINGER_MODE_NORMAL -> status = true
                    AudioManager.RINGER_MODE_SILENT -> status = false
                    AudioManager.RINGER_MODE_VIBRATE -> {
                        status = false
                        vstatus = true
                        Log.e("Service!!", "vibrate mode")
                    }
                }
            }
            //after 30 seconds disconnect call
            handler = Handler(Looper.getMainLooper())
            val delayedStopRunnable = Runnable {
                //releaseMediaPlayer()
                if (intent != null && intent.extras != null) {
                    val data = intent.extras
                    val callerData= data?.getSerializable(AppConstants.IntentConstants.CALLER_DATA) as CallerData
                    sendNotification("Missed Call",callerData?.senderName!! + " (" + callerData?.groupName!! + ")")
                }
                MainApplication.appContext.stopService(Intent(this, HeadsUpNotificationService::class.java))
            }
            handler!!.postDelayed(
                delayedStopRunnable,
                TimeUnit.SECONDS.toMillis(30)
            )
            if (status) {
                //val delayedStopRunnable = Runnable { releaseMediaPlayer() }
                afChangeListener =
                    OnAudioFocusChangeListener { focusChange ->
                        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                            Log.e("AUDIOTEST", "AUDIOFOCUS_LOSS")
                            // Permanent loss of audio focus
                            // Pause playback immediately
                            //mediaController.getTransportControls().pause();
                            if (mediaPlayer != null) {
                                if (mediaPlayer!!.isPlaying) {
                                    mediaPlayer!!.pause()
                                }
                            }
                            // Wait 30 seconds before stopping playback
                          /*  handler!!.postDelayed(
                                delayedStopRunnable,
                                TimeUnit.SECONDS.toMillis(30)
                            )*/
                        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                            // Pause playback
                            Log.e("AUDIOTEST", "AUDIOFOCUS_LOSS_TRANSIENT")
                        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                            // Lower the volume, keep playing
                            Log.e("AUDIOTEST", "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK")
                        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                            Log.e("AUDIOTEST", "AUDIOFOCUS_GAIN")
                            // Your app has been granted audio focus again
                            // Raise volume to normal, restart playback if necessary
                        }
                    }
                val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI)
                mediaPlayer!!.isLooping = true
                //mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    //handler = Handler()
                    playbackAttributes =  AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                    val focusRequest =
                        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                            .setAudioAttributes(playbackAttributes!!)
                            .setAcceptsDelayedFocusGain(true)
                            .setOnAudioFocusChangeListener(afChangeListener!!, handler!!)
                            .build()
                    val res = audioManager!!.requestAudioFocus(focusRequest)
                    if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        /*if (!keyguardManager.isDeviceLocked) {
                            mediaPlayer!!.start()
                        }*/
                        mediaPlayer!!.start()
                    }
                } else {

                    // Request audio focus for playback
                    val result = audioManager!!.requestAudioFocus(
                        afChangeListener,  // Use the music stream.
                        AudioManager.STREAM_MUSIC,  // Request permanent focus.
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                    )
                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        /*if (!keyguardManager.isDeviceLocked) {
                            // Start playback
                            mediaPlayer!!.start()
                        }*/
                        mediaPlayer!!.start()
                    }
                }
            } else if (vstatus) {
                mvibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
                // Start without a delay
                // Each element then alternates between vibrate, sleep, vibrate, sleep...
                val pattern = longArrayOf(
                    0, 250, 200, 250, 150, 150, 75,
                    150, 75, 150
                )

                // The '-1' here means to vibrate once, as '-1' is out of bounds in the pattern array
                mvibrator!!.vibrate(pattern, 0)
                Log.e("Service!!", "vibrate mode start")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (intent != null && intent.extras != null) {
            data = intent.extras
            name = data!!.getString("inititator")
            callerData= data.getSerializable(AppConstants.IntentConstants.CALLER_DATA) as CallerData
            groupId= callerData.groupId
            callType = "Audio"

        }

                val receiveCallAction = Intent(
                    MainApplication.appContext,
                    IncomingCallUIActivity::class.java
                )
                receiveCallAction.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                receiveCallAction.putExtra("ACTION_TYPE", "RECEIVE_CALL")
                receiveCallAction.putExtra("NOTIFICATION_ID", NOTIFICATION_ID)
                receiveCallAction.putExtra(AppConstants.IntentConstants.CALLER_DATA, callerData)

                val cancelCallAction = Intent(
                    MainApplication.appContext,
                    CallNotificationActionReceiver::class.java
                )
                cancelCallAction.putExtra(
                    "ConstantApp.CALL_RESPONSE_ACTION_KEY",
                    "ConstantApp.CALL_CANCEL_ACTION"
                )
                cancelCallAction.putExtra("ACTION_TYPE", "CANCEL_CALL")
                cancelCallAction.putExtra("NOTIFICATION_ID", NOTIFICATION_ID)
                cancelCallAction.putExtra(AppConstants.IntentConstants.CALLER_DATA, callerData)
                cancelCallAction.action = "CANCEL_CALL"

                val callDialogAction = Intent(
                    MainApplication.appContext,
                    IncomingCallUIActivity::class.java
                )
                receiveCallAction.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                callDialogAction.putExtra("ACTION_TYPE", "DIALOG_CALL")
                callDialogAction.putExtra("NOTIFICATION_ID", NOTIFICATION_ID)
                callDialogAction.putExtra(AppConstants.IntentConstants.CALLER_DATA, callerData)

                val receiveCallPendingIntent= PendingIntent.getActivity(
                    MainApplication.appContext,
                    1300,
                    receiveCallAction,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                val cancelCallPendingIntent = PendingIntent.getBroadcast(
                    MainApplication.appContext,
                    1301,
                    cancelCallAction,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                val callDialogPendingIntent = PendingIntent.getActivity(
                    MainApplication.appContext,
                    1302,
                    callDialogAction,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                createChannel()
                var notificationBuilder: NotificationCompat.Builder? = null
                if (data != null) {
                    // Uri ringUri= Settings.System.DEFAULT_RINGTONE_URI;
                    notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle(name + " (" + callerData?.groupName + ")")
                        .setContentText("Incoming $callType Call")
                        .setSmallIcon(R.drawable.ic_chat_audio_small_green)
                        .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(NotificationCompat.CATEGORY_CALL)
                        .addAction(
                            R.drawable.ic_alert_grey,
                            "Reject Call",
                            cancelCallPendingIntent
                        )
                        .addAction(
                            R.drawable.ic_alert_yellow,
                            "Answer Call",
                            receiveCallPendingIntent
                        )
                        .setAutoCancel(true) //.setSound(ringUri)
                        .setFullScreenIntent(callDialogPendingIntent, true)
                }
                var incomingCallNotification: Notification? = null
                if (notificationBuilder != null) {
                    incomingCallNotification = notificationBuilder.build()
                }
                startForeground(NOTIFICATION_ID, incomingCallNotification)


        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy() // release your media player here audioManager.abandonAudioFocus(afChangeListener);
        releaseMediaPlayer()
        releaseVibration()
        mediaSession?.release()
        handler?.removeCallbacksAndMessages(null)
        val intent =
            Intent(AppConstants.MISSED_CALL_BROADCAST)
        MainApplication.appContext.sendBroadcast(intent)

    }

    fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val ringUri: Uri = Settings.System.DEFAULT_RINGTONE_URI
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                )
                channel.description = "Call Notifications"
                channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                /* channel.setSound(ringUri,
                    new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setLegacyStreamType(AudioManager.STREAM_RING)
                            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION).build());*/Objects.requireNonNull(
                    MainApplication.appContext.getSystemService(
                        NotificationManager::class.java
                    )
                ).createNotificationChannel(channel)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun releaseVibration() {
        try {
            if (mvibrator != null) {
                if (mvibrator!!.hasVibrator()) {
                    mvibrator!!.cancel()
                }
                mvibrator = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releaseMediaPlayer() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer!!.isPlaying) {
                    mediaPlayer!!.stop()
                    mediaPlayer!!.reset()
                    mediaPlayer!!.release()
                }
                mediaPlayer = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPrepared(mediaPlayer: MediaPlayer) {}

    /**
     *
     * show push notification
     */
    private fun sendNotification(title: String, body: String) {

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
            channel.vibrationPattern = longArrayOf(0, 500 /*, 500, 1000*/)
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



    }

}