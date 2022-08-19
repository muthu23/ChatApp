package com.dubaipolice.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.dubaipolice.MainApplication
import com.dubaipolice.R
import com.dubaipolice.api.ApiClient
import com.dubaipolice.api.ApiResponseCallback
import com.dubaipolice.api.GenricError
import com.dubaipolice.model.CommonResponse
import com.dubaipolice.model.GoLiveStreamingResponse
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.utils.Utils
import com.dubaipolice.view.activity.ChatActivity
import com.dubaipolice.view.activity.HomeEndUserActivity
import com.dubaipolice.view.activity.VideoStreamGoLiveBackgroundActivity
import com.google.gson.JsonObject
import com.pedro.encoder.input.video.CameraHelper
import com.pedro.rtplibrary.base.Camera2Base
import com.pedro.rtplibrary.rtmp.RtmpCamera2
import com.pedro.rtplibrary.rtsp.RtspCamera2
import com.pedro.rtplibrary.view.OpenGlView
import retrofit2.Call
import java.util.*

/**
 * Basic RTMP/RTSP service streaming implementation with camera2
 */
class RtpService : Service() {

    private var endpoint: String? = null

//    val handler = Handler(Looper.getMainLooper())
//    var seconds= 0

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "RTP service create")
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_HIGH)
            notificationManager?.createNotificationChannel(channel)
        }
        keepAliveTrick()
    }

