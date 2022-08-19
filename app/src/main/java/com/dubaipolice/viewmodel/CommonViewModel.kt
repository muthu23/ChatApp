package com.dubaipolice.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dubaipolice.model.*
import com.dubaipolice.repository.Repository
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.wrapper.Resource
import com.google.gson.JsonObject

class CommonViewModel : ViewModel() {

    private var parameter: JsonObject? = null
    private var repository: Repository = Repository()

    var passcode = MutableLiveData<String>()

    private var verifyTokenResponseLiveData: MutableLiveData<Resource<VerifyTokenResponse?>>? = null
    private var verifyPasscodeResponseLiveData: MutableLiveData<Resource<CommonResponse?>>? = null
    private var getCmsResponse: MutableLiveData<Resource<GetAllCmsListResponse?>>? = null
    private var logoutResponseLiveData: MutableLiveData<Resource<CommonResponse?>>? = null

    val verifyTokenResponse: MutableLiveData<Resource<VerifyTokenResponse?>>?
        get() {

            if (verifyTokenResponseLiveData == null) {
                verifyTokenResponseLiveData = MutableLiveData<Resource<VerifyTokenResponse?>>()
            }
            verifyTokenResponseLiveData = repository.verifyToken()
            return verifyTokenResponseLiveData
        }

    val verifyPasscodeResponse: MutableLiveData<Resource<CommonResponse?>>?
        get() {

            try {
                parameter = JsonObject()
                parameter!!.addProperty(AppConstants.FIELD.PASSCODE, passcode.value)
                Log.d("Parameter", parameter.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (verifyPasscodeResponseLiveData == null) {
                verifyPasscodeResponseLiveData = MutableLiveData<Resource<CommonResponse?>>()
            }
            verifyPasscodeResponseLiveData = repository.verifyPasscode(parameter!!)
            return verifyPasscodeResponseLiveData
        }

    val allCmsResponse: MutableLiveData<Resource<GetAllCmsListResponse?>>?
        get() {
            if (getCmsResponse == null) {
                getCmsResponse = MutableLiveData<Resource<GetAllCmsListResponse?>>()
            }
            getCmsResponse = repository.getAllCms()
            return getCmsResponse
        }

    val logoutResponse: MutableLiveData<Resource<CommonResponse?>>?
        get() {

            if (logoutResponseLiveData == null) {
                logoutResponseLiveData = MutableLiveData<Resource<CommonResponse?>>()
            }
            logoutResponseLiveData = repository.logout(SharedPref.readInt(AppConstants.KEY_USER_ID).toString())
            return logoutResponseLiveData
        }

}