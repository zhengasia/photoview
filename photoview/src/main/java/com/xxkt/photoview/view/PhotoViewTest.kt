package com.xxkt.photoview.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.xxkt.photoview.R

class PhotoViewTest : View {

    private var originX: Float = 0.0f
    private var originY: Float = 0.0f
    private lateinit var bitmap: Bitmap
    private lateinit var paint: Paint

    constructor(context: Context?) : super(context) {
        init(context!!)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context!!)
    }

    constructor(context: Context?, attrs: AttributeSet?, intStyleAttr: Int) : super(
        context,
        attrs,
        intStyleAttr
    ) {
        init(context!!)
    }
    private fun init(context: Context) {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.textSize = 30f
        paint.color = Color.RED
        bitmap = BitmapFactory.decodeResource(resources, R.drawable.phototext)

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        originX = ((width - bitmap.width) / 2).toFloat()
        originY = ((height - bitmap.height) / 2).toFloat()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.scale(2f,2f, width.toFloat() / 2, height.toFloat() / 2)


        canvas?.drawBitmap(bitmap, originX,originY, paint)
        canvas?.drawText("1111",0f,20f,paint)
    }

}