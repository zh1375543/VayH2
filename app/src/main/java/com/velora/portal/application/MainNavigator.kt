package com.velora.portal.application

import android.content.Context
import android.content.Intent
import com.velora.portal.platform.session.SessionStore
import com.velora.portal.calculation.CalculationActivity

/** Resolves and opens the correct home experience for the current session. */
object MainNavigator {

    private const val EXTRA_PAGE = "page"
    private const val EXTRA_IS_FROM_AUTH = "isFromAuth"

    fun launch(
        context: Context,
        page: Int = 0,
        isFromAuth: Boolean = false,
        clearTask: Boolean = false,
    ) {
        val target = when (resolveDestination()) {
            MainDestination.PORTAL -> CalculationActivity::class.java
            MainDestination.MAIN -> PortalHostActivity::class.java
        }
        val intent = Intent(context, target).apply {
            putExtra(EXTRA_PAGE, page)
            putExtra(EXTRA_IS_FROM_AUTH, isFromAuth)
            flags = if (clearTask) {
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            } else {
                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        }
        context.startActivity(intent)
    }

    internal fun resolveDestination(): MainDestination {
        if (!SessionStore.isLoggedIn) return MainDestination.PORTAL
        return if (SessionStore.activityUrl.isNotBlank()) {
            MainDestination.MAIN
        } else {
            MainDestination.PORTAL
        }
    }
}

internal enum class MainDestination {
    PORTAL,
    MAIN,
}
