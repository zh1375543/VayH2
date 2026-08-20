package com.novexa.platform.core.network

import com.novexa.platform.core.common.data.bean.ServiceResponse
import com.novexa.platform.core.common.data.bean.TrackParamBean
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiTrack {

    @POST("track")
    suspend fun submitTrack(@Body paramBean: TrackParamBean): ServiceResponse<Any?>
}
