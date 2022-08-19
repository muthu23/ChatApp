package com.dubaipolice.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dubaipolice.api.ApiClient
import com.dubaipolice.datasource.LiveStreamListDataSource
import com.dubaipolice.datasource.VideoStreamDataSource
import com.dubaipolice.model.HLSLinkResponse
import com.dubaipolice.model.Stream
import com.dubaipolice.model.VideoItem
import com.dubaipolice.repository.Repository
import com.dubaipolice.wrapper.Resource
import kotlinx.coroutines.flow.Flow


class StreamListViewModel : ViewModel() {

    var groupId = MutableLiveData<String>()
    var organizationId = MutableLiveData<String>()

    private var repository: Repository = Repository()

    data class Link(val link: String)

    fun getVideoStreamFlow(): Flow<PagingData<VideoItem>> {
        val config = PagingConfig(pageSize = 10,enablePlaceholders = true)
        val datasource = VideoStreamDataSource(ApiClient.request!!, groupId.value)
       return Pager(config = config, pagingSourceFactory = {datasource} ).flow
    }


    fun getHLSLink(media: VideoItem) : MutableLiveData<Resource<HLSLinkResponse?>> {
        val link = Link(media.rts_link)
        return repository.getHLSLink(link)
    }

    fun getLiveStreamFlow(): Flow<PagingData<Stream>> {
        val config = PagingConfig(pageSize = 10,enablePlaceholders = true)
        val datasource = LiveStreamListDataSource(ApiClient.request!!, groupId.value)
        return Pager(config = config, pagingSourceFactory = {datasource} ).flow
    }

}