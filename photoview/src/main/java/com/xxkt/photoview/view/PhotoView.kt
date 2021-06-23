package com.xxkt.photoview.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.OverScroller
import kotlin.math.abs

class PhotoView : View {

    private var beginX: Float = 0.0f
    private var beginY: Float = 0.0f

    private var bDouble: Boolean = false

    //动画执行时间
    private val DURATION_ANIMATION: Long = 300

    private var bFinishValue: Int = 255
    private var bFinishScale: Float = 0.0f

    private var mInitScale: Float = 0.0f
    private var mInitOffsetX: Float = 0.0f
    private var mInitOffsetY: Float = 0.0f

    private var bFinishAnimation: Boolean = false
    private var bInitAnimation: Boolean = false
    private var initOffsetX: Int = 0
    private var initoffsetY: Int = 0
    private var initwidth: Int = 0
    private var initheight: Int = 0

    private var currentMaxScale: Float = 0.0f

    //X,Y 偏移量
    private var mOffsetX: Float = 0.0f
    private var mOffsetY: Float = 0.0f

    //手势
    private lateinit var gesture: GestureDetector

    //初始位置
    private var originX: Float = 0.0f
    private var originY: Float = 0.0f
    private lateinit var bitmap: Bitmap
    private lateinit var paint: Paint

    //最小，最大，当前的缩放量
    //最大的缩放量是最小的2倍
    private var smallScale = 0.0f
    private var bigScale = 0.0f
    private var currentScale = 0.0f

    //是否是大图
    private var bLarge: Boolean = false

    //最大最小的移动量
    private var leftTranslateX = 0.0f
    private var topTranslateY = 0.0f
    private var rightTranslateX = 0.0f
    private var bottomTranslateY = 0.0f

    //添加手指离开后滚动
    private lateinit var overScroller: OverScroller

    //添加放大缩小手
    private lateinit var scaleGesture: ScaleGestureDetector

    //是否正在小图拖动
    private var bSmallScroll: Boolean = false

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

