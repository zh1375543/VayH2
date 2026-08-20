package com.novexa.platform.core.common.util.platform

import android.content.Context
import com.novexa.platform.core.session.SessionStore
import com.novexa.platform.feature.onboarding.presentation.login.AccountAccessActivity
import com.novexa.platform.core.common.util.start

fun Context.requireLogin(whenLoggedIn: () -> Unit) {
    if (SessionStore.isLoggedIn) {
        whenLoggedIn()
    } else {
        start<AccountAccessActivity>()
    }
}
