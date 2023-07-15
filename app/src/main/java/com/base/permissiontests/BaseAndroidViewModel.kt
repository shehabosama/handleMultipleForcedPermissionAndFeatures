package com.base.permissiontests

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.CoroutineExceptionHandler

open class BaseAndroidViewModel(viewModelContext: Context) :
    AndroidViewModel(viewModelContext.applicationContext as Application){

    protected val exceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, _ ->
            //todo handle the exception here (we should send this information to server like firebase, this should be discussed)
        }


    val context: Context get() = getApplication()
}