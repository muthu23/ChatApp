package com.dubaipolice.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dubaipolice.model.CommonResponse
import com.dubaipolice.model.ProfileResponse
import com.dubaipolice.repository.Repository
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.wrapper.Resource
import com.google.gson.JsonObject
import java.io.File

class ProfileViewModel : ViewModel() {

    private var repository: Repository = Repository()

    var filePath = MutableLiveData<File>()
    var languageId = MutableLiveData<Int>()

    private var updateProfilePicResponseLiveData: MutableLiveData<Resource<ProfileResponse?>>? = null
    private var updateLanguageResponseLiveData: MutableLiveData<Resource<ProfileResponse?>>? = null

    val updateProfilePicResponse: MutableLiveData<Resource<ProfileResponse?>>?
        get() {

            if (updateProfilePicResponseLiveData == null) {
                updateProfilePicResponseLiveData = MutableLiveData<Resource<ProfileResponse?>>()
            }
            updateProfilePicResponseLiveData = repository.updateProfile(filePath.value)
            return updateProfilePicResponseLiveData
        }

    val updateLanguageResponse: MutableLiveData<Resource<ProfileResponse?>>?
        get() {

            if (updateLanguageResponseLiveData == null) {
                updateLanguageResponseLiveData = MutableLiveData<Resource<ProfileResponse?>>()
            }
            updateLanguageResponseLiveData = repository.updateLanguage(languageId.value)
            return updateLanguageResponseLiveData
        }

}