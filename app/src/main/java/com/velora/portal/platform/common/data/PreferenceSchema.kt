package com.velora.portal.platform.common.data

/** Stable storage identifiers. These values must remain unchanged after release. */
internal object PreferenceSchema {

    object Files {
        const val CORE = "p_71d4c9"
        const val APP = "p_b8062e"
    }

    object CoreKeys {
        const val SESSION_TOKEN = "c_19a0"
        const val LOGIN_PROFILE = "c_27bd"
        const val ACTIVITY_ROUTE = "c_35e1"
        const val LOCATION = "c_438f"
        const val SIGN_BACK_HOME = "c_56a2"
        const val AUTH_CONFIG = "c_644d"
        const val DEVICE_POSTED = "c_72f8"
    }

    object AppKeys {
        const val SIGNING_SECRET = "a_108c"
        const val APP_CHECK_TOKEN = "a_21f7"
        const val APPS_FLYER_DATA = "a_348b"
        const val LANGUAGE = "a_459e"
        const val PHONE_PRIVACY = "a_53d4"
        const val GA_REFER = "a_662a"
        const val AF_SOURCE = "a_71c5"
        const val GAID = "a_849f"
        const val RATE_APP = "a_957b"
        const val FIREBASE_TOKEN = "a_a31e"
        const val FIREBASE_ID = "a_b482"
    }
}
