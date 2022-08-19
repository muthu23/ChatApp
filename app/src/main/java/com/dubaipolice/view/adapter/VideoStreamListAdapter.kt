package com.dubaipolice.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dubaipolice.R
import com.dubaipolice.callback.HandleVideoStreamItemClick
import com.dubaipolice.databinding.ItemVideoStreamBinding
import com.dubaipolice.model.VideoItem

internal class VideoStreamListAdapter(
    private val context: Context,
    private val onVideoClickListener: HandleVideoStreamItemClick
) : PagingDataAdapter<VideoItem,VideoStreamListAdapter.MyViewHolder>(VideoStreamItemComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(
        ItemVideoStreamBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )
    override fun onBindViewHolder(viewHolder: MyViewHolder, position: Int) {
        val videoStream: VideoItem? = getItem(position)
        viewHolder.bind(videoStream)
    }

    inner class MyViewHolder(val binding:  ItemVideoStreamBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(videoStream: VideoItem?) {
            videoStream?.let {
                binding.tvUserName.text = videoStream.name
                binding.tvStartedAt.text = videoStream.created_at
                binding.tvDuration.text = videoStream.duration
                Glide.with(context).load(videoStream.icon).placeholder(
                    AppCompatResources.getDrawable(context, R.drawable.ic_profile)).into(binding.videoImage)
            }
            binding.layoutMain.setOnClickListener{
                onVideoClickListener.itemClickHandle(videoStream)
            }
        }
    }

    object VideoStreamItemComparator : DiffUtil.ItemCallback<VideoItem>() {
        override fun areItemsTheSame(oldItem: VideoItem, newItem: VideoItem): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: VideoItem, newItem: VideoItem): Boolean {
            return oldItem == newItem
        }
    }

}