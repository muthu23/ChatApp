package com.dubaipolice.view.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dubaipolice.MainApplication
import com.dubaipolice.R
import com.dubaipolice.callback.HandleClick
import com.dubaipolice.databinding.FragmentChatBinding
import com.dubaipolice.db.AppDatabase
import com.dubaipolice.db.model.GroupInfoTable
import com.dubaipolice.db.model.MessageInfoTable
import com.dubaipolice.model.GroupListResponse.Data.GroupDetails
import com.dubaipolice.utils.MyProgressDialog
import com.dubaipolice.utils.Utils
import com.dubaipolice.view.activity.HomeEndUserActivity
import com.dubaipolice.view.activity.NotificationActivity
import com.dubaipolice.view.adapter.GroupAdapter
import com.dubaipolice.viewmodel.ChatViewModel
import com.dubaipolice.wrapper.Resource
import com.dubaipolice.xmpp.XmppGroupCallback
import java.util.*


class ChatFragment : Fragment(), HandleClick, XmppGroupCallback {

    private lateinit var chatViewModel: ChatViewModel
    private lateinit var binding: FragmentChatBinding
    private lateinit var mContext: Context

    private var groupDbList: ArrayList<GroupInfoTable> = ArrayList()
    private var groupLocalDbList: List<GroupInfoTable> = emptyList<GroupInfoTable>().toMutableList()

    private var groupList: ArrayList<GroupInfoTable> = ArrayList()
    private var groupAdapter: GroupAdapter? = null

    private var refreshBroadcastReceiver: BroadcastReceiver? = null

    var isGroupListInitialized= false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //ThemeUtility.setStatusBarThemeColor(activity!!.window)

        super.onViewCreated(view, savedInstanceState)

        mContext = binding.root.context

        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        binding.lifecycleOwner = this@ChatFragment
        //binding.setHomeViewModel(homeViewModel)
        binding.clickHandle = this
        chatViewModel.isViewDestroy = false
        //ThemeUtility.setGradientThemeColor(binding.toolbar, 0f)

        // getGroupListFromDB()

        //  loadGroupList()

        getGroupsFromAPI()

        initRecyclerView()

