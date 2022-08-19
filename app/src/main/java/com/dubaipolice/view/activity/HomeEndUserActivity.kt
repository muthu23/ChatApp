package com.dubaipolice.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.dubaipolice.MainApplication
import com.dubaipolice.R
import com.dubaipolice.callback.HandleClick
import com.dubaipolice.callback.HandleGroupHelpItemChecked
import com.dubaipolice.callservices.CallConnectionService
import com.dubaipolice.callservices.CallHandler
import com.dubaipolice.databinding.ActivityHomeEnduserBinding
import com.dubaipolice.db.AppDatabase
import com.dubaipolice.db.model.GroupInfoTable
import com.dubaipolice.extensions.replaceFragment
import com.dubaipolice.extensions.replaceFragmentWithoutBackStack
import com.dubaipolice.model.*
import com.dubaipolice.service.FCMUpdater
import com.dubaipolice.service.LocationDetectorService
import com.dubaipolice.service.OnClearFromRecentService
import com.dubaipolice.utils.*
import com.dubaipolice.view.adapter.GroupHelpAdapter
import com.dubaipolice.view.adapter.NotificationsSoundAdapter
import com.dubaipolice.view.fragment.*
import com.dubaipolice.viewmodel.HomeEndUserViewModel
import com.dubaipolice.wrapper.Resource
import com.dubaipolice.xmpp.XmppCustomConnection
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.tbruyelle.rxpermissions3.RxPermissions
import java.util.*


class HomeEndUserActivity : AppCompatActivity(), HandleClick , HandleGroupHelpItemChecked {

    lateinit var homeEndUserViewModel: HomeEndUserViewModel
    lateinit var binding: ActivityHomeEnduserBinding
    lateinit var mContext: Context

    lateinit var drawer: DrawerLayout

