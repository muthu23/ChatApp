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
import com.dubaipolice.databinding.FragmentDocsBinding
import com.dubaipolice.model.Attachment
import com.dubaipolice.view.adapter.CommonLoadStateAdapter
import com.dubaipolice.view.adapter.GroupAttachmentAdapter
import com.dubaipolice.viewmodel.GroupAttachmentsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DocsFragment : Fragment(), HandleMediaItemClick {

    private lateinit var adapter: GroupAttachmentAdapter
    private lateinit var docsViewModel: GroupAttachmentsViewModel
    private lateinit var binding: FragmentDocsBinding
    private lateinit var mContext: Context

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDocsBinding.inflate(layoutInflater)
        docsViewModel =
            ViewModelProvider(requireActivity())[GroupAttachmentsViewModel::class.java]
        docsViewModel.attachmentType = "docs"
        mContext = binding.root.context

        setupRecyclerView()
        return binding.root
    }


    private fun setupRecyclerView() {
        val docsAdapter = GroupAttachmentAdapter(mContext, this)
        binding.rvDocs.apply {
            layoutManager = LinearLayoutManager(mContext)
            adapter = docsAdapter.withLoadStateFooter(
                footer = CommonLoadStateAdapter { docsAdapter.retry() }
            )
        }
        lifecycleScope.launch {
            docsViewModel.getDocsFlow().collectLatest {
                docsAdapter.submitData(it)
            }
        }
    }

    override fun itemClickHandle(media: Attachment) {

    }

}