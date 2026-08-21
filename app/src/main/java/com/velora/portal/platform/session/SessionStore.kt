package com.velora.portal.platform.session

import com.velora.portal.domain.customer.model.AccessSessionResponse
import com.velora.portal.platform.common.data.PreferenceSchema
import com.velora.portal.platform.common.util.SPUtil
import com.velora.portal.platform.common.util.text.parseJson
import com.velora.portal.platform.common.util.text.toJsonString

/** Persists the authenticated user session in the core preferences store. */
object SessionStore {

    var token: String
        get() = preferences().get(PreferenceSchema.CoreKeys.SESSION_TOKEN, "")
        set(value) {
            preferences().save(PreferenceSchema.CoreKeys.SESSION_TOKEN, value)
        }

    var loginInfo: AccessSessionResponse?
        get() = preferences()
            .get(PreferenceSchema.CoreKeys.LOGIN_PROFILE, "")
            .parseJson<AccessSessionResponse?>()
        set(value) {
            preferences().save(
                PreferenceSchema.CoreKeys.LOGIN_PROFILE,
                value?.toJsonString() ?: ""
            )
        }

    /** Server-provided route flag used to select the destination after launch. */
    var activityUrl: String
        get() = preferences().get(PreferenceSchema.CoreKeys.ACTIVITY_ROUTE, "")
        set(value) {
            preferences().save(PreferenceSchema.CoreKeys.ACTIVITY_ROUTE, value)
        }

    val isLoggedIn: Boolean
        get() = token.isNotBlank()

    /** Clears all values from the default preferences store. */
    fun clear() {
        preferences().clear()
    }

    private fun preferences(): SPUtil.SPWrapper = SPUtil.getInstance()
}