        receiveMessage()

    }

    private fun getGroupsFromAPI() {
        MyProgressDialog.show(mContext)
        chatViewModel.groupListResponse!!.observe(viewLifecycleOwner) { response ->
            when (response.status) {
                Resource.Status.SUCCESS -> {
                    if (response.data!!.isSuccess) {
                        updateAndInsertGroup(response.data.data!!.groupDetails)
                    } else {
                        loadGroupLiveData()
                    }
                }
                Resource.Status.ERROR -> {
                    loadGroupLiveData()
                }
                Resource.Status.LOADING -> {

                }
            }
        }
    }

    private fun updateAndInsertGroup(onlineGrpList: ArrayList<GroupDetails>?) {
        val localGrpList = chatViewModel.getAllGroupList()

        val newGrpList =
            onlineGrpList!!.filter { it.groupJid !in localGrpList!!.map { item -> item.groupJid } }

        val removedGrpList =
            localGrpList.let { local -> local?.filter { it.groupJid !in onlineGrpList.map { item -> item.groupJid } } }

        val updatedGrpList = onlineGrpList.asSequence().filter { online ->
            localGrpList!!.none { local ->
                local.groupName == online.groupName
                        && local.alertLevel == online.alertLevel
                        && local.imageUrl == online.image
                        && local.callActive == online.callActive
                        && local.groupActiveStatus == online.groupActiveStatus
            }
        }.toList()

        Log.d("updatedGrpList",updatedGrpList.size.toString())

        removedGrpList?.let { chatViewModel.deleteGroupList(it) } // Deleting removed groups
        chatViewModel.updateGroupList(createListGroup(updatedGrpList)) // Updating  groups
        chatViewModel.insertGroupList(createListGroup(newGrpList)) // Inserting new groups


        loadGroupLiveData()

    }

    private fun createListGroup(onlineGrpList: List<GroupDetails>): List<GroupInfoTable> {
        val newGrpList = emptyList<GroupInfoTable>().toMutableList()
        for (groups in onlineGrpList) {
            val groupInfo = GroupInfoTable()
            groupInfo.groupId = groups.groupId
            groupInfo.groupJid = groups.groupJid
            groupInfo.imageUrl = groups.image
            groupInfo.groupName = groups.groupName
            groupInfo.alertLevel = groups.alertLevel
            groupInfo.callActive = groups.callActive
            groupInfo.groupActiveStatus = groups.groupActiveStatus
            groupInfo.createdTimestamp = groups.createdAt
            groupInfo.updatedTimestamp = groups.updatedAt
            newGrpList.add(groupInfo)
        }
        return newGrpList
    }


    /**
     * This function is used to filter the data using search text box
     */
    private fun addFilterToSearchView() {
        binding.groupSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                groupAdapter!!.filter(query.toString().lowercase(Locale.getDefault()))

                return false
            }
        })
    }

    private fun loadGroupList() {

        MyProgressDialog.show(mContext)

        Log.e("GroupList", "Success")

        if (Utils.isNetConnected(mContext)) {

            Log.e("GroupList2", "Success")

            chatViewModel.groupListResponse!!.observe(
                viewLifecycleOwner
            ) { groupListResponseResource ->
                when (groupListResponseResource.status) {
                    Resource.Status.SUCCESS -> {
                        if (groupListResponseResource.data!!.isSuccess) {

                            Log.e("GroupSizes", "Success")

                            groupListResponseResource.data.data?.groupDetails?.let {
                                Log.e("GroupSizess", "not null")
                                if (it.size > 0) {
                                    val groupJidList = ArrayList<String>()
                                    it.forEachIndexed { index, response ->
                                        println("$response at $index")

                                        if (response.groupJid != null) {

                                            groupJidList.add(response.groupJid!!)

                                            if (groupDbList.isNotEmpty()) {

                                                var isFound = false

                                                groupDbList.forEachIndexed innerLoop@{ i, localdb ->

                                                    println("$localdb at $i")

                                                    Log.e("LocalJid", localdb.groupJid!!)
                                                    Log.e("ResponseJid", response.groupJid!!)
                                                    if (localdb.groupJid == response.groupJid) {
                                                        Log.e("Foundwy", "Foundwy")
                                                        isFound = true
                                                        return@innerLoop
                                                    }
                                                }

                                                if (isFound) {
                                                    Log.e("ADDINGDB", "FoundUpdate")
                                                    //if group id founds then update
                                                    MainApplication.connection?.updateGroupDb(
                                                        response.image,
                                                        response.groupName,
                                                        response.alertLevel!!,
                                                        response.groupJid!!
                                                    )
                                                } else {
                                                    Log.e("ADDINGDB", "NotFoundAdd")
                                                    addToGroupDb(response)
                                                    MainApplication.connection?.addToMultiUserChatList(
                                                        response.groupJid!!
                                                    )
                                                }

                                            } else {
                                                addToGroupDb(response)
                                                MainApplication.connection?.addToMultiUserChatList(
                                                    response.groupJid!!
                                                )
                                            }

                                        }

                                    }

                                    //remove group if not received from api
                                    if (groupDbList.isNotEmpty()) {

                                        groupDbList.forEachIndexed innerLoop@{ index, localdb ->

                                            println("$localdb at $index")

                                            var isFound = false

                                            groupJidList.forEachIndexed jidLoop@{ indexInner, groupJid ->

                                                println("$groupJid at $indexInner")

                                                if (localdb.groupJid == groupJid) {
                                                    isFound = true
                                                    return@jidLoop
                                                }

                                            }

                                            if (!isFound) {
                                                MainApplication.connection?.unJoinAndRemoveFromMultiUserChatList(
                                                    localdb.groupJid!!
                                                )
                                                MainApplication.connection?.removeGroupFromDb(
                                                    localdb.groupJid!!
                                                )
                                            }

                                        }

                                    }

                                }

                            }
                            loadGroupLiveData()
                        } else {
                            loadGroupLiveData()
                            Utils.showToast(mContext, groupListResponseResource.data.message!!)
                        }
                    }
                    Resource.Status.LOADING -> {
                    }
                    Resource.Status.ERROR -> {
                        Log.e("ERror", "")
                        Utils.showToast(mContext, groupListResponseResource.message!!)
                        loadGroupLiveData()
                    }
                }
            }

        } else {
            Log.e("GroupList3", "Success")
            loadGroupLiveData()
        }

    }

    private fun addToGroupDb(response: GroupDetails) {

        Log.e("AddToGroup", "")

        val groupInfoTable = GroupInfoTable()
        groupInfoTable.groupId = response.groupId
        groupInfoTable.groupJid = response.groupJid
        groupInfoTable.imageUrl = response.image
        groupInfoTable.groupName = response.groupName
        groupInfoTable.alertLevel = response.alertLevel
        groupInfoTable.createdTimestamp = response.createdAt
        groupInfoTable.updatedTimestamp = response.updatedAt

        //groupList.add(groupInfoTable)

        MainApplication.connection?.addGroupToDb(groupInfoTable)

    }

    private fun loadGroupLiveData() {
        if(!isGroupListInitialized)
        {
            isGroupListInitialized= true
            chatViewModel.getGroupList()!!
                .observe(viewLifecycleOwner) { groupInfoTable ->
                    groupAdapter!!.setGroupList(groupInfoTable)
                }
            addFilterToSearchView()
        }
        MyProgressDialog.dismiss()

    }

    private fun receiveMessage() {
        MainApplication.connection?.setXmppCallbackForGroups(this)
    }

    private fun getGroupListFromDB() {
        val myGroupDbList = AppDatabase.getAppDatabase(MainApplication.appContext)
            ?.groupInfoTableDao()
            ?.getAll()

        if (myGroupDbList != null && myGroupDbList.isNotEmpty()) {
            groupDbList.addAll(myGroupDbList.toMutableList())
        }

    }

    private fun initRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(
            mContext,
            LinearLayoutManager.VERTICAL,
            false
        )

        groupAdapter = GroupAdapter(mContext, groupList)
        binding.recyclerView.adapter = groupAdapter
    }

    override fun clickHandle(v: View) {
        when (v.id) {
            R.id.imgMenu -> {
                //MainApplication.connection!!.getRoaster()

                //MainApplication.connection!!.sendMessageOneToOne("firoz@dbchatdev.iworklab.com", false)
                //MainApplication.connection!!.sendMessageOneToOne("firoznew1645687895547@dbchatdev.iworklab.com", false)

                val activity: HomeEndUserActivity? = activity as HomeEndUserActivity?
                activity?.openDrawer()

                // It will generate 6 digit random Number.
                // from 0 to 999999

                // It will generate 6 digit random Number.
                // from 0 to 999999
                /*val rnd = Random()
                val number: Int = rnd.nextInt(999999)

                // this will convert any number sequence into 6 character.

                // this will convert any number sequence into 6 character.
                var msg = String.format("%06d", number)
                MainApplication.connection?.updateLastMessageInGroup(msg, System.currentTimeMillis().toString(), "open@conference.n1.iworklab.com")
*/
            }

            R.id.imgNotification -> {

                startActivity(Intent(mContext, NotificationActivity::class.java))

            }

        }
    }

    override fun receivedMessage(message: MessageInfoTable) {
    }

    override fun sentMessage(message: MessageInfoTable) {

    }

    override fun groupUpdated(isUpdated: Boolean) {
        val handler = Handler(Looper.getMainLooper())
        if (!chatViewModel.isViewDestroy && isUpdated) {
            handler.post {getGroupsFromAPI()}
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        chatViewModel.isViewDestroy = true
    }


}