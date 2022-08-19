package com.dubaipolice.view.activity

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.*
import android.util.Log
import android.util.Rational
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.dubaipolice.R
import com.dubaipolice.callback.HandleClick
import com.dubaipolice.databinding.ActivityVideoStreamBinding
import com.dubaipolice.db.model.GroupInfoTable
import com.dubaipolice.model.HLSLink
import com.dubaipolice.receiver.PIPActionsReceiver
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.MyProgressDialog
import com.dubaipolice.utils.Utils
import com.dubaipolice.viewmodel.VideoStreamViewModel
import com.dubaipolice.wrapper.Resource
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.rtsp.RtspMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util


class VideoStreamActivity : AppCompatActivity(), HandleClick {

    private lateinit var link: HLSLink

    lateinit var videoStreamViewModel: VideoStreamViewModel
    lateinit var binding: ActivityVideoStreamBinding

    lateinit  var mContext: Context

    private lateinit var simpleExoPlayer: ExoPlayer

    private var url = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@VideoStreamActivity, R.layout.activity_video_stream)
        videoStreamViewModel = ViewModelProvider(this)[VideoStreamViewModel::class.java]

        binding.lifecycleOwner= this@VideoStreamActivity
        binding.clickHandle= this

        mContext = this

        getExtras()

    }

    private fun getExtras() {
        val extras = intent.extras
        extras.let {
             link = extras?.getSerializable(AppConstants.IntentConstants.HLS_LINK_DATA) as HLSLink
            url = link.link
        }
    }

    private fun initializePlayer() {

        val mediaDataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(this)

        val mediaSource= HlsMediaSource.Factory(mediaDataSourceFactory).createMediaSource(MediaItem.Builder()
            .setUri(url)
            .setMimeType(MimeTypes.APPLICATION_M3U8)
            .build())
        //val mediaSource= RtspMediaSource.Factory().setForceUseRtpTcp(true).createMediaSource(mediaItem)
       // val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(MediaItem.fromUri(STREAM_URL))

        val mediaSourceFactory: MediaSourceFactory = DefaultMediaSourceFactory(mediaDataSourceFactory)

        simpleExoPlayer = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()

        simpleExoPlayer.addMediaSource(mediaSource)

       /* simpleExoPlayer.addListener(object: Player.Listener{
            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)

                if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                    // Re-initialize player at the current live window default position.
                    simpleExoPlayer.seekToDefaultPosition()
                    simpleExoPlayer.prepare()
                } else {
                    // Handle other errors.
                }

            }

            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                super.onTimelineChanged(timeline, reason)
            }

            override fun onIsLoadingChanged(isLoading: Boolean) {
                super.onIsLoadingChanged(isLoading)
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                super.onPositionDiscontinuity(oldPosition, newPosition, reason)
            }
        })*/

        simpleExoPlayer.prepare()
        simpleExoPlayer.playWhenReady = true
        binding.playerView.player = simpleExoPlayer
        binding.playerView.requestFocus()

    }

    private fun releasePlayer() {
        simpleExoPlayer.release()
    }

    public override fun onStart() {
        super.onStart()

        if (Util.SDK_INT > 23) initializePlayer()
    }

    public override fun onResume() {
        super.onResume()

        if (Util.SDK_INT <= 23) initializePlayer()
    }

    public override fun onPause() {
        super.onPause()

        if (Util.SDK_INT <= 23) releasePlayer()
    }

    public override fun onStop() {
        super.onStop()

        if (Util.SDK_INT > 23) releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(link.isCameraUrl)
        stopViewStream()

    }

    private fun stopViewStream() {
        videoStreamViewModel.stopStream(link)
    }

    companion object {
        //const val STREAM_URL = "rtsp://rtsp.stream/pattern"
        const val STREAM_URL = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"
    }

    override fun clickHandle(v: View) {
        when(v.id) {
            R.id.back -> {
                onBackPressed()

            }

        }
    }



}