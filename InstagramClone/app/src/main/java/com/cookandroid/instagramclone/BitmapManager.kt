package com.cookandroid.instagramclone

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition

interface OnResponse<T>{
    fun onSuccess(item: T)
    fun onFail()
}

object BitmapManager{
    val TAG = "bitmanager"
    var bitmapHash = HashMap<String, Bitmap>()

    fun getBitmapFromUrl(url: String, con: View, call: OnResponse<Bitmap>) {
        bitmapHash[url]?.let{
            call.onSuccess(it)
        }
            ?: BitmapProcess(url, con, call).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun getBitmapFromUrl(urls: ArrayList<String>, con: View, call: OnResponse<Bitmap>) {
        BitmapsProcess(con, urls, call).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }
}

class BitmapProcess(val url: String, var con: View, val response: OnResponse<Bitmap>) : AsyncTask<Void,Void,Unit>() {
    val TAG = "single bitmap task"
    override fun doInBackground(vararg p0: Void?) {
        try {
            Glide.with(con).asBitmap().load(url).into(object: SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    BitmapManager.bitmapHash[url] = resource
                    response.onSuccess(resource)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    response.onFail()
                }
            })
        } catch(e:Exception) {
            Log.e(TAG, "glide faild")
            response.onFail()
        }
    }
}

class BitmapsProcess(private var con: View, var urls: ArrayList<String>, var func: OnResponse<Bitmap>?) : AsyncTask<Void, Void, Unit>(){
    val TAG = "multi bitmaps task"
    var resources = ArrayList<Bitmap>()
    var cnt = 0
    var failed = false
    override fun doInBackground(vararg p0: Void?) {
        urls.forEach {
            try {
                BitmapManager.getBitmapFromUrl(it,con, object: OnResponse<Bitmap>{
                    override fun onSuccess(bitmap: Bitmap) {
                        cnt++
                        resources.add(bitmap)
                        Log.e(TAG, "process success")
                    }

                    override fun onFail() {
                        cnt++
                        failed = true
                    }
                })
            } catch(e:Exception){
                cnt++
                failed = true
                Log.d("url async", "failed - ${e.message}")
            }
        }
        while(cnt != urls.size) {
            Thread.sleep(100)
            Log.d("func", "sleep, ${urls.size} vs ${resources.size} vs $cnt")
        }
    }

    override fun onPostExecute(result: Unit?) {
        super.onPostExecute(result)
        try {
            if(failed) func?.onFail()
            else {
                func?.let{ resources.forEach{bitmap->it.onSuccess(bitmap)} }
            }
        } catch(e:Exception){
            func?.onFail()
            Log.d("url async", "failed - ${e.message}")
        }
    }
}
