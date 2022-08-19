package com.dubaipolice.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dubaipolice.model.CommonResponse
import com.dubaipolice.model.LoginResponse
import com.dubaipolice.model.SignupResponse
import com.dubaipolice.repository.Repository
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.wrapper.Resource
import com.google.gson.JsonObject

class PasscodeSetupViewModel : ViewModel() {

    private var parameter: JsonObject? = null
    private var repository: Repository = Repository()

    var oldPasscode = MutableLiveData<String>()
    var newPasscode = MutableLiveData<String>()

    private var passcodeResponseLiveData: MutableLiveData<Resource<CommonResponse?>>? = null

    val passcodeResponse: MutableLiveData<Resource<CommonResponse?>>?
        get() {

            try {
                parameter = JsonObject()
                parameter!!.addProperty(AppConstants.FIELD.OLD_PASSCODE, oldPasscode.value)
                parameter!!.addProperty(AppConstants.FIELD.NEW_PASSCODE, newPasscode.value)
                Log.d("Parameter", parameter.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (passcodeResponseLiveData == null) {
                passcodeResponseLiveData = MutableLiveData<Resource<CommonResponse?>>()
            }
            passcodeResponseLiveData = repository.createAndChangePasscode(parameter)
            return passcodeResponseLiveData
        }

}