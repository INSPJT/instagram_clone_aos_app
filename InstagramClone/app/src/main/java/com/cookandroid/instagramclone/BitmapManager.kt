package com.cookandroid.instagramclone

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition

object BitmapManager{
    var bitmapHash = HashMap<String, Bitmap>()

    fun getBitmap(url: String, con: Context, func: (Bitmap)->Unit) {
        bitmapHash[url]?.let{func(it)}
            ?: URLProcess(url, con, func)
    }
}

class URLProcess(val url: String, val con: Context, val func: (Bitmap)->Unit) : AsyncTask<Void,Void,Unit>() {
    val TAG = "url process"
    override fun doInBackground(vararg p0: Void?) {
        var isEnd = false
        try {
            Glide.with(con).asBitmap().load(url).into(object: SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    BitmapManager.bitmapHash[url] = resource
                    isEnd = true
                }
                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    isEnd = true
                }
            })
        } catch(e:Exception) {
            isEnd = true
            Log.e(TAG, "glide faild")
        }
    }
}

