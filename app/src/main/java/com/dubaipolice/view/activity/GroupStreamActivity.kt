package com.dubaipolice.view.activity

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dubaipolice.R
import com.dubaipolice.callback.HandleClick
import com.dubaipolice.databinding.ActivityGroupAttachmentsBinding
import com.dubaipolice.databinding.ActivityGroupStreamBinding
import com.dubaipolice.db.model.GroupInfoTable
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.utils.Utils
import com.dubaipolice.utils.Utils.showSnack
import com.dubaipolice.view.adapter.ViewPagerAdapter
import com.dubaipolice.view.fragment.*
import com.dubaipolice.viewmodel.GroupAttachmentsViewModel
import com.google.android.material.tabs.TabLayoutMediator

class GroupStreamActivity : AppCompatActivity(), HandleClick {

    private lateinit var mediaAndDocsViewModel: GroupAttachmentsViewModel
    private lateinit var binding: ActivityGroupStreamBinding
    lateinit var mContext: Context

    private var fragmentList = mutableListOf<Fragment>()
    private var titleList = mutableListOf<String>()

    private var groupData: GroupInfoTable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGroupStreamBinding.inflate(layoutInflater)
        mediaAndDocsViewModel = ViewModelProvider(this)[GroupAttachmentsViewModel::class.java]

        binding.lifecycleOwner = this@GroupStreamActivity
        binding.mediaAndDocsViewModel = mediaAndDocsViewModel
        binding.clickHandle = this
        mContext = this
        setContentView(binding.root)

        getExtras()
        setupViewPager()
        if(!Utils.isNetConnected(mContext))
         binding.parentLayout.showSnack(mContext, getString(R.string.no_internet))
    }

    private fun setupViewPager() {
        val cameraStreamListFragment = CameraStreamListFragment()
        cameraStreamListFragment.setGroupInfoTable(groupData)
        val liveStreamListFragment = LiveStreamListFragment()
        liveStreamListFragment.setGroupInfoTable(groupData)
        fragmentList.add(liveStreamListFragment)
        fragmentList.add(cameraStreamListFragment)

        binding.tabLayout.addTab(
            binding.tabLayout.newTab().setText(resources.getText(R.string.live_streams))
        )
        binding.tabLayout.addTab(
            binding.tabLayout.newTab().setText(resources.getText(R.string.ip_camera_streams))
        )


        titleList.add(resources.getText(R.string.live_streams).toString())
        titleList.add(resources.getText(R.string.ip_camera_streams).toString())

        val adapter = ViewPagerAdapter(
            fragmentList, supportFragmentManager,
            lifecycle
        )
        binding.viewPager.offscreenPageLimit = 2
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = titleList[position]
        }.attach()
    }

    private fun getExtras() {
        val extras = intent.extras
        extras.let {
            groupData = extras?.getSerializable(AppConstants.IntentConstants.GROUP_DATA) as GroupInfoTable
            binding.grpName.text = groupData!!.groupName

        }
    }
    override fun clickHandle(v: View) {
        when (v.id) {
            R.id.back -> {
                onBackPressed()
            }
        }

    }
}