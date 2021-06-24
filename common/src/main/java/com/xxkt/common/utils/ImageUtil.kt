package com.xxkt.common.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 * 保存图片
 */
object ImageUtil {
    //更新图库
    @JvmStatic
    fun updatePictures(updateFile: File, context: Context) {
        val insertImage: String
        try {
            insertImage = MediaStore.Images.Media.insertImage(
                context.contentResolver,
                updateFile.absolutePath, updateFile.name, null
            )
            context.sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(
                        File(
                            getRealPathFromURI(Uri.parse(insertImage), context)
                        )
                    )
                )
            )
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }
    fun getImageFile(context: Context?, concat: String?): File? {
        val rootDir: File? = context!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File(rootDir, concat)
    }
    /**
     * 保存到sdcard
     * @return
     */
    fun savePic(context: Context?, bitmap: Bitmap): String? {
        val fname = System.currentTimeMillis().toString() + ".jpg"
        val fileAbsoult: String = getImageFile(context, fname)!!.absolutePath
        val file = File(fileAbsoult)
        if (file.exists()) {
            file.delete()
        }
        val out: FileOutputStream
        try {
            out = FileOutputStream(file)
            // 格式为 JPEG，照相机拍出的图片为JPEG格式的，PNG格式的不能显示在相册中
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
                out.flush()
                out.close()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        updatePictures(file, context!!)
        return fileAbsoult
    }
    //得到绝对地址
    private fun getRealPathFromURI(contentUri: Uri, context: Context): String {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(contentUri, proj, null, null, null)
        val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val fileStr = cursor.getString(column_index)
        cursor.close()
        return fileStr
    }

    /**
     * @param mFile 需要分享的文件
     * 描述：
     * 调用系统分享面板分享文件
     */
    fun shareFile(mContext: Context, mFile: File?) {
        Log.e("TTAAA","pakage: ${mContext.packageName}")

        val file = FileProvider.getUriForFile(
            mContext.applicationContext,
            mContext.packageName + ".fileprovider", mFile!!
        )
        val intent = Intent(Intent.ACTION_SEND)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra(Intent.EXTRA_STREAM, file) //传输文件 采用流的方式
        intent.type = "*/*" //分享文件
        mContext.startActivity(Intent.createChooser(intent, "分享"))
    }
}