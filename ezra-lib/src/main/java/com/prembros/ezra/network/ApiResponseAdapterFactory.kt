package com.prembros.ezra.network

import com.prembros.ezra.model.ApiResponse
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Prem's creation, on 16/12/20
 */
class ApiResponseAdapterFactory : CallAdapter.Factory() {

  companion object {
    fun create() = ApiResponseAdapterFactory()
  }

  override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
    // suspend functions automatically wrap the response type in `Call`
    if (getRawType(returnType) != Call::class.java) {
      return null
    }
    // Check first that the return type is `ParameterizedType`
    check(returnType is ParameterizedType) { "returnType must be parameterized as Call<ApiResponse<<Foo>> or Call<ApiResponse<out Foo>>" }

    // Get the response type inside the `Call` type
    val responseType = getParameterUpperBound(0, returnType)
    // If the response is not ApiResponse then we can't handle this type, so we return null
    if (getRawType(responseType) != ApiResponse::class.java) {
      return null
    }
    // The responseType is ApiResponse & must be parameterized
    check(responseType is ParameterizedType) { "responseType must be parameterized as Call<ApiResponse<<Foo>> or Call<ApiResponse<out Foo>>" }

    val successBodyType = getParameterUpperBound(0, responseType)
    val errorBodyType = getParameterUpperBound(1, responseType)

    val errorBodyConverter = retrofit.nextResponseBodyConverter<Any>(null, errorBodyType, annotations)
    return ApiResponseAdapter<Any, Any>(successBodyType, errorBodyConverter)
  }
}