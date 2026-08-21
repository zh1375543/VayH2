package com.velora.portal.platform.common.util

import com.google.gson.JsonSyntaxException
import com.velora.portal.application.MainApplication
import com.velora.portal.platform.common.data.bean.ServiceResponse
import com.velora.portal.platform.common.data.bean.Event
import com.velora.portal.platform.common.data.repository.ServiceResponseException
import com.velora.portal.platform.common.util.text.gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import kotlin.coroutines.cancellation.CancellationException

class DataNetworkRequest<T>(
    private val viewModelScope: CoroutineScope,
    private val block: suspend () -> T?,
) {
    private var showLoading: Boolean = false
    private var onSuccess: (suspend (T?) -> Unit)? = null
    private var onFailed: (suspend (ServiceResponse<*>) -> Boolean) = { false }

    fun showLoading(show: Boolean = true): DataNetworkRequest<T> {
        this.showLoading = show
        return this
    }

    fun onSuccess(block: suspend (T?) -> Unit): DataNetworkRequest<T> {
        this.onSuccess = block
        return this
    }

    fun onFailed(block: suspend (ServiceResponse<*>) -> Boolean): Job {
        this.onFailed = block
        return execute()
    }

    fun execute(): Job = viewModelScope.launch {
        if (showLoading) MainApplication.appViewModel.isShowLoading.postValue(true)
        try {
            val response = withContext(Dispatchers.IO) { block() }
            onSuccess?.invoke(response)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleException(e)
        } finally {
            if (showLoading) MainApplication.appViewModel.isShowLoading.postValue(false)
        }
    }

    private suspend fun handleException(e: Exception) {
        if (e is ServiceResponseException) {
            processError(e.response)
            return
        }
        if (e is JsonSyntaxException && e.message?.contains("End of input") == true) {
            LogUtil.e("Network Empty Body: ignored")
            return
        }
        LogUtil.e("Network Exception: ${e.message}")
        processError(parseException(e))
    }

    private suspend fun processError(errorBean: ServiceResponse<*>) {
        errorBean.disabledToast = onFailed(errorBean)
        MainApplication.appViewModel.errorResponse.postValue(Event(errorBean))
    }

    private fun parseException(e: Exception): ServiceResponse<*> {
        if (e !is HttpException) {
            return ServiceResponse<Any?>(code = -1, message = e.message, disabledToast = true)
        }

        val errorBody = e.response()?.errorBody()?.string()
        if (errorBody.isNullOrBlank()) {
            return ServiceResponse<Any?>(code = e.code(), message = e.message(), disabledToast = true)
        }

        return try {
            gson.fromJson(errorBody, ServiceResponse::class.java)
                ?: ServiceResponse(code = e.code(), message = e.message(), disabledToast = true)
        } catch (_: JsonSyntaxException) {
            LogUtil.e("Parse Error Body Failed: $errorBody")
            ServiceResponse<Any?>(code = e.code(), message = e.message(), disabledToast = true)
        }
    }
}
