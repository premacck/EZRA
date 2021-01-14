package com.prembros.ezra.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Prem's creation, on 16/12/20
 */
@Parcelize
data class ApiErrorDef(

  @SerializedName("code") val code: String?,

  @SerializedName("message") val message: String?,
) : Parcelable