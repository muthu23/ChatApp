package com.dubaipolice.maputils

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.dubaipolice.R
import com.dubaipolice.db.model.MemberInfoTable
import com.dubaipolice.model.LastKnownLocation
import com.dubaipolice.model.Member
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.IndeterminateProgressDialog
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.utils.Utils
import com.dubaipolice.view.activity.MapLocationActivity
import com.here.sdk.consent.Consent
import com.here.sdk.consent.ConsentEngine
import com.here.sdk.core.*
import com.here.sdk.core.engine.SDKNativeEngine
import com.here.sdk.core.errors.InstantiationErrorException
import com.here.sdk.gestures.TapListener
import com.here.sdk.location.*
import com.here.sdk.maploader.*
import com.here.sdk.mapview.*
import com.here.sdk.mapview.MapViewBase.PickMapItemsCallback
import com.here.sdk.search.OfflineSearchEngine
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


@DelicateCoroutinesApi
class OfflineMap(currentLocation: android.location.Location) {

    private lateinit var locationStatusListener: LocationStatusListener
    private lateinit var locationListener: LocationListener
    private val TAG: String = OfflineMap::class.java.simpleName

    private lateinit var mapView: MapView
    private lateinit var context: Context

    private lateinit var offlineSearchEngine: OfflineSearchEngine


    private var poiMapCircle: MapPolygon? = null
    private val CAMERA_DISTANCE_IN_METERS = 8000

    private lateinit var locationIndicator: LocationIndicator
    private lateinit var locationEngine: LocationEngine
    private lateinit var consentEngine: ConsentEngine

    private var defaultCoordinates =
        GeoCoordinates(currentLocation.latitude, currentLocation.longitude)
    private val mapMarkerList = emptyList<MapMarker>().toMutableList()


    fun setUpMap(mapView: MapView, context: Context, mapLocationActivity: MapLocationActivity) {
        this.mapView = mapView
        this.context = context

        offlineSearchEngine = try {
            OfflineSearchEngine()
        } catch (e: InstantiationErrorException) {
            throw RuntimeException("Initialization of OfflineSearchEngine failed: " + e.error.name)
        }

        try {
            consentEngine = ConsentEngine()
            locationEngine = LocationEngine()
        } catch (e: InstantiationErrorException) {
            throw java.lang.RuntimeException("Initialization failed: " + e.message)
        }
        if (consentEngine.userConsentState == Consent.UserReply.NOT_HANDLED) {
            consentEngine.requestUserConsent()
        }

        locationListener =
            LocationListener { location: Location? ->
                location?.let {
                    updateMyLocationOnMap(it)
                }
            }

        locationStatusListener = object : LocationStatusListener {
            override fun onStatusChanged(locationEngineStatus: LocationEngineStatus) {
                if (locationEngineStatus == LocationEngineStatus.ENGINE_STOPPED) {
                    locationEngine.removeLocationListener(locationListener)
                    locationEngine.removeLocationStatusListener(this)
                } else if (locationEngineStatus == LocationEngineStatus.ENGINE_STARTED) {
                    locationEngine.addLocationListener(locationListener)
                    locationEngine.addLocationStatusListener(this)
                }
            }

            override fun onFeaturesNotAvailable(features: List<LocationFeature>) {
                for (feature in features) {
                    Log.d(TAG, "Feature not available: " + feature.name)
                }
            }
        }
        startLocating()

        val myLastLocation = locationEngine.lastKnownLocation
        if (myLastLocation != null) {
            addMyLocationToMap(myLastLocation)
        } else {
            val defaultLocation = Location(defaultCoordinates)
            defaultLocation.time = Date()
            addMyLocationToMap(defaultLocation)
        }
        setTapGestureHandler()
    }


    private fun setTapGestureHandler() {
        mapView.gestures.tapListener = TapListener { touchPoint -> pickMapMarker(touchPoint) }
    }

