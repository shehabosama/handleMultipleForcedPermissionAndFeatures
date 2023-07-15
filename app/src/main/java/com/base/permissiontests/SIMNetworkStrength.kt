package com.base.permissiontests

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SIMNetworkStrength(
    @SerializedName("strengthValues")
    val strengthValues:Pair<Int,Int>?=null
):Parcelable