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
import com.dubaipolice.callback.HandleNotificationListItemClick
import com.dubaipolice.databinding.ItemNotificationBinding
import com.dubaipolice.databinding.ItemVideoStreamBinding
import com.dubaipolice.model.Notifications
import com.dubaipolice.model.Stream
import com.dubaipolice.utils.DateUtils
import java.util.concurrent.TimeUnit


internal class NotificationListAdapter(
    private val context: Context,
    private val onItemClickListener: HandleNotificationListItemClick
) : PagingDataAdapter<Notifications,NotificationListAdapter.MyViewHolder>(NotificationItemComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(
        ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )
    override fun onBindViewHolder(viewHolder: MyViewHolder, position: Int) {
        val notifications: Notifications? = getItem(position)
        viewHolder.bind(notifications)
    }

    inner class MyViewHolder(val binding:  ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root){
        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(notifications: Notifications?) {
            notifications?.let {
                binding.tvMessageTitle.text= notifications.title
                binding.tvMessage.text= notifications.body
                binding.tvDate.text= notifications.updated_at
                onItemClickListener.itemClickHandle(true,notifications)

                if(notifications.is_read) binding.layoutMain.setBackgroundColor(context.resources.getColor(R.color.white)) else binding.layoutMain.setBackgroundColor(context.resources.getColor(R.color.amp_light_gray))
            }
            binding.layoutMain.setOnClickListener{
                var localNotificationVar= notifications
                onItemClickListener.itemClickHandle(false, localNotificationVar)
                notifications?.is_read= true
                notifyItemChanged(bindingAdapterPosition)
            }
        }
    }

    object NotificationItemComparator : DiffUtil.ItemCallback<Notifications>() {
        override fun areItemsTheSame(oldItem: Notifications, newItem: Notifications): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Notifications, newItem: Notifications): Boolean {
            return oldItem == newItem
        }
    }

}