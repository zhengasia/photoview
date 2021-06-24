package com.zasia.photoview.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.zasia.photoview.PhotoFragment

class GalleryAdapter:FragmentPagerAdapter {

    public var listFragments = ArrayList<Fragment>()

    constructor(supportFragmentManager: FragmentManager,  picBeans: ArrayList<String>):super(supportFragmentManager,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){
        listFragments.clear()
        for((index,url) in picBeans.withIndex()){
            listFragments.add(PhotoFragment.newInstance(url,index))
        }
    }
    override fun getCount(): Int {
        return listFragments?.size
    }

    override fun getItem(position: Int): Fragment {
        return listFragments[position]
    }
}