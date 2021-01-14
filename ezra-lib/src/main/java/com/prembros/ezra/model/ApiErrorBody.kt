package com.prembros.ezra.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Prem's creation, on 16/12/20
 */
@Parcelize
data class ApiErrorBody(

  @SerializedName("error") val error: ApiErrorDef,

  @SerializedName("message") val message: String,
) : Parcelable