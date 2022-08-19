package com.dubaipolice.view.activity

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dubaipolice.MainApplication
import com.dubaipolice.R
import com.dubaipolice.callback.HandleClick
import com.dubaipolice.databinding.ActivityIncomingCallUiactivityBinding
import com.dubaipolice.model.CallerData
import com.dubaipolice.service.HeadsUpNotificationService
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.utils.Utils
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetUserInfo
import java.net.URL


class IncomingCallUIActivity : AppCompatActivity(), HandleClick {

    lateinit var binding: ActivityIncomingCallUiactivityBinding

    lateinit  var mContext: Context

    var callerData: CallerData? = null

    private var broadcastReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }

        binding = DataBindingUtil.setContentView(this@IncomingCallUIActivity, R.layout.activity_incoming_call_uiactivity)

        binding.lifecycleOwner= this@IncomingCallUIActivity
        //binding.loginViewModel= loginViewModel
        binding.clickHandle= this

        mContext = this

        getExtras()

        registerBroadcastReceiver()

    }

    private fun getExtras() {

        val extras = intent.extras
        Log.e("GetExtra", "get")
        if (extras != null) {

            callerData = extras.getSerializable(AppConstants.IntentConstants.CALLER_DATA) as CallerData
            val actionType = extras.getString("ACTION_TYPE")
            if(actionType == "RECEIVE_CALL")
            {
                MainApplication.appContext.stopService(Intent(MainApplication.appContext, HeadsUpNotificationService::class.java))
                jitsiMeetAudioConference(callerData!!.groupName!!, callerData!!.groupJid!!)
                finish()
                return
            }

            Glide.with(mContext).load(callerData!!.senderProfileImage).
                    placeholder(
                        AppCompatResources.getDrawable(
                            mContext,
                            R.drawable.ic_profile
                        )
                    ).into(binding.profileImage)

            binding.tvSenderName.text= callerData!!.senderName
            binding.tvGroupName.text= callerData!!.groupName

        } else {
            Log.e("Groupname", "NULL")
        }


    }

    private fun registerBroadcastReceiver() {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                Log.i("Receiver", "Broadcast received: $action")
                if (action == AppConstants.MISSED_CALL_BROADCAST) {

                    Log.e("BroadcastReceived", "missedcall")
                    finish()

                }
            }
        }
        //registering the broadcast receiver
        val intentFilter = IntentFilter()
        intentFilter.addAction(AppConstants.MISSED_CALL_BROADCAST)
        registerReceiver(broadcastReceiver, intentFilter)
        //registerReceiver(broadcastReceiver, IntentFilter(AppConstants.PRESENCE_RECEIVED_BROADCAST))
    }

    private fun unregisterBroadcastReceiver() {
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterBroadcastReceiver()
    }

    override fun clickHandle(v: View) {

        when(v.id) {

            R.id.lnAnswer -> {

                MainApplication.appContext.stopService(Intent(MainApplication.appContext, HeadsUpNotificationService::class.java))
                jitsiMeetAudioConference(callerData!!.groupName!!, callerData!!.groupJid!!)
                finish()

            }

            R.id.lnReject -> {

                MainApplication.appContext.stopService(Intent(MainApplication.appContext, HeadsUpNotificationService::class.java))
               finish()

            }

        }

    }

    private fun jitsiMeetAudioConference(groupName: String, groupJid: String) {

        /*Log.e("CallerName2", groupName)
        Log.e("CallerJid2", groupJid)*/

       /* val hangupExistingCallBroadcastIntent = Intent("org.jitsi.meet.HANG_UP")
        //muteBroadcastIntent.putExtra("muted", muted)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(hangupExistingCallBroadcastIntent)
*/

        MainApplication.activeCallGroupJid = groupJid

        val userInfo= JitsiMeetUserInfo()
        userInfo.displayName= String.format(
            "%s %s", SharedPref.readString(
                AppConstants.KEY_FIRST_NAME
            ), SharedPref.readString(AppConstants.KEY_LAST_NAME)
        )

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