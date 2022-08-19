package com.dubaipolice.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.dubaipolice.R
import com.dubaipolice.callback.HandleMediaItemClick
import com.dubaipolice.databinding.FragmentMediaBinding
import com.dubaipolice.model.Attachment
import com.dubaipolice.utils.Utils
import com.dubaipolice.utils.Utils.showSnack
import com.dubaipolice.view.adapter.CommonLoadStateAdapter
import com.dubaipolice.view.adapter.GroupAttachmentAdapter
import com.dubaipolice.viewmodel.GroupAttachmentsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MediaFragment : Fragment(), HandleMediaItemClick {

    private lateinit var adapter: GroupAttachmentAdapter
    private lateinit var binding: FragmentMediaBinding
    private lateinit var mContext: Context
    private lateinit var mediaViewModel: GroupAttachmentsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMediaBinding.inflate(layoutInflater)
        mContext = binding.root.context
        mediaViewModel =
            ViewModelProvider(requireActivity())[GroupAttachmentsViewModel::class.java]
        mediaViewModel.attachmentType = "media"
        setupRecyclerView()
        return binding.root
    }

    private fun setupRecyclerView() {
        val mediaAdapter = GroupAttachmentAdapter(mContext, this)
        binding.rvMedia.apply {
            layoutManager = GridLayoutManager(mContext, 4)
            adapter = mediaAdapter.withLoadStateHeaderAndFooter(
                header = CommonLoadStateAdapter { mediaAdapter.retry() },
                footer = CommonLoadStateAdapter { mediaAdapter.retry() }
            )
        }
        lifecycleScope.launch {
            mediaViewModel.getMediaFlow().collectLatest {
                mediaAdapter.submitData(it)
            }
        }

    }

    override fun itemClickHandle(media: Attachment) {

    }

}