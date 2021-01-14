package com.prembros.ezra.network

import com.prembros.ezra.model.ApiResponse
import okhttp3.Request
import okhttp3.ResponseBody
import okio.Timeout
import retrofit2.*
import java.io.IOException

/**
 * Prem's creation, on 16/12/20
 */
class ApiResponseCall<DATA : Any, ERROR : Any>(
  private val delegate: Call<DATA>, private val errorConverter: Converter<ResponseBody, ERROR>
) : Call<ApiResponse<DATA, ERROR>> {

  override fun enqueue(callback: Callback<ApiResponse<DATA, ERROR>>) = delegate.enqueue(
    object : Callback<DATA> {
      override fun onResponse(call: Call<DATA>, response: Response<DATA>) {
        val code = response.code()
        val body = response.body()
        val error = response.errorBody()
        if (response.isSuccessful) {
          if (body != null) {
            // Success response
            callback.onResponse(this@ApiResponseCall, Response.success(code, ApiResponse.success(body)))
          } else {
            // Response is successful but the body is null
            callback.onResponse(this@ApiResponseCall, Response.success(ApiResponse.unknownError(null)))
          }
        } else {
          // Response not successful, try to get error body
          val errorBody = if (error == null || error.contentLength() == 0L) null else try {
            errorConverter.convert(error)
          } catch (e: Exception) {
            null
          }
          if (errorBody != null) {
            callback.onResponse(this@ApiResponseCall, Response.success(ApiResponse.apiError(errorBody, code)))
          } else {
            callback.onResponse(this@ApiResponseCall, Response.success(ApiResponse.unknownError(null)))
          }
        }
      }

      @Suppress("ThrowableNotThrown") override fun onFailure(call: Call<DATA>, throwable: Throwable) {
        val apiResponse = when (throwable) {
          is IOException -> ApiResponse.networkError(throwable)
          is HttpException -> ApiResponse.networkError((IOException(throwable.message(), throwable)))
          else -> ApiResponse.unknownError(throwable)
        }
        callback.onResponse(this@ApiResponseCall, Response.success(apiResponse))
      }
    }
  )

  override fun clone(): Call<ApiResponse<DATA, ERROR>> = ApiResponseCall(delegate.clone(), errorConverter)

  override fun execute(): Response<ApiResponse<DATA, ERROR>> = throw UnsupportedOperationException("ApiResponseCall doesn't support execute()")

  override fun cancel() = delegate.cancel()

  override fun isExecuted(): Boolean = delegate.isExecuted

  override fun isCanceled(): Boolean = delegate.isCanceled

  override fun request(): Request = delegate.request()

  override fun timeout(): Timeout = Timeout()
}