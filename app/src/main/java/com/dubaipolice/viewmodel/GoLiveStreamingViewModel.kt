package com.dubaipolice.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dubaipolice.model.GoLiveStreamingResponse
import com.dubaipolice.repository.Repository
import com.dubaipolice.wrapper.Resource

class GoLiveStreamingViewModel: ViewModel()  {

    private var repository: Repository = Repository()

    var groupId = MutableLiveData<String>()

    private var goLiveStreamingResponseLiveData: MutableLiveData<Resource<GoLiveStreamingResponse?>>? = null
    private var stopLiveStreamingResponseLiveData: MutableLiveData<Resource<GoLiveStreamingResponse?>>? = null

    val goLiveStreamingResponse: MutableLiveData<Resource<GoLiveStreamingResponse?>>?
        get() {

            if (goLiveStreamingResponseLiveData == null) {
                goLiveStreamingResponseLiveData = MutableLiveData<Resource<GoLiveStreamingResponse?>>()
            }
            goLiveStreamingResponseLiveData = repository.goLiveStreaming(groupId.value)
            return goLiveStreamingResponseLiveData
        }

    val stopLiveStreamingResponse: MutableLiveData<Resource<GoLiveStreamingResponse?>>?
        get() {

            if (stopLiveStreamingResponseLiveData == null) {
                stopLiveStreamingResponseLiveData = MutableLiveData<Resource<GoLiveStreamingResponse?>>()
            }
            stopLiveStreamingResponseLiveData = repository.stopLiveStreaming(groupId.value)
            return stopLiveStreamingResponseLiveData
        }

}