package com.dubaipolice.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dubaipolice.callback.HandleLiveStreamItemClick
import com.dubaipolice.callback.HandleMediaItemClick
import com.dubaipolice.databinding.FragmentDocsBinding
import com.dubaipolice.databinding.FragmentLiveStreamListBinding
import com.dubaipolice.db.model.GroupInfoTable
import com.dubaipolice.model.Attachment
import com.dubaipolice.model.HLSLink
import com.dubaipolice.model.Stream
import com.dubaipolice.model.VideoItem
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.MyProgressDialog
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.utils.Utils
import com.dubaipolice.view.activity.VideoStreamActivity
import com.dubaipolice.view.adapter.CommonLoadStateAdapter
import com.dubaipolice.view.adapter.GroupAttachmentAdapter
import com.dubaipolice.view.adapter.LiveStreamListAdapter
import com.dubaipolice.viewmodel.GroupAttachmentsViewModel
import com.dubaipolice.viewmodel.StreamListViewModel
import com.dubaipolice.wrapper.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LiveStreamListFragment : Fragment() , HandleLiveStreamItemClick {
    private lateinit var groupData: GroupInfoTable
    private lateinit var binding: FragmentLiveStreamListBinding
    private lateinit var liveStreamViewModel: StreamListViewModel
    private lateinit var mContext: Context

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLiveStreamListBinding.inflate(layoutInflater)
        liveStreamViewModel = ViewModelProvider(requireActivity())[StreamListViewModel::class.java]
        mContext = binding.root.context
        setData()
        //setupRecyclerView()
        return binding.root
    }
    fun setGroupInfoTable(detail: GroupInfoTable?){
        detail?.let {
            groupData = it
        }
    }
    private fun setData() {
        if (this::groupData.isInitialized) {
            liveStreamViewModel.groupId.value = groupData.groupId.toString()
            if(Utils.isNetConnected(mContext))
                initRecyclerView()
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
    private fun convertRtsToHLS(media: VideoItem) {
        liveStreamViewModel.getHLSLink(media).observe(this){
                response ->
            when (response.status) {
                Resource.Status.SUCCESS -> {
                    if (response.data!!.success) {
                        MyProgressDialog.dismiss()
                        moveToVideoStream(response.data.data)
                    }
                    Utils.showToast(requireContext(), response.data.message)
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