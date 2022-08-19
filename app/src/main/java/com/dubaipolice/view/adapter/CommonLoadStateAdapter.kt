package com.dubaipolice.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dubaipolice.databinding.LoadStateViewBinding
import com.dubaipolice.utils.visible

class CommonLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<CommonLoadStateAdapter.LoadStateViewHolder>() {

    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) { holder.bind(loadState) }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState) =  LoadStateViewHolder(
        LoadStateViewBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        retry
    )

    class LoadStateViewHolder(
        val binding: LoadStateViewBinding,
        private val retry: () -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(loadState: LoadState) {
            if (loadState is LoadState.Error) {
                binding.textViewError.text = loadState.error.localizedMessage
            }
            binding.progressbar.visible(loadState is LoadState.Loading)
            binding.tvRetry.visible(loadState is LoadState.Error)
            binding.textViewError.visible(loadState is LoadState.Error)

            binding.tvRetry.setOnClickListener { retry() }
        }
    }
}