package com.velora.portal.core.analytics

import com.velora.portal.core.common.data.bean.SurveyBean
import com.velora.portal.core.common.data.bean.TrackBean
import com.velora.portal.core.common.data.bean.TrackParamBean
import com.velora.portal.core.network.NetworkProvider
import com.velora.portal.core.common.data.repository.dataOrThrow
import com.velora.portal.core.common.util.DataNetworkRequest
import com.velora.portal.core.common.util.text.toJsonString
import kotlinx.coroutines.CoroutineScope

/** Enriches analytics events with navigation context and submits them to the tracking API. */
object AnalyticsTracker {

    private var sessionStartTime: String = System.currentTimeMillis().toString()
    private var eventCount: Int = 0
    private var lastEvent: TrackBean? = null
    private var lastPageEvent: TrackBean? = null

    /** Starts a tracking session with a new timestamp and event counter. */
    fun startSession() {
        eventCount = 0
        sessionStartTime = System.currentTimeMillis().toString()
        lastEvent = null
        lastPageEvent = null
    }

    fun submit(
        scope: CoroutineScope,
        events: List<TrackBean>,
    ) {
        if (events.isEmpty()) return

        events.forEach { event ->
            eventCount++
            event.apply {
                pp = "$sessionStartTime;$eventCount"
                prevAct = lastEvent?.act
                prevP = lastEvent?.p
                lastP = lastPageEvent?.p
                lastAct = lastPageEvent?.act
            }

            lastEvent = event
            event.takeIf { it.p != null && it.p != lastPageEvent?.p }?.let {
                lastPageEvent = it
            }
        }

        DataNetworkRequest(scope) {
            NetworkProvider.trackApi
                .submitTrack(TrackParamBean(events.map { SurveyBean(it.toJsonString()) }))
                .dataOrThrow()
        }.onSuccess { }.onFailed { true }
    }
}
