package com.dubaipolice.view.activity

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dubaipolice.R
import com.dubaipolice.api.ApiClient
import com.dubaipolice.callback.HandleClick
import com.dubaipolice.db.model.GroupInfoTable
import com.dubaipolice.maputils.OfflineMap
import com.dubaipolice.maputils.PermissionsRequester
import com.dubaipolice.model.GroupInfoResponse
import com.dubaipolice.model.Member
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.LocationUtils
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.utils.Utils
import com.dubaipolice.viewmodel.GroupInfoViewModel
import com.here.sdk.mapview.MapScheme
import com.here.sdk.mapview.MapView
import com.tbruyelle.rxpermissions3.RxPermissions
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.DelicateCoroutinesApi
import java.util.concurrent.TimeUnit


@DelicateCoroutinesApi
@Suppress("DEPRECATION")
class MapLocationActivity : AppCompatActivity(), HandleClick, LocationUtils.LocationEnable {

    private lateinit var disposable: Disposable
    private lateinit var location: Location
    private lateinit var locationUtils: LocationUtils
    private lateinit var permissionsRequester: PermissionsRequester

    lateinit var mContext: Context

    private var mInstanceActivated = false

    /*HERE Map SDK*/
    private val TAG: String? = MapLocationActivity::class.java.simpleName
    private lateinit var offlineMap: OfflineMap
    private var groupData: GroupInfoTable? = null
    private lateinit var mapView: MapView
    private lateinit var groupInfoViewModel: GroupInfoViewModel

    lateinit var rxPermissions: RxPermissions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_location)
        // Get a MapView instance from the layout.
        mapView = findViewById(R.id.map_view)
        val extras = intent.extras
        extras.let {
            groupData =
                extras?.getSerializable(AppConstants.IntentConstants.GROUP_DATA) as GroupInfoTable
        }
        groupInfoViewModel = ViewModelProvider(this)[GroupInfoViewModel::class.java]

        mContext = this
        locationUtils = LocationUtils(this)

        try {
            mapView.onCreate(savedInstanceState)
        } catch (e: Exception) {
            mapView.onDestroy()
            mapView.onCreate(savedInstanceState)
        }
        rxPermissions = RxPermissions(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            rxPermissions.request(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                .subscribe { granted ->
                    if (!granted) {
                        openSettings()
                    }
                }
        }
    }

    private fun openSettings() {
        AlertDialog.Builder(mContext)
            .setPositiveButton(R.string.ok) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                val uri: Uri = Uri.fromParts("package", mContext.packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setCancelable(true)
            .setMessage(R.string.permission_denied_msg)
            .show()
    }


    override fun onResume() {
        super.onResume()
        mapView.onResume()
        if (this::disposable.isInitialized)
            if (disposable.isDisposed) {
                disposable = Observable.interval(5000, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::observeResponse, this::onError)
            }
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        if (this::disposable.isInitialized)
            disposable.dispose()
    }

    private fun observeResponse(time: Long) {
        val observable: Observable<GroupInfoResponse> = ApiClient.request!!.getGroupData(
            Utils.getSelectedLanguage()!!,
            SharedPref.readString(AppConstants.KEY_TOKEN)!!,
            groupData?.groupId.toString()
        )
        observable.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
            .map { result: GroupInfoResponse -> result.data.groupDetails.members }
            .subscribe(this::handleResults, this::showOffline)
    }

    private fun handleResults(members: List<Member>) {
        if (members.isNotEmpty())
            offlineMap.showMapMarkers(members)
    }

    private fun showOffline(t: Throwable? = null) {
        groupInfoViewModel.getGroupMemberLocation(groupData?.groupId.toString())?.observe(this) {
            offlineMap.showLocalMembersMarkers(it)
        }
    }

    private fun onError(throwable: Throwable) {
        throwable.printStackTrace()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AppConstants.Permissions.LOCATION_PERMISSION_REQUEST_CODE)
            locationUtils.startLocationUpdates()
    }

    private fun loadMapScene() {
        mapView.mapScene.loadScene(MapScheme.NORMAL_DAY) { mapError ->
            if (mapError == null) {
                offlineMap = OfflineMap(location)
                offlineMap.setUpMap(mapView, this, this)

                if (Utils.isNetConnected(this)) {
                    disposable = Observable.interval(5000, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::observeResponse, this::onError)
                } else showOffline()

            } else {
                Log.d(TAG, "onLoadScene failed: $mapError")
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if(::offlineMap.isInitialized)
        {
            offlineMap.stopLocating()
        }
        if(::mapView.isInitialized)
        {
            mapView.onDestroy()
        }
        if(::locationUtils.isInitialized)
        {
            locationUtils.stopLocationUpdates()
        }

    }

    override fun clickHandle(v: View) {
        when (v.id) {
            R.id.back -> {
                onBackPressed()
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AppConstants.Permissions.REQUEST_CHECK_LOCATION_SETTINGS ->
                if (resultCode == RESULT_OK && data != null) {
                    locationUtils.startLocationUpdates()
                }
        }
    }

    override fun onLocationFound(location: Location) {
        this.location = location
        loadMapScene()
    }


}