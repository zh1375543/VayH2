package com.velora.portal.platform.common.util.platform

import android.content.Context
import com.google.android.gms.ads.identifier.AdvertisingIdClient

object AdvertisingIdProvider {

    fun get(context: Context): String = runCatching {
        AdvertisingIdClient.getAdvertisingIdInfo(context).id.orEmpty()
    }.getOrDefault("")
}
