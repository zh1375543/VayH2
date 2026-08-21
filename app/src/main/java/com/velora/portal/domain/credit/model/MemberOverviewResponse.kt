package com.velora.portal.domain.credit.model

import java.math.BigDecimal

data class MemberOverviewResponse(
    val customerPhone: String? = null,
    val customerEmail: String? = null,
    val calmFlag: Boolean = false,
    val enableLoanStr: String? = null,
    val loanAmountRange: String? = null,
    val bankErrorFlag: Boolean,
    val showMultipleRepaySign: Int = 0,
    val recommendText: String?=null,
    val showProducts: List<CatalogEntry>? = null,
    val repayProducts: List<CatalogEntry>? = null,
    val canNotApplyProducts: List<CatalogEntry>? = null,
    val userCreditAmount: BigDecimal? = null,
    val userCreditCurrency: String? = null,
    val userCreditCurrencySymbol: String? = null,
    val togetherLoanSign: Int? = null,
    val canApplyAmount: BigDecimal? = null,
    val allAmount: BigDecimal? = null,
    val userCashWalletId: Long? = null,
    val walletAccount: String? = null,
    val bankInfoId: Long? = null,
    val bankNo: String? = null,
    val currency: String? = null,
    val currencySymbol: String? = null,
    val isNew: Int = 1,
    val totalCreditAmount: BigDecimal? = null,
    val usedAmount: BigDecimal? = null,
    val userCreditStatus: Int? = null,
)
