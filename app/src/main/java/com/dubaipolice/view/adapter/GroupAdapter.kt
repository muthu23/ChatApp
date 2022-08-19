package com.dubaipolice.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dubaipolice.R
import com.dubaipolice.db.model.GroupInfoTable
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.DateUtils
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.view.activity.ChatActivity
import java.util.*

@SuppressLint("NotifyDataSetChanged")
internal class GroupAdapter(
    private val mContext: Context,
    groupList: ArrayList<GroupInfoTable>
) :
    RecyclerView.Adapter<GroupAdapter.MyViewHolder>() {

    var groupList: ArrayList<GroupInfoTable> = ArrayList()
    private var groupCompleteList: ArrayList<GroupInfoTable> = ArrayList()

    init {
        this.groupList = groupList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: MyViewHolder, position: Int) {
        val group: GroupInfoTable = groupList[position]
        viewHolder.tvGroupName.text = group.groupName

        if (group.unreadCount != null && group.unreadCount != 0) {
            viewHolder.tvUnreadChatCount.text = group.unreadCount.toString()
            viewHolder.tvUnreadChatCount.visibility = View.VISIBLE
        } else {
            viewHolder.tvUnreadChatCount.visibility = View.GONE
        }

        if(group.callActive == "true")
        {
            viewHolder.imgAudioCall.setImageResource(R.drawable.ic_chat_audio_small_green)
        }
        else
        {
            viewHolder.imgAudioCall.setImageResource(R.drawable.ic_chat_audio_small_grey)
        }

        if (group.senderJid == SharedPref.readString(AppConstants.KEY_XMPP_USER_JID)) {
            viewHolder.tvLastMessage.text = group.lastMessage
            viewHolder.tvSenderName.visibility = View.GONE

            if (group.sentAt != null) {
                viewHolder.imgSeenStatus.setImageResource(R.drawable.ic_chat_sent_green)
                viewHolder.imgSeenStatus.visibility = View.VISIBLE
            }

            if (group.deliveredAt != null) {
                viewHolder.imgSeenStatus.setImageResource(R.drawable.ic_chat_delivered_green)
                viewHolder.imgSeenStatus.visibility = View.VISIBLE
            }

            if (group.displayedAt != null) {
                viewHolder.imgSeenStatus.setImageResource(R.drawable.ic_chat_read_blue)
                viewHolder.imgSeenStatus.visibility = View.VISIBLE
            }

            if (group.sentAt == null && group.deliveredAt == null && group.displayedAt == null) {
                viewHolder.imgSeenStatus.setImageResource(R.drawable.ic_chat_pending_green)
                viewHolder.imgSeenStatus.visibility = View.GONE
            }

        } else {
            if(group.lastSenderName.isNotEmpty() && group.lastMessage != null && group.lastMessage!!.isNotEmpty())
            viewHolder.tvSenderName.visibility = View.VISIBLE
            viewHolder.tvSenderName.text = group.lastSenderName.plus(": ")
            viewHolder.tvLastMessage.text = group.lastMessage
            viewHolder.imgSeenStatus.visibility = View.GONE
        }

        when (group.type) {
            AppConstants.TYPE_IMAGE_CHAT -> {
                viewHolder.imgMedia.setImageResource(R.drawable.ic_chat_camera)
                viewHolder.imgMedia.visibility = View.VISIBLE
                viewHolder.tvLastMessage.text = mContext.resources.getString(R.string.photo)
            }
            AppConstants.TYPE_VIDEO_CHAT -> {
                viewHolder.imgMedia.setImageResource(R.drawable.ic_chat_video_small_green)
                viewHolder.imgMedia.visibility = View.VISIBLE
                viewHolder.tvLastMessage.text = mContext.resources.getString(R.string.video)
            }
            AppConstants.TYPE_AUDIO_CHAT -> {
                viewHolder.imgMedia.setImageResource(R.drawable.ic_chat_voice)
                viewHolder.imgMedia.visibility = View.VISIBLE
                viewHolder.tvLastMessage.text = mContext.resources.getString(R.string.audio)
            }
            AppConstants.TYPE_DOCUMENT_CHAT -> {
                viewHolder.imgMedia.setImageResource(R.drawable.ic_chat_document_green)
                viewHolder.imgMedia.visibility = View.VISIBLE
                viewHolder.tvLastMessage.text = mContext.resources.getString(R.string.document)
            }
            AppConstants.TYPE_LOCATION_CHAT -> {
                viewHolder.imgMedia.setImageResource(R.drawable.ic_chat_location_green)
                viewHolder.imgMedia.visibility = View.VISIBLE
                viewHolder.tvLastMessage.text = mContext.resources.getString(R.string.location)
            }
            AppConstants.TYPE_CONTACT_CHAT -> {
                viewHolder.imgMedia.setImageResource(R.drawable.ic_chat_contact_green)
                viewHolder.imgMedia.visibility = View.VISIBLE
                viewHolder.tvLastMessage.text = mContext.resources.getString(R.string.contact)
            }
            else -> {
                viewHolder.imgMedia.visibility = View.GONE
            }
        }


        if (group.unixTimestamp != null && !TextUtils.isEmpty(group.unixTimestamp)) {
            if (DateUtils.getChatDateFromTimestamp(group.unixTimestamp!!) == DateUtils.getChatDateFromTimestamp(
                    System.currentTimeMillis().toString()
                )
            ) {
                //show time if today date
                viewHolder.tvDate.text = DateUtils.getChatTimeFromTimestamp(group.unixTimestamp!!)
            } else {
                //else show date
                viewHolder.tvDate.text =
                    DateUtils.getChatDateFromTimestampTwoDigit(group.unixTimestamp!!)
            }
        }

        if (group.alertLevel != null) {
            viewHolder.imgAlertStatus.visibility = View.VISIBLE
            when (group.alertLevel) {
                "1" //grey
                -> {
                    viewHolder.imgAlertStatus.setImageResource(R.drawable.ic_alert_grey)
                }
                "2" //yellow
                -> {
                    viewHolder.imgAlertStatus.setImageResource(R.drawable.ic_alert_yellow)
                }
                "3" //red
                -> {
                    viewHolder.imgAlertStatus.setImageResource(R.drawable.ic_alert_red)
                }
                else -> {
                    viewHolder.imgAlertStatus.visibility = View.GONE
                }
            }
        } else {
            viewHolder.imgAlertStatus.visibility = View.GONE
        }

        //holder.tvDate.text= group.updatedTimestamp

        Glide.with(mContext).load(group.imageUrl).placeholder(
            AppCompatResources.getDrawable(mContext, R.drawable.ic_profile)
        ).into(viewHolder.imgProfile)

    }

    fun setGroupList(groupInfoTable: List<GroupInfoTable>?) {
        //this.groupList = groupInfoTable
        if (groupInfoTable != null) {
            this.groupList.clear()
            this.groupCompleteList.clear()
            this.groupList.addAll(groupInfoTable.toMutableList())
            this.groupCompleteList.addAll(groupInfoTable.toMutableList())
            notifyDataSetChanged()
        }
    }

    fun filter(query: String) {
        if (groupCompleteList.size == 0) {
            groupCompleteList.addAll(groupList)
        }
        groupList.clear()
        if (query.isEmpty()) {
            groupList.addAll(groupCompleteList)
        } else {
            for (wp in groupCompleteList) {
                val name = wp.groupName
                if (name!!.lowercase(Locale.getDefault()).contains(query)) {
                    groupList.add(wp)
                }
            }
        }
        Log.e("Changed", "Changed")
        notifyDataSetChanged()

    }

    override fun getItemCount(): Int {
        return groupList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        var tvGroupName: TextView = itemView.findViewById(R.id.tvGroupName)
        var tvLastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        var tvSenderName: TextView = itemView.findViewById(R.id.tv_sender_name)
        var tvDate: TextView = itemView.findViewById(R.id.tvDate)
        var imgProfile: ImageView = itemView.findViewById(R.id.profileImage)
        var imgAlertStatus: ImageView = itemView.findViewById(R.id.imgAlertStatus)
        var imgSeenStatus: ImageView = itemView.findViewById(R.id.imgSeenStatus)
        var imgMedia: ImageView = itemView.findViewById(R.id.img_media)
        var imgAudioCall: ImageView = itemView.findViewById(R.id.imgAudioCall)
        var imgVideoCall: ImageView = itemView.findViewById(R.id.imgVideoCall)
        var tvUnreadChatCount: TextView = itemView.findViewById(R.id.tvUnreadChatCount)
        var layoutMain: LinearLayout = itemView.findViewById(R.id.layoutMain)

        override fun onClick(v: View) {
            when (v.id) {
                R.id.layoutMain -> {
                    val i = Intent(mContext, ChatActivity::class.java)
                    i.putExtra(
                        AppConstants.IntentConstants.GROUP_DATA,
                        groupList[bindingAdapterPosition]
                    )
                    mContext.startActivity(i)
                }
            }
        }

        init {
            layoutMain.setOnClickListener(this)
        }
    }

}