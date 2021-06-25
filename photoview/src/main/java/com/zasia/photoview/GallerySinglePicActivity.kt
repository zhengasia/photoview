package com.zasia.photoview

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.xxkt.common.GlideApp
import com.zasia.photoview.view.PhotoView
import kotlinx.android.synthetic.main.activity_gallery.*
import kotlinx.android.synthetic.main.activity_gallery.linearLayout
import kotlinx.android.synthetic.main.fragment_photo.*

/**
 * @author ASIA
 * 仿微信查看大图，单图
 */
class GallerySinglePicActivity : AppCompatActivity() {

    private var photoView: PhotoView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        if (intent.hasExtra("resId")) {
            var bitmap = BitmapFactory.decodeResource(resources, intent.getIntExtra("resId", 0))
            photoView = PhotoView(this@GallerySinglePicActivity)
            photoView?.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            photoView?.initData(
                this@GallerySinglePicActivity.intent.getIntExtra("offsetX", 0),
                this@GallerySinglePicActivity.intent.getIntExtra("offsetY", 0),
                this@GallerySinglePicActivity.intent.getIntExtra("width", 0),
                this@GallerySinglePicActivity.intent.getIntExtra("height", 0), bitmap
            )
            linearLayout.addView(photoView)
        } else if (intent.hasExtra("url")) {
            GlideApp.with(this@GallerySinglePicActivity)
                .asBitmap()
                .load(intent.getStringExtra("url"))
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        photoView = PhotoView(this@GallerySinglePicActivity)
                        photoView?.layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        photoView?.initData(
                            this@GallerySinglePicActivity.intent.getIntExtra("offsetX", 0),
                            this@GallerySinglePicActivity.intent.getIntExtra("offsetY", 0),
                            this@GallerySinglePicActivity.intent.getIntExtra("width", 0),
                            this@GallerySinglePicActivity.intent.getIntExtra("height", 0), resource
                        )
                        linearLayout.addView(photoView)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        }
    }

    override fun onBackPressed() {
        photoView?.run {
            recycler()
        }
    }

}