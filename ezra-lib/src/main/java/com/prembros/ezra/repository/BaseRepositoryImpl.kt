package com.prembros.ezra.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.prembros.ezra.model.ApiResponse
import kotlinx.coroutines.Dispatchers
import timber.log.Timber

/**
 * Prem's creation, on 05/12/20
 *
 * - Format for making network calls:
 * ```
 * fun someApiCallInRepositoryImpl(arg: Type) = makeApiCallFor {
 *   responseOf {
 *     service.apiCall(arg)
 *   }
 * }
 * ```
 * - For making network calls with an action after API response:
 * ```
 * fun someApiCallInRepositoryImpl(arg: Type) = makeApiCallFor {
 *   responseOf {
 *     service.apiCall(arg)
 *   }.withTransform {
 *     /* Implement action here */
 *   }
 * }
 * ```
 */
abstract class BaseRepositoryImpl {

  protected fun <DATA : Any, ERROR : Any> makeApiCallFor(apiCall: suspend () -> ApiResponse<DATA, ERROR>): LiveData<ApiResponse<DATA, ERROR>> =
    liveData(Dispatchers.IO) { emit(apiCall()) }

  protected suspend fun <DATA : Any, ERROR : Any> responseOf(call: suspend () -> ApiResponse<DATA, ERROR>): ApiResponse<DATA, ERROR> = try {
    call()
  } catch (e: Exception) {
    Timber.e(e)
    ApiResponse.unknownError(e)
  }

  protected fun <DATA : Any, ERROR : Any> ApiResponse<DATA, ERROR>.withTransform(action: ApiResponse<DATA, ERROR>.() -> Unit): ApiResponse<DATA, ERROR> =
    apply(action)
}