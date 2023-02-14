package com.base.permissiontests

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class MainViewModel: ViewModel() {
    val strViewModel = SingleLiveEvent<Boolean>()

    fun doAction() {
        // depending on the action, do necessary business logic calls and update the
        // userLiveData.
    }
}