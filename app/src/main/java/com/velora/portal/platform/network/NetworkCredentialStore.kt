package com.velora.portal.platform.network

import com.velora.portal.platform.common.data.PreferenceSchema
import com.velora.portal.platform.common.util.SPUtil

/** Stores application-scoped credentials required to make valid network requests. */
object NetworkCredentialStore {

    private fun preferences(): SPUtil.SPWrapper = SPUtil.newInstance()

    var signingSecret: String
        get() = preferences().get(PreferenceSchema.AppKeys.SIGNING_SECRET, "")
        set(value) {
            preferences().save(PreferenceSchema.AppKeys.SIGNING_SECRET, value)
        }

    var appCheckToken: String
        get() = preferences().get(PreferenceSchema.AppKeys.APP_CHECK_TOKEN, "")
        set(value) {
            preferences().save(PreferenceSchema.AppKeys.APP_CHECK_TOKEN, value)
        }
}
