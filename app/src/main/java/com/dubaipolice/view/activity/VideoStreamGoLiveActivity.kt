package com.dubaipolice.view.activity

import android.app.PictureInPictureParams
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.util.Rational
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Chronometer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dubaipolice.R
import com.dubaipolice.callback.HandleClick
import com.dubaipolice.databinding.ActivityVideoStreamGoLiveBinding
import com.dubaipolice.db.model.GroupInfoTable
import com.dubaipolice.model.GoLiveStreamingResponse
import com.dubaipolice.model.LoginResponse
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.MyProgressDialog
import com.dubaipolice.utils.Utils
import com.dubaipolice.viewmodel.GoLiveStreamingViewModel
import com.dubaipolice.wrapper.Resource
import com.pedro.encoder.input.video.CameraOpenException
import com.pedro.rtmp.utils.ConnectCheckerRtmp
import com.pedro.rtplibrary.rtmp.RtmpCamera1
import com.pedro.rtplibrary.view.OpenGlView


class VideoStreamGoLiveActivity : AppCompatActivity(), HandleClick,
    ConnectCheckerRtmp, SurfaceHolder.Callback {

    lateinit var goLiveStreamingViewModel: GoLiveStreamingViewModel
    lateinit var binding: ActivityVideoStreamGoLiveBinding

    lateinit  var mContext: Context

    private var rtmpCamera1: RtmpCamera1? = null
    private var openGlView: OpenGlView? = null

    private var groupId: String? = null

    var isPipMode= false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@VideoStreamGoLiveActivity, R.layout.activity_video_stream_go_live)
        goLiveStreamingViewModel = ViewModelProvider(this)[GoLiveStreamingViewModel::class.java]

        binding.lifecycleOwner= this@VideoStreamGoLiveActivity
        //inding.loginViewModel= loginViewModel
        binding.clickHandle= this

        mContext = this

        getExtras()

        openGlView = findViewById(R.id.surfaceView);

        //create builder
        rtmpCamera1 = RtmpCamera1(openGlView, this)

        openGlView!!.getHolder().addCallback(this)

        setupChronometer()

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

        } else {
            // Restore the full-screen UI.
            isPipMode= false
            binding.back.visibility= View.VISIBLE
            binding.imgPipMode.visibility= View.VISIBLE
            binding.imgSwitchCamra.visibility= View.VISIBLE
            binding.imgMicrophone.visibility= View.VISIBLE
            binding.btnStartLive.visibility= View.VISIBLE
            if(rtmpCamera1!!.isStreaming)
            {
                binding.btnEndLive.visibility= View.VISIBLE
                binding.btnStartLive.visibility= View.GONE
            }
            else
            {
                binding.btnEndLive.visibility= View.GONE
                binding.btnStartLive.visibility= View.VISIBLE
            }
        }
    }


    private fun setupChronometer() {

        binding.chronometer.onChronometerTickListener =
            Chronometer.OnChronometerTickListener { chronometer ->
                val time = SystemClock.elapsedRealtime() - chronometer.base
                val h = (time / 3600000).toInt()
                val m = (time - h * 3600000).toInt() / 60000
                val s = (time - h * 3600000 - m * 60000).toInt() / 1000
                val t =
                    (if (h < 10) "0$h" else h).toString() + ":" + (if (m < 10) "0$m" else m) + ":" + if (s < 10) "0$s" else s
                chronometer.text = t
            }

    }

    private fun getExtras() {

        val extras = intent.extras
        Log.e("GetExtra", "get")
        if (extras != null) {
            groupId = extras.getString(AppConstants.IntentConstants.GROUP_ID)

            Log.e("GroupId", groupId!!)

            goLiveStreamingViewModel.groupId.value = groupId
        } else {
            Log.e("GroupName", "NULL")
        }

    }

    override fun clickHandle(v: View) {

        when(v.id) {

            R.id.back -> {

                onBackPressed()



            }

            R.id.btnStartLive -> {

                goLiveStreaming()

               /* if (!rtmpCamera1!!.isStreaming) {
                    if (rtmpCamera1!!.prepareAudio() && rtmpCamera1!!.prepareVideo()) {
                        binding.btnStartLive.visibility= View.GONE
                        binding.btnEndLive.visibility= View.VISIBLE
                        rtmpCamera1!!.startStream("rtmp://172.105.55.116/show/thisIsStreamKey")
                    } else {
                        Utils.showToast(mContext, "Error preparing stream, This device cant do it")
                    }
                }*/

            }

            R.id.btnEndLive -> {

                binding.chronometer.stop()
                binding.layoutLive.visibility = View.GONE

                if (rtmpCamera1!!.isStreaming) {

                    binding.btnStartLive.visibility= View.VISIBLE
                    binding.btnEndLive.visibility= View.GONE
                    rtmpCamera1!!.stopStream()

                }

                stopLiveStreaming()

            }

            R.id.imgMicrophone -> {

                try {
                    if(rtmpCamera1!!.isAudioMuted)
                    {
                        rtmpCamera1!!.enableAudio()
                        binding.imgMicrophone.setImageResource(R.drawable.ic_go_live_microphone)
                    }
                    else
                    {
                        rtmpCamera1!!.disableAudio()
                        binding.imgMicrophone.setImageResource(R.drawable.ic_go_live_microphone_mute)
                    }

                } catch (e: CameraOpenException) {
                    Utils.showToast(mContext, e.message!!)
                }

            }


            R.id.imgSwitchCamra -> {

                try {
                    rtmpCamera1!!.switchCamera()
                } catch (e: CameraOpenException) {
                    Utils.showToast(mContext, e.message!!)
                }

            }

            R.id.imgPipMode -> {

                startPictureInPictureMode()

            }

        }

    }

    private fun stopLiveStreaming() {

        //MyProgressDialog.show(mContext);

        goLiveStreamingViewModel.stopLiveStreamingResponse!!.observe(
            this@VideoStreamGoLiveActivity,
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

    private fun goLiveStreaming() {

        MyProgressDialog.show(mContext);

        goLiveStreamingViewModel.goLiveStreamingResponse!!.observe(
            this@VideoStreamGoLiveActivity,
            Observer<Resource<GoLiveStreamingResponse?>> { goLiveResponseResource ->
                MyProgressDialog.dismiss()
                when (goLiveResponseResource.status) {
                    Resource.Status.SUCCESS -> {
                        if (goLiveResponseResource.data!!.isSuccess) {

                            val streamingLink= goLiveResponseResource.data.data?.streamingLink

                            if(streamingLink != null)
                            {

                                if (!rtmpCamera1!!.isStreaming) {
                                    if (rtmpCamera1!!.prepareAudio() && rtmpCamera1!!.prepareVideo()) {
                                        binding.btnStartLive.visibility= View.GONE
                                        binding.btnEndLive.visibility= View.VISIBLE
                                        rtmpCamera1!!.startStream(streamingLink)
                                        //rtmpCamera1!!.startStream("rtmp://172.105.55.116/show/thisIsStreamKey")
                                    } else {
                                        Utils.showToast(mContext, "Error preparing stream, This device cant do it")
                                    }
                                }

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

    override fun onAuthErrorRtmp() {
        runOnUiThread {
            Utils.showToast(mContext, "Auth error")
        }
    }

    override fun onAuthSuccessRtmp() {
        runOnUiThread {
            Utils.showToast(mContext, "Auth success")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onConnectionFailedRtmp(reason: String) {
        Log.e("SurfaceDestroyed", "ConnectionFailed")
        runOnUiThread {
            Utils.showToast(mContext, "Connection failed. " + reason)
            rtmpCamera1!!.stopStream()
            if(!isPipMode)
            {
                binding.btnStartLive.visibility = View.VISIBLE
                binding.btnEndLive.visibility = View.GONE
            }
            binding.chronometer.stop()
            binding.layoutLive.visibility = View.GONE
            stopLiveStreaming()
        }

    }

    override fun onConnectionStartedRtmp(rtmpUrl: String) {

    }

    override fun onConnectionSuccessRtmp() {

        runOnUiThread {
            Utils.showToast(mContext, "Connection success")
            binding.chronometer.base = SystemClock.elapsedRealtime()
            binding.layoutLive.visibility = View.VISIBLE
            binding.chronometer.start()
        }

    }

    override fun onDisconnectRtmp() {
        Log.e("SurfaceDestroyed", "OnDisconnectRtmp")
        runOnUiThread {
            Utils.showToast(mContext, "Disconnected")
        }
    }

    override fun onNewBitrateRtmp(bitrate: Long) {

    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {

    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        rtmpCamera1!!.startPreview()
    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        Log.e("SurfaceDestroyed", "SurfaceDestroye")
        if (rtmpCamera1!!.isStreaming) {
            rtmpCamera1!!.stopStream()
            if(!isPipMode)
            {
                binding.btnStartLive.visibility = View.VISIBLE
                binding.btnEndLive.visibility = View.GONE
            }
            binding.chronometer.stop()
            binding.layoutLive.visibility = View.GONE
            stopLiveStreaming()
        }
        rtmpCamera1!!.stopPreview();
    }

}