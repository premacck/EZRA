package com.prembros.ezra.network

import com.prembros.ezra.model.ApiResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Converter
import java.lang.reflect.Type

/**
 * Prem's creation, on 16/12/20
 */
class ApiResponseAdapter<DATA : Any, ERROR : Any>(
  private val successType: Type, private val errorBodyConverter: Converter<ResponseBody, ERROR>
) : CallAdapter<DATA, Call<ApiResponse<DATA, ERROR>>> {

  override fun responseType(): Type = successType

  override fun adapt(call: Call<DATA>): Call<ApiResponse<DATA, ERROR>> = ApiResponseCall(call, errorBodyConverter)
}