package com.velora.portal.platform.network

import com.velora.portal.platform.common.data.bean.ServiceResponse
import com.velora.portal.platform.common.data.bean.TrackParamBean
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiTrack {

    @POST("track")
    suspend fun submitTrack(@Body payload: TrackParamBean): ServiceResponse<Any?>
}