    /**
     * 手势
     */
    inner class SimpleGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }

        // 单击（退出APP）
        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            recycler()
            return super.onSingleTapConfirmed(e)
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            if (bLarge) {
                //双击缩小
                bLarge = false
                getAnimator(currentScale, smallScale, object : PhotoAnimatorListener() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        mOffsetX = 0.0f
                        mOffsetY = 0.0f
                    }
                }).start()
            } else {
                setMaxTranslateOffset(bigScale)
                mOffsetX = (e!!.x - width / 2) * (1 - bigScale / smallScale)
                mOffsetY = (e.y - height / 2) * (1 - bigScale / smallScale)
                if (leftTranslateX <= 0) {
                    mOffsetX = 0.0f
                } else {
                    if (mOffsetX > leftTranslateX) {
                        mOffsetX = leftTranslateX
                    } else if (mOffsetX < rightTranslateX) {
                        mOffsetX = rightTranslateX
                    }
                }
                if (topTranslateY <= 0) {
                    mOffsetY = 0.0f

                } else {
                    if (mOffsetY > topTranslateY) {
                        mOffsetY = topTranslateY
                    } else if (mOffsetY < bottomTranslateY) {
                        mOffsetY = bottomTranslateY
                    }
                }

                getAnimator(smallScale, bigScale).start()
                bLarge = true
            }
            return super.onDoubleTap(e)
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (bLarge) {
                overScroller.fling(
                    mOffsetX.toInt(),
                    mOffsetY.toInt(),
                    velocityX.toInt(),
                    velocityY.toInt(),
                    leftTranslateX.coerceAtMost(rightTranslateX).toInt(),
                    rightTranslateX.coerceAtLeast(leftTranslateX).toInt(),
                    topTranslateY.coerceAtMost(bottomTranslateY).toInt(),
                    topTranslateY.coerceAtLeast(bottomTranslateY).toInt()
                )
                postOnAnimation(FlingRunnable())
            }
            return super.onFling(e1, e2, velocityX, velocityY)
        }

        /**
         *  手势滑动
         */
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            //大图情况下，可以滑动
            if (bLarge) {
                if (leftTranslateX <= 0) {
                    mOffsetX = 0.0f
                } else {
                    mOffsetX -= distanceX
                    if (mOffsetX > leftTranslateX) {
                        mOffsetX = leftTranslateX
                    } else if (mOffsetX < rightTranslateX) {
                        mOffsetX = rightTranslateX
                    }
                }
                if (topTranslateY <= 0) {
                    mOffsetY = 0.0f
                } else {
                    mOffsetY -= distanceY
                    if (mOffsetY > topTranslateY) {
                        mOffsetY = topTranslateY
                    } else if (mOffsetY < bottomTranslateY) {
                        mOffsetY = bottomTranslateY
                    }
                }
                invalidate()
            } else {//原图情况下
                if (bSmallScroll) {  //如果正在滚动，请继续滚动
                    mOffsetX -= distanceX
                    mOffsetY -= distanceY
                    invalidate()
                } else {  //如果没有滚动的话，当Y方向滑动（必须向下）的距离大于X方向，可以滑动（退出使用）
                    if (-distanceY > abs(distanceX)) {
                        bSmallScroll = true
                        mOffsetX -= distanceX
                        mOffsetY -= distanceY
                        invalidate()
                    }
                }
            }
            return super.onScroll(e1, e2, distanceX, distanceY)
        }
    }

    inner class ScaleGestureListener : ScaleGestureDetector.OnScaleGestureListener {

        private var initScale = 0.0f

        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            currentScale = initScale * detector!!.scaleFactor
            if (currentScale < 0.8 * smallScale) {
                currentScale = (0.8 * smallScale).toFloat()
            } else if (currentScale > 2 * bigScale) {
                currentScale = 2 * bigScale
            }
            invalidate()
            return false
        }

        override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
            if (currentScale == smallScale) {
                mOffsetX = 0.0f
                mOffsetY = 0.0f
            }
            initScale = currentScale
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector?) {
            if (currentScale > smallScale) {
                //大图
                setMaxTranslateOffset(currentScale)
                bLarge = true
            } else {
                //小图
                bLarge = false
                getAnimator(currentScale, smallScale, object : PhotoAnimatorListener() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        mOffsetX = 0.0f
                        mOffsetY = 0.0f
                    }
                }).start()
            }
        }

    }

    private inner class FlingRunnable : Runnable {
        override fun run() {
            if (overScroller.computeScrollOffset()) {
                mOffsetX = overScroller.currX.toFloat()
                mOffsetY = overScroller.currY.toFloat()

                if (leftTranslateX <= 0) {
                    mOffsetX = 0.0f
                } else {
                    if (mOffsetX > leftTranslateX) {
                        mOffsetX = leftTranslateX
                    } else if (mOffsetX < rightTranslateX) {
                        mOffsetX = rightTranslateX
                    }
                }
                if (topTranslateY <= 0) {
                    mOffsetY = 0.0f
                } else {
                    if (mOffsetY > topTranslateY) {
                        mOffsetY = topTranslateY
                    } else if (mOffsetY < bottomTranslateY) {
                        mOffsetY = bottomTranslateY
                    }
                }

                invalidate()
                postOnAnimation(this)
            }
        }
    }

    private fun init(context: Context) {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        gesture = GestureDetector(context, SimpleGestureListener())
        scaleGesture = ScaleGestureDetector(context, ScaleGestureListener())
        overScroller = OverScroller(context)
    }

    fun getAnimator(
        smallScale: Float,
        bigScale: Float,
        param: PhotoAnimatorListener? = null, duration: Long = DURATION_ANIMATION
    ): ObjectAnimator {
        val objectAnimator = ObjectAnimator.ofFloat(this, "scaleValue", smallScale, bigScale)
        if (param != null) {
            objectAnimator.addListener(param)
        }
        objectAnimator.duration = duration
        return objectAnimator
    }

    fun setScaleValue(scale: Float) {
        currentScale = scale
        invalidate()
    }

    fun setTranslateX(tranX: Float) {
        mOffsetX = tranX
        invalidate()
    }

    fun setTranslateY(tranX: Float) {
        mOffsetY = tranX
        invalidate()
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        Log.e("TTTAAA", "photoView dispatchTouchEvent  event.action = ${event!!.action}")


        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!bSmallScroll) {

                    beginX = event.rawX
                    beginY = event.rawY
                    parent.requestDisallowInterceptTouchEvent(true)
                }
            }
            in 250..500 -> {
                if (!bSmallScroll) {

                    bDouble = true;
                    parent.requestDisallowInterceptTouchEvent(true)
                }

            }
            MotionEvent.ACTION_MOVE -> {
                if (!bDouble && !bSmallScroll) {
                    if (bLarge) {//大图情况下一般不需要拦截
                        var detalX = event.rawX - beginX
                        var detalY = event.rawY - beginY
                        if (abs(detalY) < abs(detalX)) {
                            //左右滑动
                            parent.requestDisallowInterceptTouchEvent(true)
                            if (detalX > 0.0f) {//往右边滑动
                                if (mOffsetX == leftTranslateX) {
                                    parent.requestDisallowInterceptTouchEvent(false)
                                } else {
                                    parent.requestDisallowInterceptTouchEvent(true)
                                }
                            } else {//往左边滑动
                                if (mOffsetX == rightTranslateX) {
                                    parent.requestDisallowInterceptTouchEvent(false)
                                } else {
                                    parent.requestDisallowInterceptTouchEvent(true)
                                }
                            }
                        } else {
                            parent.requestDisallowInterceptTouchEvent(true)

                        }
                    } else {
                        var detalX = event.rawX - beginX
                        var detalY = event.rawY - beginY
                        if (abs(detalY) >= abs(detalX)) {
                            //上下滑动，不需要管intercept
                            parent.requestDisallowInterceptTouchEvent(true)
                        } else {
                            parent.requestDisallowInterceptTouchEvent(false)

                        }
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                if (!bSmallScroll) {

                    bDouble = false
                    parent.requestDisallowInterceptTouchEvent(false)
                }
            }
        }
        var result = super.dispatchTouchEvent(event)
        return result
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (bSmallScroll && event!!.action == MotionEvent.ACTION_UP) {
            if (mOffsetY > DURATION_ANIMATION) {
                recycler()
            } else {
                Log.e("TAG", "抬起")
                var animator1: ObjectAnimator =
                    ObjectAnimator.ofFloat(this, "translateX", mOffsetX, 0f)
                var animator2: ObjectAnimator =
                    ObjectAnimator.ofFloat(this, "translateY", mOffsetY, 0f)
                val animatorSet = AnimatorSet()
                animatorSet.playTogether(animator1, animator2)
                animatorSet.duration = DURATION_ANIMATION
                animatorSet.start()
                animatorSet.addListener(object : PhotoAnimatorListener() {
                    override fun onAnimationEnd(animation: Animator?) {
                        bSmallScroll = false
                    }
                })
            }
        }
        var result = scaleGesture.onTouchEvent(event)
        if (!scaleGesture.isInProgress) {
            result = gesture.onTouchEvent(event)
        }
        Log.e("TTTAAA", "photoView onTouchEvent result = $result")

        return result
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.run {
            when {
                bSmallScroll -> {
                    //当小图的时候，拖动图片
                    canvasSmallScroll(this)
                }
                bInitAnimation -> {
                    setBackgroundColor(Color.parseColor("#000000"))
                    var offsetFactor: Float = 1 - currentScale / smallScale
                    translate(mOffsetX * offsetFactor, mOffsetY * offsetFactor)
                    scale(currentScale, currentScale, width.toFloat() / 2, height.toFloat() / 2)
                    drawBitmap(bitmap, originX, originY, paint)
                }
                bFinishAnimation -> {
                    setBackgroundColor(Color.parseColor("#000000"))
                    var offsetFactor: Float =
                        1 - (bFinishScale - currentScale) / (bFinishScale - mInitScale)
                    translate(
                        mInitOffsetX + (mOffsetX - mInitOffsetX) * offsetFactor,
                        mInitOffsetY + (mOffsetY - mInitOffsetY) * offsetFactor
                    )
                    background.alpha = (bFinishValue * offsetFactor).toInt()

                    scale(currentScale, currentScale, width.toFloat() / 2, height.toFloat() / 2)
                    drawBitmap(bitmap, originX, originY, paint)
                }
                else -> {
                    canvasNormal(this)


                }
            }
        }
    }

    /**
     * 正常绘制
     */
    private fun canvasNormal(canvas: Canvas) {
        setBackgroundColor(Color.parseColor("#000000"))
        var offsetFactor: Float =
            (currentScale - smallScale) / (currentMaxScale - smallScale)
        canvas.translate(mOffsetX * offsetFactor, mOffsetY * offsetFactor)
        canvas.scale(currentScale, currentScale, width.toFloat() / 2, height.toFloat() / 2)
        canvas.drawBitmap(bitmap, originX, originY, paint)
    }

    /**
     *  小图的时候，可以拖动图片，然后退出Activity或者复原
     */
    private fun canvasSmallScroll(canvas: Canvas) {
        canvas.translate(mOffsetX, mOffsetY)
        var scale: Float = currentScale
        setBackgroundColor(Color.parseColor("#000000"))
        scale = if (mOffsetY < 0) {
            background.alpha = 255
            currentScale
        } else {
            var value =
                (255 * (1 - mOffsetY * 2 / (height + bitmap.height * currentScale))).toInt()
            if (value < 0) {
                value = 0
            }
            background.alpha = value
            currentScale * (1 - mOffsetY * 3 / (height * 2 + bitmap.height * currentScale * 2))
        }
        canvas.scale(scale, scale, width.toFloat() / 2, height.toFloat() / 2)
        canvas.drawBitmap(bitmap, originX, originY, paint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        //初始值位置是中心
        originX = ((width - bitmap.width) / 2).toFloat()
        originY = ((height - bitmap.height) / 2).toFloat()

        //得到小的缩放量（横屏充满或者竖屏充满即是最小缩放量，设最大缩放两是最小缩放量的2倍）
        smallScale =
            if (bitmap.width.toFloat() / bitmap.height.toFloat() > width.toFloat() / height.toFloat()) {
                width / bitmap.width.toFloat()
            } else {
                height / bitmap.height.toFloat()
            }
        bigScale = 2 * smallScale


        if (bInitAnimation) {
            mInitOffsetX =
                (-width / 2 + initwidth / 2 + initOffsetX).toFloat() / (1 - currentScale / smallScale)
            mInitOffsetY =
                (-height / 2 + initheight / 2 + initoffsetY - 90).toFloat() / (1 - currentScale / smallScale)
            mInitScale = initwidth / bitmap.width.toFloat()

            currentScale = mInitScale

            mOffsetX = mInitOffsetX
            mOffsetY = mInitOffsetY
            getAnimator(
                initwidth / bitmap.width.toFloat(),
                smallScale,
                object : PhotoAnimatorListener() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        mOffsetY = 0.0f
                        mOffsetX = 0.0f
                        bInitAnimation = false
                    }
                }).start()
        } else {
            currentScale = smallScale
        }

    }

    //初始化进来
    fun initData(offsetX: Int, offsetY: Int, width: Int, height: Int, bmp: Bitmap) {
        this.initOffsetX = offsetX
        this.initoffsetY = offsetY
        this.initwidth = width
        this.initheight = height
        this.bitmap = bmp

        bInitAnimation = true
        Log.e("TAG", "offsetX=$offsetX  offsetY=$offsetY width=$width height=$height   ")
    }

    //初始化进来
    fun initData(bmp: Bitmap) {
        this.bitmap = bmp
        bInitAnimation = false
    }

    /**
     * todo 设置最大偏移量
     */
    private fun setMaxTranslateOffset(maxScale: Float) {
        currentMaxScale = maxScale
        leftTranslateX = (bitmap.width * maxScale - width) / 2
        rightTranslateX = (width - bitmap.width * maxScale) / 2
        topTranslateY = (bitmap.height * maxScale - height) / 2
        bottomTranslateY = (height - bitmap.height * maxScale) / 2
    }

    /**
     * 释放资源并销毁
     */
    fun recycler() {
        bFinishAnimation = true
        bSmallScroll = false
        bFinishScale = currentScale
        bFinishValue = background.alpha
        getAnimator(
            currentScale,
            mInitScale,
            object : PhotoAnimatorListener() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    if (context is Activity) {
                        (context as Activity).finish()
                    }
                }
            }, DURATION_ANIMATION
        ).start()
    }


    abstract inner class PhotoAnimatorListener : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator?) {
        }

        override fun onAnimationEnd(animation: Animator?) {
        }

        override fun onAnimationCancel(animation: Animator?) {
        }

        override fun onAnimationRepeat(animation: Animator?) {
        }

    }
}