package com.velora.portal.journey.lending.catalog.data

import com.velora.portal.platform.network.Api
import com.velora.portal.platform.network.NetworkProvider
import com.velora.portal.platform.common.data.repository.dataOrThrow
import com.velora.portal.domain.credit.model.MemberOverviewResponse
import com.velora.portal.domain.credit.model.CatalogItemBean
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
