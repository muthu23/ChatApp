package com.dubaipolice.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dubaipolice.MainApplication
import com.dubaipolice.db.AppDatabase
import com.dubaipolice.db.model.GroupInfoTable
import com.dubaipolice.model.GroupListResponse
import com.dubaipolice.repository.Repository
import com.dubaipolice.wrapper.Resource
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

     var isViewDestroy = false

    private var groupList: LiveData<List<GroupInfoTable>>? = null

    private var repository: Repository = Repository()

    private var groupListResponseLiveData: MutableLiveData<Resource<GroupListResponse?>>? = null

    val groupListResponse: MutableLiveData<Resource<GroupListResponse?>>?
        get() {
            if (groupListResponseLiveData == null) {
                groupListResponseLiveData = MutableLiveData<Resource<GroupListResponse?>>()
            }
            groupListResponseLiveData = repository.getGroupList()
            return groupListResponseLiveData
        }

    fun getGroupList(): LiveData<List<GroupInfoTable>>? {
        if (groupList == null) {
            groupList = AppDatabase.getAppDatabase(MainApplication.appContext)
                ?.groupInfoTableDao()
                ?.getAllLiveData()
        }
        return groupList
    }

    fun getAllGroupList(): List<GroupInfoTable>? {
        var grpList: List<GroupInfoTable>? = emptyList()
        viewModelScope.launch {
            grpList = AppDatabase.getAppDatabase(MainApplication.appContext)
                ?.groupInfoTableDao()
                ?.getAll()
            return@launch
        }
        return grpList
    }

    fun deleteGroupList(deletedGrpList: List<GroupInfoTable>) {
        viewModelScope.launch {
            for (deletedGrp in deletedGrpList) {
                MainApplication.connection?.unJoinAndRemoveFromMultiUserChatList(deletedGrp.groupJid.toString())
                AppDatabase.getAppDatabase(MainApplication.appContext)
                    ?.groupInfoTableDao()
                    ?.deleteGroup(deletedGrp.groupJid.toString())

                AppDatabase.getAppDatabase(MainApplication.appContext)
                    ?.messageInfoTableDao()
                    ?.deleteChatForDeletedGroup(deletedGrp.groupJid.toString())

                AppDatabase.getAppDatabase(MainApplication.appContext)
                    ?.memberInfoTableDao()
                    ?.deleteMemberForDeletedGroup(deletedGrp.groupJid.toString())
            }
        }
    }

    fun insertGroupList(newGrpList: List<GroupInfoTable>) {
        viewModelScope.launch {
            for (newGrp in newGrpList) {
                AppDatabase.getAppDatabase(MainApplication.appContext)
                    ?.groupInfoTableDao()
                    ?.insert(newGrp)
                MainApplication.connection?.addToMultiUserChatList(
                    newGrp.groupJid.toString()
                )
            }
        }
    }

    fun updateGroupList(updatedGrpList: List<GroupInfoTable>) {
        viewModelScope.launch {
            for (updatedGrp in updatedGrpList) {
                AppDatabase.getAppDatabase(MainApplication.appContext)
                    ?.groupInfoTableDao()
                    ?.updateGroupDataFromHome(
                        updatedGrp.imageUrl,
                        updatedGrp.groupName,
                        updatedGrp.alertLevel!!,
                        updatedGrp.callActive,
                        updatedGrp.groupActiveStatus,
                        updatedGrp.groupJid!!
                    )
            }

        }
    }

}