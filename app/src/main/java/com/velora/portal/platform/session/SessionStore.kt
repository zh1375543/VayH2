package com.velora.portal.platform.session

import com.velora.portal.feature.onboarding.model.AccessSessionResponse
import com.velora.portal.platform.common.util.SPUtil
import com.velora.portal.platform.common.util.text.parseJson
import com.velora.portal.platform.common.util.text.toJsonString

/** Persists the authenticated user session in the default preferences store. */
object SessionStore {

    private const val TOKEN_KEY = "TOKEN_KEY"
    private const val LOGIN_KEY = "LOGIN_KEY"
    private const val ACTIVITY_URL_KEY = "ACTIVITY_URL_KEY"

    var token: String
        get() = preferences().get(TOKEN_KEY, "")
        set(value) {
            preferences().save(TOKEN_KEY, value)
        }

    var loginInfo: AccessSessionResponse?
        get() = preferences()
            .get(LOGIN_KEY, "")
            .parseJson<AccessSessionResponse?>()
        set(value) {
            preferences().save(LOGIN_KEY, value?.toJsonString() ?: "")
        }

    /** Server-provided route flag used to select the destination after launch. */
    var activityUrl: String
        get() = preferences().get(ACTIVITY_URL_KEY, "")
        set(value) {
            preferences().save(ACTIVITY_URL_KEY, value)
        }

    val isLoggedIn: Boolean
        get() = token.isNotBlank()

    /** Clears all values from the default preferences store. */
    fun clear() {
        preferences().clear()
    }

    private fun preferences(): SPUtil.SPWrapper = SPUtil.getInstance()
}
