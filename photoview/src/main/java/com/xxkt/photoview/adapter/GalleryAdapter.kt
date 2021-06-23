package com.xxkt.photoview.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.xxkt.photoview.PhotoFragment

class GalleryAdapter(supportFragmentManager: FragmentManager, var picBeans: ArrayList<String>) :
    FragmentPagerAdapter(
        supportFragmentManager,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {
    override fun getCount(): Int {
        return picBeans?.size
    }

    override fun getItem(position: Int): Fragment {
        return PhotoFragment.newInstance(picBeans[position],position);
    }


}