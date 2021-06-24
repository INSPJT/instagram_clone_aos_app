package com.cookandroid.instagramclone

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import java.io.File
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import okhttp3.internal.notify


class MainNavigationActivity : Fragment() {
    val TAG = "MainNavigationActivity"
    var posts = ArrayList<PostDTO>()
    lateinit var feedRecyclerView : RecyclerView.Adapter<RecyclerView.ViewHolder>
    lateinit var recyclerView : RecyclerView
    companion object{
        var imageView:ImageView ? = null
    }
    var bitmapManager = BitmapManager()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.activity_main_navigation, container, false)
        imageView = view.findViewById(R.id.myProfile)

        recyclerView = view.findViewById(R.id.feed_recycler_view)

        feedRecyclerView = PostRecyclerView(posts, PostViewHolderMaker(bitmapManager, FullSIzeMatcher(resources)))

        recyclerView.adapter = feedRecyclerView
        recyclerView.layoutManager = GridLayoutManager(activity, 1)

        GetFeedTask(onResponse = object: OnResponse<FeedInfo> {
            override fun onSuccess(item: FeedInfo) {
                item.posts.forEach{posts.add(it)}
                feedRecyclerView.notifyDataSetChanged()
                if(item.last) {
                    posts.forEach{p-> Log.e("SD", "${p.id}") }
                }
            }

            override fun onFail() {
                Log.e("SD", "feed failed")
                posts.clear()
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        return view
    }
}

