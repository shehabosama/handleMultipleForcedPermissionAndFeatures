package com.base.permissiontests

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager


class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext
    val strengthValues = MutableLiveData<SIMNetworkStrength>()
    val strViewModel = SingleLiveEvent<Boolean>()
    fun doAction() {
        // depending on the action, do necessary business logic calls and update the
        // userLiveData.
    }

    private val broadcastReceiverStrengthValues = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == StrengthService.BROAD_CAST_STREGTH_VALUES) {
                intent?.getParcelableExtra<SIMNetworkStrength>("strengthValues")?.let {
                    Log.d("TAG", "onReceive: $it")
                    strengthValues.postValue(it)
                }
            }
        }
    }
    init {
        LocalBroadcastManager.getInstance(context).registerReceiver(
            broadcastReceiverStrengthValues, IntentFilter(
                StrengthService.BROAD_CAST_STREGTH_VALUES
            )
        )

    }

    fun UnRegisterBroadCastReceiver(){
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiverStrengthValues)
    }

}