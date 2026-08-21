package com.velora.portal.core.common.util.platform

import android.content.Context
import com.velora.portal.core.session.SessionStore
import com.velora.portal.feature.onboarding.presentation.login.AccountAccessActivity
import com.velora.portal.core.common.util.start

fun Context.requireLogin(whenLoggedIn: () -> Unit) {
    if (SessionStore.isLoggedIn) {
        whenLoggedIn()
    } else {
        start<AccountAccessActivity>()
    }
}
