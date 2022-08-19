package com.dubaipolice.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dubaipolice.api.ApiClient
import com.dubaipolice.datasource.MediaDataSource
import com.dubaipolice.model.Attachment
import com.dubaipolice.repository.Repository
import kotlinx.coroutines.flow.Flow

class GroupAttachmentsViewModel : ViewModel() {

    var groupId = MutableLiveData<String>()
    lateinit var attachmentType: String

    private var repository: Repository = Repository()

    fun getMediaFlow(): Flow<PagingData<Attachment>> {
        val config = PagingConfig(pageSize = 10, enablePlaceholders = true)
        val datasource = MediaDataSource(ApiClient.request!!, groupId.value, attachmentType)
        return Pager(config = config, pagingSourceFactory = { datasource }).flow
    }

    fun getDocsFlow(): Flow<PagingData<Attachment>> {
        val config = PagingConfig(pageSize = 10, enablePlaceholders = true)
        val datasource = MediaDataSource(ApiClient.request!!, groupId.value, attachmentType)
        return Pager(config = config, pagingSourceFactory = { datasource }).flow
    }
}