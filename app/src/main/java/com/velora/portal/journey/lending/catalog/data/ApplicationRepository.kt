package com.velora.portal.journey.lending.catalog.data

import com.velora.portal.platform.network.Api
import com.velora.portal.platform.network.NetworkProvider
import com.velora.portal.platform.common.data.repository.dataOrThrow
import com.velora.portal.domain.credit.model.MemberOverviewResponse
import com.velora.portal.domain.credit.model.CatalogEntry
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ApplicationRepository(
    private val api: Api = NetworkProvider.api,
) {

    suspend fun submitBundleOrder(
        files: List<MultipartBody.Part>,
        multipartBody: Map<String, RequestBody>,
    ): List<CatalogEntry>? {
        return api.submitBundleOrder(files, multipartBody).dataOrThrow()
    }

    suspend fun submitLoanOrder(
        files: List<MultipartBody.Part>,
        multipartBody: Map<String, RequestBody>,
    ): CatalogEntry? {
        return api.submitLoanOrder(files, multipartBody).dataOrThrow()
    }

    suspend fun loadBundleLoanPage(): MemberOverviewResponse? {
        return api.loadBundleLoanPage().dataOrThrow()
    }
}
