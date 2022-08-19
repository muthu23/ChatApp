package com.dubaipolice.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dubaipolice.callback.HandleMediaItemClick
import com.dubaipolice.databinding.FragmentVoiceNoteBinding
import com.dubaipolice.model.Attachment
import com.dubaipolice.view.adapter.CommonLoadStateAdapter
import com.dubaipolice.view.adapter.GroupAttachmentAdapter
import com.dubaipolice.viewmodel.GroupAttachmentsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class VoiceNoteFragment : Fragment(), HandleMediaItemClick {

    private lateinit var adapter: GroupAttachmentAdapter
    private lateinit var binding: FragmentVoiceNoteBinding
    private lateinit var voiceViewModel: GroupAttachmentsViewModel

    private lateinit var mContext: Context
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVoiceNoteBinding.inflate(layoutInflater)
        voiceViewModel =
            ViewModelProvider(requireActivity())[GroupAttachmentsViewModel::class.java]
        voiceViewModel.attachmentType = "audio"
        mContext = binding.root.context
        setupRecyclerView()
        return binding.root
    }

    private fun setupRecyclerView() {
        val voiceAdapter = GroupAttachmentAdapter(mContext, this)
        binding.rvVoice.apply {
            layoutManager = LinearLayoutManager(mContext)
            adapter = voiceAdapter.withLoadStateFooter(
                footer = CommonLoadStateAdapter { voiceAdapter.retry() }
            )
        }
        lifecycleScope.launch {
            voiceViewModel.getDocsFlow().collectLatest {
                voiceAdapter.submitData(it)
            }
        }
    }

    override fun itemClickHandle(media: Attachment) {
    }

}