package com.velora.portal.core.common.data.repository

import com.velora.portal.core.common.data.bean.ServiceResponse

class ServiceResponseException(
    val response: ServiceResponse<*>,
) : RuntimeException(response.message)

fun <T> ServiceResponse<T?>.dataOrThrow(): T? {
    if (code == 200) return data
    throw ServiceResponseException(this)
}
