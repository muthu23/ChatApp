package com.dubaipolice.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dubaipolice.R
import com.dubaipolice.callback.HandleLiveStreamItemClick
import com.dubaipolice.databinding.ItemVideoStreamBinding
import com.dubaipolice.model.Stream
import com.dubaipolice.utils.DateUtils
import java.util.concurrent.TimeUnit


internal class LiveStreamListAdapter(
    private val context: Context,
    private val onVideoClickListener: HandleLiveStreamItemClick
) : PagingDataAdapter<Stream,LiveStreamListAdapter.MyViewHolder>(LiveStreamItemComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(
        ItemVideoStreamBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )
    override fun onBindViewHolder(viewHolder: MyViewHolder, position: Int) {
        val liveStream: Stream? = getItem(position)
        viewHolder.bind(liveStream)
    }

    inner class MyViewHolder(val binding:  ItemVideoStreamBinding) : RecyclerView.ViewHolder(binding.root){
        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(liveStream: Stream?) {
            liveStream?.let {
                Glide.with(context).load(liveStream.profile_image).placeholder(
                    AppCompatResources.getDrawable(context, R.drawable.ic_profile)).into(binding.videoImage)

                binding.tvUserName.text= liveStream.first_name + " " + liveStream.last_name
                //2022-04-08T11:25:01.000Z - convert this to timestamp
                binding.tvStartedAt.text= "Started at" + " " + DateUtils.getTime12FromTimestamp(DateUtils.getTimestampFromDateTimeString(liveStream.createdAt).toString())  //Started at 11:40 PM

                // Calculate time difference in milliseconds
                val differenceInTime: Long = System.currentTimeMillis() - DateUtils.getTimestampFromDateTimeString(liveStream.createdAt)

                // Calucalte time difference in seconds,
                // minutes, hours, years, and days
                val differenceInSeconds: Long = (TimeUnit.MILLISECONDS
                    .toSeconds(differenceInTime)
                        % 60)

                val differenceInMinutes: Long = (TimeUnit.MILLISECONDS
                    .toMinutes(differenceInTime)
                        % 60)

                val differenceInHours: Long = (TimeUnit.MILLISECONDS
                    .toHours(differenceInTime)
                        % 24)

                val differenceInDays: Long = (TimeUnit.MILLISECONDS
                    .toDays(differenceInTime)
                        % 365)

                val differenceInYears: Long = (TimeUnit.MILLISECONDS
                    .toDays(differenceInTime)
                        / 365L)

                //binding.tvDuration.text= "00:15:25"  //currenttime - liveStream.createdAt
                binding.tvDuration.text=
                    "$differenceInHours:$differenceInMinutes:$differenceInSeconds"

            }
            binding.layoutMain.setOnClickListener{
                onVideoClickListener.itemClickHandle(liveStream)
            }
        }
    }

    object LiveStreamItemComparator : DiffUtil.ItemCallback<Stream>() {
        override fun areItemsTheSame(oldItem: Stream, newItem: Stream): Boolean {
            return oldItem.user_id == newItem.user_id
        }
        override fun areContentsTheSame(oldItem: Stream, newItem: Stream): Boolean {
            return oldItem == newItem
        }
    }

}