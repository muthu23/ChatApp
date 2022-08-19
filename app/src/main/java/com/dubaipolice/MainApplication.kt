package com.dubaipolice

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.dubaipolice.api.ApiClient
import com.dubaipolice.api.ApiRequests
import com.dubaipolice.db.AppDatabase
import com.dubaipolice.service.RtpService
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.AppConstants.KEY_IS_APP_EXIT_TIMEOUT
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.utils.ThemeUtility
import com.dubaipolice.utils.Utils
import com.dubaipolice.view.activity.AppLockActivity
import com.dubaipolice.view.activity.ChatActivity
import com.dubaipolice.xmpp.XmppCustomConnection

class MainApplication : Application(), Application.ActivityLifecycleCallbacks {


    private var countDownTimer: CountDownTimer? = null
    private var activityStarted = 0
    private var activityStopped = 0
    private var isActivityChangingConfigurations = false

    override fun onCreate() {
        super.onCreate()
        mApplicationContext = this
        registerActivityLifecycleCallbacks(this)


        //initialize preference class
        SharedPref.init(applicationContext)
        // initialize network api client
        ApiClient.init(
            ApiRequests::class.java,
            appContext
        )

        if (SharedPref.readBoolean(AppConstants.KEY_IS_LOGGEDIN)) {
            if (SharedPref.readString(AppConstants.KEY_XMPP_USER_JID) != null &&
                !TextUtils.isEmpty(SharedPref.readString(AppConstants.KEY_XMPP_USER_JID)!!) &&
                SharedPref.readString(AppConstants.KEY_XMPP_USER_PASSWORD) != null &&
                !TextUtils.isEmpty(SharedPref.readString(AppConstants.KEY_XMPP_USER_PASSWORD)!!)
            ) {
                //Log.e("User", SharedPref.readString(AppConstants.KEY_XMPP_USER_JID)!!)
                //Log.e("Password", SharedPref.readString(AppConstants.KEY_XMPP_USER_PASSWORD)!!)
                connection = XmppCustomConnection(
                    applicationContext,
                    Utils.getUserFromJid(SharedPref.readString(AppConstants.KEY_XMPP_USER_JID)!!),
                    SharedPref.readString(AppConstants.KEY_XMPP_USER_PASSWORD)
                )
                connection!!.doConnect()
            }

            ProcessLifecycleOwner.get().lifecycle.addObserver(ForegroundLifecycleObserver())

        }

    }

    companion object {

        var liveStreamGroupId: String?= null

        var activeRoomJid: String? = null

        var activeCallGroupJid: String? = null

        @SuppressLint("StaticFieldLeak")
        var connection: XmppCustomConnection? = null

        private lateinit var mApplicationContext: MainApplication
        val appContext: Context
            get() = mApplicationContext


        fun stopRtpService()
        {
            appContext.stopService(Intent(appContext, RtpService::class.java))
        }
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {

        //theme/CMS
        if(!TextUtils.isEmpty(SharedPref.readString(AppConstants.COLOR_SCHEME)))
        {

           ThemeUtility.setStatusBarThemeColor(activity.window)

        }

    }

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}

    class ForegroundLifecycleObserver : DefaultLifecycleObserver {

        private var countDownTimer: CountDownTimer? = null

        fun startCounter() {

            SharedPref.writeBoolean(KEY_IS_APP_EXIT_TIMEOUT, false)

            countDownTimer = object : CountDownTimer(300000, 1000) {
                override fun onTick(milliSecond: Long) {
                    //Log.e("MainApplication", "Timer ${Utils.formatMilliSecond(milliSecond)}")
                }
                override fun onFinish() {
                    //Log.e("MainApplication", "Timer finished 30 seconds")
                    SharedPref.writeBoolean(KEY_IS_APP_EXIT_TIMEOUT, true)
                }
            }.start()
        }

        override fun onCreate(owner: LifecycleOwner) {
            super.onCreate(owner)
            Log.e("ActivityStatus", "onCreate")
        }

        override fun onStart(owner: LifecycleOwner) {
            super.onStart(owner)
            Log.e("ActivityStatus", "onStart")
            Log.e("ProcessLog", "APP IS ON FOREGROUND")
            if(connection != null && connection!!.isConnected() && connection!!.isAuthenticated())
            {
                connection?.sendOnlinePresence()
                val membersList = AppDatabase.getAppDatabase(MainApplication.appContext)
                    ?.memberInfoTableDao()
                    ?.getAllGroupMembers()

                if(membersList != null && membersList.size > 0)
                {
                    Log.e("MemberListSize", membersList.size.toString())
                    connection?.sendOnlineOfflineCustomStanza(membersList, "online")
                }
            }

            countDownTimer?.cancel()

            if(SharedPref.readBoolean(AppConstants.KEY_IS_LOGGEDIN))
            {
                if(SharedPref.readBoolean(KEY_IS_APP_EXIT_TIMEOUT)){
                    SharedPref.writeBoolean(KEY_IS_APP_EXIT_TIMEOUT, false)
                    MainApplication.appContext.startActivity(Intent(MainApplication.appContext, AppLockActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                or Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }

        }

        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            Log.e("ActivityStatus", "onResume")
        }

        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            Log.e("ActivityStatus", "onPause")
        }

        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            Log.e("ActivityStatus", "onStop")
            Log.e("ProcessLog", "APP IS ON BACKGROUND")
            startCounter()
            if(connection != null && connection!!.isConnected() && connection!!.isAuthenticated())
            {
                connection?.sendOfflinePresence()
                val membersList = AppDatabase.getAppDatabase(MainApplication.appContext)
                    ?.memberInfoTableDao()
                    ?.getAllGroupMembers()

                if(membersList != null && membersList.size > 0)
                {
                    Log.e("MemberListSize", membersList.size.toString())
                    connection?.sendOnlineOfflineCustomStanza(membersList, "offline")
                }
            }

        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            Log.e("ActivityStatus", "onDestroy")
        }

    }

}