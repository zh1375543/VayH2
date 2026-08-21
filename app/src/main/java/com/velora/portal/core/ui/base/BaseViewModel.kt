package com.velora.portal.core.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.velora.portal.core.analytics.AnalyticsTracker
import com.velora.portal.core.common.data.bean.TrackBean
import com.velora.portal.core.common.util.DataNetworkRequest

/** Base ViewModel class */
abstract class BaseViewModel : ViewModel() {

    /** Launch a network request returning unwrapped business data */
    fun <T> createNetworkRequest(block: suspend () -> T?): DataNetworkRequest<T> {
        return DataNetworkRequest(viewModelScope, block)
    }

    /** Submit a single analytics event */
    fun submitTrackingEvent(log: TrackBean) {
        AnalyticsTracker.submit(viewModelScope, listOf(log))
    }

    /** Submit analytics events after enriching each one with navigation context */
    fun submitTrackingEvents(logs: List<TrackBean>) {
        AnalyticsTracker.submit(viewModelScope, logs)
    }
}
