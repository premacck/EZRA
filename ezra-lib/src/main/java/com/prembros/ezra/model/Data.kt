package com.prembros.ezra.model

import com.google.gson.annotations.SerializedName

/**
 * Prem's creation, on 12/12/20
 */
data class Data<T>(
  @SerializedName("data") val data: T?
)