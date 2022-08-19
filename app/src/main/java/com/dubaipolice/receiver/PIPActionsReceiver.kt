package com.dubaipolice.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dubaipolice.utils.AppConstants

class PIPActionsReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        if(intent != null)
        {

            if(intent.action.equals(AppConstants.PIP_ACTION_MAIN_BROADCAST))
            {

                val actionType = intent.extras!!.getString(AppConstants.PIP_ACTION_TYPE)
                if (actionType != null) {

                    val intent = Intent(AppConstants.PIP_ACTION_LOCAL_BROADCAST)
                    intent.putExtra(AppConstants.PIP_ACTION_TYPE, actionType)
                    context!!.sendBroadcast(intent)

                }

            }

        }

    }

}