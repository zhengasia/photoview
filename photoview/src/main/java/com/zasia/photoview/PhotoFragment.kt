package com.zasia.photoview

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.xxkt.common.GlideApp
import com.zasia.photoview.view.PhotoView
import kotlinx.android.synthetic.main.fragment_photo.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PhotoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PhotoFragment : Fragment() {
    //是否加载过
    private var bLoad: Boolean = false

    // TODO: Rename and change types of parameters
    private lateinit var param1: String
    private var param2: Int = 0
    private var photoView: PhotoView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)!!
            param2 = it.getInt(ARG_PARAM2)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        GlideApp.with(requireActivity())
            .asBitmap()
            .load(param1)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    bLoad = true;
                    photoView = PhotoView(requireActivity())
                    photoView?.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    if (param2 == (requireActivity() as GalleryListActivity).index) {
                        photoView?.initData(
                            requireActivity().intent.getIntExtra("offsetX", 0),
                            requireActivity().intent.getIntExtra("offsetY", 0),
                            requireActivity().intent.getIntExtra("width", 0),
                            requireActivity().intent.getIntExtra("height", 0), resource
                        )
                    } else {
                        photoView?.initData(
                            resource
                        )
                    }
                    linearLayout.addView(photoView)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }

    override fun onPause() {
        super.onPause()
        //处于隐藏状态后，放大的图片复原
        if(requireActivity()!=null&&!requireActivity().isFinishing&&!requireActivity().isDestroyed){
            photoView?.reset()

        }
    }

    fun recycler() {
        if(requireActivity()!=null&&!requireActivity().isFinishing&&!requireActivity().isDestroyed){
            photoView?.recycler()
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: Int) =
            PhotoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putInt(ARG_PARAM2, param2)
                }
            }
    }
}