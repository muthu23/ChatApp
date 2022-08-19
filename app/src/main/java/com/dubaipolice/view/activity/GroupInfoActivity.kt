package com.dubaipolice.view.activity

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dubaipolice.MainApplication
import com.dubaipolice.R
import com.dubaipolice.callback.HandleClick
import com.dubaipolice.databinding.ActivityGroupInfoBinding
import com.dubaipolice.db.AppDatabase
import com.dubaipolice.db.model.GroupInfoTable
import com.dubaipolice.db.model.MemberInfoTable
import com.dubaipolice.model.GroupDetails
import com.dubaipolice.model.Member
import com.dubaipolice.utils.*
import com.dubaipolice.view.adapter.GroupParticipantAdapter
import com.dubaipolice.viewmodel.GroupInfoViewModel
import com.dubaipolice.wrapper.Resource
import com.github.dhaval2404.imagepicker.ImagePicker
import com.stfalcon.imageviewer.StfalconImageViewer
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class GroupInfoActivity : AppCompatActivity(), HandleClick {

    private var isEndUser: Boolean = false
    private lateinit var membersFromLocal: LiveData<List<MemberInfoTable>>
    private lateinit var groupInfoViewModel: GroupInfoViewModel
    private lateinit var binding: ActivityGroupInfoBinding

    lateinit var mContext: Context

    private var groupChatParticipantAdapter: GroupParticipantAdapter? = null

    private var groupData: GroupInfoTable? = null

    private var broadcastReceiver: BroadcastReceiver? = null

    private lateinit var members: MutableList<MemberInfoTable>

    var dialog: AlertDialog? = null

    var isGroupActive= true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            DataBindingUtil.setContentView(this@GroupInfoActivity, R.layout.activity_group_info)
        groupInfoViewModel = ViewModelProvider(this)[GroupInfoViewModel::class.java]

        binding.lifecycleOwner = this@GroupInfoActivity
        binding.groupInfoViewModel = groupInfoViewModel
        binding.clickHandle = this

        mContext = this
        getExtras()

        registerBroadcastReceiver()

    }

    /**
     * This function is used to register for broadcast events
     * to be received from background roster presence listener
     *
     *
     */
    private fun registerBroadcastReceiver() {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                Log.i("Receiver", "Broadcast received: $action")
                if (action == AppConstants.PRESENCE_RECEIVED_BROADCAST) {

                    val extras = intent.extras

                    if (extras != null) {
                        val userJid =
                            extras.getString(AppConstants.IntentConstants.PRESENCE_USER_JID)
                        val userPresenceStatus =
                            extras.getString(AppConstants.IntentConstants.PRESENCE_USER_STATUS)

                        if (userJid != null && userPresenceStatus != null) {
                            refreshPresenceInGroupList(userJid, userPresenceStatus)
                        }

                    }

                }//refresh group
                else if (action == AppConstants.REFRESH_GROUP_BROADCAST) {

                    Log.e("REFRESHGROUP", "REFERESHGROUP2")
                    loadGroupInfo()

                }
            }
        }
        //registering the broadcast receiver
        val intentFilter = IntentFilter()
        intentFilter.addAction(AppConstants.PRESENCE_RECEIVED_BROADCAST)

        intentFilter.addAction(AppConstants.REFRESH_GROUP_BROADCAST)

        registerReceiver(broadcastReceiver, intentFilter)
        //registerReceiver(broadcastReceiver, IntentFilter(AppConstants.PRESENCE_RECEIVED_BROADCAST))
    }

    private fun refreshPresenceInGroupList(userJid: String, presenceStatus: String) {
        if (this::members.isInitialized && members.isNotEmpty()) {
            members.forEachIndexed { index, member ->
                if (member.userJid == userJid) {
                    if(presenceStatus == AppConstants.IntentConstants.PRESENCE_USER_ONLINE)
                    {
                        members[index].isOnline = true
                    }
                    else
                    {
                        members[index].isOnline = false
                    }
                    groupChatParticipantAdapter!!.notifyItemChanged(index)
                }
            }
        }

    }

    private fun requestUserPresence() {
        if (members.isNotEmpty()) {
            members.forEachIndexed { _, e ->
                MainApplication.connection!!.sendMessageOneToOne(e.userJid!!, false, "online")
            }
        }
    }

    private fun requestGroupUpdated() {
        groupData?.let {
            val membersList = groupInfoViewModel.getGroupMemberList(it.groupId.toString())
            val userJid = membersList!!.map { user -> user.userJid }
            GlobalScope.launch {
                MainApplication.connection!!.sendStanzaToReceiveGrpUpdate(
                    it.groupJid.toString(),
                    it.groupId.toString(),
                    userJid
                )
            }
        }
    }

    /**
     * This function is used to unregister broadcast receiver
     */
    private fun unregisterBroadcastReceiver() {
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterBroadcastReceiver()
    }

    private fun getExtras() {
        val extras = intent.extras
        Log.e("GetExtra", "get")
        if (extras != null) {
            groupData =
                extras.getSerializable(AppConstants.IntentConstants.GROUP_DATA) as GroupInfoTable

            loadGroupData(
                groupData?.groupName,
                groupData?.alertLevel!!.toInt(),
                groupData?.imageUrl
            )

            if(groupData?.groupActiveStatus == "true")
            {
                isGroupActive= true
            }
            else
            {
                isGroupActive= false
            }

            groupInfoViewModel.groupId.value = groupData?.groupId.toString()
        } else {
            Log.e("GroupName", "NULL")
        }

    }

    override fun onResume() {
        super.onResume()
        if (Utils.isNetConnected(mContext))
            loadGroupInfo()
        else loadGroupInfoFromLocalDB(groupInfoViewModel.groupId.value.toString())

    }

    private fun getAllMembersFromLocal(groupId: String): List<MemberInfoTable>? {
        return groupInfoViewModel.getGroupMemberList(groupId)
    }

    private fun loadGroupInfo() {
        MyProgressDialog.show(mContext)
        groupInfoViewModel.groupInfoResponse!!.observe(this) { response ->
            Log.e("GroupInfo", "Success")
            when (response.status) {
                Resource.Status.SUCCESS -> {
                    if (response.data!!.success) {
                        SharedPref.writeBoolean(
                            AppConstants.MUTE_GROUP,
                            response.data.data.groupDetails.mute
                        )
                        binding.tvGroupName.text = response.data.data.groupDetails.name
                        Glide.with(mContext).load(response.data.data.groupDetails.image)
                            .placeholder(
                                AppCompatResources.getDrawable(
                                    mContext,
                                    R.drawable.ic_profile
                                )
                            ).into(binding.groupImage)

                        val updatedGroupName =
                            response.data.data?.groupDetails?.name
                        val alertLevel =
                            response.data.data?.groupDetails?.alert_level
                        val image = response.data.data?.groupDetails?.image
                        val isCallActive = response.data.data?.groupDetails?.is_call_active
                        val groupActiveStatus = response.data.data?.groupDetails?.status

                        MainApplication.connection?.updateGroupDb(
                            image,
                            updatedGroupName,
                            alertLevel.toString(),
                            groupData!!.groupJid!!
                        )

                        MainApplication.connection?.updateGroupCallStatus(
                            isCallActive,
                            groupData!!.groupJid!!
                        )

                        MainApplication.connection?.updateGroupActiveStatus(
                            groupActiveStatus,
                            groupData!!.groupJid!!
                        )

                        groupData!!.alertLevel = alertLevel.toString()

                        if(groupActiveStatus == "true")
                        {
                            isGroupActive= true
                        }
                        else
                        {
                            isGroupActive= false
                        }

                        ChatActivity.groupData?.alertLevel = alertLevel.toString()
                        ChatActivity.groupData?.imageUrl = image
                        ChatActivity.groupData?.groupName = updatedGroupName
                        ChatActivity.groupData?.callActive = isCallActive.toString()
                        ChatActivity.groupData?.groupActiveStatus = groupActiveStatus

                        updateAndDeleteMembers(response.data.data.groupDetails)

                    } else loadGroupInfoFromLocalDB(groupInfoViewModel.groupId.value.toString())

                }
                Resource.Status.LOADING -> {}
                Resource.Status.ERROR -> {
                    MyProgressDialog.dismiss()
                    Log.e("Error", "")
                    loadGroupInfoFromLocalDB(groupInfoViewModel.groupId.value.toString())

                }
            }

        }
    }

    private fun updateAndDeleteMembers(groupData: GroupDetails) {
        val onlineMembers = groupData.members
        val localMembers = getAllMembersFromLocal(groupInfoViewModel.groupId.value.toString())

        lifecycleScope.launch(Dispatchers.Default) {
            val newMembers =
                onlineMembers.filter { it.user.user_id !in localMembers!!.map { item -> item.userId } }
            if (newMembers.isNotEmpty()) {
                val newMembersInfoList = createMemberInfo(newMembers, groupData)
                AppDatabase.getAppDatabase(MainApplication.appContext)
                    ?.memberInfoTableDao()
                    ?.insert(newMembersInfoList)
            }
        }
        lifecycleScope.launch(Dispatchers.Default) {
            val deletedMembers =
                localMembers.let { local -> local?.filter { it.userId !in onlineMembers.map { item -> item.user.user_id } } }
            deletedMembers?.let {
                AppDatabase.getAppDatabase(MainApplication.appContext)
                    ?.memberInfoTableDao()
                    ?.delete(deletedMembers)
            }
            Log.v("deletedMembers", deletedMembers?.size.toString())
        }

        lifecycleScope.launch(Dispatchers.Default) {
            val updatedMemberList: MutableList<Member> = mutableListOf()
            onlineMembers.forEachIndexed { i, online ->
                if (online.user.lastKnownLocation != null && localMembers?.isNotEmpty() == true) {
                    var isFound = false
                    var localPosition= 0
                    localMembers.forEachIndexed innerLoop@{ j, local ->
                        if (local.userId == online.user.user_id) {
                            isFound = true
                            localPosition= j
                            return@innerLoop
                        }
                    }

                    if (isFound) {

                        //if (localMembers[localPosition].createdAt != online.user.lastKnownLocation.created_at) {
                            updatedMemberList.add(online)
                        //}
                       
                    }
                    
                    /*if (localMembers[i].userId == onlineMembers[i].user.user_id) {
                        if (localMembers[i].createdAt != online.user.lastKnownLocation.created_at) {
                            updatedMemberList.add(online)
                        }
                    }*/
                }
            }
            if (updatedMemberList.isNotEmpty()) {
                val updateMembers = createMemberInfo(updatedMemberList, groupData)
                for (updatedMember in updateMembers) {
                    Log.d("updatedMember", updatedMember.toString())
                    AppDatabase.getAppDatabase(MainApplication.appContext)
                        ?.memberInfoTableDao()
                        ?.updateMembersData(
                            updatedMember.latitude,
                            updatedMember.longitude,
                            updatedMember.createdAt,
                            updatedMember.userJid.toString()
                        )

                    AppDatabase.getAppDatabase(MainApplication.appContext)
                        ?.memberInfoTableDao()
                        ?.updateMembersDetails(
                            updatedMember.firstName,
                            updatedMember.lastName,
                            updatedMember.roleId,
                            updatedMember.userJid.toString(),
                            updatedMember.groupJid.toString()
                        )
                }
            }
        }



        loadGroupInfoFromLocalDB(groupData.group_id.toString())

    }

    private fun createMemberInfo(
        newMembers: List<Member>,
        groupData: GroupDetails
    ): MutableList<MemberInfoTable> {
        val newMembersList = emptyList<MemberInfoTable>().toMutableList()
        for (member in newMembers) {
            val memberInfo = MemberInfoTable()
            memberInfo.userId = member.user.user_id
            memberInfo.userJid = member.user.jid.toString()
            memberInfo.roleId = member.role_id
            memberInfo.firstName = member.user.first_name
            memberInfo.lastName = member.user.last_name
            memberInfo.imageUrl = member.user.profile_image
            memberInfo.groupId = groupData.group_id
            memberInfo.groupJid = groupData.room_jid
            memberInfo.groupName = groupData.name

            member.user.lastKnownLocation?.let {
                memberInfo.latitude = member.user.lastKnownLocation.latitude
                memberInfo.longitude = member.user.lastKnownLocation.longitude
                memberInfo.createdAt = member.user.lastKnownLocation.created_at
            }
            memberInfo.groupName = groupData.name
            newMembersList.add(memberInfo)
        }
        return newMembersList
    }

    private fun loadGroupInfoFromLocalDB(groupId: String) {
        val mute = SharedPref.readBoolean(AppConstants.MUTE_GROUP)
        if (mute) {
            binding.txtMute.text = getString(R.string.muted)
            binding.imgMute.setImageResource(R.drawable.ic_mute)
        } else {
            binding.txtMute.text = getString(R.string.unmuted)
            binding.imgMute.setImageResource(R.drawable.ic_unmute)
        }

        groupInfoViewModel.getGroupMemberLiveData(groupId)?.observe(this) {
            loadMemberList(it)
        }
    }

    private fun loadMemberList(membersList: List<MemberInfoTable>) {
        members = membersList.toMutableList()
        binding.recyclerView.layoutManager = LinearLayoutManager(
            mContext,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.tvTotalParticipantsCount.text = members.size.toString().plus(" PARTICIPANTS")
        if (members.isNotEmpty()) {
            val user = members.toMutableList()
                .find { it.userId == SharedPref.readInt(AppConstants.KEY_USER_ID) }

            /*var isFound = false
            membersList.forEachIndexed { index, data ->
                if (data.userId == SharedPref.readInt(AppConstants.KEY_USER_ID)) {
                    isFound = true
                    return@forEachIndexed
                }
            }

            if (isFound) {


            }*/

            user?.let {
                members.remove(it)
                members.add(0, it)
                if(isGroupActive)
                {

                    if (it.roleId == 2) {
                        isEndUser = false
                        binding.layoutAddParticipants.visibility = View.VISIBLE
                        binding.imgEdit.visibility = View.VISIBLE
                        addSwipeToRecyclerView()
                        addClickListenerForSuperUsers()
                    } else {
                        isEndUser = true
                        binding.layoutAddParticipants.visibility = View.GONE
                        binding.imgEdit.visibility = View.GONE
                    }

                }
                else
                {
                    isEndUser = true
                    binding.layoutAddParticipants.visibility = View.GONE
                    binding.imgEdit.visibility = View.GONE
                }

            }
            groupChatParticipantAdapter =
                GroupParticipantAdapter(mContext, members)
            binding.recyclerView.adapter = groupChatParticipantAdapter
        } else {
            isEndUser = true
            binding.layoutAddParticipants.visibility = View.GONE
            binding.imgEdit.visibility = View.GONE
        }
        MyProgressDialog.dismiss()
        requestUserPresence()

    }

    private fun addClickListenerForSuperUsers() {
        binding.layoutAlertLevel.setOnClickListener { clickHandle(it) }
        binding.imgEdit.setOnClickListener { clickHandle(it) }
    }

    private fun addSwipeToRecyclerView() {
        val swipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding.recyclerView.adapter as GroupParticipantAdapter
                if (viewHolder.absoluteAdapterPosition != 0)
                    removeUserFromGroup(viewHolder.absoluteAdapterPosition)
                else {
                    adapter.notifyItemChanged(viewHolder.absoluteAdapterPosition)
                    Utils.showToast(
                        this@GroupInfoActivity,
                        getString(R.string.you_cannot_remove_by_yourself)
                    )
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun removeUserFromGroup(position: Int) {
        val adapter = binding.recyclerView.adapter as GroupParticipantAdapter
        val user = adapter.groupChatParticipantsList[position]
        groupInfoViewModel.userId.value = user.userId.toString()
        MyProgressDialog.show(mContext)
        groupInfoViewModel.deleteGroupMemberResponse!!.observe(this) { response ->
            MyProgressDialog.show(mContext)
            when (response.status) {
                Resource.Status.SUCCESS -> {
                    MyProgressDialog.dismiss()
                    if (response.data!!.isSuccess) {
                        Utils.showToast(
                            mContext,
                            user.firstName.plus(getString(R.string.removed_successfully))
                        )
                        loadGroupInfo()
                        requestGroupUpdated()
                    } else {
                        Utils.showToast(
                            mContext,
                            response.data.message.toString()
                        )
                        loadGroupInfoFromLocalDB(groupInfoViewModel.groupId.value.toString())
                    }
                }
                Resource.Status.ERROR -> {
                    MyProgressDialog.dismiss()
                    Utils.showToast(mContext, response.message!!)
                    loadGroupInfo()
                }
                Resource.Status.LOADING -> {

                }

            }
        }
    }

    override fun clickHandle(v: View) {
        when (v.id) {
            R.id.back -> {
                onBackPressed()
            }
            R.id.layoutAddParticipants -> {
                val i = Intent(mContext, AddMemberActivity::class.java)
                i.putExtra(AppConstants.IntentConstants.GROUP_DATA, groupData)
                startActivity(i)
            }
            R.id.layoutMute -> {
                muteGroupPopup()
            }

            R.id.layoutMediaDocs -> {
                val i = Intent(mContext, GroupAttachmentsActivity::class.java)
                i.putExtra(AppConstants.IntentConstants.GROUP_DATA, groupData)
                startActivity(i)
            }

            R.id.groupImage -> {
                groupLogoPopup()
            }

            R.id.layoutAlertLevel -> {
                popupUpdateGroupAlertLevel()

            }

            R.id.imgEdit -> {
                popupEditGroupName()

            }
            R.id.layoutVideoStreamList -> {
                /*val i = Intent(mContext, CameraStreamListActivity::class.java)
                i.putExtra(AppConstants.IntentConstants.GROUP_DATA, groupData)
                startActivity(i)*/

                /*val i = Intent(mContext, LiveStreamListActivity::class.java)
                i.putExtra(AppConstants.IntentConstants.GROUP_DATA, groupData)
                startActivity(i) */

                val i = Intent(mContext, GroupStreamActivity::class.java)
                i.putExtra(AppConstants.IntentConstants.GROUP_DATA, groupData)
                startActivity(i)
            }
            R.id.layoutMemberLocations -> {
                val i = Intent(mContext, MapsLocationActivity::class.java)
                i.putExtra(AppConstants.IntentConstants.GROUP_DATA, groupData)
                startActivity(i)
               /* val i = Intent(mContext, MapLocationActivity::class.java)
                i.putExtra(AppConstants.IntentConstants.GROUP_DATA, groupData)
                startActivity(i)*/
            }

        }
    }

    private fun muteGroup(mute: Boolean) {
        MyProgressDialog.show(mContext)
        groupInfoViewModel.groupId.value = groupData!!.groupId.toString()
        groupInfoViewModel.mute.value = mute
        groupInfoViewModel.muteGroupResponse!!.observe(
            this
        ) { commonResponseResource ->
            MyProgressDialog.dismiss()
            when (commonResponseResource.status) {
                Resource.Status.SUCCESS -> {
                    if (commonResponseResource.data!!.isSuccess) {
                        SharedPref.writeBoolean(AppConstants.MUTE_GROUP, mute)

                        if (mute) {
                            binding.txtMute.text = getString(R.string.muted)
                            binding.imgMute.setImageResource(R.drawable.ic_mute)
                        } else {
                            binding.txtMute.text = getString(R.string.unmuted)
                            binding.imgMute.setImageResource(R.drawable.ic_unmute)

                        }

                        Utils.showToast(mContext, commonResponseResource.data.message!!)
                    } else {
                        Utils.showToast(mContext, commonResponseResource.data.message!!)
                    }
                }
                Resource.Status.LOADING -> {
                }
                Resource.Status.ERROR -> {
                    Utils.showToast(mContext, commonResponseResource.message!!)
                }
            }
        }

    }

    private fun muteGroupPopup() {
        val items = arrayOf<CharSequence>(
            getString(R.string.mute),
            getString(R.string.unmute),
            getString(R.string.cancel)
        )
        val builder = AlertDialog.Builder(this@GroupInfoActivity)
        builder.setTitle(getString(R.string.mute_group))
        builder.setCancelable(true)
        builder.setItems(items) { dialog, item ->
            when (item) {
                0 -> {
                    muteGroup(true)
                }
                1 -> {
                    muteGroup(false)
                }
                else -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()

    }

    private fun groupLogoPopup() {

        val items = arrayOf<CharSequence>(
            getString(R.string.view_group_image),
            getString(R.string.change_group_image)
        )
        val listItems = items.toMutableList()
        if (isEndUser) listItems.removeAt(1)

        val builder = AlertDialog.Builder(this@GroupInfoActivity)
        builder.setTitle(getString(R.string.choose_an_action))
        builder.setCancelable(true)
        builder.setItems(listItems.toTypedArray()) { _, item ->
            if (item == 0) {

                val images = mutableListOf<String>()
                groupData!!.imageUrl?.let {
                    images.add(it)
                }
                if (images.isNotEmpty() && Utils.isNetConnected(this))
                    StfalconImageViewer.Builder(mContext, images) { view, image ->
                        Glide.with(mContext).load(image)
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(view)
                    }.show()
                else Utils.showToast(mContext, getString(R.string.no_group_photo))

            } else if (item == 1) {

                ImagePicker.with(this)
                    .cropSquare()
                    .galleryOnly()
                    .compress(1024)         //Final image size will be less than 1 MB(Optional)
                    .maxResultSize(
                        1080,
                        1080
                    )  //Final image resolution will be less than 1080 x 1080(Optional)
                    .createIntent { intent ->
                        startForProfileImageResult.launch(intent)
                    }

            }
        }
        builder.show()

    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data
            when (resultCode) {
                Activity.RESULT_OK -> {
                    //Image Uri will not be null for RESULT_OK
                    val fileUri = data?.data!!

                    //mProfileUri = fileUri
                    Log.e("URI: ", fileUri.toString())
                    Log.e("PATH: ", fileUri.path.toString())
                    //binding.profileImage.setImageURI(fileUri)

                    val groupImage = File(fileUri.path.toString())

                    updateGroup(null, null, groupImage)

                    /*Glide.with(mContext).load(profileImage).placeholder(
                            AppCompatResources.getDrawable(mContext, R.drawable.ic_profile)).into(binding.profileImage)
        */
                }
                ImagePicker.RESULT_ERROR -> {
                    Utils.showToast(mContext, ImagePicker.getError(data))
                }
                else -> {
                    Log.e("Cancelled", "Task Cancelled")
                }
            }
        }


    //pass null values for which we are not updating info
    @OptIn(DelicateCoroutinesApi::class)
    private fun updateGroup(groupName: String?, groupAlertLevel: String?, groupLogo: File?) {
        MyProgressDialog.show(mContext)

        groupInfoViewModel.groupId.value = groupData!!.groupId.toString()
        groupInfoViewModel.groupName.value = groupName
        groupInfoViewModel.groupAlertLevel.value = groupAlertLevel
        groupInfoViewModel.groupLogo.value = groupLogo

        groupInfoViewModel.updateGroupResponse!!.observe(this) { updateGroupResponseResource ->
            MyProgressDialog.dismiss()
            when (updateGroupResponseResource.status) {
                Resource.Status.SUCCESS -> {
                    if (updateGroupResponseResource.data!!.isSuccess) {

                        val updatedGroupName =
                            updateGroupResponseResource.data.data?.groupDetails?.name
                        val alertLevel =
                            updateGroupResponseResource.data.data?.groupDetails?.alertLevel
                        val image = updateGroupResponseResource.data.data?.groupDetails?.image

                        MainApplication.connection?.updateGroupDb(
                            image,
                            updatedGroupName,
                            alertLevel.toString(),
                            groupData!!.groupJid!!
                        )

                        groupData!!.alertLevel = alertLevel.toString()
                        ChatActivity.groupData?.alertLevel = alertLevel.toString()
                        ChatActivity.groupData?.imageUrl = image
                        ChatActivity.groupData?.groupName = updatedGroupName
                        loadGroupData(updatedGroupName, alertLevel, image)
                        requestGroupUpdated()
                        Utils.showToast(mContext, updateGroupResponseResource.data.message!!)

                    } else {
                        Utils.showToast(mContext, updateGroupResponseResource.data.message!!)
                    }
                }
                Resource.Status.LOADING -> {
                }
                Resource.Status.ERROR -> {
                    Utils.showToast(mContext, updateGroupResponseResource.message!!)
                }
            }
        }

    }

    private fun loadGroupData(groupName: String?, alertLevel: Int?, image: String?) {
        when (alertLevel) {
            1 -> {
                binding.tvAlertLevel.text = getString(R.string.alert_level1)
                binding.imgAlertLevel.setImageResource(R.drawable.alert_grey)
            }
            2 -> {
                binding.tvAlertLevel.text = getString(R.string.alert_level2)
                binding.imgAlertLevel.setImageResource(R.drawable.alert_yellow)
            }
            3 -> {
                binding.tvAlertLevel.text = getString(R.string.alert_level3)
                binding.imgAlertLevel.setImageResource(R.drawable.alert_red)
            }
            else -> {
                binding.tvAlertLevel.text = getString(R.string.alert_level1)
                binding.imgAlertLevel.setImageResource(R.drawable.ic_alert_grey)
            }
        }

        binding.tvGroupName.text = groupName
        Glide.with(mContext).load(image)
            .placeholder(
                AppCompatResources.getDrawable(
                    mContext,
                    R.drawable.ic_profile
                )
            ).into(binding.groupImage)
    }

    private fun popupEditGroupName() {

        val builder = AlertDialog.Builder(mContext)
        val customLayout: View = layoutInflater.inflate(R.layout.popup_group_name_edit, null)
        builder.setView(customLayout)
        val etGroupName: EditText = customLayout.findViewById(R.id.etGroupName)
        val btnSave: Button = customLayout.findViewById(R.id.btnSave)
        val btnCancel: Button = customLayout.findViewById(R.id.btnCancel)

        etGroupName.setText(binding.tvGroupName.text.toString())

        btnSave.setOnClickListener {

            val groupName = etGroupName.text.toString().trim()
            if (!TextUtils.isEmpty(groupName)) {

                dialog!!.dismiss()

                updateGroup(groupName, null, null)

            }

        }

        btnCancel.setOnClickListener {
            dialog!!.dismiss()

        }

        dialog = builder.create()
        dialog?.let {
            it.setCancelable(false)
            it.show()
            it.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

    }

    private fun popupUpdateGroupAlertLevel() {

        val builder = AlertDialog.Builder(mContext)
        val customLayout: View = layoutInflater.inflate(R.layout.popup_group_alert_level_edit, null)
        builder.setView(customLayout)

        val layoutAlertLevel1: RelativeLayout = customLayout.findViewById(R.id.layoutAlertLevel1)
        val layoutAlertLevel2: RelativeLayout = customLayout.findViewById(R.id.layoutAlertLevel2)
        val layoutAlertLevel3: RelativeLayout = customLayout.findViewById(R.id.layoutAlertLevel3)
        val imgAlertLevel1: ImageView = customLayout.findViewById(R.id.imgAlertLevel1)
        val imgAlertLevel2: ImageView = customLayout.findViewById(R.id.imgAlertLevel2)
        val imgAlertLevel3: ImageView = customLayout.findViewById(R.id.imgAlertLevel3)
        val btnSave: Button = customLayout.findViewById(R.id.btnSave)
        val btnCancel: Button = customLayout.findViewById(R.id.btnCancel)

        var alertLevel = groupData?.alertLevel!!.toInt()

        when (alertLevel) {
            1 -> {
                imgAlertLevel1.setImageResource(R.drawable.ic_radio_checked)
                imgAlertLevel2.setImageResource(R.drawable.ic_radio_unchecked)
                imgAlertLevel3.setImageResource(R.drawable.ic_radio_unchecked)
            }
            2 -> {
                imgAlertLevel1.setImageResource(R.drawable.ic_radio_unchecked)
                imgAlertLevel2.setImageResource(R.drawable.ic_radio_checked)
                imgAlertLevel3.setImageResource(R.drawable.ic_radio_unchecked)
            }
            3 -> {
                imgAlertLevel1.setImageResource(R.drawable.ic_radio_unchecked)
                imgAlertLevel2.setImageResource(R.drawable.ic_radio_unchecked)
                imgAlertLevel3.setImageResource(R.drawable.ic_radio_checked)
            }
            else -> {
                imgAlertLevel1.setImageResource(R.drawable.ic_radio_checked)
                imgAlertLevel2.setImageResource(R.drawable.ic_radio_unchecked)
                imgAlertLevel3.setImageResource(R.drawable.ic_radio_unchecked)
            }
        }


        layoutAlertLevel1.setOnClickListener {

            alertLevel = 1

            imgAlertLevel1.setImageResource(R.drawable.ic_radio_checked)
            imgAlertLevel2.setImageResource(R.drawable.ic_radio_unchecked)
            imgAlertLevel3.setImageResource(R.drawable.ic_radio_unchecked)

        }

        layoutAlertLevel2.setOnClickListener {

            alertLevel = 2

            imgAlertLevel1.setImageResource(R.drawable.ic_radio_unchecked)
            imgAlertLevel2.setImageResource(R.drawable.ic_radio_checked)
            imgAlertLevel3.setImageResource(R.drawable.ic_radio_unchecked)

        }

        layoutAlertLevel3.setOnClickListener {

            alertLevel = 3

            imgAlertLevel1.setImageResource(R.drawable.ic_radio_unchecked)
            imgAlertLevel2.setImageResource(R.drawable.ic_radio_unchecked)
            imgAlertLevel3.setImageResource(R.drawable.ic_radio_checked)


        }


        btnSave.setOnClickListener {

            dialog!!.dismiss()

            updateGroup(null, alertLevel.toString(), null)

        }

        btnCancel.setOnClickListener {
            dialog!!.dismiss()

        }

        dialog = builder.create()
        dialog!!.setCancelable(false)
        dialog!!.show()
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    }

}


