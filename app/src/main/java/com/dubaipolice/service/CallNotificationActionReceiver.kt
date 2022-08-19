package com.dubaipolice.service

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.text.TextUtils
import android.util.Log
import androidx.core.content.ContextCompat
import com.dubaipolice.MainApplication
import com.dubaipolice.model.CallerData
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.utils.Utils
import com.dubaipolice.view.activity.IncomingCallUIActivity
import com.dubaipolice.view.activity.NotificationActivity
import com.dubaipolice.view.activity.ProfilePictureActivity
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetUserInfo
import java.net.URL


class CallNotificationActionReceiver : BroadcastReceiver() {

    var mContext: Context? = null
    var callerData: CallerData? = null

    override fun onReceive(context: Context, intent: Intent?) {
        mContext = context
        if (intent != null && intent.extras != null) {
            var action: String? = ""
            action = intent.getStringExtra("ACTION_TYPE")
            callerData= intent.getSerializableExtra(AppConstants.IntentConstants.CALLER_DATA) as CallerData
            if (action != null && !action.equals("", ignoreCase = true)) {
                performClickAction(context, action)
            }

            // Close the notification after the click action is performed.
            /*val iclose = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            context.sendBroadcast(iclose)*/
            //context.stopService(Intent(context, HeadsUpNotificationService::class.java))
        }
    }

    private fun performClickAction(context: Context, action: String) {
        if (action.equals("RECEIVE_CALL", ignoreCase = true)) {
            if (checkAppPermissions()) {
                //VideoCallActivity
                /*val intentCallReceive = Intent(mContext, NotificationActivity::class.java)
                intentCallReceive.putExtra("Call", "incoming")
                intentCallReceive.putExtra(AppConstants.IntentConstants.CALLER_DATA, callerData)
                intentCallReceive.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                mContext!!.startActivity(intentCallReceive)*/
                context.stopService(Intent(MainApplication.appContext, HeadsUpNotificationService::class.java))
                jitsiMeetAudioConference(callerData!!.groupName!!, callerData!!.groupJid!!)
            } else {
                //VideoCallRingingActivity
                val intent = Intent(
                    MainApplication.appContext,
                    IncomingCallUIActivity::class.java
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra("CallFrom", "call from push")
                intent.putExtra(AppConstants.IntentConstants.CALLER_DATA, callerData)
                mContext!!.startActivity(intent)
            }
        } else if (action.equals("DIALOG_CALL", ignoreCase = true)) {

            // show ringing activity when phone is locked
            //VideoCallRingingActivity
            val intent = Intent(
                MainApplication.appContext,
                IncomingCallUIActivity::class.java
            )
            intent.putExtra(AppConstants.IntentConstants.CALLER_DATA, callerData)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            mContext!!.startActivity(intent)
        } else {
            //Reject
            context.stopService(Intent(context, HeadsUpNotificationService::class.java))
//            val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
//            context.sendBroadcast(it)
        }
    }

    private fun checkAppPermissions(): Boolean {
        return hasReadPermissions() && hasWritePermissions() && hasCameraPermissions() && hasAudioPermissions()
    }

    private fun hasAudioPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            MainApplication.appContext,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasReadPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            MainApplication.appContext,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasWritePermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            MainApplication.appContext,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasCameraPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            MainApplication.appContext,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun jitsiMeetAudioConference(groupName: String, groupJid: String) {

        Log.e("CallerName2", groupName)
        Log.e("CallerJid2", groupJid)

        val userInfo= JitsiMeetUserInfo()
        userInfo.displayName= String.format(
            "%s %s", SharedPref.readString(
                AppConstants.KEY_FIRST_NAME
            ), SharedPref.readString(AppConstants.KEY_LAST_NAME)
        )

        if(!TextUtils.isEmpty(SharedPref.readString(AppConstants.KEY_PROFILE_PHOTO)))
        {
            userInfo.avatar= URL(SharedPref.readString(AppConstants.KEY_PROFILE_PHOTO))
        }

        val options = JitsiMeetConferenceOptions.Builder()
            .setServerURL(URL("https://dbvoicedev.iworklab.com"))
            .setRoom(Utils.getGroupFromJid(groupJid))
            //.setServerURL(URL("https://meet.jit.si"))
            //.setRoom("firozaudio")
            //.setRoom("https://meet.jit.si/firozaudi")
            .setAudioMuted(false)
            .setVideoMuted(true)
            .setAudioOnly(true)
            //.setConfigOverride("requireDisplayName", true)
            //.setConfigOverride("disableInviteFunctions", false)
            .setFeatureFlag("invite.enabled", false)
            .setFeatureFlag("chat.enabled", false)
            .setFeatureFlag("recording.enabled", false)
            .setFeatureFlag("video-mute.enabled", false)
            .setFeatureFlag("video-share.enabled", false)
            .setFeatureFlag("welcomepage.enabled", false)
            .setFeatureFlag("live-streaming.enabled", false)
            .setFeatureFlag("call-integration.enabled", false)
            .setUserInfo(userInfo)
            .setSubject(groupName)
            .build()

        JitsiMeetActivity.launch(MainApplication.appContext, options)

    }

}