package com.velora.portal.platform.network

import com.velora.portal.platform.network.api.AuthApi
import com.velora.portal.platform.network.api.CommonApi
import com.velora.portal.platform.network.api.FinanceApi
import com.velora.portal.platform.network.api.LoanApi
import com.velora.portal.platform.network.api.PayoutApi
import com.velora.portal.platform.network.api.UserApi

interface Api : AuthApi, UserApi, LoanApi, PayoutApi, FinanceApi, CommonApi
