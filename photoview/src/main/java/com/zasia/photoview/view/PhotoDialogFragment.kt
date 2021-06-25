package com.zasia.photoview.view

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import androidx.fragment.app.DialogFragment
import com.zasia.photoview.R
import com.zasia.photoview.inter.PhotoListener
import kotlinx.android.synthetic.main.fragment_photo_dialog.*

class PhotoDialogFragment : DialogFragment() {
    private  var listener: PhotoListener?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.upgrade_dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return View.inflate(requireActivity(), R.layout.fragment_photo_dialog, container)
    }

    override fun onStart() {
        super.onStart()
        dialog?.let {
            val dm = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(dm)
            it.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            it.window?.setWindowAnimations(R.style.up_dialog)
            it.window?.setGravity(Gravity.BOTTOM)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvSavePic.setOnClickListener {
            listener?.savePic()
            dismissAllowingStateLoss()
        }
        tvSharePic.setOnClickListener {
            listener?.sharePic()
            dismissAllowingStateLoss()

        }
        tvCancelPic.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }

    fun addOnListener(photoListener: PhotoListener): PhotoDialogFragment {
        this.listener = photoListener
        return this
    }

}