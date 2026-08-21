package com.velora.portal.platform.common.util.platform

import android.content.Context
import com.velora.portal.platform.session.SessionStore
import com.velora.portal.journey.access.presentation.login.AccountAccessActivity
import com.velora.portal.platform.common.util.start

fun Context.requireLogin(whenLoggedIn: () -> Unit) {
    if (SessionStore.isLoggedIn) {
        whenLoggedIn()
    } else {
        start<AccountAccessActivity>()
    }
}
