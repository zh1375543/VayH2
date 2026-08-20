package com.novexa.platform.feature.accounts.presentation.binding

import android.widget.ImageView
import com.novexa.platform.R

/** Renders the local icon that matches the selected payout account type. */
fun ImageView.bindAccountIcon(payWay: String?) {
    setImageResource(
        if (payWay == "WALLET") R.mipmap.ic_wallet_header else R.mipmap.ic_bank_header,
    )
}