    private fun pickMapMarker(touchPoint: Point2D) {
        val radiusInPixel = 2f
        mapView.pickMapItems(touchPoint, radiusInPixel.toDouble(),
            PickMapItemsCallback { pickMapItemsResult ->
                if (pickMapItemsResult == null) {
                    // An error occurred while performing the pick operation.
                    return@PickMapItemsCallback
                }
                // Note that 3D map markers can't be picked yet. Only marker, polygon and polyline map items are pickable.
                val mapMarkerList = pickMapItemsResult.markers
                val listSize = mapMarkerList.size
                if (listSize == 0) {
                    return@PickMapItemsCallback
                }
                val topmostMapMarker = mapMarkerList[0]
                val metadata = topmostMapMarker.metadata
                if (metadata != null) {
                    var message = "No message found."
                    val string = metadata.getString("user")
                    if (string != null) {
                        message = string
                    }
                    showDialog("Map marker picked", message)
                    return@PickMapItemsCallback
                }
            })
    }

    private fun startLocating() {
        locationEngine.addLocationStatusListener(locationStatusListener)
        locationEngine.addLocationListener(locationListener)
        locationEngine.start(LocationAccuracy.BEST_AVAILABLE)
    }

    fun stopLocating() {
        locationEngine.stop()
    }

    private fun addMyLocationToMap(myLocation: Location) {
        locationIndicator = LocationIndicator()
        locationIndicator.locationIndicatorStyle = LocationIndicator.IndicatorStyle.PEDESTRIAN
        locationIndicator.updateLocation(myLocation)
        mapView.addLifecycleListener(locationIndicator)
        mapView.camera.lookAt(myLocation.coordinates, CAMERA_DISTANCE_IN_METERS.toDouble())

    }

    private fun updateMyLocationOnMap(myLocation: Location) {
        locationIndicator.updateLocation(myLocation)
    }

    private fun addCircleMapMarker(geoCoordinates: GeoCoordinates) {
        val mapImage = MapImageFactory.fromResource(context.resources, R.drawable.circle)
        val mapMarker = MapMarker(geoCoordinates, mapImage)
        mapView.mapScene.addMapMarker(mapMarker)
        mapMarkerList.add(mapMarker)
    }

    private fun addPOIMapMarker(geoCoordinates: GeoCoordinates, name: String, userId: String) {
        val mapImage = MapImageFactory.fromResource(context.resources, R.drawable.poi)
        val anchor2D = Anchor2D(0.5, 1.0)
        val mapMarker = MapMarker(geoCoordinates, mapImage, anchor2D)
        val metadata = Metadata()
        val userData = name.plus(" ID: ").plus(userId)
        metadata.setString("user", userData)
        mapMarker.metadata = metadata
        mapView.mapScene.addMapMarker(mapMarker)
        mapMarkerList.add(mapMarker)
    }

    private fun clearMap() {
        if (mapMarkerList.isNotEmpty())
            mapView.mapScene.removeMapMarkers(mapMarkerList)
        mapMarkerList.clear()
    }

    fun showMapMarkers(members: List<Member>) {
        clearMap()
        for (member in members) {
            val lastLocation: LastKnownLocation? = member.user.lastKnownLocation
            lastLocation?.let {
                val geoCoordinates = GeoCoordinates(
                    lastLocation.latitude.toDouble(),
                    lastLocation.longitude.toDouble()
                )

                if(SharedPref.readInt(AppConstants.KEY_USER_ID) != member.user.user_id) {
                    addCircleMapMarker(geoCoordinates)
                    addPOIMapMarker(
                        geoCoordinates,
                        member.user.first_name,
                        member.member_id.toString()
                    )
                }
            }

        }
    }

    fun showLocalMembersMarkers(members: List<MemberInfoTable>) {
        for (member in members) {
            if (member.latitude != null) {
                val geoCoordinates = GeoCoordinates(
                    member.latitude!!.toDouble(),
                    member.longitude!!.toDouble()
                )

                if (SharedPref.readInt(AppConstants.KEY_USER_ID) != member.userId) {
                    addCircleMapMarker(geoCoordinates)
                    addPOIMapMarker(
                        geoCoordinates,
                        member.firstName.toString(),
                        member.userId.toString()
                    )
                }
            }
        }
    }

    private fun showDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.show()
    }


}