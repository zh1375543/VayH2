package com.velora.portal.platform.design.binding

import android.widget.ImageView
import com.velora.portal.platform.design.image.UiImageSource
import com.velora.portal.platform.design.extension.loadImage

/** Renders either a remote URL or a local Uri from one consistent UI state. */
fun ImageView.bindImageUrl(source: UiImageSource?) {
    when (source) {
        is UiImageSource.RemoteUrl -> loadImage(source.value)
        is UiImageSource.LocalUri -> loadImage(source.value)
        null -> setImageDrawable(null)
    }
}
