package com.zasia.photoview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zasia.photoview.adapter.GalleryAdapter
import kotlinx.android.synthetic.main.activity_gallery_list.*

/**
 * @author ASIA
 * 仿微信查看大图
 */
class GalleryListActivity : AppCompatActivity() {

    private lateinit var adapter: GalleryAdapter
    public var index = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery_list)
        var urls: ArrayList<String> = intent.getStringArrayListExtra("urls")
         adapter = GalleryAdapter(supportFragmentManager, urls);
        index = intent.getIntExtra("index", 0);
        viewPager.adapter = adapter;
        viewPager.currentItem = index
        viewPager.offscreenPageLimit = urls.size
    }

    override fun onBackPressed() {
        (adapter.getItem(viewPager.currentItem) as PhotoFragment).recycler()

    }
}