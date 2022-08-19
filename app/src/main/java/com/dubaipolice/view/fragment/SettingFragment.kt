package com.dubaipolice.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.bumptech.glide.Glide
import com.dubaipolice.R
import com.dubaipolice.callback.HandleClick
import com.dubaipolice.databinding.FragmentSettingBinding
import com.dubaipolice.downloader.DownloadWorker
import com.dubaipolice.downloader.MapDownloadWorker
import com.dubaipolice.extensions.replaceFragment
import com.dubaipolice.utils.*
import com.dubaipolice.view.activity.HomeEndUserActivity
import com.dubaipolice.view.activity.NotificationActivity
import com.here.sdk.core.engine.SDKNativeEngine
import com.here.sdk.maploader.*
import kotlinx.coroutines.launch


class SettingFragment : Fragment(), HandleClick {

    private lateinit var workManager: WorkManager

    //lateinit var homeViewModel: HomeViewModel? = null
    lateinit var binding: FragmentSettingBinding
    lateinit var mContext: Context
    private lateinit var mapUpdater: MapUpdater
    private lateinit var mapDownloader: MapDownloader

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_setting, container, false)

        MyProgressDialog.dismiss()

        setUpMapRequirement()


        return binding.root
    }

    private fun setUpMapRequirement() {
        val sdkNativeEngine = SDKNativeEngine.getSharedInstance()
            ?: throw java.lang.RuntimeException("SDKNativeEngine not initialized.")

        mapDownloader = MapDownloader.fromEngine(sdkNativeEngine)
        mapUpdater = MapUpdater.fromEngine(sdkNativeEngine)
        // Note that the default storage path can be adapted when creating a new SDKNativeEngine.
        val storagePath = sdkNativeEngine.options.cachePath
        Log.d("TAG", "StoragePath: $storagePath")

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mContext = binding.root.context

        //homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        binding.lifecycleOwner = this@SettingFragment
        //binding.setHomeViewModel(homeViewModel)
        binding.clickHandle = this

        loadUserInfo()

        ThemeUtility.setGradientThemeColorUsingDrawable(mContext, R.drawable.bg_button)

    }

    private fun loadUserInfo() {

        binding.tvName.text = String.format(
            "%s %s", SharedPref.readString(
                AppConstants.KEY_FIRST_NAME
            ), SharedPref.readString(AppConstants.KEY_LAST_NAME)
        )
        binding.tvEmail.text = SharedPref.readString(AppConstants.KEY_USER_NAME)

        binding.tvYesNo.text = if(SharedPref.readBoolean(AppConstants.KEY_IS_APP_MAP_DOWNLOADED)) getString(R.string.yes) else getString(R.string.no)

        Glide.with(mContext).load(SharedPref.readString(AppConstants.KEY_PROFILE_PHOTO))
            .placeholder(
                AppCompatResources.getDrawable(
                    mContext,
                    R.drawable.ic_profile
                )
            ).into(binding.profileImage)

    }


    override fun clickHandle(v: View) {
        when (v.id) {

            R.id.imgMenu -> {

                val activity: HomeEndUserActivity? = activity as HomeEndUserActivity?
                activity?.openDrawer()

            }


            R.id.imgNotification -> {

                startActivity(Intent(mContext, NotificationActivity::class.java))

            }

            R.id.imgEdit -> {

                replaceFragment(MyProfileFragment(), R.id.frame_container)

            }

            R.id.layoutLanguage -> {

                replaceFragment(LanguageFragment(), R.id.frame_container)

            }

            R.id.layoutNotifications -> {

                replaceFragment(NotificationSettingFragment(), R.id.frame_container)

            }

            R.id.layoutPasscode -> {
                replaceFragment(PasscodeChangeFragment(), R.id.frame_container)
            }
            R.id.layout_offline_map -> {
                if (Utils.isNetConnected(requireContext())) {
                    if (!SharedPref.readBoolean(AppConstants.KEY_IS_APP_MAP_DOWNLOADED)) startMapDownloader()
                    else {
                        checkForMapUpdates()
                        checkInstallationStatus()
                    }
                }else Utils.showToast(requireContext(),getString(R.string.no_internet))
            }

        }
    }

    private fun checkForMapUpdates() {
        mapUpdater.checkMapUpdate { mapLoaderError: MapLoaderError?, mapUpdateAvailability: MapUpdateAvailability? ->
            if (mapLoaderError != null) {
                Utils.showToast(mContext, mapLoaderError.name)
            }
            if (mapUpdateAvailability == MapUpdateAvailability.AVAILABLE) {
                Utils.showToast(mContext, "One or more map updates are available")
                startMapDownloader()
            }
        }
    }

    private fun checkInstallationStatus() {
        // Note that this value will not change during the lifetime of an app.
        lifecycleScope.launch {
            val persistentMapStatus = mapDownloader.initialPersistentMapStatus
            if (persistentMapStatus != PersistentMapStatus.OK) {
                // Let's try to repair.
                mapDownloader.repairPersistentMap { persistentMapRepairError: PersistentMapRepairError? ->
                    if (persistentMapRepairError == null) {
                        return@repairPersistentMap
                    }
                }
            }
        }

    }

    private fun startMapDownloader() {
        workManager = WorkManager.getInstance(mContext)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val downloadRequest = OneTimeWorkRequest
            .Builder(MapDownloadWorker::class.java)
            .setConstraints(constraints)
            .build()
        workManager.enqueueUniqueWork(
            System.currentTimeMillis().toString(),
            ExistingWorkPolicy.APPEND,
            downloadRequest
        )

    }


}