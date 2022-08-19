package com.dubaipolice.view.adapter

import android.content.Context
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
import com.dubaipolice.db.model.MemberInfoTable

internal class GroupParticipantAdapter(
    context: Context,
    groupChatParticipantsList: MutableList<MemberInfoTable>?
) :
    RecyclerView.Adapter<GroupParticipantAdapter.MyViewHolder>() {

    var mContext: Context? = null
    var groupChatParticipantsList: MutableList<MemberInfoTable> = ArrayList()

    init {
        this.mContext = context
        this.groupChatParticipantsList = groupChatParticipantsList!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_group_participants, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: MyViewHolder, position: Int) {
        val member: MemberInfoTable = groupChatParticipantsList[position]
        if (position == 0)
            viewHolder.tvUserName.text = mContext!!.resources.getString(R.string.you)
        else
            viewHolder.tvUserName.text = member.firstName.plus(" " + member.lastName)

        member.isOnline?.let {
            viewHolder.imgOnlineIndicator.visibility =
                if (it) View.VISIBLE else View.GONE
        }

        if(member.roleId == 2)
        {
            viewHolder.tvRoleName.visibility= View.VISIBLE
        }
        else{
            viewHolder.tvRoleName.visibility= View.GONE
        }

        Glide.with(mContext!!).load(member.imageUrl).placeholder(
            AppCompatResources.getDrawable(mContext!!, R.drawable.ic_profile)
        ).into(viewHolder.imgProfile)

    }

    override fun getItemCount(): Int {
        return groupChatParticipantsList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun removeAt(position: Int) {
        groupChatParticipantsList.removeAt(position)
        notifyItemRemoved(position)
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        var tvUserName: TextView = itemView.findViewById(R.id.tvName)
        var tvRoleName: TextView = itemView.findViewById(R.id.tvRoleName)
        var imgProfile: ImageView = itemView.findViewById(R.id.profileImage)
        var imgOnlineIndicator: ImageView = itemView.findViewById(R.id.imgOnlineIndicator)
        var layoutMain: LinearLayout = itemView.findViewById(R.id.layoutMain)

        init {
            // layoutMain.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            /*when (v.id) {
                R.id.layoutMain -> {
                    mContext!!.startActivity(Intent(mContext, ChatActivity::class.java))
                }
            }*/
        }
    }

}