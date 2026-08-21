package com.velora.portal.feature.catalog.data

import com.velora.portal.core.network.Api
import com.velora.portal.core.network.NetworkProvider
import com.velora.portal.core.common.data.repository.dataOrThrow
import com.velora.portal.feature.catalog.model.MemberOverviewResponse
import com.velora.portal.feature.catalog.model.CatalogItemBean
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ApplicationRepository(
    private val api: Api = NetworkProvider.api,
) {

    suspend fun submitTogetherLoan(
        files: List<MultipartBody.Part>,
        multipartBody: Map<String, RequestBody>,
    ): List<CatalogItemBean>? {
        return api.oneLoanApply(files, multipartBody).dataOrThrow()
    }

    suspend fun submitLoan(
        files: List<MultipartBody.Part>,
        multipartBody: Map<String, RequestBody>,
    ): CatalogItemBean? {
        return api.loanApply(files, multipartBody).dataOrThrow()
    }

    suspend fun fetchTogetherLoan(): MemberOverviewResponse? {
        return api.togetherLoan().dataOrThrow()
    }
}
