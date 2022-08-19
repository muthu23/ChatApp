package com.dubaipolice.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dubaipolice.model.CommonResponse
import com.dubaipolice.repository.Repository
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.wrapper.Resource
import com.google.gson.JsonObject

class ChatActivityViewModel: ViewModel()  {

    private var parameter: JsonObject? = null
    private var repository: Repository = Repository()

    var groupId = MutableLiveData<String>()

    private var sendCallNotificationResponseLiveData: MutableLiveData<Resource<CommonResponse?>>? = null


    val sendCallNotificationResponse: MutableLiveData<Resource<CommonResponse?>>?
        get() {

            try {
                parameter = JsonObject()
                //parameter!!.addProperty(AppConstants.FIELD.TITLE, System.currentTimeMillis().toString() + "-Test")
                parameter!!.addProperty(AppConstants.FIELD.GROUP_ID, groupId.value)
                Log.d("Parameter", parameter.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (sendCallNotificationResponseLiveData == null) {
                sendCallNotificationResponseLiveData = MutableLiveData<Resource<CommonResponse?>>()
            }
            sendCallNotificationResponseLiveData = repository.sendCallNotification(parameter!!)
            return sendCallNotificationResponseLiveData
        }

}