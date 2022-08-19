package com.dubaipolice.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dubaipolice.R
import com.dubaipolice.callback.HandleClick
import com.dubaipolice.callback.HandleVideoStreamItemClick
import com.dubaipolice.databinding.ActivityCameraStreamListBinding
import com.dubaipolice.db.model.GroupInfoTable
import com.dubaipolice.model.HLSLink
import com.dubaipolice.model.VideoItem
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.MyProgressDialog
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.utils.Utils
import com.dubaipolice.utils.Utils.showSnack
import com.dubaipolice.view.adapter.CommonLoadStateAdapter
import com.dubaipolice.view.adapter.VideoStreamListAdapter
import com.dubaipolice.viewmodel.StreamListViewModel
import com.dubaipolice.wrapper.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CameraStreamListActivity : AppCompatActivity(), HandleClick, HandleVideoStreamItemClick {

    private lateinit var cameraListViewModel: StreamListViewModel
    lateinit var binding: ActivityCameraStreamListBinding
    private var groupData: GroupInfoTable? = null

    lateinit var mContext: Context

    private var videoStreamListAdapter: VideoStreamListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCameraStreamListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cameraListViewModel = ViewModelProvider(this)[StreamListViewModel::class.java]

        binding.lifecycleOwner = this@CameraStreamListActivity
        binding.cameraListViewModel = cameraListViewModel
        binding.clickHandle = this
        mContext = this
        getExtras()

    }

    private fun getExtras() {
        val extras = intent.extras
        extras.let {
            groupData =
                extras?.getSerializable(AppConstants.IntentConstants.GROUP_DATA) as GroupInfoTable
            cameraListViewModel.groupId.value = groupData?.groupId.toString()
            cameraListViewModel.organizationId.value =
                SharedPref.readInt(AppConstants.KEY_ORGANIZATION_ID).toString()
            if(Utils.isNetConnected(mContext))
            initRecyclerView()
            else binding.parentLayout.showSnack(mContext, getString(R.string.no_internet))
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

    override fun clickHandle(v: View) {
        when (v.id) {
            R.id.back -> {
                onBackPressed()
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