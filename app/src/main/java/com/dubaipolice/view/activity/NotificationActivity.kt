package com.dubaipolice.view.activity

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dubaipolice.R
import com.dubaipolice.callback.HandleClick
import com.dubaipolice.callback.HandleNotificationListItemClick
import com.dubaipolice.databinding.ActivityNotificationBinding
import com.dubaipolice.model.Notifications
import com.dubaipolice.utils.MyProgressDialog
import com.dubaipolice.utils.Utils
import com.dubaipolice.view.adapter.CommonLoadStateAdapter
import com.dubaipolice.view.adapter.LiveStreamListAdapter
import com.dubaipolice.view.adapter.NotificationListAdapter
import com.dubaipolice.view.adapter.NotificationsAdapter
import com.dubaipolice.viewmodel.NotificationListViewModel
import com.dubaipolice.viewmodel.NotificationViewModel
import com.dubaipolice.viewmodel.StreamListViewModel
import com.dubaipolice.wrapper.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NotificationActivity : AppCompatActivity(), HandleClick, HandleNotificationListItemClick {

    lateinit var binding: ActivityNotificationBinding
    private lateinit var notificationViewModel: NotificationListViewModel

    lateinit  var mContext: Context

    var notificationsList: ArrayList<Notifications> = ArrayList<Notifications>()
    private var notificationsAdapter: NotificationsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this@NotificationActivity, R.layout.activity_notification)
        notificationViewModel = ViewModelProvider(this)[NotificationListViewModel::class.java]

        binding.lifecycleOwner= this@NotificationActivity
        //binding.loginViewModel= loginViewModel
        binding.clickHandle= this

        mContext = this

        initRecyclerView()

    }
    private fun initRecyclerView() {
        val notificationListAdapter = NotificationListAdapter(mContext, this)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
            adapter = notificationListAdapter.withLoadStateFooter(
                footer = CommonLoadStateAdapter{notificationListAdapter.retry()}
            )
        }
        lifecycleScope.launch(Dispatchers.IO) {
            notificationViewModel.getNotificationListFlow().collectLatest {
                notificationListAdapter.submitData(it)
            }
        }

    }
    override fun clickHandle(v: View) {
        when(v.id) {

            R.id.back -> {
                onBackPressed()
            }

            R.id.tvMarkAllAsRead -> {
                notificationViewModel.isRead.value=1
                MyProgressDialog.show(mContext)
                notificationViewModel.readAllNotificationResponse!!.observe(
                    this
                ) { commonResponse ->
                    MyProgressDialog.dismiss()
                    when (commonResponse.status) {
                        Resource.Status.SUCCESS -> {
                            if (commonResponse.data!!.isSuccess) {
                                Utils.showToast(mContext, commonResponse.data.message!!)
                                initRecyclerView()
                            } else {
                                Utils.showToast(mContext, commonResponse.data.message!!)
                            }

                        }
                        Resource.Status.LOADING -> {
                        }
                        Resource.Status.ERROR -> {
                            Utils.showToast(mContext, commonResponse.message!!)
                        }
                    }
                }
            }

        }

    }

    override fun itemClickHandle(isVisible:Boolean,notifications: Notifications?) {
        if(isVisible)
        { binding.tvMarkAllAsRead.visibility=View.VISIBLE

        }else {
            if (notifications != null) {
                notificationViewModel.notificationId.value = notifications.id

                if (!notifications.is_read) {
                    notificationViewModel.notificationId.value = notifications.id
                    notificationViewModel.isRead.value = 1
                    MyProgressDialog.show(mContext)
                    notificationViewModel.notificationReadResponse!!.observe(
                        this
                    ) { commonResponse ->
                        MyProgressDialog.dismiss()
                        when (commonResponse.status) {
                            Resource.Status.SUCCESS -> {
                                if (commonResponse.data!!.isSuccess) {
                                    Utils.showToast(mContext, commonResponse.data.message!!)
                                } else {
                                    Utils.showToast(mContext, commonResponse.data.message!!)
                                }
                            }
                            Resource.Status.LOADING -> {
                            }
                            Resource.Status.ERROR -> {
                                Utils.showToast(mContext, commonResponse.message!!)
                            }
                        }
                    }
                }
            }
        }
    }
}