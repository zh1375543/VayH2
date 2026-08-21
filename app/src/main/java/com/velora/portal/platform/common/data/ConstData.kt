package com.velora.portal.platform.common.data

import com.velora.portal.platform.common.util.SPUtil
import com.velora.portal.platform.common.util.text.parseJsonList
import com.velora.portal.platform.common.util.text.toJsonString
import com.velora.portal.platform.common.util.text.parseJson

const val APPCODE = "gaanpuhunan"
var HomeLoanAmountRange: String? = null

var appFlyer: String
    get() = SPUtil.newInstance().get(PreferenceSchema.AppKeys.APPS_FLYER_DATA, "")
    set(value) {
        SPUtil.newInstance().save(PreferenceSchema.AppKeys.APPS_FLYER_DATA, value)
    }

var language: String
    get() = "en"
    set(value) {
        SPUtil.newInstance().save(PreferenceSchema.AppKeys.LANGUAGE, "en")
    }

var agreePhonePrivacy: Boolean
    get() = SPUtil.newInstance().get(PreferenceSchema.AppKeys.PHONE_PRIVACY, false)
    set(value) {
        SPUtil.newInstance().save(PreferenceSchema.AppKeys.PHONE_PRIVACY, value)
    }

var location: Pair<Double, Double>
    get() = SPUtil.getInstance()
        .get(PreferenceSchema.CoreKeys.LOCATION, "")
        .parseJson<Pair<Double, Double>>() ?: (0.0 to 0.0)
    set(value) {
        SPUtil.getInstance().save(PreferenceSchema.CoreKeys.LOCATION, value.toJsonString())
    }

var signBackHome: Boolean
    get() = SPUtil.getInstance().get(PreferenceSchema.CoreKeys.SIGN_BACK_HOME, false)
    set(value) {
        SPUtil.getInstance().save(PreferenceSchema.CoreKeys.SIGN_BACK_HOME, value)
    }

var authConfigList: List<String>
    get() = SPUtil.getInstance()
        .get(PreferenceSchema.CoreKeys.AUTH_CONFIG, "")
        .parseJsonList<String>() ?: emptyList()
    set(value) {
        SPUtil.getInstance().save(PreferenceSchema.CoreKeys.AUTH_CONFIG, value.toJsonString())
    }

var isPostDeviceInfo: Boolean
    get() = SPUtil.getInstance().get(PreferenceSchema.CoreKeys.DEVICE_POSTED, false)
    set(value) {
        SPUtil.getInstance().save(PreferenceSchema.CoreKeys.DEVICE_POSTED, value)
    }

var refer: String
    get() = SPUtil.newInstance().get(PreferenceSchema.AppKeys.GA_REFER, "")
    set(value) {
        SPUtil.newInstance().save(PreferenceSchema.AppKeys.GA_REFER, value)
    }

var afSource: String
    get() = SPUtil.newInstance().get(PreferenceSchema.AppKeys.AF_SOURCE, "")
    set(value) {
        SPUtil.newInstance().save(PreferenceSchema.AppKeys.AF_SOURCE, value)
    }

var gaId: String
    get() = SPUtil.newInstance().get(PreferenceSchema.AppKeys.GAID, "")
    set(value) {
        SPUtil.newInstance().save(PreferenceSchema.AppKeys.GAID, value)
    }

var rateApp: Boolean
    get() = SPUtil.newInstance().get(PreferenceSchema.AppKeys.RATE_APP, false)
    set(value) {
        SPUtil.newInstance().save(PreferenceSchema.AppKeys.RATE_APP, value)
    }

var firebaseToken: String
    get() = SPUtil.newInstance().get(PreferenceSchema.AppKeys.FIREBASE_TOKEN, "")
    set(value) {
        SPUtil.newInstance().save(PreferenceSchema.AppKeys.FIREBASE_TOKEN, value)
    }

var firebaseId: String
    get() = SPUtil.newInstance().get(PreferenceSchema.AppKeys.FIREBASE_ID, "")
    set(value) {
        SPUtil.newInstance().save(PreferenceSchema.AppKeys.FIREBASE_ID, value)
    }

const val ORDER_STATUS_NOT = -1
const val ORDER_STATUS_SUCCESS = 10
const val ORDER_STATUS_REVIEW = 11 // pre-review
const val ORDER_STATUS_AUTO = 12 // auto review
const val ORDER_STATUS_MANUAL = 13 // manual review
const val ORDER_STATUS_AUTO_FAIL = 14
const val ORDER_STATUS_MANUAL_FAIL = 15
const val ORDER_STATUS_AUTO_SUCCESS = 16
const val ORDER_STATUS_MANUAL_SUCCESS = 17
const val ORDER_STATUS_BANK_VERIFIED = 18
const val ORDER_STATUS_SIGNED = 20
const val ORDER_STATUS_CASH = 21
const val ORDER_STATUS_INVALID = 22
const val ORDER_STATUS_CLOSE = 23
const val ORDER_STATUS_PAYMENT_ING = 24
const val ORDER_STATUS_PAYMENT_FAIL = 25
const val ORDER_STATUS_PAYMENT_PENDING = 30 // pending repayment
const val ORDER_STATUS_PAYMENT_PROCESS = 31 // repayment processing
const val ORDER_STATUS_IN_RENEWAL = 32
const val ORDER_STATUS_IN_RENEWAL_PROCESS = 33
const val ORDER_STATUS_OVERDUE = 34 // overdue
const val ORDER_STATUS_BAD_DEBTS = 35
const val ORDER_STATUS_SETTLE = 40
const val ORDER_STATUS_SETTLE_REDUCE = 41
const val ORDER_STATUS_SETTLE_RENEWAL = 42
const val ORDER_STATUS_SETTLE_REDUCE_OR_RENEWAL = 43


const val AF_DEV_KEY = "XyR2FTp7tA3UDGzmzNqtui"

private const val baseWebUrl = "https://www.papavay.com"
const val PRIVACY_POLICY = "$baseWebUrl/agreement/protocol_privacy_index.html"
const val AGREEMENT_ABOUT = "$baseWebUrl/agreement/about.html"
const val AGREEMENT_REGISTER = "$baseWebUrl/agreement/register.html"

const val HTTP_INFORMATION_COLLECTION =
    "$baseWebUrl/agreement/contact_license_agreement.html"
const val PRIVACY_COLLECT =
    "$baseWebUrl/agreement/Information_collection_service_agreement.html"
const val LEASE_AGREEMENT = "$baseWebUrl/agreement/leaseAgreement.html?"
const val PAWN_AGREEMENT = "$baseWebUrl/agreement/pawnAgreement.html?"

const val PRODUCT_AGREEMENT = "$baseWebUrl/agreement/borrow.html?"