    lateinit var rxPermissions: RxPermissions
    var dialog: androidx.appcompat.app.AlertDialog? = null
    private var groupHelpAdapter: GroupHelpAdapter? = null
    private var groupDbList: ArrayList<GroupInfoTable> = ArrayList()
    private val groupHelpItemList = ArrayList<GroupHelpItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this@HomeEndUserActivity, R.layout.activity_home_enduser)
        homeEndUserViewModel = ViewModelProvider(this)[HomeEndUserViewModel::class.java]

        binding.lifecycleOwner= this@HomeEndUserActivity
        //binding.loginViewModel= loginViewModel
        binding.clickHandle= this

        mContext = this

        drawer = binding.drawerLayout

        //init xmpp if not initialized in MainApplication class
        initXmppIfNotInitialized()

        loadUserInfo()

        //select default chat optiion
        binding.tvChat.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_chats_selected, 0, 0)
        binding.tvSetting.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_settings_unselected, 0, 0)
        binding.tvChat.setTextColor(ContextCompat.getColor(mContext, R.color.bottom_nav_txt_selected))
        binding.tvHelp.setTextColor(ContextCompat.getColor(mContext, R.color.bottom_nav_txt_unselected))
        binding.tvSetting.setTextColor(ContextCompat.getColor(mContext, R.color.bottom_nav_txt_unselected))

        binding.btnHelp.setOnLongClickListener{
           groupHelpItemList.clear()
            getGroupListFromDB().forEach {
                groupHelpItemList.add(GroupHelpItem(it.groupName,it.groupId,false))
            }
            popupGroupHelp(groupHelpItemList)

            return@setOnLongClickListener true
        }

        replaceFragmentWithoutBackStack(ChatFragment(), R.id.frame_container)

        rxPermissions= RxPermissions(this)

        checkPermissions()

        getFirebaseToken()

        forceUpgradeApp()

        registerPhoneAccount()

        //startService(Intent(this, OnClearFromRecentService::class.java))

        //startChrono()
        getAllCms()
    }

    override fun onStart() {
        super.onStart()
        if(!TextUtils.isEmpty(SharedPref.readString(AppConstants.COLOR_SCHEME)))
        {


            ThemeUtility.setGradientThemeColorUsingDrawable(mContext, R.drawable.bg_gradient)
            ThemeUtility.setGradientThemeColorUsingDrawable(mContext, R.drawable.bg_button)

        }

    }
    private fun getGroupListFromDB():ArrayList<GroupInfoTable> {
        groupDbList.clear()
        val myGroupDbList = AppDatabase.getAppDatabase(MainApplication.appContext)
            ?.groupInfoTableDao()
            ?.getAll()

        if (myGroupDbList != null && myGroupDbList.isNotEmpty()) {
            groupDbList.addAll(myGroupDbList.toMutableList())
        }
       return groupDbList
    }
    private fun startChrono()
    {

//        var chronometer = Chronometer(mContext)
//
//        chronometer.onChronometerTickListener =
//            Chronometer.OnChronometerTickListener { chronometer ->
//                val time = SystemClock.elapsedRealtime() - chronometer.base
//                val h = (time / 3600000).toInt()
//                val m = (time - h * 3600000).toInt() / 60000
//                val s = (time - h * 3600000 - m * 60000).toInt() / 1000
//                val t =
//                    (if (h < 10) "0$h" else h).toString() + ":" + (if (m < 10) "0$m" else m) + ":" + if (s < 10) "0$s" else s
//                chronometer.text = t
//                Log.e("TimerStart", t)
//            }
//
//        chronometer.setBase(SystemClock.elapsedRealtime())
//        chronometer.start()


        val handler = Handler(Looper.getMainLooper())
        //handler.removeCallbacksAndMessages(null)
        var seconds= 0

        // Call the post() method,
        // passing in a new Runnable.
        // The post() method processes
        // code without a delay,
        // so the code in the Runnable
        // will run almost immediately.

        // Call the post() method,
        // passing in a new Runnable.
        // The post() method processes
        // code without a delay,
        // so the code in the Runnable
        // will run almost immediately.
        handler.post(object : Runnable {
            override fun run() {
                val hours: Int = seconds / 3600
                val minutes: Int = seconds % 3600 / 60
                val secs: Int = seconds % 60

                // Format the seconds into hours, minutes,
                // and seconds.
                val time: String = java.lang.String
                    .format(
                        Locale.getDefault(),
                        "%d:%02d:%02d", hours,
                        minutes, secs
                    )

                // Set the text view text.
                //timeView.setText(time)

                Log.e("TimerStart2", time)

                // If running is true, increment the
                // seconds variable.
                //if (running) {
                    seconds++
                //}

                // Post the code again
                // with a delay of 1 second.
                handler.postDelayed(this, 1000)
            }
        })

    }

    private fun logoutApi()
    {

        MyProgressDialog.show(mContext)

        homeEndUserViewModel.logoutResponse!!.observe(
            this@HomeEndUserActivity
        ) { logoutResponse ->
            MyProgressDialog.dismiss()
            when (logoutResponse.status) {
                Resource.Status.SUCCESS -> {

                    Utils.logout()

                }
                Resource.Status.LOADING -> {
                }
                Resource.Status.ERROR -> {

                    Utils.logout()

                }
            }
        }


    }

    private fun initXmppIfNotInitialized() {
        if(MainApplication.connection == null)
        {

            if(SharedPref.readString(AppConstants.KEY_XMPP_USER_JID) != null &&
                !TextUtils.isEmpty(SharedPref.readString(AppConstants.KEY_XMPP_USER_JID)!!) &&
                SharedPref.readString(AppConstants.KEY_XMPP_USER_PASSWORD) != null &&
                !TextUtils.isEmpty(SharedPref.readString(AppConstants.KEY_XMPP_USER_PASSWORD)!!))
            {
                //Log.e("User", SharedPref.readString(AppConstants.KEY_XMPP_USER_JID)!!)
                //Log.e("Password", SharedPref.readString(AppConstants.KEY_XMPP_USER_PASSWORD)!!)
                MainApplication.connection = XmppCustomConnection(MainApplication.appContext, Utils.getUserFromJid(
                    SharedPref.readString(AppConstants.KEY_XMPP_USER_JID)!!), SharedPref.readString(
                    AppConstants.KEY_XMPP_USER_PASSWORD))
                MainApplication.connection!!.doConnect()
            }

        }
    }

    /**
     * It is used to fetch the firebase token and save to shared pref
     */
    private fun getFirebaseToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(
                        "HomeActivity",
                        "Fetching FCM registration token failed",
                        task.exception
                    )
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result!!
                SharedPref.writeString(AppConstants.KEY_FIREBASE_TOKEN, token)
                Log.e("Firebase token", token)
                sendTokenOnServer(token)
            })
    }

    /**
     * It is used to update the firebase token on server
     */
    private fun sendTokenOnServer(token: String) {

        val sendLocationWorkRequest =
            OneTimeWorkRequest.Builder(FCMUpdater::class.java) //pass data to work manager
                .setInputData(
                    Data.Builder()
                        .putString(AppConstants.FCM_TOKEN, token)
                        .build()
                )
                .build()

        WorkManager
            .getInstance(applicationContext)
            .enqueueUniqueWork(
                AppConstants.FCM_TOKEN,
                ExistingWorkPolicy.REPLACE,
                sendLocationWorkRequest
            )

    }

    private fun getAllCms()
    {
        homeEndUserViewModel.allCmsResponse!!.observe(
            this@HomeEndUserActivity
        ) { getAllCmsListResponse ->
            when (getAllCmsListResponse.status) {
                Resource.Status.SUCCESS -> {
                    if (getAllCmsListResponse.data!!.isSuccess) {
                        with(SharedPref) {
                            writeString(
                                                AppConstants.COLOR_SCHEME,
                                                getAllCmsListResponse.data.data?.colorScheme.toString()
                                            )
                            writeString(
                                                AppConstants.FONT_SCHEME,
                                                getAllCmsListResponse.data.data?.colorScheme.toString()
                                            )
                        }
                        //Log.e("ColorSchemes",SharedPref.readString(AppConstants.COLOR_SCHEME)!!)
                       /* if(!TextUtils.isEmpty(SharedPref.readString(AppConstants.COLOR_SCHEME)))
                        {
                            val colorSchemes = SharedPref.readString(AppConstants.COLOR_SCHEME)!!.split(",".toRegex()).toTypedArray()
                            val firstColor= colorSchemes[0]
                            val secondColor= colorSchemes[1]
                            Log.e("FirstColor", firstColor)
                            Log.e("SecondColor", secondColor)
                            ThemeUtility.setGradientThemeColorUsingDrawable(mContext, R.drawable.bg_button)
                            ThemeUtility.setGradientThemeColorUsingDrawable(mContext, R.drawable.bg_gradient)
                            ThemeUtility.setStatusBarThemeColor(window)
                        }*/

                    } else {
                        Utils.showToast(mContext, getAllCmsListResponse.data.message!!)
                    }
                }
                Resource.Status.LOADING -> {
                }
                Resource.Status.ERROR -> {
                    Utils.showToast(mContext, getAllCmsListResponse.message!!)
                }
            }
        }


    }

    fun loadUserInfo() {

        binding.layoutNavHeader.tvName.text= String.format(
            "%s %s", SharedPref.readString(
                AppConstants.KEY_FIRST_NAME
            ), SharedPref.readString(AppConstants.KEY_LAST_NAME)
        )

        binding.layoutNavItem.tvVersion.text= String.format(
            "%s %s", getString(R.string.version), Utils.getAppVersion(mContext)
        )

        Glide.with(mContext).load(SharedPref.readString(AppConstants.KEY_PROFILE_PHOTO)).placeholder(
            AppCompatResources.getDrawable(
                mContext,
                R.drawable.ic_profile
            )
        ).into(binding.layoutNavHeader.profileImage);

        /*if(SharedPref.readString(AppConstants.KEY_LANGUAGE).equals("hi"))
        {

            binding.layoutNavItem.imgEnglish.setImageResource(R.drawable.ic_radio_no)
            binding.layoutNavItem.imgHindi.setImageResource(R.drawable.ic_radio_yes)

        }
        else
        {

            binding.layoutNavItem.imgEnglish.setImageResource(R.drawable.ic_radio_yes)
            binding.layoutNavItem.imgHindi.setImageResource(R.drawable.ic_radio_no)

        }*/


    }

    private fun popupGroupHelp(arrayList:ArrayList<GroupHelpItem>) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(mContext)
        val customLayout: View = layoutInflater.inflate(R.layout.popup_group_help, null)
        builder.setView(customLayout)

        val recyclerGroupHelp: androidx.recyclerview.widget.RecyclerView = customLayout.findViewById(R.id.recyclerGroupHelp)
        val btnSave: Button = customLayout.findViewById(R.id.btnSave)
        val btnCancel: Button = customLayout.findViewById(R.id.btnCancel)

        recyclerGroupHelp.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
        groupHelpAdapter = GroupHelpAdapter(mContext, arrayList,this)
        recyclerGroupHelp.adapter= groupHelpAdapter
        btnSave.setOnClickListener {
            if(isSendNotificationValidate(arrayList))
            {
                var selectedId=""
                arrayList.forEach {
                    if(it.isSelected){
                        selectedId=selectedId+it.groupId.toString()+","
                    }
                }
                homeEndUserViewModel.groupIds.value= selectedId
                MyProgressDialog.show(mContext)
               homeEndUserViewModel.sendHelpNotificationResponse!!.observe(
                    this
                ) { commonResponse ->
                    MyProgressDialog.dismiss()
                    when (commonResponse.status) {
                        Resource.Status.SUCCESS -> {
                            if (commonResponse.data!!.isSuccess) {
                                Utils.showToast(mContext, commonResponse.data.message!!)
                                dialog!!.dismiss()

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

        btnCancel.setOnClickListener {
            dialog!!.dismiss()

        }

        dialog = builder.create()
        dialog!!.setCancelable(false)
        dialog!!.show()
        ThemeUtility.setGradientThemeColorUsingDrawable(mContext, R.drawable.bg_button)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    }


    private fun isSendNotificationValidate(arrayList:ArrayList<GroupHelpItem>) : Boolean
    {
       var isChecked=false
        arrayList.forEach {
            if(it.isSelected){
                isChecked=true
                return@forEach
            }
        }
        if(!isChecked)
        {
            Utils.showToast(mContext, getString(R.string.please_select_group))
            return false
        }
        return true
    }


    /*private fun updateFirebaseToken()
    {

        homeEndUserViewModel.fcmToken.value= "fcmtoken"

        homeEndUserViewModel.updateFcmTokenResponse!!.observe(
            this@HomeEndUserActivity,
            Observer<Resource<CommonResponse?>> { commonResponseResource ->

                when (commonResponseResource.status) {
                    Resource.Status.SUCCESS -> {
                        if (commonResponseResource.data!!.isSuccess) {



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
            })


    }*/

    private fun forceUpgradeApp()
    {

        homeEndUserViewModel.forceUpgradeAppResponse!!.observe(
            this@HomeEndUserActivity,
            Observer<Resource<ForceUpgradeResponse?>> { forceUpgradeResponseResource ->

                when (forceUpgradeResponseResource.status) {
                    Resource.Status.SUCCESS -> {
                        if (forceUpgradeResponseResource.data!!.isSuccess) {

                            val appVersion: String? = Utils.getAppVersion(mContext)
                            val newVersionDevice = appVersion!!.replace(".", "")
                            var serverVersion: String?= forceUpgradeResponseResource.data.data!!.androidAppVersion
                            serverVersion.let {

                                val newVersionServer= serverVersion!!.replace(".", "")
                                //Log.d("NewDeviceVer", newVersionDevice)
                                //Log.d("NewServerVer", newVersionServer)
                                if (newVersionDevice.toInt() < newVersionServer.toInt()) {
                                    Utils.forceUpdate(mContext)
                                }

                            }

                        } else {
                            Utils.showToast(mContext, forceUpgradeResponseResource.data.message!!)
                        }
                    }
                    Resource.Status.LOADING -> {
                    }
                    Resource.Status.ERROR -> {
                        Utils.showToast(mContext, forceUpgradeResponseResource.message!!)
                    }
                }
            })


    }


    //open side drawer
    fun openDrawer() {
        if (drawer != null) {
            drawer.openDrawer(GravityCompat.START)
        }
    }

    //close side drawer
    fun closeDrawer() {
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START)
        }
    }

    override fun clickHandle(v: View) {
        when(v.id) {

            R.id.tvChat -> {

                binding.tvChat.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_chats_selected, 0, 0)
                binding.tvSetting.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_settings_unselected, 0, 0)
                binding.tvChat.setTextColor(ContextCompat.getColor(mContext, R.color.bottom_nav_txt_selected))
                //binding.tvHelp.setTextColor(ContextCompat.getColor(mContext, R.color.bottom_nav_txt_unselected))
                binding.tvSetting.setTextColor(ContextCompat.getColor(mContext, R.color.bottom_nav_txt_unselected))

                clearAllFragments()
                replaceFragmentWithoutBackStack(ChatFragment(), R.id.frame_container)

            }

            R.id.btnHelp -> {

                /*binding.tvChat.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_chats_unselected, 0, 0)
                binding.tvSetting.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_settings_unselected, 0, 0)
                binding.tvChat.setTextColor(ContextCompat.getColor(mContext, R.color.bottom_nav_txt_unselected))
                binding.tvHelp.setTextColor(ContextCompat.getColor(mContext, R.color.bottom_nav_txt_selected))
                binding.tvSetting.setTextColor(ContextCompat.getColor(mContext, R.color.bottom_nav_txt_unselected))

                clearAllFragments
                replaceFragmentWithoutBackStack(HelpFragment(), R.id.frame_container)
*/
            }

            R.id.tvSetting -> {

                binding.tvChat.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_chats_unselected, 0, 0)
                binding.tvSetting.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_settings_selected, 0, 0)
                binding.tvChat.setTextColor(ContextCompat.getColor(mContext, R.color.bottom_nav_txt_unselected))
                //binding.tvHelp.setTextColor(ContextCompat.getColor(mContext, R.color.bottom_nav_txt_unselected))
                binding.tvSetting.setTextColor(ContextCompat.getColor(mContext, R.color.bottom_nav_txt_selected))

                clearAllFragments()
                replaceFragmentWithoutBackStack(SettingFragment(), R.id.frame_container)

            }

            R.id.layoutMyProfile -> {

                replaceFragment(MyProfileFragment(), R.id.frame_container)
                closeDrawer()

            }

            R.id.layoutLogout -> {

                logout()

            }

        }
    }

    fun logout() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(mContext)
        dialog.setMessage(getString(R.string.are_you_sure_logout))
        dialog.setPositiveButton(
            getString(R.string.yes)
        ) { dialog, which ->

            dialog.dismiss()

            if(Utils.isNetConnected(mContext))
            {
                logoutApi()
            }
            else{
                Utils.showToast(mContext, getString(R.string.no_internet))
            }



        }
        dialog.setNegativeButton(
            getString(R.string.no)
        ) { dialog, which -> dialog.dismiss() }
        val alert = dialog.create()
        alert.show()
    }

    //to remove all the fragments from the stack
    private fun clearAllFragments() {
        val fragmentManager: FragmentManager = supportFragmentManager
        val backstackCount: Int = fragmentManager.backStackEntryCount
        if (backstackCount > 0) {
            for (i in 0 until backstackCount) {
                // fragmentManager.popBackStackImmediate();
                fragmentManager.popBackStack()
            }
        }
    }

    @SuppressLint("InlinedApi")
    private fun checkPermissions() {

        /*if(PermissionUtils.haveAllPermissions(mContext)) {
            Log.e("h", "h")

        }*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            rxPermissions.requestEachCombined(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_PHONE_STATE)
                .subscribe { permission ->

                    if (permission.granted) {
                        // All permissions are granted !
                        //Utils.showToast(context!!, "Permission granted")

                        startLocationDetectorService()

                        //read phone state permission required
                      /*  val callManager = CallHandler(applicationContext)
                        if (!callManager.checkIsAccountEnabled()) {
                            val intent = Intent()
                            intent.action = TelecomManager.ACTION_CHANGE_PHONE_ACCOUNTS
                            val componentName = ComponentName(
                                "com.android.server.telecom",
                                "com.android.server.telecom.settings.EnableAccountPreferenceActivity"
                            )
                            intent.component = componentName
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            mContext.startActivity(intent)
                        }
*/
                        Log.e("CheckPermission", "CheckPermission")

                    } else if (permission.shouldShowRequestPermissionRationale) {
                        // At least one denied permission without ask never again

                        Utils.showToast(this, "Permission denied ${permission.name}")
                    } else {
                        // At least one denied permission with ask never again
                        // Need to go to the settings
                        //Utils.showToast(context!!, "Permission denied permanently")
                        openSettings()
                    }

                }

        }
        else
        {

            rxPermissions.requestEachCombined(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_PHONE_STATE,
            )
                .subscribe { permission ->

                    if (permission.granted) {
                        // All permissions are granted !
                        //Utils.showToast(context!!, "Permission granted")

                        startLocationDetectorService()

                        //read phone state permission required
                       /* val callManager = CallHandler(applicationContext)
                        if (!callManager.checkIsAccountEnabled()) {
                            val intent = Intent()
                            intent.action = TelecomManager.ACTION_CHANGE_PHONE_ACCOUNTS
                            val componentName = ComponentName(
                                "com.android.server.telecom",
                                "com.android.server.telecom.settings.EnableAccountPreferenceActivity"
                            )
                            intent.component = componentName
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            mContext.startActivity(intent)
                        }*/

                        Log.e("CheckPermission", "CheckPermission")

                    } else if (permission.shouldShowRequestPermissionRationale) {
                        // At least one denied permission without ask never again
                        //Utils.showToast(context!!, "Permission denied")
                    } else {
                        // At least one denied permission with ask never again
                        // Need to go to the settings
                        //Utils.showToast(context!!, "Permission denied permanently")
                        openSettings()
                    }

                }

        }



        /*if(PermissionUtils.haveAllPermissions(mContext)) {
            Log.e("h", "h")

        } else {
            ActivityCompat.requestPermissions(
                this,
                PERMISSIONS_ALL,
                PERMISSION_ALL_REQUEST_CODE
            )
        }*/
    }

    /**
     * open mobile settings
     */
    private fun openSettings()
    {
        AlertDialog.Builder(mContext)
            .setPositiveButton(R.string.ok) { _, _ ->

                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                val uri: Uri = Uri.fromParts("package", mContext!!.getPackageName(), null)
                intent.data = uri
                startActivity(intent)

            }
            .setCancelable(true)
            .setMessage(R.string.permission_denied_msg)
            .show()
    }

    fun startLocationDetectorService() {
        val locationService = Intent(mContext, LocationDetectorService::class.java)
        //locationService.putExtra(AppConstants.IntentConstants.RIDE_ID, String.valueOf(rideId));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(mContext, locationService)
        } else {
            mContext.startService(locationService)
        }
    }

    fun stopLocationDetectorService() {
        val locationService = Intent(mContext, LocationDetectorService::class.java)
        mContext.stopService(locationService)
    }

    private fun registerPhoneAccount() {
        val telecomManager: TelecomManager =
            mContext.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        val componentName = ComponentName(mContext, CallConnectionService::class.java)
        val phoneAccountHandle = PhoneAccountHandle(componentName, "123456")
        val phoneAccount =
            PhoneAccount.builder(phoneAccountHandle, mContext.getString(R.string.app_name))
                .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER).build()
        telecomManager.registerPhoneAccount(phoneAccount)
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            closeDrawer()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        MainApplication.connection?.disconnectXmpp("dis")
        MainApplication.connection = null
        super.onDestroy()
    }

    override fun itemClickHandle(item: GroupHelpItem?, isChecked: Boolean?, position: Int) {
        if (item != null) {
            if (isChecked != null) {
                item.isSelected=isChecked
            }
            groupHelpItemList[position] = item
        }
    }


}