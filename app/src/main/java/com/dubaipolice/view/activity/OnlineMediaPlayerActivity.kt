package com.dubaipolice.view.activity

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.dubaipolice.R
import com.dubaipolice.callback.HandleClick
import com.dubaipolice.databinding.ActivityOnlineMediaPlayerBinding
import com.dubaipolice.databinding.ActivityVideoStreamBinding
import com.dubaipolice.model.HLSLink
import com.dubaipolice.utils.AppConstants
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.Util


class OnlineMediaPlayerActivity : AppCompatActivity(), HandleClick {

    lateinit var binding: ActivityOnlineMediaPlayerBinding
    lateinit var mContext: Context
    private lateinit var simpleExoPlayer: ExoPlayer
    private var url = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this@OnlineMediaPlayerActivity,
            R.layout.activity_online_media_player
        )
        binding.lifecycleOwner = this@OnlineMediaPlayerActivity
        binding.clickHandle = this
        mContext = this
        getExtras()
    }

    private fun getExtras() {
        val extras = intent.extras
        extras.let {
            url = extras?.getString(AppConstants.IntentConstants.HLS_LINK_DATA)!!
        }
    }

    private fun initializePlayer() {
        val mediaDataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(this)

        /*val mediaSource= HlsMediaSource.Factory(mediaDataSourceFactory).createMediaSource(MediaItem.Builder()
            .setUri(url)
            .setMimeType(MimeTypes.APPLICATION_M3U8)
            .build())*/
        //val mediaSource= RtspMediaSource.Factory().setForceUseRtpTcp(true).createMediaSource(mediaItem)
        val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory)
            .createMediaSource(MediaItem.fromUri(url))
        val mediaSourceFactory: MediaSourceFactory =
            DefaultMediaSourceFactory(mediaDataSourceFactory)

        simpleExoPlayer = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
        simpleExoPlayer.addMediaSource(mediaSource)

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

    companion object {
        //const val STREAM_URL = "rtsp://rtsp.stream/pattern"
        const val STREAM_URL = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"
    }

    override fun clickHandle(v: View) {
        when (v.id) {
            R.id.back -> {
                onBackPressed()
            }

        }
    }


}