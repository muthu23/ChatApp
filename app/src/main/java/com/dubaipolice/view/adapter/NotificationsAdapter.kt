package com.dubaipolice.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dubaipolice.R
import com.dubaipolice.model.GroupChat
import com.dubaipolice.model.Notifications


internal class NotificationsAdapter(
    context: Context,
    notificationList: ArrayList<Notifications>
) :
    RecyclerView.Adapter<NotificationsAdapter.MyViewHolder>() {

    var mContext: Context? = null
    var notificationList: ArrayList<Notifications> = ArrayList<Notifications>()

    init {
        this.mContext= context
        this.notificationList = notificationList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: MyViewHolder, position: Int) {
        val notification: Notifications = notificationList[position]
        val holder = viewHolder
        holder.tvMessageTitle.text= notification.title
        holder.tvMessage.text= notification.body
        holder.tvDate.text= notification.updated_at
/*

        Glide.with(mContext).load(groupChat.profilePicture).placeholder(
            AppCompatResources.getDrawable(mContext!!, R.drawable.ic_profile)).into(holder.imgProfile)
*/

    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        lateinit var tvMessageTitle: TextView
        lateinit var tvMessage: TextView
        lateinit var tvDate: TextView
        lateinit var imgProfile: ImageView
        lateinit var layoutMain: LinearLayout

        override fun onClick(v: View) {
            /*when (v.id) {
                R.id.rlMain -> if (notificationList[adapterPosition].getNotificationType() === 3) {
                    (context as NotificationActivity).redirectPlayStore()
                }
            }*/
        }

        init {
            tvMessageTitle = itemView.findViewById(R.id.tvMessageTitle)
            tvMessage = itemView.findViewById(R.id.tvMessage)
            tvDate = itemView.findViewById(R.id.tvDate)
            imgProfile = itemView.findViewById(R.id.profileImage)
            layoutMain = itemView.findViewById(R.id.layoutMain)
            layoutMain.setOnClickListener(this)
        }
    }

}