package com.zasia.photoview.inter

import android.content.Context
import android.content.Intent
import android.view.View
import com.google.auto.service.AutoService
import com.zasia.photoview.GallerySinglePicActivity
import com.zasia.photoview.GalleryMorePicActivity


@AutoService(com.xxkt.common.IPhotoViewInterface::class)
open class PhotoViewImpl : com.xxkt.common.IPhotoViewInterface {
    override fun openGalleryPage(
        context: Context,
        view: View,
        resId: Int,
        url: String?,
        urls: ArrayList<String>?
    ) {
        var intent = Intent(context, GallerySinglePicActivity::class.java)
        when {
            !urls.isNullOrEmpty() -> {
                intent = Intent(context, GalleryMorePicActivity::class.java)
                intent.putStringArrayListExtra("urls", urls)
                intent.putExtra("index", resId)
            }
            resId != 0 -> {
                intent = Intent(context, GallerySinglePicActivity::class.java)
                intent.putExtra("resId", resId)
            }
            url != null -> {
                intent = Intent(context, GallerySinglePicActivity::class.java)
                intent.putExtra("url", url)
            }
        }
        val location: IntArray = IntArray(2)
        view.getLocationOnScreen(location)
        intent.putExtra("offsetX", location[0])
        intent.putExtra("offsetY",location[1])
        intent.putExtra("width", view.width)
        intent.putExtra("height", view.height)
        context.startActivity(intent)

    }
}