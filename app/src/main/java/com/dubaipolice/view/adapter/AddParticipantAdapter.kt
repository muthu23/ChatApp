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
import com.dubaipolice.callback.HandleUserItemClick
import com.dubaipolice.databinding.ItemAddParticipantsBinding
import com.dubaipolice.model.Users

internal class AddParticipantAdapter(
    private val mContext: Context,
    private val handleUserItemClick: HandleUserItemClick
) : PagingDataAdapter<Users, AddParticipantAdapter.MyViewHolder>(UserComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(
        ItemAddParticipantsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )


    override fun onBindViewHolder(viewHolder: MyViewHolder, position: Int) {
        val user: Users? = getItem(position)
        viewHolder.bind(user)
    }

    inner class MyViewHolder(val binding: ItemAddParticipantsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: Users?) {
            user?.let {
                binding.tvName.text = user.first_name.plus(" " + user.last_name)
                Glide.with(mContext).load(user.profile_image).placeholder(
                    AppCompatResources.getDrawable(mContext, R.drawable.ic_profile)
                ).into(binding.profileImage)
                binding.cbSelectedUser.isChecked = user.isSelected

                binding.cbSelectedUser.setOnCheckedChangeListener { _, isChecked ->
                    user.isSelected = isChecked
                    handleUserItemClick.itemClickHandle(user)
                }
                binding.layoutMain.setOnClickListener {
                    binding.cbSelectedUser.performClick()
                }
            }
        }
    }

    object UserComparator : DiffUtil.ItemCallback<Users>() {
        override fun areItemsTheSame(oldItem: Users, newItem: Users): Boolean {
            return oldItem.jid == newItem.jid
        }
        override fun areContentsTheSame(oldItem: Users, newItem: Users): Boolean {
            return oldItem == newItem
        }
    }
}