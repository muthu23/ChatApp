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
import com.dubaipolice.callback.HandleMediaItemClick
import com.dubaipolice.callback.HandleVideoStreamItemClick
import com.dubaipolice.databinding.FragmentCameraStreamListBinding
import com.dubaipolice.db.model.GroupInfoTable
import com.dubaipolice.model.HLSLink
import com.dubaipolice.model.VideoItem
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.MyProgressDialog
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.utils.Utils
import com.dubaipolice.view.activity.VideoStreamActivity
import com.dubaipolice.view.adapter.CommonLoadStateAdapter
import com.dubaipolice.view.adapter.VideoStreamListAdapter
import com.dubaipolice.viewmodel.StreamListViewModel
import com.dubaipolice.wrapper.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CameraStreamListFragment : Fragment() , HandleVideoStreamItemClick {

    private lateinit var binding: FragmentCameraStreamListBinding
    private lateinit var mContext: Context
    private lateinit var cameraListViewModel: StreamListViewModel
    private lateinit var groupData: GroupInfoTable

   // private lateinit var groupData: GroupInfoTable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCameraStreamListBinding.inflate(layoutInflater)
        cameraListViewModel = ViewModelProvider(requireActivity())[StreamListViewModel::class.java]
        mContext = binding.root.context

        //setupRecyclerView()
        setData()
        return binding.root
    }

    fun setGroupInfoTable(detail: GroupInfoTable?){
        detail?.let {
            groupData = it
        }
    }
    private fun setData() {
        if (this::groupData.isInitialized) {
            cameraListViewModel.groupId.value = groupData.groupId.toString()
            cameraListViewModel.organizationId.value =
                SharedPref.readInt(AppConstants.KEY_ORGANIZATION_ID).toString()
            if(Utils.isNetConnected(mContext))
                initRecyclerView()
        }
    }

    private fun initRecyclerView() {
        val videoStreamListAdapter = VideoStreamListAdapter(mContext, this)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
            adapter = videoStreamListAdapter.withLoadStateFooter(
                footer = CommonLoadStateAdapter{videoStreamListAdapter.retry()}
            )
        }
        lifecycleScope.launch(Dispatchers.IO) {
            cameraListViewModel.getVideoStreamFlow().collectLatest {
                videoStreamListAdapter.submitData(it)
            }
        }

    }
    override fun itemClickHandle(media: VideoItem?) {
        media?.let {
            MyProgressDialog.show(mContext)
            convertRtsToHLS(media)
        }
    }
    private fun convertRtsToHLS(media: VideoItem) {
        cameraListViewModel.getHLSLink(media).observe(this){response ->
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