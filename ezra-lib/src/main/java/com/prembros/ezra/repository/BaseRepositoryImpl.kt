package com.prembros.ezra.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.liveData
import com.prembros.ezra.model.ApiResponse
import kotlinx.coroutines.Dispatchers
import timber.log.Timber

/**
 * Prem's creation, on 05/12/20
 *
 * - Format for making network calls:
 * ```
 * fun someApiCallInRepositoryImpl(arg: Type) = makeApiCall {
 *   responseOf {
 *     service.apiCall(arg)
 *   }
 * }
 * ```
 * - For making network calls with an action after API response:
 * ```
 * fun someApiCallInRepositoryImpl(arg: Type) = makeApiCall {
 *   responseOf {
 *     service.apiCall(arg)
 *   }.withTransform {
 *     /* Implement action here */
 *   }
 * }
 * ```
 * - If you want to pass data in your own [LiveData]:
 * ```
 * fun someApiCallInRepositoryImpl(arg: Type, liveData: MutableLiveData<...>) = makeApiCall(liveData) {
 *   responseOf {
 *     service.apiCall(arg)
 *   }
 * }
 * ```
 */
abstract class BaseRepositoryImpl {

  protected fun <DATA : Any, ERROR : Any> makeApiCallFor(
    optionalLiveData: MutableLiveData<ApiResponse<DATA, ERROR>>? = null, apiCall: suspend () -> ApiResponse<DATA, ERROR>
  ): LiveData<ApiResponse<DATA, ERROR>> = optionalLiveData?.also { provided ->
    val result = liveData(Dispatchers.IO) { emit(apiCall()) }
    val observer: (ApiResponse<DATA, ERROR>) -> Unit = { provided.value = it }
    result.observeOnce(observer)
  } ?: run {
    liveData(Dispatchers.IO) { emit(apiCall()) }
  }

  protected suspend fun <DATA : Any, ERROR : Any> responseOf(call: suspend () -> ApiResponse<DATA, ERROR>): ApiResponse<DATA, ERROR> = try {
    call()
  } catch (e: Exception) {
    Timber.e(e)
    ApiResponse.unknownError(e)
  }

  protected fun <DATA : Any, ERROR : Any> ApiResponse<DATA, ERROR>.withTransform(action: ApiResponse<DATA, ERROR>.() -> Unit): ApiResponse<DATA, ERROR> =
    apply(action)

  private fun <T : Any> LiveData<T>.observeOnce(observer: Observer<T>) = observeForever(object : Observer<T> {
    override fun onChanged(t: T?) {
      observer.onChanged(t)
      removeObserver(this)
    }
  })
}