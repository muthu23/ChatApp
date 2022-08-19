package com.dubaipolice.callservices

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.Connection
import android.telecom.DisconnectCause
import android.util.Log
import androidx.annotation.RequiresApi
import com.dubaipolice.MainApplication
import com.dubaipolice.model.CallerData
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.utils.Utils
import com.dubaipolice.view.activity.ChatActivity
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetUserInfo
import java.net.URL

@RequiresApi(Build.VERSION_CODES.M)
class CallConnection(context: Context, callerData: CallerData) : Connection() {

    private val mTAG = "CallConnection"
    private var callerData : CallerData = callerData
    private var connectionContext : Context = context

    override fun onCallAudioStateChanged(state: CallAudioState?) {
        Log.e(mTAG, "onCallAudioStateChange:" + state.toString())
    }

    override fun onStateChanged(state: Int) {
        super.onStateChanged(state)
        Log.e(mTAG, "onStateChanged: $state" )
    }

    override fun onDisconnect() {
        super.onDisconnect()
        destroyConnection()
        Log.e(mTAG,"onDisconnect")
        setDisconnected(DisconnectCause(DisconnectCause.LOCAL, "Missed"))
    }

    fun onDisconnect(call : Call) {
        Log.e(mTAG,"onDisconnect Call: $call")
    }
    override fun onHold() {
        super.onHold()
    }

    override fun onAnswer(videoState: Int) {
        super.onAnswer(videoState)
    }

    override fun onStopDtmfTone() {
        super.onStopDtmfTone()
        Log.e(mTAG, "onStopDtmfTone: " )
    }

    override fun onCallEvent(event: String?, extras: Bundle?) {
        super.onCallEvent(event, extras)
        Log.e(mTAG, "onCallEvent: $event" )
    }

    override fun onShowIncomingCallUi() {
        super.onShowIncomingCallUi()
    }

    override fun onAnswer() {
        //jitsiMeetAudioConference("conferences2", "conference1645601723113@conference.dbchatdev.iworklab.com")
        jitsiMeetAudioConference(callerData.groupName!!, callerData.groupJid!!)
        destroyConnection()
    }

    private fun destroyConnection() {
        setDisconnected(DisconnectCause(DisconnectCause.REMOTE, "Rejected"))
        Log.e(mTAG, "destroyConnection" )
        super.destroy()
    }

    override fun onReject() {
        Log.e(mTAG, "onReject: " )
        destroyConnection()
    }

    override fun onAbort() {
        super.onAbort()
        Log.e(mTAG,"OnAbort")

    }

    fun onOutgoingReject() {
        Log.e(mTAG,"onDisconnect")
        destroyConnection()
        setDisconnected(DisconnectCause(DisconnectCause.REMOTE, "REJECTED"))
    }



    override fun onReject(rejectReason: Int) {
        Log.e(mTAG, "onReject: $rejectReason")
        super.onReject(rejectReason)
    }

    override fun onReject(replyMessage: String?) {
        Log.e(mTAG, "onReject: $replyMessage" )
        super.onReject(replyMessage)
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