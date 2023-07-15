package com.base.permissiontests

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.telephony.CellInfo
import android.telephony.CellInfoCdma
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoWcdma
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.base.permissiontests.Constants.TIME_UPDATE_INTERVAL
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.Provider.Service


class StrengthService : LifecycleService() {

    companion object {
        val TAG = StrengthService::class.java.simpleName
        val BROAD_CAST_IS_TRACKING = "broadCast_is_Tracking"
        val BROAD_CAST_STREGTH_VALUES = "broadCast_strength_values"
    }

    private var telephonyManager: TelephonyManager? = null
    override fun onCreate() {
        super.onCreate()
        telephonyManager =
            applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        try {
            LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
                broadcastReceiverIsTracking, IntentFilter(
                    BROAD_CAST_IS_TRACKING
                )
            )
        } catch (e: Exception) {

        }

    }

    private val broadcastReceiverIsTracking = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BROAD_CAST_IS_TRACKING) {
                intent?.getBooleanExtra("isTracking", false)?.let {
                    CoroutineScope(Dispatchers.Main).launch {
                        while (it) {
                            val response: SIMNetworkStrength = Gson().fromJson(
                                Gson().toJson(SIMNetworkStrength(getNetworkStrength())).toString(),
                                object : TypeToken<SIMNetworkStrength>() {}.type
                            )
                            delay(TIME_UPDATE_INTERVAL)
                            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(
                                Intent(BROAD_CAST_STREGTH_VALUES).putExtra(
                                    "strengthValues",
                                    response
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun postInitialValues() {
        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(Intent(BROAD_CAST_IS_TRACKING).putExtra("isTracking", false))
    }

    private fun killService() {
        postInitialValues()
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                Constants.ACTION_START_SERVICE -> {
                    startTracking()
                }

                Constants.ACTION_FINISH_SERVICE -> {
                    killService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startTracking() {
        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(Intent(BROAD_CAST_IS_TRACKING).putExtra("isTracking", true))
    }

    fun updateStrengthValues(isTracking: Boolean) {
        if (isTracking) {
            getNetworkStrength()
        }
    }

    fun getRegisteredCellInfo(cellInfos: MutableList<CellInfo>): ArrayList<CellInfo> {
        val registeredCellInfos = ArrayList<CellInfo>()
        if (cellInfos.isNotEmpty()) {
            for (i in cellInfos.indices) {
                if (cellInfos[i].isRegistered) {
                    registeredCellInfos.add(cellInfos[i])
                }
            }
        }
        return registeredCellInfos
    }

    fun getNetworkStrength(): Pair<Int, Int> {

        var strength1 = -1
        var strength2 = -1


        val manager =
            getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return Pair(0, 0)
        }
        if (telephonyManager?.allCellInfo != null) {

            val allCellinfo = telephonyManager?.allCellInfo
            val activeSubscriptionInfoList = manager.activeSubscriptionInfoList
            val regCellInfo = allCellinfo?.let { getRegisteredCellInfo(it) }

            activeSubscriptionInfoList.forEachIndexed { Subindex, subs ->
                if (activeSubscriptionInfoList.size >= 2) {
                    regCellInfo?.size?.let {
                        if (it >= 2) {

                            if (subs.simSlotIndex == 0) {

                                if (subs.carrierName != "No service") {
                                    strength1 = when (val info1 = regCellInfo?.get(0)) {
                                        is CellInfoLte -> info1.cellSignalStrength.asuLevel
                                        is CellInfoGsm -> info1.cellSignalStrength.asuLevel
                                        is CellInfoCdma -> info1.cellSignalStrength.asuLevel
                                        is CellInfoWcdma -> info1.cellSignalStrength.asuLevel
                                        else -> 0
                                    }
                                    Log.i(TAG, "subs $subs")
                                    Log.i(
                                        TAG,
                                        "sim1   ${subs.carrierName}  ${subs.mnc}  $strength1"
                                    )
                                } else {

                                    strength1 = -1
                                }

                            } else if (subs.simSlotIndex == 1) {
                                if (subs.carrierName != "No service") {
                                    strength2 = when (val info2 = regCellInfo[1]) {
                                        is CellInfoLte -> info2.cellSignalStrength.asuLevel
                                        is CellInfoGsm -> info2.cellSignalStrength.asuLevel
                                        is CellInfoCdma -> info2.cellSignalStrength.asuLevel
                                        is CellInfoWcdma -> info2.cellSignalStrength.asuLevel
                                        else -> 0
                                    }
                                    Log.i(TAG, "subs $subs")

                                    Log.i(
                                        TAG,
                                        "sim2   ${subs.carrierName}  ${subs.mnc}  $strength2"
                                    )
                                } else {

                                    strength2 = -1
                                }

                            }

                        }
                    }

                } else if (activeSubscriptionInfoList.size == 1) {
                    regCellInfo?.size?.let {
                        if (it >= 1) {
                            if (subs.simSlotIndex == 0) {
                                if (subs.carrierName != "No service") {

                                    strength1 = when (val info1 = regCellInfo[0]) {
                                        is CellInfoLte -> info1.cellSignalStrength.level
                                        is CellInfoGsm -> info1.cellSignalStrength.level
                                        is CellInfoCdma -> info1.cellSignalStrength.level
                                        is CellInfoWcdma -> info1.cellSignalStrength.level
                                        else -> 0
                                    }
                                } else {

                                    strength1 = -1
                                }

                            }
                        }
                    }

                    strength2 = -2

                }
            }

        }
        Log.i(TAG, "final strenght   sim1 $strength1  sim2 $strength2")

        return Pair(strength1, strength2)
    }
}