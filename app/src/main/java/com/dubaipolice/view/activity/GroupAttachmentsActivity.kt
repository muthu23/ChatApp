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
import com.dubaipolice.db.model.GroupInfoTable
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.Utils
import com.dubaipolice.utils.Utils.showSnack
import com.dubaipolice.view.adapter.ViewPagerAdapter
import com.dubaipolice.view.fragment.DocsFragment
import com.dubaipolice.view.fragment.MediaFragment
import com.dubaipolice.view.fragment.VoiceNoteFragment
import com.dubaipolice.viewmodel.GroupAttachmentsViewModel
import com.google.android.material.tabs.TabLayoutMediator

class GroupAttachmentsActivity : AppCompatActivity(), HandleClick {

    private lateinit var mediaAndDocsViewModel: GroupAttachmentsViewModel
    private lateinit var binding: ActivityGroupAttachmentsBinding
    lateinit var mContext: Context

    private var fragmentList = mutableListOf<Fragment>()
    private var titleList = mutableListOf<String>()

    private var groupData: GroupInfoTable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGroupAttachmentsBinding.inflate(layoutInflater)
        mediaAndDocsViewModel = ViewModelProvider(this)[GroupAttachmentsViewModel::class.java]

        binding.lifecycleOwner = this@GroupAttachmentsActivity
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
        val fragmentMedia = MediaFragment()
        val fragmentDocs = DocsFragment()
        val fragmentVoice = VoiceNoteFragment()

        fragmentList.add(fragmentMedia)
        fragmentList.add(fragmentDocs)
        fragmentList.add(fragmentVoice)

        binding.tabLayout.addTab(
            binding.tabLayout.newTab().setText(resources.getText(R.string.media))
        )
        binding.tabLayout.addTab(
            binding.tabLayout.newTab().setText(resources.getText(R.string.docs))
        )
        binding.tabLayout.addTab(
            binding.tabLayout.newTab().setText(resources.getText(R.string.voice_note))
        )

        titleList.add(resources.getText(R.string.media).toString())
        titleList.add(resources.getText(R.string.docs).toString())
        titleList.add(resources.getText(R.string.voice_note).toString())

        val adapter = ViewPagerAdapter(
            fragmentList, supportFragmentManager,
            lifecycle
        )
        binding.viewPager.offscreenPageLimit = 3
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = titleList[position]
        }.attach()
    }

    private fun getExtras() {
        val extras = intent.extras
        extras?.let {
            groupData =
                it.getSerializable(AppConstants.IntentConstants.GROUP_DATA) as GroupInfoTable
            mediaAndDocsViewModel.groupId.value = groupData?.groupId.toString()
            binding.grpName.text = groupData?.groupName.toString()
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