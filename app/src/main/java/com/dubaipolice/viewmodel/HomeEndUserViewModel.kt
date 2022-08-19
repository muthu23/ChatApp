package com.dubaipolice.viewmodel

import android.os.Build
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dubaipolice.model.CommonResponse
import com.dubaipolice.model.ForceUpgradeResponse
import com.dubaipolice.model.GetAllCmsListResponse
import com.dubaipolice.model.LoginResponse
import com.dubaipolice.repository.Repository
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.wrapper.Resource
import com.google.gson.JsonObject

class HomeEndUserViewModel: ViewModel()  {

    private var parameter: JsonObject? = null
    private var repository: Repository = Repository()

    var fcmToken = MutableLiveData<String>()
    var groupIds = MutableLiveData<String>()

    private var updateFcmTokenResponseLiveData: MutableLiveData<Resource<CommonResponse?>>? = null
    private var forceUpgradeResponseLiveData: MutableLiveData<Resource<ForceUpgradeResponse?>>? = null
    private var getCmsResponse: MutableLiveData<Resource<GetAllCmsListResponse?>>? = null
    private var logoutResponseLiveData: MutableLiveData<Resource<CommonResponse?>>? = null

    val updateFcmTokenResponse: MutableLiveData<Resource<CommonResponse?>>?
        get() {

            try {
                parameter = JsonObject()
                parameter!!.addProperty(AppConstants.FIELD.FCM_TOKEN, fcmToken.value)
                Log.d("Parameter", parameter.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (updateFcmTokenResponseLiveData == null) {
                updateFcmTokenResponseLiveData = MutableLiveData<Resource<CommonResponse?>>()
            }
            updateFcmTokenResponseLiveData = repository.updateFcmToken(parameter)
            return updateFcmTokenResponseLiveData
        }
    val allCmsResponse: MutableLiveData<Resource<GetAllCmsListResponse?>>?
        get() {
            if (getCmsResponse == null) {
                getCmsResponse = MutableLiveData<Resource<GetAllCmsListResponse?>>()
            }
            getCmsResponse = repository.getAllCms()
            return getCmsResponse
        }

    val forceUpgradeAppResponse: MutableLiveData<Resource<ForceUpgradeResponse?>>?
        get() {

            if (forceUpgradeResponseLiveData == null) {
                forceUpgradeResponseLiveData = MutableLiveData<Resource<ForceUpgradeResponse?>>()
            }
            forceUpgradeResponseLiveData = repository.forceUpgradeApp()
            return forceUpgradeResponseLiveData
        }


    private var helpNotificationResponseLiveData: MutableLiveData<Resource<CommonResponse?>>? = null

    val sendHelpNotificationResponse: MutableLiveData<Resource<CommonResponse?>>?
        get() {
            try {
                parameter = JsonObject()
                parameter!!.addProperty(AppConstants.FIELD.GROUP_IDS, groupIds.value)
                Log.d("Parameter", parameter.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (helpNotificationResponseLiveData == null) {
                helpNotificationResponseLiveData = MutableLiveData<Resource<CommonResponse?>>()
            }
            helpNotificationResponseLiveData = repository.sendHelpNotification(parameter)
            return helpNotificationResponseLiveData
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