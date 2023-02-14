package com.base.permissiontests

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.*
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider


class MainActivity : AppCompatActivity() {
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    lateinit var permissionDialog: AlertDialog
    var permissionDialogBuilder:AlertDialog.Builder?=null
    lateinit var bluetoothDialog: AlertDialog
    var bluetoothDialogBuilder:AlertDialog.Builder?=null
    lateinit var gpsDialog: AlertDialog
    var gpsDialogBuilder:AlertDialog.Builder?=null

    var viewModel: MainViewModel?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        permissionSettingsDialog()
        bluetoothStateDialog()
        gpsStateDialog()
        checkBluetoothEnabled()
        checkGpsEnabled()
        viewModel = ViewModelProvider.AndroidViewModelFactory(application).create(MainViewModel::class.java)
        viewModel?.strViewModel?.observe(this,Observer {
            if (it){
                try {
                    if (!permissionDialog.isShowing)
                        permissionDialog.show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }else{
                permissionDialog.dismiss()
            }
        })

         permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true &&
                permissions[Manifest.permission.BLUETOOTH_SCAN] == true &&
                permissions[Manifest.permission.BLUETOOTH_CONNECT] == true){
                viewModel?.strViewModel?.value = false
            } else {
                permissionDialog.dismiss()
                viewModel?.strViewModel?.value = true
            }
        }
        permissionLauncher.launch(arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ))
        findViewById<TextView?>(R.id.textView).setOnClickListener {
        permissionSettingsDialog()
        }
    }

    private fun checkGpsEnabled() {
        if (isGpsEnabled().not()){
            try {
                if (!gpsDialog.isShowing)
                    gpsDialog.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }else{
            gpsDialog.dismiss()
        }
    }

    private fun permissionSettingsDialog() {
        permissionDialogBuilder = AlertDialog.Builder(this@MainActivity)
        permissionDialogBuilder?.apply {
            setTitle("test")
            setMessage("test message")
            setCancelable(false)
        }

        val dialogClickListener = DialogInterface.OnClickListener { _, which ->

            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    openAppSettings()
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                    viewModel?.strViewModel?.value = false
                    permissionDialog.dismiss()
                }
            }
        }
        permissionDialogBuilder?.setPositiveButton(
          "go to settings",
            dialogClickListener
        )
        permissionDialogBuilder?.setNeutralButton("cancel")
        { dialogInterface: DialogInterface, i: Int ->
            finish()
        }
        permissionDialog = permissionDialogBuilder?.create()!!
    }

    private fun bluetoothStateDialog() {
        bluetoothDialogBuilder = AlertDialog.Builder(this@MainActivity)
        bluetoothDialogBuilder?.apply {
            setTitle("test")
            setMessage("test message")
            setCancelable(false)
        }

        val dialogClickListener = DialogInterface.OnClickListener { _, which ->

            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    bluetoothSettings()
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                    bluetoothDialog.dismiss()
                }
            }
        }
        bluetoothDialogBuilder?.setPositiveButton(
            "Enable bluetooth",
            dialogClickListener
        )
        bluetoothDialogBuilder?.setNeutralButton("cancel")
        { dialogInterface: DialogInterface, i: Int ->
            finish()
        }
        bluetoothDialog = bluetoothDialogBuilder?.create()!!
    }
    private fun gpsStateDialog() {
        gpsDialogBuilder = AlertDialog.Builder(this@MainActivity)
        gpsDialogBuilder?.apply {
            setTitle("test")
            setMessage("test message")
            setCancelable(false)
        }

        val dialogClickListener = DialogInterface.OnClickListener { _, which ->

            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    gpsSettings()
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                    bluetoothDialog.dismiss()
                }
            }
        }
        gpsDialogBuilder?.setPositiveButton(
            "Enable Gps",
            dialogClickListener
        )
        gpsDialogBuilder?.setNeutralButton("cancel")
        { dialogInterface: DialogInterface, i: Int ->
            finish()
        }
        gpsDialog = gpsDialogBuilder?.create()!!
    }

    private fun gpsSettings() {
        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
 fun bluetoothSettings(){
     startActivity(Intent().setAction(Settings.ACTION_BLUETOOTH_SETTINGS))
 }

    fun checkBluetoothEnabled(){
        val bluetoothManager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val mBluetoothAdapter = bluetoothManager.adapter
        if (mBluetoothAdapter == null) {
        } else if (!mBluetoothAdapter.isEnabled) {
            try {
                if (!bluetoothDialog.isShowing)
                    bluetoothDialog.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            bluetoothDialog.dismiss()
        }
    }

private val gpsReceiver: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (isGpsEnabled().not()) {
            try {
                if (!gpsDialog.isShowing)
                    gpsDialog.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {

           gpsDialog.dismiss()
        }
    }
}
    private fun isGpsEnabled(): Boolean {
        val contentResolver: ContentResolver = this.contentResolver
        val mode: Int = Settings.Secure.getInt(
            contentResolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF
        )
        return mode != Settings.Secure.LOCATION_MODE_OFF
    }
    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR
                )
                when (state) {
                    BluetoothAdapter.STATE_OFF -> {
                        try {
                            if (!bluetoothDialog.isShowing)
                                bluetoothDialog.show()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    BluetoothAdapter.STATE_TURNING_OFF -> Unit
                    BluetoothAdapter.STATE_ON -> {
                        bluetoothDialog.dismiss()
                    }
                    BluetoothAdapter.STATE_TURNING_ON -> Unit
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        val bluetoothIntentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(mReceiver, bluetoothIntentFilter)
        val gpsIntentFilter = IntentFilter(LocationManager.MODE_CHANGED_ACTION)
        registerReceiver(gpsReceiver, gpsIntentFilter)

        handlePermissionDialogOnResume()
        checkBluetoothEnabled()
        checkGpsEnabled()
    }

    private fun handlePermissionDialogOnResume() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel?.strViewModel?.value = false

        } else {
            permissionDialog.dismiss()
            viewModel?.strViewModel?.value = true
        }
    }
    override fun onStop() {
        super.onStop()
        unregisterReceiver(mReceiver)
        unregisterReceiver(gpsReceiver)
    }

}