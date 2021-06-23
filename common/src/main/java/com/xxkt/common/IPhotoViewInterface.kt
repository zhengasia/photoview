package com.xxkt.common

import android.content.Context
import android.view.View

interface IPhotoViewInterface {
    fun openGalleryPage(
        context: Context,
        view: View,
        resId: Int = 0,
        url: String? = null,
        urls: ArrayList<String>? = null
    )
}