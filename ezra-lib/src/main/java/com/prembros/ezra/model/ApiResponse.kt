package com.prembros.ezra.model

import java.io.IOException

/**
 * Prem's creation, on 05/12/20
 */
typealias ApiCall<DATA> = ApiResponse<Data<DATA>, ApiErrorBody>

sealed class ApiResponse<out DATA : Any, out ERROR : Any> {

  var isSuccessful: Boolean = false

  fun withError() = apply {
    isSuccessful = false
  }

  companion object {

    /**
     * This method is used in case of API Success response.
     */
    fun <DATA : Any> success(body: DATA?): ApiResponse<DATA, Nothing> {
      val apiResponse = ApiSuccess(body)
      apiResponse.isSuccessful = true
      return apiResponse
    }

    /**
     * This method is used in case of api errors.
     * This will contain the error body & code.
     */
    fun <ERROR : Any> apiError(error: ERROR, code: Int): ApiResponse<Nothing, ERROR> = ApiError(error, code).withError()

    /**
     * This method is used in case of network errors.
     * This will contain the IOException for the error.
     */
    fun networkError(exception: IOException): ApiResponse<Nothing, Nothing> = NetworkError(exception).withError()

    /**
     * This method is used in case of unexpected exceptions.
     * This will contain the optional Throwable for the error.
     */
    fun unknownError(exception: Throwable?): ApiResponse<Nothing, Nothing> = UnknownError(exception).withError()
  }

  /**
   * Contains the body of the success state of the request.
   */
  data class ApiSuccess<DATA : Any>(val body: DATA?) : ApiResponse<DATA, Nothing>()

  /**
   * Represents the non-2xx responses, & also contains the error body and the response status code.
   */
  data class ApiError<ERROR : Any>(val errorBody: ERROR, val code: Int) : ApiResponse<Nothing, ERROR>()

  /**
   * Represents network failure such as no internet connection cases.
   */
  data class NetworkError(val error: IOException) : ApiResponse<Nothing, Nothing>()

  /**
   * Represents unexpected exceptions occurred creating the request or processing the response, for example parsing issues.
   */
  data class UnknownError(val error: Throwable?) : ApiResponse<Nothing, Nothing>()
}
