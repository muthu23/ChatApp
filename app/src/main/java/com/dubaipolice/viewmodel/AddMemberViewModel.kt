package com.dubaipolice.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.dubaipolice.api.ApiClient
import com.dubaipolice.datasource.MembersDataSource
import com.dubaipolice.model.AddMembers
import com.dubaipolice.model.CommonResponse
import com.dubaipolice.model.UserListResponse
import com.dubaipolice.model.Users
import com.dubaipolice.repository.Repository
import com.dubaipolice.wrapper.Resource


class AddMemberViewModel : ViewModel() {

    var groupId = MutableLiveData<String>()
    var organizationId = MutableLiveData<String>()
    var search = MutableLiveData<String>()
    var addMembers = MutableLiveData<AddMembers>()

    private var repository: Repository = Repository()

    private var userListResponseLiveData: MutableLiveData<Resource<UserListResponse?>>? = null
    private var adduserListResponseLiveData: MutableLiveData<Resource<CommonResponse?>>? = null


    val userListResponse: MutableLiveData<Resource<UserListResponse?>>?
        get() {
            if (userListResponseLiveData == null) {
                userListResponseLiveData = MutableLiveData<Resource<UserListResponse?>>()
            }
            userListResponseLiveData = repository.getAllMember(groupId.value, organizationId.value)
            return userListResponseLiveData
        }

    val adduserListResponse: MutableLiveData<Resource<CommonResponse?>>?
        get() {
            if (adduserListResponseLiveData == null) {
                adduserListResponseLiveData = MutableLiveData<Resource<CommonResponse?>>()
            }
            adduserListResponseLiveData = repository.addMemberList(addMembers.value!!)
            return adduserListResponseLiveData
        }


    fun getMembersLivedata(): LiveData<PagingData<Users>> {
        val config = PagingConfig(pageSize = 10,enablePlaceholders = true)
       return Transformations.switchMap(search) { query ->
            if (query.isNullOrEmpty()) {
                val datasource = MembersDataSource(ApiClient.request!!, groupId.value, organizationId.value, null)
                Pager(config = config, pagingSourceFactory = { datasource }).liveData
            } else {
                val datasource = MembersDataSource(ApiClient.request!!, groupId.value, organizationId.value, query)
                Pager(config = config, pagingSourceFactory = { datasource }).liveData
            }
        }
    }

}