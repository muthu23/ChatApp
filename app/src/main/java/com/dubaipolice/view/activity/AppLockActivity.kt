package com.dubaipolice.view.activity

import `in`.aabhasjindal.otptextview.OTPListener
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_WEAK
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dubaipolice.MainApplication
import com.dubaipolice.R
import com.dubaipolice.callback.HandleClick
import com.dubaipolice.databinding.ActivityAppLockBinding
import com.dubaipolice.db.AppDatabase
import com.dubaipolice.model.CommonResponse
import com.dubaipolice.model.VerifyTokenResponse
import com.dubaipolice.utils.MyProgressDialog
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.utils.Utils
import com.dubaipolice.viewmodel.CommonViewModel
import com.dubaipolice.wrapper.Resource
import java.util.concurrent.Executor
import android.content.pm.PackageManager

import androidx.core.app.ActivityCompat

import android.app.KeyguardManager
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import com.dubaipolice.utils.AppConstants
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey


class AppLockActivity : AppCompatActivity(), HandleClick {

    lateinit var commonViewModel: CommonViewModel
    lateinit var binding: ActivityAppLockBinding
    lateinit  var mContext: Context
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this@AppLockActivity, R.layout.activity_app_lock)
        commonViewModel = ViewModelProvider(this)[CommonViewModel::class.java]

        binding.lifecycleOwner= this@AppLockActivity
        //binding.loginViewModel= loginViewModel
        binding.clickHandle= this

        mContext = this

        verifyToken()
        verifyPasscodeAutomaticallyAfterEntering6DigitsPin()
        //ThemeUtility.setGradientThemeColorUsingDrawable(mContext, R.drawable.bg_button)
        //ThemeUtility.setGradientThemeColorUsingDrawable(mContext, R.drawable.bg_gradient)

        //request focus to the otpview to open the keyboard automatically
        binding.otpView.requestFocusOTP()

        getAllCms()

    }

    private fun getAllCms()
    {
        commonViewModel.allCmsResponse!!.observe(
            this@AppLockActivity
        ) { getAllCmsListResponse ->
            when (getAllCmsListResponse.status) {
                Resource.Status.SUCCESS -> {
                    if (getAllCmsListResponse.data!!.isSuccess) {
                        with(SharedPref) {
                            writeString(
                                AppConstants.COLOR_SCHEME,
                                getAllCmsListResponse.data.data?.colorScheme.toString()
                            )
                            writeString(
                                AppConstants.FONT_SCHEME,
                                getAllCmsListResponse.data.data?.colorScheme.toString()
                            )
                        }
                        //Log.e("ColorSchemes",SharedPref.readString(AppConstants.COLOR_SCHEME)!!)
                        /* if(!TextUtils.isEmpty(SharedPref.readString(AppConstants.COLOR_SCHEME)))
                         {
                             val colorSchemes = SharedPref.readString(AppConstants.COLOR_SCHEME)!!.split(",".toRegex()).toTypedArray()
                             val firstColor= colorSchemes[0]
                             val secondColor= colorSchemes[1]
                             Log.e("FirstColor", firstColor)
                             Log.e("SecondColor", secondColor)
                             ThemeUtility.setGradientThemeColorUsingDrawable(mContext, R.drawable.bg_button)
                             ThemeUtility.setGradientThemeColorUsingDrawable(mContext, R.drawable.bg_gradient)
                             ThemeUtility.setStatusBarThemeColor(window)
                         }*/

                    } else {
                        Utils.showToast(mContext, getAllCmsListResponse.data.message!!)
                    }
                }
                Resource.Status.LOADING -> {
                }
                Resource.Status.ERROR -> {
                    Utils.showToast(mContext, getAllCmsListResponse.message!!)
                }
            }
        }


    }

    private fun logoutApi()
    {
        MyProgressDialog.show(mContext)

        commonViewModel.logoutResponse!!.observe(
            this@AppLockActivity
        ) { logoutResponse ->
            MyProgressDialog.dismiss()
            when (logoutResponse.status) {
                Resource.Status.SUCCESS -> {

                    Utils.logout()

                }
                Resource.Status.LOADING -> {
                }
                Resource.Status.ERROR -> {
                    Utils.logout()
                }
            }
        }


    }

    override fun onResume() {
        super.onResume()
        showBiometricDialog()

    }
    private fun verifyPasscodeAutomaticallyAfterEntering6DigitsPin() {

        binding.otpView.otpListener = object : OTPListener {
            override fun onInteractionListener() {

            }

            override fun onOTPComplete(otp: String) {
                verifyPasscode()
            }
        }

    }


    override fun clickHandle(v: View) {
        when(v.id) {

            R.id.tvLogout -> {

                logout()
                //forgotPinPopup()

            }

        }
    }

    fun logout() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(mContext)
        dialog.setMessage(getString(R.string.are_you_sure_logout))
        dialog.setPositiveButton(
            getString(R.string.yes)
        ) { dialog, which ->

            dialog.dismiss()

            if(Utils.isNetConnected(mContext))
            {
                logoutApi()
            }
            else{
                Utils.showToast(mContext, getString(R.string.no_internet))
            }

        }
        dialog.setNegativeButton(
            getString(R.string.no)
        ) { dialog, which -> dialog.dismiss() }
        val alert = dialog.create()
        alert.show()
    }

    private fun isPinValidate() : Boolean
    {
        val pin: String = binding.otpView.otp!!

        if (pin.length < 6) {
            Utils.showToast(mContext, getString(R.string.please_enter_6_digits_pin))
            return false
        }
        return true
    }

    private fun verifyPasscode()
    {

        if(isPinValidate())
        {
            MyProgressDialog.show(mContext);

            commonViewModel.passcode.value= binding.otpView.otp!!

            commonViewModel.verifyPasscodeResponse!!.observe(
                this@AppLockActivity,
                Observer<Resource<CommonResponse?>> { commonResponseResource ->
                    MyProgressDialog.dismiss()
                    when (commonResponseResource.status) {
                        Resource.Status.SUCCESS -> {
                            if (commonResponseResource.data!!.isSuccess) {

                                startActivity(Intent(mContext, HomeEndUserActivity::class.java))
                                finish()

                            } else {
                                Utils.showToast(mContext, commonResponseResource.data.message!!)
                            }
                        }
                        Resource.Status.LOADING -> {
                        }
                        Resource.Status.ERROR -> {
                            Utils.showToast(mContext, commonResponseResource.message!!)
                            /*if(commonResponseResource.message == getString(R.string.error_401_relogin))
                            {
                                Utils.sessionExpiredPopup()
                            }
                            else
                            {
                                Utils.showToast(mContext, commonResponseResource.message!!)
                            }*/
                        }
                    }
                })
        }

    }

    private fun verifyToken()
    {

            //MyProgressDialog.show(mContext);

            commonViewModel.verifyTokenResponse!!.observe(
                this@AppLockActivity,
                Observer<Resource<VerifyTokenResponse?>> { verifyTokenResponseResource ->
                    //MyProgressDialog.dismiss()
                    when (verifyTokenResponseResource.status) {
                        Resource.Status.SUCCESS -> {
                            /*if (verifyTokenResponseResource.data!!.isSuccess) {

                                var isValid= verifyTokenResponseResource.data!!.data?.isValid
                                if(isValid != null && isValid)
                                {

                                }
                                else
                                {
                                    Utils.showToast(mContext, getString(R.string.session_expired))
                                    val i = Intent(MainApplication.appContext, LoginActivity::class.java)
                                    i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                    MainApplication.appContext.startActivity(i)
                                }


                            } else {
                                //Utils.showToast(mContext, verifyTokenResponseResource.data.message!!)
                                var isValid= verifyTokenResponseResource.data!!.data?.isValid
                                if(isValid != null && !isValid)
                                {
                                    Utils.showToast(mContext, getString(R.string.session_expired))
                                    val i = Intent(MainApplication.appContext, LoginActivity::class.java)
                                    i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                    MainApplication.appContext.startActivity(i)
                                }

                            }*/
                        }
                        Resource.Status.LOADING -> {
                        }
                        Resource.Status.ERROR -> {
                            Utils.showToast(mContext, verifyTokenResponseResource.message!!)
                        }
                    }
                })

    }

    private fun forgotPinPopup()
    {
        AlertDialog.Builder(mContext)
            .setPositiveButton(R.string.logout) { dialogInterface, which ->

                dialogInterface.dismiss()
                //Utils.logout()

            }
            .setNegativeButton(R.string.cancel) { dialogInterface, which ->

                dialogInterface.dismiss()

            }
            .setCancelable(true)
            .setMessage(R.string.forgot_pin_message)
            .show()
    }

    private fun showBiometricDialog() {
        if (checkBiometricSupport()) {
            executor =
                ContextCompat.getMainExecutor(this)
            biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence
                    ) {
                        super.onAuthenticationError(errorCode, errString)

                       /* Toast.makeText(
                            applicationContext,
                            "Authentication error: $errString", Toast.LENGTH_SHORT
                        ).show()*/
                    }

                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult
                    ) {
                        super.onAuthenticationSucceeded(result)
                        Toast.makeText(applicationContext, "Authentication succeeded!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(mContext, HomeEndUserActivity::class.java))
                        finish()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Toast.makeText(
                            applicationContext, "Authentication failed",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                })

            try {
                promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Biometric login for my app")
                    .setSubtitle("Log in using your biometric credential")
                    .setNegativeButtonText("cancel")
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                    .build()
                //biometricPrompt.authenticate(promptInfo, cryptoObject)
                biometricPrompt.authenticate(promptInfo)
            } catch (e: Exception) {
            }

        }
    }
    private fun checkBiometricSupport(): Boolean {
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        return if (!keyguardManager.isKeyguardSecure) {
            Utils.showToast(mContext, "Lock screen security not enabled in Settings")
            false
        } else{
            true
        }
    }




}