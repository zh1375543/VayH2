package com.velora.portal.core.common.util

sealed interface PageLoadState<out T> {
    data object Loading : PageLoadState<Nothing>

    data object Error : PageLoadState<Nothing>

    data object Empty : PageLoadState<Nothing>

    data class Content<T>(
        val data: T,
    ) : PageLoadState<T>
}
