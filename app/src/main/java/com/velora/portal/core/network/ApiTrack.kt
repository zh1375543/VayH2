package com.velora.portal.core.network

import com.velora.portal.core.common.data.bean.ServiceResponse
import com.velora.portal.core.common.data.bean.TrackParamBean
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiTrack {

    @POST("track")
    suspend fun submitTrack(@Body paramBean: TrackParamBean): ServiceResponse<Any?>
}
