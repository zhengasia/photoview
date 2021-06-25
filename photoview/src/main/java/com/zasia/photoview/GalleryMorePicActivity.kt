package com.zasia.photoview

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.zasia.photoview.adapter.GalleryAdapter
import kotlinx.android.synthetic.main.activity_gallery_list.*

/**
 * @author ASIA
 * 仿微信查看大图,多图
 */
class GalleryMorePicActivity : AppCompatActivity() {

    private lateinit var adapter: GalleryAdapter
    var index = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery_list)
        var urls: ArrayList<String> = intent.getStringArrayListExtra("urls")
        adapter = GalleryAdapter(supportFragmentManager, urls)
        index = intent.getIntExtra("index", 0)
        viewPager.adapter = adapter
        viewPager.currentItem = index
        viewPager.offscreenPageLimit = urls.size
        viewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }
            override fun onPageSelected(position: Int) {
                setIndictorView()
            }
            override fun onPageScrollStateChanged(state: Int) {
            }

        })
        if (urls.size > 1) {
            linearIndicator.visibility = View.VISIBLE
            setIndictorView()
        } else {
            linearIndicator.visibility = View.GONE
        }

    }

    public fun setIndictorView() {
        linearIndicator.removeAllViews()
        for (position in 0 until adapter.count) {
            var view = View(this@GalleryMorePicActivity)
            view.layoutParams = ViewGroup.LayoutParams(15, 15)
            if (viewPager.currentItem == position) {
                view.setBackgroundResource(R.drawable.point_white_icon)
            } else {
                view.setBackgroundResource(R.drawable.point_gray_icon)
            }
            linearIndicator.addView(view)

        }
    }

    override fun onBackPressed() {
        (adapter.getItem(viewPager.currentItem) as PhotoFragment).recycler()

    }

    fun hideIndictor() {
        linearIndicator.removeAllViews()
        linearIndicator.visibility = View.GONE
    }
}