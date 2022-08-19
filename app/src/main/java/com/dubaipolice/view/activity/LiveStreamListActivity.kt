package com.dubaipolice.view.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dubaipolice.R
import com.dubaipolice.callback.HandleClick
import com.dubaipolice.callback.HandleLiveStreamItemClick
import com.dubaipolice.callback.HandleVideoStreamItemClick
import com.dubaipolice.databinding.ActivityLiveStreamListBinding
import com.dubaipolice.db.model.GroupInfoTable
import com.dubaipolice.model.HLSLink
import com.dubaipolice.model.Stream
import com.dubaipolice.model.VideoItem
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.MyProgressDialog
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.utils.Utils
import com.dubaipolice.utils.Utils.showSnack
import com.dubaipolice.view.adapter.CommonLoadStateAdapter
import com.dubaipolice.view.adapter.LiveStreamListAdapter
import com.dubaipolice.view.adapter.VideoStreamListAdapter
import com.dubaipolice.viewmodel.StreamListViewModel
import com.dubaipolice.wrapper.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LiveStreamListActivity : AppCompatActivity(),HandleClick, HandleLiveStreamItemClick {
    private lateinit var groupData: GroupInfoTable
    private lateinit var mContext: Context
    private lateinit var liveStreamViewModel: StreamListViewModel
    private lateinit var binding: ActivityLiveStreamListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLiveStreamListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        liveStreamViewModel = ViewModelProvider(this)[StreamListViewModel::class.java]

        binding.lifecycleOwner = this@LiveStreamListActivity
        binding.videoStreamListViewModel = liveStreamViewModel
        binding.clickHandle = this
        mContext = this
        getExtras()
    }

    private fun getExtras() {
        val extras = intent.extras
        extras.let {
            groupData =
                extras?.getSerializable(AppConstants.IntentConstants.GROUP_DATA) as GroupInfoTable
            liveStreamViewModel.groupId.value = groupData.groupId.toString()
            if(Utils.isNetConnected(mContext))
                initRecyclerView()
            else binding.parentLayout.showSnack(mContext, getString(R.string.no_internet))
        }
    }

    private fun initRecyclerView() {
        val streamListAdapter = LiveStreamListAdapter(mContext, this)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
            adapter = streamListAdapter.withLoadStateFooter(
                footer = CommonLoadStateAdapter{streamListAdapter.retry()}
            )
        }
        lifecycleScope.launch(Dispatchers.IO) {
            liveStreamViewModel.getLiveStreamFlow().collectLatest {
                streamListAdapter.submitData(it)
            }
        }

    }

    override fun clickHandle(v: View) {
        when (v.id) {
            R.id.back -> {
                onBackPressed()
            }
        }
    }

    override fun itemClickHandle(stream: Stream?) {
        stream?.let {
            val streamLink = HLSLink(
               it.stream_key,
                0,
                false
            )
            moveToVideoStream(streamLink)
        }
    }

    private fun convertRtsToHLS(media: VideoItem) {
        liveStreamViewModel.getHLSLink(media).observe(this){response ->
            when (response.status) {
                Resource.Status.SUCCESS -> {
                    if (response.data!!.success) {
                        MyProgressDialog.dismiss()
                        moveToVideoStream(response.data.data)
                    }
                    Utils.showToast(this, response.data.message)
                }
                Resource.Status.ERROR -> {
                    MyProgressDialog.dismiss()
                    Utils.showToast(mContext, response.message!!)
                }
                Resource.Status.LOADING -> {

                }
            }
        }
    }

    private fun moveToVideoStream(hlsLink: HLSLink) {
        val i = Intent(mContext, VideoStreamActivity::class.java)
        i.putExtra(AppConstants.IntentConstants.HLS_LINK_DATA, hlsLink)
        startActivity(i)
    }
}