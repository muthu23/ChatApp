package com.dubaipolice.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.dubaipolice.api.ApiClient
import com.dubaipolice.datasource.VideoStreamDataSource
import com.dubaipolice.model.*
import com.dubaipolice.repository.Repository
import com.dubaipolice.wrapper.Resource
import kotlinx.coroutines.flow.Flow


class VideoStreamViewModel : ViewModel() {

    var groupId = MutableLiveData<String>()
    var organizationId = MutableLiveData<String>()

    private var repository: Repository = Repository()

    data class Link(val link: String)


    fun stopStream(hlsLink: HLSLink) : MutableLiveData<Resource<CommonResponse?>> {
        return repository.stopStream(hlsLink)
    }

}