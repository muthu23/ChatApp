package com.dubaipolice.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dubaipolice.R
import com.dubaipolice.callback.HandleNotificationSoundItemClick
import com.dubaipolice.model.NotificationTune


internal class NotificationsSoundAdapter(
    context: Context,
    notificationSoundList: ArrayList<NotificationTune>,
    private val onSoundClickListener: HandleNotificationSoundItemClick
) :
    RecyclerView.Adapter<NotificationsSoundAdapter.MyViewHolder>() {

    var mContext: Context? = null
    var notificationSoundList: ArrayList<NotificationTune> = ArrayList<NotificationTune>()

    init {
        this.mContext= context
        this.notificationSoundList = notificationSoundList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_notification_sound_list, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: MyViewHolder, position: Int) {
        val item = notificationSoundList[position]
        item.also { viewHolder.tvMessageTitle.text = it.notificationTitle }
        viewHolder.radioNotification.isChecked = item.isSelected
        viewHolder.radioNotification.setOnClickListener {
            onSoundClickListener.itemClickHandle(item,
                viewHolder.radioNotification.isChecked, position)
        }
    }

    override fun getItemCount(): Int {
        return notificationSoundList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        var tvMessageTitle: TextView = itemView.findViewById(R.id.notificationSoundTitle)
        var lnPlay: LinearLayout = itemView.findViewById(R.id.lnPlay)
        var radioNotification: RadioButton = itemView.findViewById(R.id.radioNotification)


        }


}