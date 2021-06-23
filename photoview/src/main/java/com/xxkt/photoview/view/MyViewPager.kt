package com.xxkt.photoview.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class MyViewPager: ViewPager {

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        Log.e("TTTAAA","viewPager onInterceptToucheEVent")
        if(ev!!.action == MotionEvent.ACTION_DOWN){
            Log.e("TTTAAA","viewPager onInterceptToucheEVent   不拦截")
            super.onInterceptTouchEvent(ev)
            return false
        }else{
            Log.e("TTTAAA","ViewPager  onInterceptToucheEVent 拦截")
            super.onInterceptTouchEvent(ev)
            return true
        }
    }
}