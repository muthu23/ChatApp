package com.dubaipolice

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import com.dubaipolice.service.OnClearFromRecentService
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.AppConstants.KEY_IS_APP_EXIT_TIMEOUT
import com.dubaipolice.utils.AppConstants.KEY_IS_LOGGEDIN
import com.dubaipolice.utils.AppConstants.KEY_IS_PASSCODE_GENERATED
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.view.activity.AppLockActivity
import com.dubaipolice.view.activity.HomeEndUserActivity
import com.dubaipolice.view.activity.LoginActivity
import com.dubaipolice.view.activity.PasscodeSetupActivity
import java.util.*


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_splash)

        if(!TextUtils.isEmpty(SharedPref.readString(AppConstants.KEY_LANGUAGE)))
        {
            updateLanguage(SharedPref.readString(AppConstants.KEY_LANGUAGE)!!)
        }
        else
        {
            SharedPref.writeString(AppConstants.KEY_LANGUAGE, AppConstants.LANGUAGE_ENGLISH)
            updateLanguage(SharedPref.readString(AppConstants.KEY_LANGUAGE)!!)
        }

        if (SharedPref.readBoolean(KEY_IS_LOGGEDIN)) {
            if(SharedPref.readBoolean(KEY_IS_PASSCODE_GENERATED)) {
                //if(SharedPref.readBoolean(KEY_IS_APP_EXIT_TIMEOUT))
                startActivity(Intent(this, AppLockActivity::class.java))
                finish()
                /*else {
                    startActivity(Intent(this, HomeEndUserActivity::class.java))
                    finish()
                }*/
            }
            else
                startActivity(Intent(this, PasscodeSetupActivity::class.java))
            finish()
        } else {
            //if not logged in
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }


        /*   Handler(Looper.getMainLooper()).postDelayed({
            if (SharedPref.readBoolean(AppConstants.KEY_IS_LOGGEDIN)) {
                if(SharedPref.readBoolean(AppConstants.KEY_IS_PASSCODE_GENERATED))
                {
                    startActivity(Intent(this@SplashActivity, AppLockActivity::class.java))
                }
                else
                {
                    startActivity(Intent(this@SplashActivity, PasscodeSetupActivity::class.java))
                }
                finish()
            } else {
                //if not logged in
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                finish()
            }
        }, 3000)*/

    }

    private fun updateLanguage(lang: String) {

        var myLocale = Locale(lang)
        val res: Resources = resources
        val dm: DisplayMetrics = res.getDisplayMetrics()
        val conf: Configuration = res.getConfiguration()
        conf.locale = myLocale
        res.updateConfiguration(conf, dm)

    }

}