/*    fun startTimer()
    {


        seconds= 0

        // Call the post() method,
        // passing in a new Runnable.
        // The post() method processes
        // code without a delay,
        // so the code in the Runnable
        // will run almost immediately.

        // Call the post() method,
        // passing in a new Runnable.
        // The post() method processes
        // code without a delay,
        // so the code in the Runnable
        // will run almost immediately.
        handler.post(object : Runnable {
            override fun run() {
                val hours: Int = seconds / 3600
                val minutes: Int = seconds % 3600 / 60
                val secs: Int = seconds % 60

                // Format the seconds into hours, minutes,
                // and seconds.
                val time: String = java.lang.String
                    .format(
                        Locale.getDefault(),
                        "%d:%02d:%02d", hours,
                        minutes, secs
                    )

                // Set the text view text.
                //timeView.setText(time)

                Log.e("TimerStart2", time)

                // If running is true, increment the
                // seconds variable.
                //if (running) {
                seconds++
                //}

                // Post the code again
                // with a delay of 1 second.
                handler.postDelayed(this, 1000)
            }
        })

    }

    fun stopTimer()
    {
        handler.removeCallbacksAndMessages(null)
        seconds= 0
    }*/

    private fun keepAliveTrick() {

        val notificationIntent = Intent(this, VideoStreamGoLiveBackgroundActivity::class.java)
        notificationIntent.putExtra(AppConstants.IntentConstants.GROUP_ID, MainApplication.liveStreamGroupId)

        val pendingIntent= PendingIntent.getActivity(
            this,
            0, notificationIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val input =
            "Live streaming is Running"
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle("Live Streaming")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_logo_push_notification)
                .setColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setAutoCancel(false)
                .setContentIntent(pendingIntent).build()
            startForeground(2, notification)
        } else {
            startForeground(2, Notification())
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "RTP service started")
        endpoint = intent?.extras?.getString("endpoint")
        streamingLink = intent?.extras?.getString("endpoint")
        groupId= intent?.extras?.getString("groupId")
        if (endpoint != null) {
            prepareStreamRtp()
            startStreamRtp(endpoint!!)
        }
        return START_STICKY
    }

    companion object {
        private const val TAG = "RtpService"
        private const val channelId = "rtpStreamChannel"
        private const val notifyId = 123456
        private var notificationManager: NotificationManager? = null
        private var camera2Base: Camera2Base? = null
        private var openGlView: OpenGlView? = null
        private var contextApp: Context? = null
        var cameraFace: CameraHelper.Facing? = null
        var isAudioMuted: Boolean = false

        var groupId: String? = null
        var streamingLink: String? = null

        val handler = Handler(Looper.getMainLooper())
        var seconds= 0

        fun setView(openGlView: OpenGlView) {
            this.openGlView = openGlView
            camera2Base?.replaceView(openGlView)
        }

        fun setView(context: Context) {
            contextApp = context
            this.openGlView = null
            camera2Base?.replaceView(context)
        }

        fun startPreview() {
            camera2Base?.startPreview()
        }

        fun init(context: Context) {
            contextApp = context
            if (camera2Base == null) camera2Base = RtmpCamera2(context, true, connectCheckerRtp)
        }

        fun stopStream() {
            if (camera2Base != null) {
                if (camera2Base!!.isStreaming) camera2Base!!.stopStream()
            }
        }

        fun stopPreview() {
            if (camera2Base != null) {
                if (camera2Base!!.isOnPreview) camera2Base!!.stopPreview()
            }
        }

        fun isStreamAudioMuted(): Boolean {
            if (camera2Base != null) {
                if(camera2Base!!.isAudioMuted)
                {
                    camera2Base!!.enableAudio()
                    isAudioMuted= false
                    return true
                }
                else
                {
                    camera2Base!!.disableAudio()
                    isAudioMuted= true
                    return false
                }
            }
            return false
        }

        fun switchStreamCamera() {
            if (camera2Base != null) {
                camera2Base!!.switchCamera()
                cameraFace= camera2Base!!.cameraFacing
            }
        }

        fun startTimer()
        {

//        var chronometer = Chronometer(mContext)
//
//        chronometer.onChronometerTickListener =
//            Chronometer.OnChronometerTickListener { chronometer ->
//                val time = SystemClock.elapsedRealtime() - chronometer.base
//                val h = (time / 3600000).toInt()
//                val m = (time - h * 3600000).toInt() / 60000
//                val s = (time - h * 3600000 - m * 60000).toInt() / 1000
//                val t =
//                    (if (h < 10) "0$h" else h).toString() + ":" + (if (m < 10) "0$m" else m) + ":" + if (s < 10) "0$s" else s
//                chronometer.text = t
//                Log.e("TimerStart", t)
//            }
//
//        chronometer.setBase(SystemClock.elapsedRealtime())
//        chronometer.start()


            //val handler = Handler(Looper.getMainLooper())
            //handler.removeCallbacksAndMessages(null)
            //var seconds= 0
            seconds= 0

            // Call the post() method,
            // passing in a new Runnable.
            // The post() method processes
            // code without a delay,
            // so the code in the Runnable
            // will run almost immediately.

            // Call the post() method,
            // passing in a new Runnable.
            // The post() method processes
            // code without a delay,
            // so the code in the Runnable
            // will run almost immediately.
            handler.post(object : Runnable {
                override fun run() {
                    val hours: Int = seconds / 3600
                    val minutes: Int = seconds % 3600 / 60
                    val secs: Int = seconds % 60

                    // Format the seconds into hours, minutes,
                    // and seconds.
                    val time: String = java.lang.String
                        .format(
                            Locale.getDefault(),
                            "%d:%02d:%02d", hours,
                            minutes, secs
                        )

                    // Set the text view text.
                    //timeView.setText(time)

                    Log.e("TimerStart2", time)
                    val intent = Intent(AppConstants.STREAM_TIMER_BROADCAST)
                    intent.putExtra(AppConstants.STREAM_TIME, time)
                    MainApplication.appContext.sendBroadcast(intent)

                    // If running is true, increment the
                    // seconds variable.
                    //if (running) {
                    seconds++
                    //}

                    // Post the code again
                    // with a delay of 1 second.
                    handler.postDelayed(this, 1000)
                }
            })

        }

        fun stopTimer()
        {
            Log.e("TimerStop", "TimerStop")
            handler.removeCallbacksAndMessages(null)
            seconds= 0
        }

        fun stopLiveStreaming()
        {
            val call: Call<GoLiveStreamingResponse> =
                ApiClient.request!!.stopLiveStreaming(Utils.getSelectedLanguage()!!, SharedPref.readString(AppConstants.KEY_TOKEN)!!, groupId)
            call.enqueue(object : ApiResponseCallback<GoLiveStreamingResponse?>() {

                override fun onSuccess(response: GoLiveStreamingResponse?) {
                    if (response != null) {
                        if (response.isSuccess) {
                            Log.e("StopStream", "Success")
                        }
                    }
                }

                override fun onError(msg: GenricError?) {
                    if (msg != null && !TextUtils.isEmpty(msg.message)) {
                        Log.e("StopStream", msg.message!!)
                    }
                }
            })

        }

        fun saveLiveStreaming()
        {

            var parameter: JsonObject? = null

            try {
                parameter = JsonObject()
                parameter!!.addProperty(AppConstants.FIELD.LIVE_STREAM_LINK, streamingLink)
                parameter!!.addProperty(AppConstants.FIELD.GROUP_ID, groupId)
                Log.d("Parameter", parameter.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val call: Call<CommonResponse> =
                ApiClient.request!!.saveLiveStreaming(Utils.getSelectedLanguage()!!, SharedPref.readString(AppConstants.KEY_TOKEN)!!, parameter!!)
            call.enqueue(object : ApiResponseCallback<CommonResponse?>() {

                override fun onSuccess(response: CommonResponse?) {
                    if (response != null) {
                        if (response.isSuccess) {
                            Log.e("StopStream", "Success")
                        }
                    }
                }

                override fun onError(msg: GenricError?) {
                    if (msg != null && !TextUtils.isEmpty(msg.message)) {
                        Log.e("StopStream", msg.message!!)
                    }
                }
            })

        }


        private val connectCheckerRtp = object : ConnectCheckerRtp {
            override fun onConnectionStartedRtp(rtpUrl: String) {
                showNotification("Stream connection started")
            }

            override fun onConnectionSuccessRtp() {
                showNotification("Stream started")
                Log.e(TAG, "RTP service connected")
                startTimer()
                //saveLiveStreaming()
            }

            override fun onNewBitrateRtp(bitrate: Long) {

            }

            override fun onConnectionFailedRtp(reason: String) {
                showNotification("Stream connection failed")
                Log.e("TimerStop1", "TimerStop")
                Log.e(TAG, "RTP service destroy")
                stopTimer()
                stopStream()
                stopLiveStreaming()
                val intent = Intent(AppConstants.STREAM_STOP_BROADCAST)
                MainApplication.appContext.sendBroadcast(intent)
                MainApplication.stopRtpService()

            }

            override fun onDisconnectRtp() {
                showNotification("Stream stopped")
                Log.e("TimerStop2", "TimerStop")
                stopTimer()
                stopStream()
                stopLiveStreaming()
                val intent = Intent(AppConstants.STREAM_STOP_BROADCAST)
                MainApplication.appContext.sendBroadcast(intent)
                MainApplication.stopRtpService()
            }

            override fun onAuthErrorRtp() {
                showNotification("Stream auth error")
            }

            override fun onAuthSuccessRtp() {
                showNotification("Stream auth success")
            }
        }

        private fun showNotification(text: String) {
            contextApp?.let {
                val notification = NotificationCompat.Builder(it, channelId)
                    .setSmallIcon(R.drawable.ic_logo_push_notification)
                    .setColor(ContextCompat.getColor(contextApp!!, R.color.colorPrimary))
                    .setContentTitle("RTP Stream")
                    .setContentText(text).build()
                notificationManager?.notify(notifyId, notification)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "RTP service destroy")
        stopStream()
    }

    private fun prepareStreamRtp() {
        stopStream()
        stopPreview()
        if (endpoint!!.startsWith("rtmp")) {
            camera2Base = if (openGlView == null) {
                RtmpCamera2(baseContext, true, connectCheckerRtp)
            } else {
                RtmpCamera2(openGlView, connectCheckerRtp)
            }
        } else {
            camera2Base = if (openGlView == null) {
                RtspCamera2(baseContext, true, connectCheckerRtp)
            } else {
                RtspCamera2(openGlView, connectCheckerRtp)
            }
        }
    }

    private fun startStreamRtp(endpoint: String) {
        if (!camera2Base!!.isStreaming) {
            if (camera2Base!!.prepareVideo() && camera2Base!!.prepareAudio()) {
                /*if(camera2Base!!.cameraFacing == CameraHelper.Facing.BACK)
                {
                    camera2Base!!.switchCamera()
                }*/
                if(cameraFace != null && cameraFace != camera2Base!!.cameraFacing)
                {
                    camera2Base!!.switchCamera()
                }

                if(isAudioMuted)
                {
                    camera2Base!!.disableAudio()
                }
                else
                {
                    camera2Base!!.enableAudio()
                }
                camera2Base!!.startStream(endpoint)
            }
        } else {
            showNotification("You are already streaming :(")
        }
    }

}