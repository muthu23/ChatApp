package com.dubaipolice.service

import android.app.Service
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.utils.Utils

class OnClearFromRecentService : Service() {
    private var countDownTimer: CountDownTimer? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        countDownTimer?.cancel()
        return super.onStartCommand(intent, flags, startId)

    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        SharedPref.writeBoolean(AppConstants.KEY_IS_APP_EXIT_TIMEOUT, true)
       // startCounter()
    }

    private fun startCounter() {
        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(milliSecond: Long) {
                Log.e("MainApplication", "Timer ${Utils.formatMilliSecond(milliSecond)}")
            }

            override fun onFinish() {
                Log.e("MainApplication", "Timer finished 30 seconds")
                SharedPref.writeBoolean(AppConstants.KEY_IS_APP_EXIT_TIMEOUT, true)
                stopSelf()
            }
        }.start()
    }

}