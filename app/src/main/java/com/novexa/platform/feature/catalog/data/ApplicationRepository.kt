package com.novexa.platform.feature.catalog.data

import com.novexa.platform.core.network.Api
import com.novexa.platform.core.network.NetworkProvider
import com.novexa.platform.core.common.data.repository.dataOrThrow
import com.novexa.platform.feature.catalog.model.MemberOverviewResponse
import com.novexa.platform.feature.catalog.model.CatalogItemBean
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
