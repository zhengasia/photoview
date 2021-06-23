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
    //屏幕上是否是双指操作中
    private var bDoubleFinger: Boolean = false

    //动画执行时间
    private val DURATION_ANIMATION: Long = 300

    private var bFinishValue: Int = 255
    private var bFinishScale: Float = 0.0f

    private var mInitScale: Float = 0.0f

    //初始图片偏移量，为了退出时返回原来的位置
    private var mInitOffsetX: Float = 0.0f
    private var mInitOffsetY: Float = 0.0f

    private var bFinishAnimation: Boolean = false

    //第一次进来是否需要偏移动画
    private var bInitAnimation: Boolean = false
    private var initOffsetX: Int = 0
    private var initoffsetY: Int = 0
    private var initwidth: Int = 0
    private var initheight: Int = 0


    //X,Y 当前偏移量
    private var mCurrentOffsetX: Float = 0.0f
    private var mCurrentOffsetY: Float = 0.0f

    //手势
    private lateinit var gesture: GestureDetector

    //初始位置
    private var originX: Float = 0.0f
    private var originY: Float = 0.0f
    private lateinit var bitmap: Bitmap
    private lateinit var paint: Paint

    //最小，最大，当前的缩放量和当前最大的偏移量
    //最大的缩放量是最小的2倍
    private var smallScale = 0.0f
    private var bigScale = 0.0f
    private var currentScale = 0.0f
    private var currentMaxScale = 0.0f

    //是否是大图
    private var bLarge: Boolean = false

    //图片放大后，可以拖动的最大最小的距离
    private var maxOffsetX = 0.0f
    private var maxOffsetY = 0.0f

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
            return true //todo 返回true，不然手势无法执行
        }

        // 单击（退出APP）
        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            recycler()
            return super.onSingleTapConfirmed(e)
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            if (bLarge) { //todo 双击缩小
                bLarge = false
                getAnimator(currentScale, smallScale, object : PhotoAnimatorListener() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        //动画结束后设偏移量为0
                        mCurrentOffsetX = 0.0f
                        mCurrentOffsetY = 0.0f
                    }
                }).start()
            } else {//todo 双击放大
                setMaxTranslateOffset(bigScale)
                //点击哪里，哪里放大，需要获取偏移量
                mCurrentOffsetX = (e!!.x - width / 2) * (1 - bigScale / smallScale)
                mCurrentOffsetY = (e!!.y - height / 2) * (1 - bigScale / smallScale)
                //矫正偏移量，不能超过可以拖动的边界
                correctOffset()
                getAnimator(smallScale, bigScale).start()
                bLarge = true
            }
            return super.onDoubleTap(e)
        }
        //todo 惯性滑动
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (bLarge) {
                overScroller.fling(
                    mCurrentOffsetX.toInt(),
                    mCurrentOffsetY.toInt(),
                    velocityX.toInt(),
                    velocityY.toInt(),
                    (-maxOffsetX).toInt(),
                    maxOffsetX.toInt(),
                    (-maxOffsetY).toInt(),
                    maxOffsetY.toInt()
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
            if(bDoubleFinger){//双指在屏幕上的时候禁止滑动，规避scaleEnd触发
                return super.onScroll(e1, e2, distanceX, distanceY)
            }
            //大图情况下，可以滑动
            if (bLarge) {
                mCurrentOffsetX -= distanceX
                mCurrentOffsetY -= distanceY
                correctOffset()
                invalidate()
            } else {//原图情况下
                if (bSmallScroll) {  //如果正在滚动，请继续滚动
                    mCurrentOffsetX -= distanceX
                    mCurrentOffsetY -= distanceY
                    invalidate()
                } else {  //如果没有滚动的话，当Y方向滑动（必须向下）的距离大于X方向，可以滑动（退出使用）
                    if (-distanceY > abs(distanceX)) {
                        bSmallScroll = true
                        mCurrentOffsetX -= distanceX
                        mCurrentOffsetY -= distanceY
                        invalidate()
                    }
                }
            }
            return super.onScroll(e1, e2, distanceX, distanceY)
        }
    }

    /**
     * 矫正偏移量，不能超过可以拖动的最大边界
     */
    private fun correctOffset() {
        when {
            maxOffsetX==0.0f -> {
                mCurrentOffsetX = 0.0f;
            }
            mCurrentOffsetX>maxOffsetX -> {
                mCurrentOffsetX = maxOffsetX
            }
            mCurrentOffsetX<-maxOffsetX -> {
                mCurrentOffsetX = -maxOffsetX
            }
        }
        when {
            maxOffsetY==0.0f -> {
                mCurrentOffsetY = 0.0f;
            }
            mCurrentOffsetY>maxOffsetY -> {
                mCurrentOffsetY = maxOffsetY
            }
            mCurrentOffsetY<-maxOffsetY -> {
                mCurrentOffsetY = -maxOffsetY
            }
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
                mCurrentOffsetX = 0.0f
                mCurrentOffsetY = 0.0f
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
                        mCurrentOffsetX = 0.0f
                        mCurrentOffsetY = 0.0f
                    }
                }).start()
            }
        }

    }

    /**
     * 惯性滑动
     */
    private inner class FlingRunnable : Runnable {
        override fun run() {
            if (overScroller.computeScrollOffset()) {
                mCurrentOffsetX = overScroller.currX.toFloat()
                mCurrentOffsetY = overScroller.currY.toFloat()
                correctOffset()
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

    /**
     * 放大缩小动画
     */
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
        mCurrentOffsetX = tranX
        invalidate()
    }

    fun setTranslateY(tranX: Float) {
        mCurrentOffsetY = tranX
        invalidate()
    }

    /**
     * 解决ViewPager  图片左右拖动  事件冲突
     */
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!bSmallScroll) {

                    beginX = event.rawX
                    beginY = event.rawY
                    parent.requestDisallowInterceptTouchEvent(true)
                }
            }
            in 250..500 -> {//todo ？ 有点问题，怎么判断是两个手指在一起？

                if (!bSmallScroll) {
                    bDoubleFinger = true;
                    parent.requestDisallowInterceptTouchEvent(true)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (!bDoubleFinger && !bSmallScroll) {
                    if (bLarge) {//todo 大图情况下一般不需要拦截
                        var detalX = event.rawX - beginX
                        var detalY = event.rawY - beginY
                        if (abs(detalY) < abs(detalX)) {//todo 左右滑动
                            if(maxOffsetX == 0.0f){//如果是不能左右拖动，交给viewpager拖动
                                parent.requestDisallowInterceptTouchEvent(false)
                            }else{
                                if (detalX > 0.0f) {//往右边滑动
                                    if (mCurrentOffsetX == maxOffsetX) {
                                        parent.requestDisallowInterceptTouchEvent(false)
                                    } else {
                                        parent.requestDisallowInterceptTouchEvent(true)
                                    }
                                } else {//往左边滑动
                                    if (mCurrentOffsetX == -maxOffsetX) {
                                        parent.requestDisallowInterceptTouchEvent(false)
                                    } else {
                                        parent.requestDisallowInterceptTouchEvent(true)
                                    }
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
                    bDoubleFinger = false
                    parent.requestDisallowInterceptTouchEvent(false)
                }
            }
        }
        var result = super.dispatchTouchEvent(event)
        return result
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (bSmallScroll && event!!.action == MotionEvent.ACTION_UP) {//todo 小图拖动情况下，手指抬起
            if (mCurrentOffsetY > DURATION_ANIMATION) {//超过拖动距离，退出页面
                recycler()
            } else {//回归原位置
                var animator1: ObjectAnimator =
                    ObjectAnimator.ofFloat(this, "translateX", mCurrentOffsetX, 0f)
                var animator2: ObjectAnimator =
                    ObjectAnimator.ofFloat(this, "translateY", mCurrentOffsetY, 0f)
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
        return result
    }

    /**
     * 填充bitmap加画布拖动，放大缩小
     */
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.run {
            when {
                bSmallScroll -> {
                    //当小图的时候，拖动图片
                    canvasSmallScroll(this)
                }
                bInitAnimation -> {
                    //第一次进来时候绘制
                    canvasInitComing(this)
                }
                bFinishAnimation -> {
                    //页面退出时绘制，需要返回初始点
                    canvasFinish(this)
                }
                else -> {
                    //正常绘制
                    canvasNormal(this)
                }
            }
        }
    }

    /**
     * 页面退出时绘制，需要返回初始点
     */
    private fun canvasFinish(canvas: Canvas) {
        setBackgroundColor(Color.parseColor("#000000"))
        var offsetFactor: Float =
            1 - (bFinishScale - currentScale) / (bFinishScale - mInitScale)
        canvas.translate(
            mInitOffsetX + (mCurrentOffsetX - mInitOffsetX) * offsetFactor,
            mInitOffsetY + (mCurrentOffsetY - mInitOffsetY) * offsetFactor
        )
        background.alpha = (bFinishValue * offsetFactor).toInt()
        canvas.scale(currentScale, currentScale, width.toFloat() / 2, height.toFloat() / 2)
        canvas.drawBitmap(bitmap, originX, originY, paint)
    }

    /**
     * 第一次进来时候绘制
     */

    private fun canvasInitComing(canvas: Canvas) {
        setBackgroundColor(Color.parseColor("#000000"))
        var offsetFactor: Float = 1 - currentScale / smallScale
        canvas.translate(mCurrentOffsetX * offsetFactor, mCurrentOffsetY * offsetFactor)
        canvas.scale(currentScale, currentScale, width.toFloat() / 2, height.toFloat() / 2)
        canvas.drawBitmap(bitmap, originX, originY, paint)
    }

    /**
     * 正常绘制
     */
    private fun canvasNormal(canvas: Canvas) {
        setBackgroundColor(Color.parseColor("#000000"))
        //偏移因子，大小从0到1或者从1到0，
        var offsetFactor: Float =  (currentScale - smallScale) / (currentMaxScale - smallScale)
        canvas.translate(mCurrentOffsetX * offsetFactor, mCurrentOffsetY * offsetFactor)

        canvas.scale(currentScale, currentScale, width.toFloat() / 2, height.toFloat() / 2)
        canvas.drawBitmap(bitmap, originX, originY, paint)
    }

    /**
     *  小图的时候，可以拖动图片，然后退出Activity或者复原
     */
    private fun canvasSmallScroll(canvas: Canvas) {
        canvas.translate(mCurrentOffsetX, mCurrentOffsetY)
        var scale: Float = currentScale
        setBackgroundColor(Color.parseColor("#000000"))
        scale = if (mCurrentOffsetY < 0) {
            background.alpha = 255
            currentScale
        } else {
            var value =
                (255 * (1 - mCurrentOffsetY * 2 / (height + bitmap.height * currentScale))).toInt()
            if (value < 0) {
                value = 0
            }
            background.alpha = value
            currentScale * (1 - mCurrentOffsetY * 3 / (height * 2 + bitmap.height * currentScale * 2))
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



        if (bInitAnimation) {//todo 需要首次进来动画
            mInitOffsetX  =
                (-width / 2 + initwidth / 2 + initOffsetX).toFloat()
            mInitOffsetY =
                (-height / 2 + initheight / 2 + initoffsetY - getStatusBarHeight()).toFloat()
            mInitScale = initwidth / bitmap.width.toFloat()
            currentScale = mInitScale
            mCurrentOffsetX = mInitOffsetX
            mCurrentOffsetY = mInitOffsetY
            getAnimator(
                initwidth / bitmap.width.toFloat(),
                smallScale,
                object : PhotoAnimatorListener() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        mCurrentOffsetY = 0.0f
                        mCurrentOffsetX = 0.0f
                        bInitAnimation = false
                    }
                }).start()
        } else {
            currentScale = smallScale
        }

    }

    //初始化进来，需要位置移动
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
     * todo 设置最大偏移量,即：拖动的时候可以拖多少，拖到边界不能再拖动了
     */
    private fun setMaxTranslateOffset(maxScale: Float) {
        currentMaxScale = maxScale

        maxOffsetX = if(bitmap.width*maxScale-width<=0){
            0.0f
        }else{
            (bitmap.width * maxScale - width) / 2
        }

        maxOffsetY = if(bitmap.height*maxScale-height<=0){
            0.0f
        }else{
            (bitmap.height * maxScale - height) / 2
        }
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

    /**
     * 动画结束监听
     */
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

    /**
     * 获取状态栏高度
     */
    private fun getStatusBarHeight(): Int {
        val resourceId: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
        return resources.getDimensionPixelSize(resourceId)
    }
}