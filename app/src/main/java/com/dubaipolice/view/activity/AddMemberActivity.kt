package com.dubaipolice.view.activity

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dubaipolice.MainApplication
import com.dubaipolice.R
import com.dubaipolice.callback.HandleClick
import com.dubaipolice.callback.HandleUserItemClick
import com.dubaipolice.databinding.ActivityAddMemberBinding
import com.dubaipolice.db.model.GroupInfoTable
import com.dubaipolice.model.AddMembers
import com.dubaipolice.model.Users
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.MyProgressDialog
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.utils.Utils
import com.dubaipolice.utils.Utils.showSnack
import com.dubaipolice.view.adapter.AddParticipantAdapter
import com.dubaipolice.view.adapter.CommonLoadStateAdapter
import com.dubaipolice.viewmodel.AddMemberViewModel
import com.dubaipolice.wrapper.Resource
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddMemberActivity : AppCompatActivity(), HandleClick, HandleUserItemClick {

    private lateinit var addMemberViewModel: AddMemberViewModel
    private lateinit var binding: ActivityAddMemberBinding
    lateinit var mContext: Context
    private var groupData: GroupInfoTable? = null
    private var selectedUsers: MutableList<Users> = emptyList<Users>().toMutableList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMemberBinding.inflate(layoutInflater)
        addMemberViewModel = ViewModelProvider(this)[AddMemberViewModel::class.java]
        binding.lifecycleOwner = this@AddMemberActivity
        binding.addMemberViewModel = addMemberViewModel
        binding.clickHandle = this
        setContentView(binding.root)
        mContext = this
        getExtras()
        addFilterToSearchView()
    }


    private fun getExtras() {
        val extras = intent.extras
        extras.let {
            groupData =
                extras?.getSerializable(AppConstants.IntentConstants.GROUP_DATA) as GroupInfoTable
            addMemberViewModel.groupId.value = groupData?.groupId.toString()
            addMemberViewModel.organizationId.value =
                SharedPref.readInt(AppConstants.KEY_ORGANIZATION_ID).toString()

            addMemberViewModel.search.value = null

            if(Utils.isNetConnected(mContext))
            submitUserList()
            else binding.parentLayout.showSnack(mContext,getString(R.string.no_internet))
        }
    }

    private fun addFilterToSearchView() {
        binding.userSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                addMemberViewModel.search.value = query.toString()
                return false
            }
        })

    }

    private fun submitUserList() {
       val addAdapter = AddParticipantAdapter(mContext, this)
        binding.recyclerView.adapter = addAdapter.withLoadStateHeaderAndFooter(
            header = CommonLoadStateAdapter {addAdapter.retry()},
            footer = CommonLoadStateAdapter {addAdapter.retry()}
        )
        addMemberViewModel.getMembersLivedata().observe(this@AddMemberActivity) {
            launchOnLifecycleScope { addAdapter.submitData(it) }
        }
    }

    override fun clickHandle(v: View) {
        when (v.id) {
            R.id.back -> {
                onBackPressed()
            }
            R.id.add_participant -> {
                addMembersToGroup()
            }
        }
    }

    override fun itemClickHandle(user: Users) {
        if (user.isSelected)
            selectedUsers.add(user)
        else selectedUsers.remove(user)
        binding.addParticipant.visibility =
            if (selectedUsers.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun addMembersToGroup() {
        val listOfUserId = emptyList<Int>().toMutableList()
        val listOfUserJId = emptyList<String>().toMutableList()
        selectedUsers.forEach { user ->
            listOfUserId.add(user.user_id)
            listOfUserJId.add(user.jid)
        }
        val addMembers = AddMembers(
            groupData!!.groupId!!,
            listOfUserId
        )
        addMemberViewModel.addMembers.value = addMembers
        addMemberViewModel.adduserListResponse!!.observe(this) { response ->
            MyProgressDialog.show(mContext)
            when (response.status) {
                Resource.Status.SUCCESS -> {
                    if (response.data!!.isSuccess) {
                        MyProgressDialog.dismiss()
                        requestGroupUpdated(listOfUserJId)
                    }
                    Utils.showToast(this, response.data.message.toString())
                }
                Resource.Status.ERROR -> {
                    MyProgressDialog.dismiss()
                    Utils.showToast(mContext, response.message!!)
                }
                Resource.Status.LOADING -> {

                }
            }
        }
    }

    private fun requestGroupUpdated(listOfUserId: MutableList<String>) {
        groupData?.let {
            GlobalScope.launch {

                MainApplication.connection!!.sendStanzaToReceiveGrpUpdate(
                    it.groupJid.toString(),
                    it.groupId.toString(),
                    listOfUserId
                )

            }
        }
        onBackPressed()
    }

    private fun launchOnLifecycleScope(execute: suspend () -> Unit) {
        lifecycleScope.launchWhenCreated {
            execute()
        }
    }
}