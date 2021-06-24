package com.cookandroid.instagramclone

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition

interface OnResponse<T>{
    fun onSuccess(item: T)
    fun onFail()
}

interface BitmapManagerInterface{
    fun getBitmapFromUrl(url: String, con: View, call: OnResponse<Pair<String,Bitmap>>)
    fun getBitmapFromUrl(urls: ArrayList<String>, con: View, call: OnResponse<Pair<String,Bitmap>>)
}

class BitmapManager: BitmapManagerInterface{
    val TAG = "bitmanager"
    companion object {
        var bitmapHash = HashMap<String, Bitmap>()
    }

    override fun getBitmapFromUrl(url: String, con: View, call: OnResponse<Pair<String, Bitmap>>) {
        bitmapHash[url]?.let{
            call.onSuccess(Pair(url,it))
        }
            ?: {
                try {
                    Glide.with(con).asBitmap().load(url).apply(RequestOptions().centerCrop()).into(object: SimpleTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            bitmapHash[url] = resource
                            call.onSuccess(Pair(url,resource))
                        }

                    })
                } catch(e:Exception) {
                    Log.e(TAG, "glide failed (${e.message})")
                }
            }()
    }

    override fun getBitmapFromUrl(urls: ArrayList<String>, con: View, call: OnResponse<Pair<String,Bitmap>>) {
        BitmapsProcess(con, urls, call).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

 inner class BitmapsProcess(private var con: View, var urls: ArrayList<String>, var func: OnResponse<Pair<String,Bitmap>>?) : AsyncTask<Void, Void, Unit>(){
        val TAG = "multi bitmaps task"
        var resources = Array<Pair<String,Bitmap>>(urls.size){Pair("WaitDefault", BitmapFactory.decodeResource(con.resources, R.drawable.ic_wait))}
        var hash = HashMap<String, Int>()
        var cnt = 0
        var retry = 0
        var failed = false
        init{
            for(i in urls.indices) {
                hash[urls[i]]  = i
            }
        }

        override fun doInBackground(vararg p0: Void?) {
            urls.forEach {
                try {
                    getBitmapFromUrl(it,con, object: OnResponse<Pair<String,Bitmap>>{
                        override fun onSuccess(item: Pair<String, Bitmap>) {
                            cnt++
                            var i = hash[item.first]
                            resources[i!!] = item
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
                if(retry > 5) {
                    failed = true
                    break
                }
                retry++
            }
        }

        override fun onPostExecute(result: Unit?) {
            super.onPostExecute(result)
            try {
                if(failed) func?.onFail()
                else {
                    func?.let{ resources.forEach{data->it.onSuccess(data) }}
                }
            } catch(e:Exception) {
                func?.onFail()
                Log.d("url async", "failed - ${e.message}")
            }
        }
    }
}

