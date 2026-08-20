package com.novexa.platform.core.ui.binding

import android.widget.ImageView
import com.novexa.platform.core.ui.image.UiImageSource
import com.novexa.platform.core.ui.extension.loadImage

/** Renders either a remote URL or a local Uri from one consistent UI state. */
fun ImageView.bindImageUrl(source: UiImageSource?) {
    when (source) {
        is UiImageSource.RemoteUrl -> loadImage(source.value)
        is UiImageSource.LocalUri -> loadImage(source.value)
        null -> setImageDrawable(null)
    }
}
