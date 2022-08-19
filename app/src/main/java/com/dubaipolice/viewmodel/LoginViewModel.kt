package com.dubaipolice.viewmodel

import android.os.Build
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dubaipolice.BuildConfig
import com.dubaipolice.model.LoginResponse
import com.dubaipolice.repository.Repository
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.wrapper.Resource
import com.google.gson.JsonObject

class LoginViewModel : ViewModel() {

    private var parameter: JsonObject? = null
    private var repository: Repository = Repository()

    var email = MutableLiveData<String>()
    var otp = MutableLiveData<String>()

    private var loginResponseLiveData: MutableLiveData<Resource<LoginResponse?>>? = null
    private var verifyLoginResponseLiveData: MutableLiveData<Resource<LoginResponse?>>? = null

    val loginResponse: MutableLiveData<Resource<LoginResponse?>>?
        get() {

            try {
                parameter = JsonObject()
                parameter!!.addProperty(AppConstants.FIELD.EMAIL, email.value)
                Log.d("Parameter", parameter.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (loginResponseLiveData == null) {
                loginResponseLiveData = MutableLiveData<Resource<LoginResponse?>>()
            }
            loginResponseLiveData = repository.login(parameter)
            return loginResponseLiveData
        }

    val verifyLoginResponse: MutableLiveData<Resource<LoginResponse?>>?
        get() {
            try {
                parameter = JsonObject()
                parameter!!.addProperty(AppConstants.FIELD.EMAIL, email.value)
                parameter!!.addProperty(AppConstants.FIELD.OTP, otp.value)
                parameter!!.addProperty(AppConstants.FIELD.DEVICE_OS_TYPE, AppConstants.DEVICE_TYPE_ANDROID)
                parameter!!.addProperty(AppConstants.FIELD.DEVICE_OS_VERSION, Build.VERSION.SDK_INT.toString())
                Log.d("Parameter", parameter.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (verifyLoginResponseLiveData == null) {
                verifyLoginResponseLiveData = MutableLiveData<Resource<LoginResponse?>>()
            }
            verifyLoginResponseLiveData = repository.verifyLoginUsingOtp(parameter)
            return verifyLoginResponseLiveData
        }

}
