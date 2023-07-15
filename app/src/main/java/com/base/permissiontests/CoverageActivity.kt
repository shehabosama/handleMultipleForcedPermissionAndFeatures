package com.base.permissiontests

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dmgdesignuk.locationutils.easyaddressutility.EasyAddressUtility
import com.dmgdesignuk.locationutils.easylocationutility.EasyLocationUtility
import com.dmgdesignuk.locationutils.easylocationutility.LocationRequestCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class CoverageActivity : AppCompatActivity() {
    var locationUtility: EasyLocationUtility?=null
    var addressUtility: EasyAddressUtility?=null
    var googleMap: GoogleMap? = null
    var tvLocationNames:TextView? = null
    private var numUpdates = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coverage)
        tvLocationNames = findViewById(R.id.locationTExtView)
        locationUtility = EasyLocationUtility(this)
        addressUtility = EasyAddressUtility(this)
        setUpMap()
        supportFragmentManager.beginTransaction().add(R.id.fragment_container,MapFragment(),"MY_FRAGMENT").commit()

    }
    fun getLocationUpdates(tvLocationName: TextView) {

        // OPTIONAL: Use the setLocationRequestParams() method to change the default location
        //           request values. Here we're setting up the location request with a target update
        //           interval of 10 seconds and a fastest update interval cap of 5 seconds using the
        //           high accuracy power priority.
        locationUtility?.setLocationRequestParams(
            10000,
            5000,
            LocationRequest.PRIORITY_HIGH_ACCURACY
        )

        // Check the permissions
        if (locationUtility?.permissionIsGranted()!!) {

            // Check device settings
            locationUtility?.checkDeviceSettings(EasyLocationUtility.RequestCodes.CURRENT_LOCATION_UPDATES)

            // Request location updates
            locationUtility?.getCurrentLocationUpdates(object : LocationRequestCallback {
                override fun onLocationResult(location: Location) {


                    // Increment the counter every time a location update is received
                    numUpdates++
                    //                    logOutputText.setText(getString(R.string.output_updates, String.valueOf(numUpdates)));
                    getAddressElementsFromLocation(location, tvLocationName)
                }

                override fun onFailedRequest(result: String) {

                    // Location request failed, output the error.
                    tvLocationName.text = "" + result
                }
            })
        } else {

            // Permission not granted, ask for it
            locationUtility?.requestPermission(EasyLocationUtility.RequestCodes.CURRENT_LOCATION_UPDATES)
        }
    }
    val MAP_DEFAULT_LOCATION = LatLng(25.2869397, 55.3509348)
    fun getAddressElementsFromLocation(location: Location?, tvLocationName: TextView) {
        val street = addressUtility!!.getAddressElement(
            EasyAddressUtility.AddressCodes.STREET_NAME,
            location
        )
        val city =
            addressUtility!!.getAddressElement(EasyAddressUtility.AddressCodes.CITY_NAME, location)

        // Best practice is to place all Strings in a String resource xml file as we have done elsewhere, but
        // for the sake of example we'll just hard-code these ones.
        tvLocationNames = tvLocationName
        if (tvLocationNames == null) {
          //  tvLocationNames = getLocatioNameTextView()
        }
        if (tvLocationNames != null) {
            var fullPath: String? = ""
            if (street != null) {
                fullPath += street
            }
            if (city != null) {
                fullPath += " $city"
            }
            tvLocationNames?.setText(fullPath)
        }
    }
    protected fun getMapView(): GoogleMap? {
        return googleMap
    }
    private fun setUpMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        val mapFragment = supportFragmentManager
//            .findFragmentById(R.id.mapView) as SupportMapFragment
//        mapFragment.getMapAsync { googleMap: GoogleMap ->
//            val center = CameraUpdateFactory.newLatLng(MAP_DEFAULT_LOCATION)
//            val zoom = CameraUpdateFactory.zoomTo(17f)
//            this.googleMap = googleMap
//            googleMap.moveCamera(center)
//            googleMap.animateCamera(zoom)
//            if (ContextCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                // TODO: Consider calling
//                //    Activity#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for Activity#requestPermissions for more details.
//                return@getMapAsync
//            }
//            googleMap.isMyLocationEnabled = true
//            googleMap.setOnMyLocationChangeListener { location: Location ->
//                // TODO Auto-generated method stub
//                googleMap.clear()
//                googleMap.addMarker(
//                    MarkerOptions().position(
//                        LatLng(
//                            location.latitude,
//                            location.longitude
//                        )
//                    ).title("")
//                )
//            }
//        }
    }
    fun getLastLocation(map: GoogleMap, tvLocationName: TextView?) {

        // First, check the user has granted the required permission.
        if (locationUtility!!.permissionIsGranted()) {

            // Permission is already granted. First, we'll check the required device location settings
            // are satisfied. If they are not the user will automatically be prompted to enable them.
            // The result of this can be checked and handled by implementing and overriding the
            // onActivityResult callback in your calling Activity. The request code we're passing in
            // can be tested for in onActivityResult to determine where the request originated.
            locationUtility!!.checkDeviceSettings(EasyLocationUtility.RequestCodes.LAST_KNOWN_LOCATION)

            // Now we can request the last known location from the device's cache.
            locationUtility!!.getLastKnownLocation(object : LocationRequestCallback {
                override fun onLocationResult(location: Location) {
                    googleMap = map
                    if (googleMap == null) {
                        googleMap = getMapView()
                    }
                    googleMap!!.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                location.latitude,
                                location.longitude
                            ), 12.0f
                        )
                    )
                    if (tvLocationName == null) {
                       // tvLocationNames = getLocatioNameTextView()
                    }
                    getAddressElementsFromLocation(location, tvLocationNames!!)
                }

                override fun onFailedRequest(result: String) {

                    //       toast(getString(R.string.error_location_update), BaseActivity.ToastType.ERROR);
                }
            })
        } else {

            // Permission not granted, ask for it. You must implement and override the
            // onRequestPermissionsResult callback method in your calling Activity in order to handle
            // the result of the request.
            // Here we're passing in a request code that corresponds to the type of location request
            // we're attempting to make. We can test for the result of this specific request in the
            // onRequestPermissionResult implementation.
            locationUtility!!.requestPermission(EasyLocationUtility.RequestCodes.LAST_KNOWN_LOCATION)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != RESULT_OK) {

            // The user has either denied or cancelled out of the request to change device
            // settings so you will need to handle for that here.
            Log.d(
               "TAG",
                "Unable to proceed: required device settings not satisfied"
            )
        } else {

            // Location settings have been enabled, query the incoming request code to determine
            // which request we're responding to and take the appropriate action.
            when (requestCode) {
                EasyLocationUtility.RequestCodes.LAST_KNOWN_LOCATION ->                     // Carry on where we left off...
                    getMapView()?.let { getLastLocation(it,null) }

                EasyLocationUtility.RequestCodes.CURRENT_LOCATION_UPDATES -> {}
                else -> {}
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPause() {
        super.onPause()

        // No need to check if location updates are currently being received as the
        // stopLocationUpdates() method will take care of that.
        locationUtility?.stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()

        // Once again, no need for any state checks as the resumeLocationUpdates() method
        // will take care of it.
        locationUtility?.resumeLocationUpdates()
    }
}