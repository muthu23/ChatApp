package com.dubaipolice.view.activity

import android.app.ActivityManager
import android.app.PictureInPictureParams
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Rational
import android.view.SurfaceHolder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dubaipolice.MainApplication
import com.dubaipolice.R
import com.dubaipolice.callback.HandleClick
import com.dubaipolice.databinding.ActivityVideoStreamGoLiveBackgroundBinding
import com.dubaipolice.model.GoLiveStreamingResponse
import com.dubaipolice.service.RtpService
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.MyProgressDialog
import com.dubaipolice.utils.Utils
import com.dubaipolice.viewmodel.GoLiveStreamingViewModel
import com.dubaipolice.wrapper.Resource
import com.pedro.encoder.input.video.CameraOpenException

class VideoStreamGoLiveBackgroundActivity : AppCompatActivity(), HandleClick,
     SurfaceHolder.Callback {

    lateinit var goLiveStreamingViewModel: GoLiveStreamingViewModel
    lateinit var binding: ActivityVideoStreamGoLiveBackgroundBinding

    lateinit  var mContext: Context

    private var groupId: String? = null

    private var broadcastReceiver: BroadcastReceiver? = null

    var isPipMode= false

    var myStreamingLink= ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@VideoStreamGoLiveBackgroundActivity, R.layout.activity_video_stream_go_live_background)
        goLiveStreamingViewModel = ViewModelProvider(this)[GoLiveStreamingViewModel::class.java]

        binding.lifecycleOwner= this@VideoStreamGoLiveBackgroundActivity
        //inding.loginViewModel= loginViewModel
        binding.clickHandle= this

        mContext = this

        getExtras()

        RtpService.init(this)

        binding.surfaceView.holder.addCallback(this)

        registerBroadcastReceiver()

    }

    private fun getExtras() {

        val extras = intent.extras
        Log.e("GetExtra", "get")
        if (extras != null) {
            groupId = extras.getString(AppConstants.IntentConstants.GROUP_ID)
            MainApplication.liveStreamGroupId= groupId
            Log.e("GroupId", groupId!!)

            goLiveStreamingViewModel.groupId.value = groupId
        } else {
            Log.e("GroupName", "NULL")
        }

    }

    /**
     * This function is used to register for broadcase events
     * to be received from background media downloads worker
     *
     *
     */
    private fun registerBroadcastReceiver() {

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                Log.i("Receiver", "Broadcast received: $action")
                if (action == AppConstants.STREAM_TIMER_BROADCAST) {

                    val extras = intent.extras

                    if (extras != null) {
                        val streamTime = extras.getString(AppConstants.STREAM_TIME)

                        if (streamTime != null) {
                            binding.layoutLive.visibility = View.VISIBLE
                            binding.chronometer.text = streamTime
                        }

                    }

                }
                else   if (action == AppConstants.STREAM_STOP_BROADCAST) {

                    binding.layoutLive.visibility = View.GONE
                    binding.chronometer.text = "00:00:00"

                    if(!isPipMode)
                    {
                        binding.btnStartLive.visibility= View.VISIBLE
                        binding.btnEndLive.visibility= View.GONE
                    }

                    //stopLiveStreaming()

                }
            }
        }


        val intentFilter = IntentFilter()
        intentFilter.addAction(AppConstants.STREAM_TIMER_BROADCAST)

        intentFilter.addAction(AppConstants.STREAM_STOP_BROADCAST)


        //registering the broadcast receiver
        registerReceiver(
            broadcastReceiver,
            intentFilter
        )
        /*registerReceiver(
            broadcastReceiver,
            IntentFilter(AppConstants.STREAM_TIMER_BROADCAST)
        )*/
    }

    override fun onStop() {
        super.onStop()
        stopService(Intent(applicationContext, RtpService::class.java))
        binding.btnStartLive.visibility= View.VISIBLE
        binding.btnEndLive.visibility= View.GONE
        binding.layoutLive.visibility = View.GONE
        MainApplication.liveStreamGroupId= null

        RtpService.cameraFace= null
        RtpService.isAudioMuted= false

        finish()
    }

    /**
     * This function is used to unregister broadcast receiver
     */
    private fun unregisterBroadcastReceiver() {
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterBroadcastReceiver()
    }

    override fun onBackPressed() {

        if (isMyServiceRunning(RtpService::class.java)) {
            startPictureInPictureMode()
        }
        else
        {
            finish()
        }


    }

    override fun clickHandle(v: View) {
        when(v.id) {

            R.id.back -> {

                onBackPressed()

            }

            R.id.btnStartLive -> {

                //current link is there then start live using old link
                goLiveStreaming()
                /*if(!TextUtils.isEmpty(myStreamingLink.trim()))
                {

                    val intent = Intent(applicationContext, RtpService::class.java)
                    //intent.putExtra("endpoint", "rtmp://172.105.55.116/show/thisIsStreamKey")
                    intent.putExtra("endpoint", myStreamingLink)
                    intent.putExtra("groupId", groupId!!)
                    startService(intent)
                    binding.btnStartLive.visibility= View.GONE
                    binding.btnEndLive.visibility= View.VISIBLE

                }
                else
                {
                    goLiveStreaming()
                }*/



                /*val intent = Intent(applicationContext, RtpService::class.java)
                intent.putExtra("endpoint", "rtmp://172.105.55.116/show/thisIsStreamKey")
                intent.putExtra("groupId", groupId!!)
                startService(intent)
                binding.btnStartLive.visibility= View.GONE
                binding.btnEndLive.visibility= View.VISIBLE
*/
               /* if (isMyServiceRunning(RtpService::class.java)) {
                    stopService(Intent(applicationContext, RtpService::class.java))
                    binding.btnStartLive.visibility= View.VISIBLE
                    binding.btnEndLive.visibility= View.GONE
                    //binding.bStartStop.setText(R.string.start_button)
                } else {
                    val intent = Intent(applicationContext, RtpService::class.java)
                    intent.putExtra("endpoint", "rtmp://172.105.55.11/show/thisIsStreamKey")
                    startService(intent)
                    binding.btnStartLive.visibility= View.GONE
                    binding.btnEndLive.visibility= View.VISIBLE
                    //binding.bStartStop.setText(R.string.stop_button)
                }*/

            }

            R.id.btnEndLive -> {

                stopService(Intent(applicationContext, RtpService::class.java))
                binding.btnStartLive.visibility= View.VISIBLE
                binding.btnEndLive.visibility= View.GONE
                binding.layoutLive.visibility = View.GONE

                stopLiveStreaming()

              /*  if (isMyServiceRunning(RtpService::class.java)) {
                    stopService(Intent(applicationContext, RtpService::class.java))
                    binding.btnStartLive.visibility= View.VISIBLE
                    binding.btnEndLive.visibility= View.GONE
                    //binding.bStartStop.setText(R.string.start_button)
                } else {
                    val intent = Intent(applicationContext, RtpService::class.java)
                    intent.putExtra("endpoint", "rtmp://172.105.55.116/show/thisIsStreamKey")
                    startService(intent)
                    binding.btnStartLive.visibility= View.GONE
                    binding.btnEndLive.visibility= View.VISIBLE
                    //binding.bStartStop.setText(R.string.stop_button)
                }*/

            }

            R.id.imgMicrophone -> {

                try {
                    //RtpService.setView(applicationContext)
                    if(RtpService.isStreamAudioMuted())
                    {
                        binding.imgMicrophone.setImageResource(R.drawable.ic_go_live_microphone)
                    }
                    else
                    {
                        binding.imgMicrophone.setImageResource(R.drawable.ic_go_live_microphone_mute)
                    }
                } catch (e: CameraOpenException) {
                    Utils.showToast(mContext, e.message!!)
                }


            }


            R.id.imgSwitchCamra -> {

                try {
                    //RtpService.setView(applicationContext)
                    RtpService.switchStreamCamera()
                } catch (e: CameraOpenException) {
                    Utils.showToast(mContext, e.message!!)
                }

            }

            R.id.imgPipMode -> {

                startPictureInPictureMode()

            }

        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        startPictureInPictureMode()
    }

    private fun startPictureInPictureMode()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val aspectRatio = Rational(resources.getDimension(R.dimen._300sdp).toInt(), resources.getDimension(R.dimen._400sdp).toInt())
            val params: PictureInPictureParams = PictureInPictureParams.Builder()
                //.setAspectRatio(aspectRatio)
                .setAspectRatio(Rational(2, 3))
                .build()
            enterPictureInPictureMode(params)
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean,
                                               newConfig: Configuration
    ) {

        if (isInPictureInPictureMode) {
            // Hide the full-screen UI (controls, etc.) while in picture-in-picture mode.
            isPipMode= true
            binding.back.visibility= View.GONE
            binding.imgPipMode.visibility= View.GONE
            binding.imgSwitchCamra.visibility= View.GONE
            binding.imgMicrophone.visibility= View.GONE
            binding.btnStartLive.visibility= View.GONE
            binding.btnEndLive.visibility= View.GONE

            binding.tvLive.textSize= resources.getDimension(R.dimen._5sdp)
            binding.chronometer.textSize= resources.getDimension(R.dimen._5sdp)

        } else {
            // Restore the full-screen UI.
            isPipMode= false
            binding.back.visibility= View.VISIBLE
            binding.imgPipMode.visibility= View.VISIBLE
            binding.imgSwitchCamra.visibility= View.VISIBLE
            binding.imgMicrophone.visibility= View.VISIBLE
            if (isMyServiceRunning(RtpService::class.java)) {
                binding.btnStartLive.visibility= View.GONE
                binding.btnEndLive.visibility= View.VISIBLE
                binding.layoutLive.visibility= View.VISIBLE
                //binding.bStartStop.setText(R.string.stop_button)
            } else {
                binding.btnStartLive.visibility= View.VISIBLE
                binding.btnEndLive.visibility= View.GONE
                binding.layoutLive.visibility= View.GONE
                //binding.bStartStop.setText(R.string.start_button)
            }
            binding.tvLive.textSize= resources.getDimension(R.dimen._8sdp)
            binding.chronometer.textSize= resources.getDimension(R.dimen._8sdp)

        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        RtpService.setView(binding.surfaceView)
        RtpService.startPreview()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        RtpService.setView(applicationContext)
        RtpService.stopPreview()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {

    }

    override fun onResume() {
        super.onResume()
        if (isMyServiceRunning(RtpService::class.java)) {
            binding.btnStartLive.visibility= View.GONE
            binding.btnEndLive.visibility= View.VISIBLE
            binding.layoutLive.visibility= View.VISIBLE
            //binding.bStartStop.setText(R.string.stop_button)
        } else {
            binding.btnStartLive.visibility= View.VISIBLE
            binding.btnEndLive.visibility= View.GONE
            binding.layoutLive.visibility= View.GONE
            //binding.bStartStop.setText(R.string.start_button)
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

    private fun goLiveStreaming() {

        MyProgressDialog.show(mContext);

        goLiveStreamingViewModel.goLiveStreamingResponse!!.observe(
            this@VideoStreamGoLiveBackgroundActivity,
            Observer<Resource<GoLiveStreamingResponse?>> { goLiveResponseResource ->
                MyProgressDialog.dismiss()
                when (goLiveResponseResource.status) {
                    Resource.Status.SUCCESS -> {
                        if (goLiveResponseResource.data!!.isSuccess) {

                            val streamingLink= goLiveResponseResource.data.data?.streamingLink

                            if(streamingLink != null)
                            {

                                val intent = Intent(applicationContext, RtpService::class.java)
                                //intent.putExtra("endpoint", "rtmp://172.105.55.116/show/thisIsStreamKey")
                                intent.putExtra("endpoint", streamingLink)
                                intent.putExtra("groupId", groupId!!)
                                startService(intent)
                                binding.btnStartLive.visibility= View.GONE
                                binding.btnEndLive.visibility= View.VISIBLE

                            }
                            else
                            {
                                Utils.showToast(mContext, "Streaming link not found!")
                            }

                        } else {
                            Utils.showToast(mContext, goLiveResponseResource.data.message!!)
                        }
                    }
                    Resource.Status.LOADING -> {
                    }
                    Resource.Status.ERROR -> {
                        Utils.showToast(mContext, goLiveResponseResource.message!!)
                    }
                }
            })

    }

    private fun stopLiveStreaming() {

        //MyProgressDialog.show(mContext);

        goLiveStreamingViewModel.stopLiveStreamingResponse!!.observe(
            this@VideoStreamGoLiveBackgroundActivity,
            Observer<Resource<GoLiveStreamingResponse?>> { goLiveResponseResource ->
                //MyProgressDialog.dismiss()
                when (goLiveResponseResource.status) {
                    Resource.Status.SUCCESS -> {
                        if (goLiveResponseResource.data!!.isSuccess) {

                            Utils.showToast(mContext, "Streaming stopped")

                        } else {
                            Utils.showToast(mContext, goLiveResponseResource.data.message!!)
                        }
                    }
                    Resource.Status.LOADING -> {
                    }
                    Resource.Status.ERROR -> {
                        Utils.showToast(mContext, goLiveResponseResource.message!!)
                    }
                }
            })

    }

}