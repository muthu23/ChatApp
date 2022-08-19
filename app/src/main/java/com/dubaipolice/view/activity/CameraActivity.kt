package com.dubaipolice.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Chronometer.OnChronometerTickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.dubaipolice.R
import com.dubaipolice.callback.HandleClick
import com.dubaipolice.databinding.ActivityCameraBinding
import com.dubaipolice.utils.FileUtils
import com.otaliastudios.cameraview.CameraException
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Mode
import java.io.File

class CameraActivity : AppCompatActivity(), HandleClick {

    lateinit var binding: ActivityCameraBinding

    lateinit  var mContext: Context

    private var isTypeVideo = false
    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_camera)

        binding = DataBindingUtil.setContentView(this@CameraActivity, R.layout.activity_camera)

        binding.lifecycleOwner= this@CameraActivity
        //binding.loginViewModel= loginViewModel
        binding.clickHandle= this

        mContext = this

        binding.camera.setLifecycleOwner(this)

        setupCamera()

        setupChronometer()

        changeCameraType(isTypeVideo)

    }

    private fun setupChronometer() {

        binding.chronometer.onChronometerTickListener =
            OnChronometerTickListener { chronometer ->
                val time = SystemClock.elapsedRealtime() - chronometer.base
                val h = (time / 3600000).toInt()
                val m = (time - h * 3600000).toInt() / 60000
                val s = (time - h * 3600000 - m * 60000).toInt() / 1000
                val t =
                    (if (h < 10) "0$h" else h).toString() + ":" + (if (m < 10) "0$m" else m) + ":" + if (s < 10) "0$s" else s
                chronometer.text = t
            }

    }

    private fun hideUi() {
        binding.videoSwitch.visibility = View.GONE
        binding.cameraSwitch.visibility = View.GONE
    }

    private fun showUi() {
        binding.videoSwitch.visibility = View.VISIBLE
        binding.cameraSwitch.visibility = View.VISIBLE
    }

    private fun setupCamera() {

        binding.camera.addCameraListener(object: CameraListener() {

            override fun onPictureTaken(result: PictureResult) {
                super.onPictureTaken(result)

                val file= FileUtils.createFileForUploads(this@CameraActivity, System.currentTimeMillis().toString() + ".jpg")
                if(file != null)
                {
                    result.toFile(
                        file
                    ) { file ->
                        Log.e("Image Saved", file!!.absolutePath!!)
                        setDataResult(file.absolutePath, true)
                    }

                }
            }

            override fun onVideoTaken(result: VideoResult) {
                super.onVideoTaken(result)
                Log.e("Video Saved", result.file.absolutePath)
                setDataResult(result.file.absolutePath, false)
            }

            override fun onCameraError(exception: CameraException) {
                super.onCameraError(exception)
            }

        })

    }

    override fun clickHandle(v: View) {

        when(v.id) {

            R.id.videoSwitch -> {

                isTypeVideo = !isTypeVideo
                changeCameraType(isTypeVideo)

            }

            R.id.cameraSwitch -> {

                toggleCamera()

            }

            R.id.start -> {

                if (isTypeVideo) {
                    if (isRecording) {
                        binding.chronometer.stop()
                        //binding.ivCameraClick.setImageDrawable(resources.getDrawable(R.drawable.camera_normal))
                        isRecording = false
                        binding.camera.stopVideo()
                        binding.start.setImageResource(R.drawable.ic_circle)
                        showUi()
                    } else {
                        val file: File = FileUtils.createFileForUploads(mContext, System.currentTimeMillis().toString()+".mp4")!!
                        if (file != null) {

                            binding.start.setImageResource(R.drawable.ic_stop_circle)
                            hideUi()

                            binding.camera.takeVideo(file)
                            binding.chronometer.base = SystemClock.elapsedRealtime()
                            binding.chronometer.start()
                            //binding.ivCameraClick.setImageDrawable(resources.getDrawable(R.drawable.camera_pressed))
                            isRecording = true
                        }
                    }
                } else {
                    //binding.ivCameraClick.setImageDrawable(resources.getDrawable(R.drawable.camera_pressed))
                    binding.camera.takePicture()
                    Handler().postDelayed({
                        /*binding.ivCameraClick.setImageDrawable(
                            resources.getDrawable(
                                R.drawable.camera_normal
                            )
                        )*/
                    }, 200)
                }

            }

        }

    }

    private fun changeCameraType(isVideo: Boolean) {
        if (isVideo) {
            binding.videoSwitch.setImageResource(R.drawable.ic_camera_outline)
            binding.chronometer.visibility = View.VISIBLE
            isTypeVideo = true
            binding.camera.mode = Mode.VIDEO
            binding.chronometer.text = "00:00:00"
        } else {
            binding.videoSwitch.setImageResource(R.drawable.ic_video_outline)
            binding.chronometer.visibility = View.GONE
            isTypeVideo = false
            binding.camera.mode = Mode.PICTURE
        }
    }

    private fun setDataResult(file: String, isImage: Boolean) {
        val intent = Intent()
        intent.putExtra("isImage", isImage)
        intent.putExtra("selectedFilePath", file)
        setResult(RESULT_OK, intent)

        finish()
    }

    private fun toggleCamera() {
        if (binding.camera.isTakingPicture || binding.camera.isTakingVideo) return
        if (binding.camera.facing === Facing.BACK) binding.camera.facing =
            Facing.FRONT else binding.camera.facing =
            Facing.BACK
    }


}