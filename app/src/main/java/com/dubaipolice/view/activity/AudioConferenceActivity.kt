package com.dubaipolice.view.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.dubaipolice.R
import com.dubaipolice.db.model.GroupInfoTable
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.utils.Utils
import com.facebook.react.modules.core.PermissionListener
import org.jitsi.meet.sdk.*
import java.net.URL


class AudioConferenceActivity : FragmentActivity(), JitsiMeetActivityInterface {

    private var view: JitsiMeetView? = null

    var groupData: GroupInfoTable? = null

    var roomJid: String?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getExtras()

        val userInfo= JitsiMeetUserInfo()
        userInfo.displayName= String.format(
            "%s %s", SharedPref.readString(
                AppConstants.KEY_FIRST_NAME
            ), SharedPref.readString(AppConstants.KEY_LAST_NAME)
        )

        view = JitsiMeetView(this)
        val options = JitsiMeetConferenceOptions.Builder()
            .setServerURL(URL("https://dbvoicedev.iworklab.com"))
            .setRoom(roomJid)
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
            .setSubject(groupData!!.groupName)
            .build()

        view!!.join(options)

        setContentView(view)

    }

    private fun getExtras() {

        val extras = intent.extras
        Log.e("GetExtra", "get")
        if (extras != null) {

            groupData =
                extras.getSerializable(AppConstants.IntentConstants.GROUP_DATA) as GroupInfoTable
            roomJid = groupData!!.groupJid
            Log.e("RoomJid1", roomJid!!)
            roomJid= Utils.getGroupFromJid(roomJid!!)

            Log.e("RoomJid2", roomJid!!)

        } else {
            Log.e("GroupJid", "NULL")
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        JitsiMeetActivityDelegate.onActivityResult(
            this, requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        //JitsiMeetActivityDelegate.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()

        view?.leave() //to leave the call
        view?.dispose()
        view = null

        JitsiMeetActivityDelegate.onHostDestroy(this)

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        JitsiMeetActivityDelegate.onNewIntent(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        JitsiMeetActivityDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()

        JitsiMeetActivityDelegate.onHostResume(this)

    }

    override fun onStop() {
        super.onStop()

        JitsiMeetActivityDelegate.onHostPause(this)

    }

    override fun requestPermissions(p0: Array<out String>?, p1: Int, p2: PermissionListener?) {
        TODO("Not yet implemented")
    }
}