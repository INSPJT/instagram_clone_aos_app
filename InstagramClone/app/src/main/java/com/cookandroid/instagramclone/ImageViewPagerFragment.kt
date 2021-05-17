package com.cookandroid.instagramclone

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

/**
 * The number of pages (wizard steps) to show in this demo.
 */

class ScreenSlidePagerActivity() : FragmentActivity() {
    val TAG = "getPostData"

    enum class TYPE(var type: Int){
        DRAWABLE(0),
        URL(1)
    }

    var postId = "230"
    constructor(postId: Int): this(){
        this.postId = postId.toString()
    }
    companion object {
        private const val MAX_PAGES = 10
    }

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private lateinit var mPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_view_pager_fragment)
        TokenManager.addTokenHeader(InternetService.TEMP_TOKEN)

        var retrofitService = InternetCommunication.getRetrofitGson().create(PostService::class.java)

        var service = retrofitService?.getPost(postId)

        service?.enqueue(object: Callback<GetPostData>{
            override fun onFailure(call: Call<GetPostData>, t: Throwable) {
                Log.d(TAG,t.message)
            }

            override fun onResponse(call: Call<GetPostData>, response: Response<GetPostData>) {
                var urls = ArrayList<String>()
                try {
                    var message: String = when (response.code()) {
                        200 -> {
                            urls = response.body()!!.images
                            "Success"
                        }
                        401 -> {
                            "Unauthorized"
                        }
                        403 -> {
                            "Forbidden"
                        }
                        404 -> {
                            "Not Found"
                        }
                        else -> {
                            "Unknown"
                        }
                    }
                    Toast.makeText(this@ScreenSlidePagerActivity, "$message ${response.body().toString()}", Toast.LENGTH_LONG)
                } catch(e:Exception){
                    Log.d(TAG, e.message)
                }
                for(url in urls){
                    Log.d(TAG, url)
                }

                mPager = findViewById(R.id.pager)
                var fragments = Array<Fragment>(urls.size){i->ImageViewPagerFragment(urls[i],this@ScreenSlidePagerActivity)}
                val pagerAdapter = ScreenSlidePagerAdapter(fragments, supportFragmentManager)
                mPager.adapter = pagerAdapter
            }
        })
    }

    override fun onBackPressed() {
        if (mPager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous step.
            mPager.currentItem = mPager.currentItem - 1
        }
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private inner class ScreenSlidePagerAdapter(var imageFragment: Array<Fragment>, fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getCount(): Int = imageFragment.size

        override fun getItem(position: Int): Fragment = imageFragment[position]
    }


    class ImageViewPagerFragment(): Fragment() {
        var TAG = "imageViewFragment"
        var v:View? = null
        var drawable = -1
        lateinit var url: String
        var type =  TYPE.DRAWABLE
        lateinit var con: Context

        constructor(drawable: Int): this(){
            type = TYPE.DRAWABLE
            this.drawable = drawable
        }
        constructor(url: String,c: Context): this() {
            type = TYPE.URL
            this.url = url
            this.con = c
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            if(v == null) {
                v = inflater.inflate(R.layout.view_pager, container, false)
                var imageView: ImageView = v!!.findViewById(R.id.fragmentImage)

                when(type){
                    TYPE.DRAWABLE->imageView.setImageResource(drawable)
                    TYPE.URL->{
                        if(!this::url.isInitialized) throw IOException("url was not initialized")
                        Glide.with(con).load(url).into(imageView)
                        Log.d(TAG, "glide bitmap image generator was called")
                    }
                }
            }
            return v
        }
    }
}

