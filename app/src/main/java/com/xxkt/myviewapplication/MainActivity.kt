package com.xxkt.myviewapplication

import android.os.*
import android.util.Log
import android.widget.LinearLayout.VERTICAL
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xxkt.common.GlideApp
import com.xxkt.common.IPhotoViewInterface
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private val url: String ="http://5b0988e595225.cdn.sohucs.com/q_70,c_zoom,w_640/images/20190311/4162480529c74a05bbcda401f14a94d3.jpeg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var iPhotoViewInterface:IPhotoViewInterface = ServiceLoader.load(IPhotoViewInterface::class.java).iterator().next()
        ivPic.setOnClickListener {
            iPhotoViewInterface.openGalleryPage(this@MainActivity,ivPic,R.drawable.test)
        }

        GlideApp.with(this@MainActivity)
            .load(url)
            .into(ivPic1)

        ivPic1.setOnClickListener {
            iPhotoViewInterface.openGalleryPage(this@MainActivity,ivPic1,url = url)
        }

        var list: ArrayList<String> = arrayListOf<String>(
            "https://p0.ssl.qhimgs1.com/sdr/400__/t01a550dac96cf197da.jpg"
            ,"https://p3.ssl.qhimgs1.com/sdr/400__/t01b7a264866758cae3.jpg"
            ,"https://p0.ssl.qhimgs1.com/sdr/400__/t01c5cfd984973268aa.webp"
            ,"http://d00.paixin.com/thumbs/1006318/31742539/staff_1024.jpg?watermark/1/image/aHR0cDovL2QwMC5wYWl4aW4uY29tL2RwVG8zNjBfd20ucG5n/dissolve/100/gravity/SouthWest/dx/0/dy/0"
            ,"http://5b0988e595225.cdn.sohucs.com/images/20190509/cc1c7badb3a8479ba4f0c8905c5cc394.jpeg"
            ,"https://p0.ssl.qhimgs1.com/sdr/400__/t01aca20086ea32d9a4.webp"
            ,"https://p5.ssl.qhimgs1.com/sdr/400__/t01b2796c46e9cfa89f.webp"
            ,"https://p0.ssl.qhimgs1.com/sdr/400__/t01024cbfc32c0281e6.webp"
            ,"https://p0.ssl.qhimgs1.com/sdr/400__/t01c204020e2657934e.webp"
            ,"https://p0.ssl.qhimgs1.com/sdr/400__/t015f0b380b500951e4.webp"
            ,"https://p2.ssl.qhimgs1.com/sdr/400__/t01746b90fd42831942.webp"
        );

        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        recyclerView.addItemDecoration(DividerItemDecoration(this@MainActivity,VERTICAL))
        recyclerView.adapter = PhotoAdapter(this@MainActivity,list,iPhotoViewInterface)

    }

}