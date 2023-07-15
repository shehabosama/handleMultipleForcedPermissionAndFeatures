package com.base.permissiontests

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MapFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MapFragment : Fragment(), OnMapReadyCallback {
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    protected var mLastLocation: Location? = null
    private var btnClose: Button? = null
    private var lat = 0.0
    private  var lang:kotlin.Double = 0.0
    var mMapView: MapView? = null
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 111
    private var textView: TextView?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //  mMapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        MapsInitializer.initialize(requireActivity())
        mMapView = view.findViewById<MapView>(R.id.mapView)
        textView = view.findViewById(R.id.textSignlString)
        mMapView?.onCreate(savedInstanceState)

        mMapView!!.onResume() // needed to get the map to display immediately


        if (!checkPermissions()) {
            requestPermissions()
        } else {
            getLastLocation()
            createSignalStrengthListener()
        }

    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        when (requestCode) {
           REQUEST_PERMISSIONS_REQUEST_CODE -> if (grantResults.size <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(ContentValues.TAG, "User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                getLastLocation()
            }
        }
    }


    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(
                ContentValues.TAG,
                "Displaying permission rationale to provide additional context."
            )
        } else {
            Log.i(ContentValues.TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling here to request the missing permissions
            return
        }
        mFusedLocationClient?.lastLocation?.addOnCompleteListener(requireActivity(),
                OnCompleteListener<Location> { task ->
                    if (task.isSuccessful && task.result != null) {
                        mLastLocation = task.result
                        lat = mLastLocation?.latitude!!
                        lang = mLastLocation?.longitude!!

                    } else {
                        Log.w(ContentValues.TAG, "getLastLocation:exception", task.exception)
                    }
                }
        )
    }


    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        googleMap.clear() //clear old markers
        val googlePlex = CameraPosition.builder()
            .target(LatLng(lat, lang))
            .zoom(15f)
            .bearing(0f)
            .tilt(45f)
            .build()
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 10000, null)

//                mMap.addMarker(new MarkerOptions()
//                        .position(new LatLng(37.4219999, -122.0862462))
//                        .title("Spider Man")
//                        .icon(bitmapDescriptorFromVector(getActivity(),R.drawable.used_car)));
        googleMap.addMarker(
            MarkerOptions()
                .position(LatLng(lat, lang))
                .title("Iron Man")
                .snippet("His Talent : Plenty of money")
        )
        Log.e(ContentValues.TAG, "onMapReady: $lat,$lang")
        //                mMap.addMarker(new MarkerOptions()
//                        .position(new LatLng(37.3092293,-122.1136845))
//                        .title("Captain America"));
        googleMap.setOnMapClickListener { latLng ->
            lat = latLng.latitude
            lang = latLng.longitude
           // Message.message(activity, "The Location is Selected")
        }
    }
    override fun onStart() {
        super.onStart()
        mMapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mMapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mMapView?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView?.onLowMemory()
    }

    private var mTelephonyManager: TelephonyManager? = null
    private var mSignalStrengthListener: SignalStrengthListener? =
        null
    private fun createSignalStrengthListener() {
        mSignalStrengthListener = SignalStrengthListener()
        mTelephonyManager =
            activity?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        mTelephonyManager?.listen(mSignalStrengthListener,256)

    }

    private inner class SignalStrengthListener : PhoneStateListener() {
        override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val strengthAmplitude = signalStrength.level - 1.9f
                Log.d("onSignalStrengthsChanged", "SignalStrengths = $strengthAmplitude")
                // setProgress(signalStrength.level)
                textView?.text = strengthAmplitude.toString()

                super.onSignalStrengthsChanged(signalStrength)
            } else if (signalStrength.isGsm) {
                val gsmSignalStrength = signalStrength.gsmSignalStrength
                if (gsmSignalStrength.toFloat() != ERROR_SIGNAL_LEVEL) {
                    val strengthAmplitude =
                        Math.round(signalStrength.gsmSignalStrength / MAX_GSM_LEVEL * MAX_SEEK_BAR_LEVEL)
                    val strengthAmplitudeF = strengthAmplitude - 0.1f
                    Log.d(
                        "onSignalStrengthsChanged",
                        "SignalStrengths(pre-M) = $strengthAmplitude"
                    )
                    //  setProgress(strengthAmplitude)
                    textView?.text = strengthAmplitude.toString()
                }
            } else {
                mTelephonyManager?.listen(mSignalStrengthListener, LISTEN_NONE)
            }
        }


        private  val MAX_GSM_LEVEL = 31f
        private  val MAX_SEEK_BAR_LEVEL = 4f
        private  val ERROR_SIGNAL_LEVEL = 99f

    }

}