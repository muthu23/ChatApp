package com.dubaipolice.callservices

import android.net.Uri
import android.os.Build
import android.telecom.*
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.dubaipolice.db.model.GroupInfoTable
import com.dubaipolice.model.CallerData
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.view.activity.ChatActivity

@RequiresApi(Build.VERSION_CODES.M)
class CallConnectionService : ConnectionService() {


    companion object {
        var conn : CallConnection? = null
    }
    override fun onCreateIncomingConnection(connectionManagerPhoneAccount: PhoneAccountHandle?, request: ConnectionRequest?): Connection {
        val bundle = request!!.extras
        //Log.e("MyExtras", request.extras.toString())
        val callerGroupJid = bundle.getString(AppConstants.IntentConstants.CALLER_GROUP_JID)
        val callerGroupName = bundle.getString(AppConstants.IntentConstants.CALLER_GROUP_NAME)
        val callerGroupId = bundle.getString(AppConstants.IntentConstants.CALLER_GROUP_ID)
        val callerSenderName = bundle.getString(AppConstants.IntentConstants.CALLER_GROUP_SENDER_NAME)
        val name = bundle.getString("NAME")
        val callType = bundle.getString("CALLTYPE")
        var callerData= CallerData()
        callerData.groupName= callerGroupName
        callerData.groupJid= callerGroupJid
        callerData.groupId= callerGroupId
        callerData.senderName= callerSenderName
        conn = CallConnection(applicationContext, callerData)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            conn?.connectionProperties = Connection.PROPERTY_SELF_MANAGED
        }
        val displayName= callerData.senderName + " (" + callerData.groupName + ")"
        conn?.setCallerDisplayName(displayName, TelecomManager.PRESENTATION_ALLOWED)
        conn?.setAddress(Uri.parse(displayName), TelecomManager.PRESENTATION_ALLOWED)
        conn?.setInitializing()
        conn?.setActive()
        return conn!!
    }

    override fun onCreateIncomingConnectionFailed(connectionManagerPhoneAccount: PhoneAccountHandle?, request: ConnectionRequest?) {
        super.onCreateIncomingConnectionFailed(connectionManagerPhoneAccount, request)
        Log.e("onCreateIncomingFailed:",request.toString())
        Toast.makeText(applicationContext,"onCreateIncomingConnectionFailed",Toast.LENGTH_LONG).show();
    }

    override fun onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount: PhoneAccountHandle?, request: ConnectionRequest?) {
        super.onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount, request)
        Log.e("onCreateOutgoingFailed:",request.toString())
        Toast.makeText(applicationContext,"onCreateOutgoingConnectionFailed",Toast.LENGTH_LONG).show();
    }

    override fun onCreateOutgoingConnection(connectionManagerPhoneAccount: PhoneAccountHandle?, request: ConnectionRequest?): Connection {
        val bundle = request!!.extras
        var callerData= CallerData()
        callerData.groupName= "groupName"
        callerData.groupJid= "groupJid"
        val sessionID = bundle.getString("SESSIONID")
        val name = bundle.getString("NAME")
        val receiverType = bundle.getString("RECEIVERTYPE")
        val callType = bundle.getString("CALLTYPE")
        val receiverID = bundle.getString("RECEIVERID")
        Log.e("onCreateOutgoingConn","$bundle \n $sessionID $name $receiverID $receiverType $callType")

        conn = CallConnection(applicationContext, callerData)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            conn?.connectionProperties = Connection.PROPERTY_SELF_MANAGED
        }
        conn?.setCallerDisplayName("Muthu", TelecomManager.PRESENTATION_ALLOWED)
        conn?.setAddress(Uri.parse("Muthu"), TelecomManager.PRESENTATION_ALLOWED)
        conn?.setInitializing()
        conn?.setActive()
        return conn!!
    }
}