package com.prembros.ezra

import com.prembros.ezra.model.ApiCall
import com.prembros.ezra.model.ApiErrorBody
import com.prembros.ezra.model.ApiResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException

/**
 * Convenient getter for nullable [ApiResponse] for checking [ApiResponse.isSuccessful] without the `== true` or `== false` in the `if` statement
 */
val <DATA : Any, ERROR : Any> ApiResponse<DATA, ERROR>?.isSuccess: Boolean
  get() = this?.isSuccessful == true

fun <DATA : Any> ApiResponse<DATA, Any>?.success(): DATA? = (this as? ApiResponse.ApiSuccess<DATA>)?.body

fun <ERROR : Any> ApiResponse<Nothing, ERROR>?.apiErrorBody(): ERROR? = (this as? ApiResponse.ApiError<ERROR>)?.errorBody

fun <T> ApiCall<T>?.apiErrorBody(): ApiErrorBody? = (this as? ApiResponse.ApiError<ApiErrorBody>)?.errorBody

fun <T> ApiCall<T>?.error(): ApiResponse.ApiError<ApiErrorBody>? = this as? ApiResponse.ApiError<ApiErrorBody>

fun <T> ApiCall<T>?.errorMessage(): String? =
  (this as? ApiResponse.ApiError<ApiErrorBody>)?.let {
    it.errorBody.error.message ?: it.errorBody.message
  } ?: (this as? ApiResponse.NetworkError)?.error?.message
  ?: (this as? ApiResponse.UnknownError)?.error?.message

fun ApiResponse<Nothing, Nothing>?.networkError(): IOException? = (this as? ApiResponse.NetworkError)?.error

fun ApiResponse<Nothing, Nothing>?.unknownError(): Throwable? = (this as? ApiResponse.UnknownError)?.error

fun File.createMultiPartImage(type: String = "image/jpeg"): MultipartBody.Part =
  MultipartBody.Part.createFormData("image", name, this.asRequestBody(type.toMediaType()))