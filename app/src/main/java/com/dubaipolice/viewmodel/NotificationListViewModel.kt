package com.dubaipolice.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dubaipolice.api.ApiClient
import com.dubaipolice.datasource.LiveStreamListDataSource
import com.dubaipolice.datasource.NotificationListDataSource
import com.dubaipolice.datasource.VideoStreamDataSource
import com.dubaipolice.model.*
import com.dubaipolice.repository.Repository
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.wrapper.Resource
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow


class NotificationListViewModel : ViewModel() {

    var groupId = MutableLiveData<String>()
    var notificationId = MutableLiveData<Int>()
     var isRead = MutableLiveData<Int>()
    var organizationId = MutableLiveData<String>()
    private var parameter: JsonObject? = null

    private var repository: Repository = Repository()
    private var notificationReadResponseLiveData: MutableLiveData<Resource<CommonResponse?>>? = null
    private var readAllNotificationResponseLiveData: MutableLiveData<Resource<CommonResponse?>>? = null

    val notificationReadResponse: MutableLiveData<Resource<CommonResponse?>>?
        get() {
            try {
                parameter = JsonObject()
                parameter!!.addProperty(AppConstants.FIELD.ISREAD, isRead.value)
                Log.d("Parameter", parameter.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (notificationReadResponseLiveData == null) {
                notificationReadResponseLiveData = MutableLiveData<Resource<CommonResponse?>>()
            }
            notificationReadResponseLiveData = repository.readNotification(parameter,notificationId.value)
            return notificationReadResponseLiveData
        }
    val readAllNotificationResponse: MutableLiveData<Resource<CommonResponse?>>?
        get() {
            try {
                parameter = JsonObject()
                parameter!!.addProperty(AppConstants.FIELD.ISREAD, isRead.value)
                Log.d("Parameter", parameter.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (readAllNotificationResponseLiveData == null) {
                readAllNotificationResponseLiveData = MutableLiveData<Resource<CommonResponse?>>()
            }
            readAllNotificationResponseLiveData = repository.readAllNotification(parameter)
            return readAllNotificationResponseLiveData
        }

    fun getNotificationListFlow(): Flow<PagingData<Notifications>> {
        val config = PagingConfig(pageSize = 10,enablePlaceholders = true)
        val datasource = NotificationListDataSource(ApiClient.request!!, groupId.value)
        return Pager(config = config, pagingSourceFactory = {datasource} ).flow
    }

}