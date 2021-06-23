package com.xxkt.myviewapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.xxkt.common.GlideApp
import com.xxkt.common.IPhotoViewInterface
import kotlin.collections.ArrayList

class PhotoAdapter(var context: Context,var listUrls:ArrayList<String>,var iPhotoViewInterface:IPhotoViewInterface): RecyclerView.Adapter<PhotoAdapter.ViewHolder>() {
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var picView = itemView.findViewById<ImageView>(R.id.iv_pic)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.item_view,null)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        GlideApp.with(context).load(listUrls[position]).into(holder.picView)
        holder.picView.setOnClickListener {
            val location: IntArray = IntArray(2)
            holder.picView.getLocationOnScreen(location)
            iPhotoViewInterface.openGalleryPage(context,holder.picView,resId = position,urls = listUrls)
        }
    }
    override fun getItemCount(): Int {
        return listUrls?.size
    }
}