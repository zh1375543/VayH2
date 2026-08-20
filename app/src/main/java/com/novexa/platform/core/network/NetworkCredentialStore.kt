package com.novexa.platform.core.network

import com.novexa.platform.core.common.util.SPUtil

/** Stores application-scoped credentials required to make valid network requests. */
object NetworkCredentialStore {

    private const val SIGNING_SECRET_KEY = "ST_KEY"
    private const val APP_CHECK_TOKEN_KEY = "APP_CHECK_TOKEN_KEY"

    private fun preferences(): SPUtil.SPWrapper = SPUtil.newInstance()

    var signingSecret: String
        get() = preferences().get(SIGNING_SECRET_KEY, "")
        set(value) {
            preferences().save(SIGNING_SECRET_KEY, value)
        }

    var appCheckToken: String
        get() = preferences().get(APP_CHECK_TOKEN_KEY, "")
        set(value) {
            preferences().save(APP_CHECK_TOKEN_KEY, value)
        }
}
