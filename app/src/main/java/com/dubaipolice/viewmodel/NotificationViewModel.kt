package com.dubaipolice.viewmodel

import android.os.Build
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dubaipolice.BuildConfig
import com.dubaipolice.model.CommonResponse
import com.dubaipolice.model.LoginResponse
import com.dubaipolice.repository.Repository
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.wrapper.Resource
import com.google.gson.JsonObject

class NotificationViewModel : ViewModel() {

    private var parameter: JsonObject? = null
    private var repository: Repository = Repository()

    var soundFile = MutableLiveData<String>()
    var showNotification = MutableLiveData<Boolean>()

    private var notificationSoundResponseLiveData: MutableLiveData<Resource<CommonResponse?>>? = null
    private var notificationShowResponseLiveData: MutableLiveData<Resource<CommonResponse?>>? = null

    val notificationUpdateResponse: MutableLiveData<Resource<CommonResponse?>>?
        get() {
            try {
                parameter = JsonObject()
                parameter!!.addProperty(AppConstants.FIELD.SOUND_FILE, soundFile.value)
                Log.d("Parameter", parameter.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (notificationSoundResponseLiveData == null) {
                notificationSoundResponseLiveData = MutableLiveData<Resource<CommonResponse?>>()
            }
            notificationSoundResponseLiveData = repository.updateNotificationSound(parameter)
            return notificationSoundResponseLiveData
        }
    val notificationShowResponse: MutableLiveData<Resource<CommonResponse?>>?
        get() {
            try {
                parameter = JsonObject()
                parameter!!.addProperty(AppConstants.FIELD.NOTIFICATION, showNotification.value)
                Log.d("Parameter", parameter.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (notificationShowResponseLiveData == null) {
                notificationShowResponseLiveData = MutableLiveData<Resource<CommonResponse?>>()
            }
            notificationShowResponseLiveData = repository.showNotification(parameter)
            return notificationShowResponseLiveData
        }


}
