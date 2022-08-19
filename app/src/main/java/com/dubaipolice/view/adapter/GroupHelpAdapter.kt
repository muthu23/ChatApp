package com.dubaipolice.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dubaipolice.R
import com.dubaipolice.callback.HandleGroupHelpItemChecked
import com.dubaipolice.callback.HandleNotificationSoundItemClick
import com.dubaipolice.model.GroupHelpItem
import com.dubaipolice.model.NotificationTune


internal class GroupHelpAdapter(
    context: Context,
    notificationSoundList: ArrayList<GroupHelpItem>,
    private val onItemChecked: HandleGroupHelpItemChecked
) :
    RecyclerView.Adapter<GroupHelpAdapter.MyViewHolder>() {

    var mContext: Context? = null
    var notificationSoundList: ArrayList<GroupHelpItem> = ArrayList<GroupHelpItem>()

    init {
        this.mContext= context
        this.notificationSoundList = notificationSoundList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_group_help_list, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: MyViewHolder, position: Int) {
        val item = notificationSoundList[position]
        item.also { viewHolder.tvGroupName.text = it.groupName }
      //  viewHolder.checkGroup.isChecked = item.isSelected == true
        viewHolder.checkGroup.setOnClickListener {
            onItemChecked.itemClickHandle(item,
                viewHolder.checkGroup.isChecked, position)
        }
    }

    override fun getItemCount(): Int {
        return notificationSoundList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        var tvGroupName: TextView = itemView.findViewById(R.id.tvGroupName)
        var checkGroup: CheckBox = itemView.findViewById(R.id.checkGroup)


        }


}