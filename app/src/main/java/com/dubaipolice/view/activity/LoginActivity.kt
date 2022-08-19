package com.dubaipolice.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import com.dubaipolice.MainApplication
import com.dubaipolice.R
import com.dubaipolice.callback.HandleClick
import com.dubaipolice.databinding.ActivityLoginBinding
import com.dubaipolice.db.AppDatabase
import com.dubaipolice.model.LoginResponse
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.MyProgressDialog
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.utils.Utils
import com.dubaipolice.viewmodel.LoginViewModel
import com.dubaipolice.wrapper.Resource

class LoginActivity : AppCompatActivity(), HandleClick {

    lateinit var loginViewModel: LoginViewModel
    lateinit var binding: ActivityLoginBinding

    lateinit  var mContext: Context

    private var page = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this@LoginActivity, R.layout.activity_login)
        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        binding.lifecycleOwner= this@LoginActivity
        binding.loginViewModel= loginViewModel
        binding.clickHandle= this

        mContext = this

        clearExistingData()

    }

    private fun isEmailValidate() : Boolean
    {
        if(TextUtils.isEmpty(binding.etEmail.text.toString().trim()))
        {
            Utils.showToast(mContext, getString(R.string.please_enter_email))
            return false
        }
        return true
    }

    private fun isOtpValidate() : Boolean
    {
        if(TextUtils.isEmpty(binding.etOtp.text.toString().trim()))
        {
            Utils.showToast(mContext, getString(R.string.please_enter_otp))
            return false
        }
        return true
    }

    private fun loadData(loginResponse: LoginResponse?) {

        //clear all database and tables if any

        //clearExistingData()

        SharedPref.writeBoolean(AppConstants.KEY_IS_LOGGEDIN, true)
        SharedPref.writeString(AppConstants.KEY_FIRST_NAME, loginResponse!!.data?.userDetails?.firstName)
        SharedPref.writeString(AppConstants.KEY_LAST_NAME, loginResponse.data?.userDetails?.lastName)
        SharedPref.writeString(AppConstants.KEY_USER_NAME, loginResponse.data?.userDetails?.userName)
        SharedPref.writeString(AppConstants.KEY_SOUND_FILE, loginResponse.data?.userDetails?.soundFile)
        SharedPref.writeString(AppConstants.KEY_PROFILE_PHOTO, loginResponse.data?.userDetails?.profileImage)
        SharedPref.writeString(AppConstants.KEY_XMPP_USER_JID, loginResponse.data?.userDetails?.xmppUserJid)
        SharedPref.writeString(AppConstants.KEY_XMPP_USER_PASSWORD, loginResponse.data?.userDetails?.xmppUserPassword)
        SharedPref.writeInt(AppConstants.KEY_USER_ID, loginResponse.data?.userDetails?.userId)
        SharedPref.writeInt(AppConstants.KEY_ROLE_ID, loginResponse.data?.userDetails?.role?.roleId)
        SharedPref.writeInt(AppConstants.KEY_ORGANIZATION_ID, loginResponse.data?.userDetails?.organization?.organizationId)
        SharedPref.writeString(AppConstants.KEY_TOKEN, loginResponse.data?.userDetails?.token)
        SharedPref.writeBoolean(AppConstants.KEY_IS_PASSCODE_GENERATED, loginResponse.data?.userDetails?.isPasscodeGenerated!!)

        SharedPref.writeString(AppConstants.KEY_LANGUAGE, loginResponse.data?.userDetails?.language?.languageCode)

        if(SharedPref.readBoolean(AppConstants.KEY_IS_PASSCODE_GENERATED))
        {

            startActivity(Intent(mContext, AppLockActivity::class.java))
            finish()

        }
        else
        {
            startActivity(Intent(mContext, PasscodeSetupActivity::class.java))
            finish()
        }


    }

    private fun clearExistingData() {

        WorkManager.getInstance(MainApplication.appContext).cancelAllWork()

        if(MainApplication.connection != null)
        {
            MainApplication.connection?.disconnectXmpp("dis")
            MainApplication.connection = null
        }

        SharedPref.clearData()

        AppDatabase.getAppDatabase(MainApplication.appContext)
            ?.groupInfoTableDao()
            ?.deleteAllGroups()

        AppDatabase.getAppDatabase(MainApplication.appContext)
            ?.messageInfoTableDao()
            ?.deleteAllChats()

        AppDatabase.getAppDatabase(MainApplication.appContext)
            ?.memberInfoTableDao()
            ?.deleteAllMembers()

    }

    override fun clickHandle(v: View) {
        when(v.id){
            R.id.btnGetOtp -> {

                if(isEmailValidate())
                {

                    MyProgressDialog.show(mContext);

                    loginViewModel.loginResponse!!.observe(
                        this@LoginActivity,
                        Observer<Resource<LoginResponse?>> { loginResponseResource ->
                            MyProgressDialog.dismiss()
                            when (loginResponseResource.status) {
                                Resource.Status.SUCCESS -> {
                                    if (loginResponseResource.data!!.isSuccess) {
                                        Utils.showToast(mContext, loginResponseResource.data.message!!)
                                        if (page == 1) {
                                            //Utils.hideKeyboard(mContext)
                                            page = 2
                                            binding.layoutEmailId.visibility = View.GONE
                                            binding.layoutOtp.visibility = View.VISIBLE
                                            binding.etOtp.requestFocus()
                                            loginResponseResource.data.data?.otp.let {

                                                loginViewModel.otp.value= loginResponseResource.data.data?.otp

                                            }

                                        }
                                    } else {
                                        Utils.showToast(mContext, loginResponseResource.data.message!!)
                                    }

                                }
                                Resource.Status.LOADING -> {
                                }
                                Resource.Status.ERROR -> {
                                    Utils.showToast(mContext, loginResponseResource.message!!)
                                }
                            }
                        })

                }

            }

            R.id.btnSubmit -> {

                if(isOtpValidate())
                {
                    MyProgressDialog.show(mContext);
                    loginViewModel.verifyLoginResponse!!.observe(
                        this@LoginActivity,
                        Observer<Resource<LoginResponse?>> { loginResponseResource ->
                            MyProgressDialog.dismiss()
                            when (loginResponseResource.status) {
                                Resource.Status.SUCCESS -> {
                                    if (loginResponseResource.data!!.isSuccess) {
                                        loadData(loginResponseResource.data)
                                    } else {
                                        Utils.showToast(mContext, loginResponseResource.data.message!!)
                                    }
                                }
                                Resource.Status.LOADING -> {
                                }
                                Resource.Status.ERROR -> {
                                    Utils.showToast(mContext, loginResponseResource.message!!)
                                }
                            }
                        })
                }

            }
        }
    }

    override fun onBackPressed() {
        if (page == 2) {
            loginViewModel.otp.value= ""
            page = 1
            binding.layoutEmailId.visibility= View.VISIBLE
            binding.layoutOtp.visibility= View.GONE
        } else {
            super.onBackPressed()
        }
    }

}