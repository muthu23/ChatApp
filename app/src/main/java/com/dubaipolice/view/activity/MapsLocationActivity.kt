package com.dubaipolice.view.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.dubaipolice.R
import com.dubaipolice.callback.HandleClick
import com.dubaipolice.databinding.ActivityMapsLocationBinding
import com.dubaipolice.databinding.ActivityProfilePictureBinding
import com.dubaipolice.viewmodel.ProfileViewModel
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView

class MapsLocationActivity : AppCompatActivity(), HandleClick {

    lateinit var binding: ActivityMapsLocationBinding

    lateinit  var mContext: Context

    private var mapView: MapView? = null

    val styleUrl = "https://dbmapdev.iworklab.com/api/maps/dubai/style.json";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, "TOKEN")
        binding = DataBindingUtil.setContentView(this@MapsLocationActivity, R.layout.activity_maps_location)
       
        binding.lifecycleOwner= this@MapsLocationActivity
        //binding.loginViewModel= loginViewModel
        binding.clickHandle= this

        mContext = this

        initializeMap(savedInstanceState)

    }

    private fun initializeMap(savedInstanceState: Bundle?) {

        // Create map view
        mapView = binding.mapView
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync { map ->
            // Set the style after mapView was loaded
            map.setStyle(styleUrl) {
                map.uiSettings.setAttributionMargins(15, 0, 0, 15)
                // Set the map view center
                map.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(25.2048, 55.2708))
                    .zoom(4.0)
                    .build()

            }
        }

    }


    override fun clickHandle(v: View) {

        when(v.id) {

            R.id.back -> {

                onBackPressed()

            }

        }

    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

}