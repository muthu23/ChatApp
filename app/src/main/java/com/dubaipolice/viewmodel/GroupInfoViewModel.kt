package com.dubaipolice.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dubaipolice.MainApplication
import com.dubaipolice.db.AppDatabase
import com.dubaipolice.db.model.MemberInfoTable
import com.dubaipolice.model.CommonResponse
import com.dubaipolice.model.GroupInfoResponse
import com.dubaipolice.model.UpdateGroupResponse
import com.dubaipolice.repository.Repository
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.wrapper.Resource
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class GroupInfoViewModel : ViewModel() {

    private var parameter: JsonObject? = null
    var mute = MutableLiveData<Boolean>()

    var groupId = MutableLiveData<String>()
    var userId = MutableLiveData<String>()

    var groupName = MutableLiveData<String>()
    var groupAlertLevel = MutableLiveData<String>()
    var groupLogo = MutableLiveData<File>()

    private var repository: Repository = Repository()
    private var groupInfoResponseLiveData: MutableLiveData<Resource<GroupInfoResponse?>>? = null
    private var deleteGroupMemberResponseLiveData: MutableLiveData<Resource<CommonResponse?>>? =
        null

    private var muteGroupResponseLiveData: MutableLiveData<Resource<CommonResponse?>>? = null
    private var updateGroupResponseLiveData: MutableLiveData<Resource<UpdateGroupResponse?>>? = null

    val groupInfoResponse: MutableLiveData<Resource<GroupInfoResponse?>>?
        get() {

            if (groupInfoResponseLiveData == null) {
                groupInfoResponseLiveData = MutableLiveData<Resource<GroupInfoResponse?>>()
            }
            groupInfoResponseLiveData = repository.getGroupInfo(groupId.value)
            return groupInfoResponseLiveData
        }


    val deleteGroupMemberResponse: MutableLiveData<Resource<CommonResponse?>>?
        get() {

            if (deleteGroupMemberResponseLiveData == null) {
                deleteGroupMemberResponseLiveData = MutableLiveData<Resource<CommonResponse?>>()
            }
            deleteGroupMemberResponseLiveData =
                repository.deleteGroupMember(groupId.value, userId.value)
            return deleteGroupMemberResponseLiveData
        }


   fun getGroupMemberLiveData(groupId: String): LiveData<List<MemberInfoTable>>? {
         var membersList: LiveData<List<MemberInfoTable>>? = MutableLiveData()
        viewModelScope.launch {
            membersList = AppDatabase.getAppDatabase(MainApplication.appContext)
                ?.memberInfoTableDao()
                ?.getAll(groupId)
        }
        return membersList
    }

    fun getGroupMemberList(groupId: String): List<MemberInfoTable>? {
        var membersList: List<MemberInfoTable>? = emptyList()
        viewModelScope.launch {
            membersList = AppDatabase.getAppDatabase(MainApplication.appContext)
                ?.memberInfoTableDao()
                ?.getAllMembers(groupId)
        }
        return membersList
    }

    val muteGroupResponse: MutableLiveData<Resource<CommonResponse?>>?
        get() {
            try {
                parameter = JsonObject()
                parameter!!.addProperty(AppConstants.FIELD.MUTE, mute.value)
                Log.d("Parameter", parameter.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (muteGroupResponseLiveData == null) {
                muteGroupResponseLiveData = MutableLiveData<Resource<CommonResponse?>>()
            }
            muteGroupResponseLiveData = repository.muteGroup(groupId.value, parameter)
            return muteGroupResponseLiveData
        }

    val updateGroupResponse: MutableLiveData<Resource<UpdateGroupResponse?>>?
        get() {

            if (updateGroupResponseLiveData == null) {
                updateGroupResponseLiveData = MutableLiveData<Resource<UpdateGroupResponse?>>()
            }
            updateGroupResponseLiveData = repository.updateGroup(
                groupId.value!!,
                groupName.value,
                groupAlertLevel.value,
                groupLogo.value
            )
            return updateGroupResponseLiveData
        }

    fun getGroupMemberLocation(groupId: String): LiveData<List<MemberInfoTable>>? {
        var membersList: LiveData<List<MemberInfoTable>>? = MutableLiveData()
        viewModelScope.launch {
            membersList = AppDatabase.getAppDatabase(MainApplication.appContext)
                ?.memberInfoTableDao()
                ?.getAllMembersLocation(groupId)
        }
        return membersList
    }

}