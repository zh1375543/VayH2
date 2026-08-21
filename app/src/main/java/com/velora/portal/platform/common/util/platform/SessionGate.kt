package com.velora.portal.platform.common.util.platform

import android.content.Context
import com.velora.portal.platform.session.SessionStore
import com.velora.portal.feature.onboarding.presentation.login.AccountAccessActivity
import com.velora.portal.platform.common.util.start

fun Context.requireLogin(whenLoggedIn: () -> Unit) {
    if (SessionStore.isLoggedIn) {
        whenLoggedIn()
    } else {
        start<AccountAccessActivity>()
    }
}
