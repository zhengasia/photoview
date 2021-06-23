package com.xxkt.photoview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xxkt.photoview.adapter.GalleryAdapter
import kotlinx.android.synthetic.main.activity_gallery_list.*

/**
 * @author ASIA
 * 仿微信查看大图
 */
class GalleryListActivity : AppCompatActivity() {

    public var index = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery_list)
        var urls: ArrayList<String> = intent.getStringArrayListExtra("urls")
        var adapter = GalleryAdapter(supportFragmentManager, urls);
        index = intent.getIntExtra("index", 0);
        viewPager.adapter = adapter;
        viewPager.currentItem = index
    }
}