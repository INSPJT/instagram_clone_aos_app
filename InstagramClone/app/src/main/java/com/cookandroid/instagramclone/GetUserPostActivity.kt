package com.cookandroid.instagramclone

import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.Slide
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.View.inflate
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.android.synthetic.main.activity_get_user_post.*
import kotlinx.android.synthetic.main.image_view_pager_fragment.*
import kotlinx.android.synthetic.main.view_pager.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException

class GetUserPostActivity : Fragment() {
    val TAG = "get user Post"
    var userPostRecyclerViewAdapter = UserFragmentRecyclerViewAdapter()
    lateinit var content: View
    inner class PostView(var view: View, var vp: ViewPager2): View(activity) {
        lateinit var resources: ArrayList<Bitmap>

        constructor(resources: ArrayList<Bitmap>, view : View, vp: ViewPager2): this(view,vp){
            this.resources = resources
        }
    }

    fun setPostDataView(post: UserPostData){
        val urls = ArrayList<String>()
        post.mediaUrls.forEach{urls.add(it.url)}

        var view = layoutInflater.inflate(R.layout.image_view_pager_fragment,null)
        var vp: ViewPager2 = view.findViewById(R.id.pager)
        var bitmaps = ArrayList<Bitmap>()

        if(!this::content.isInitialized) throw IOException("content is not initialized")

        BitmapManager.getBitmapFromUrl(urls, content, object: OnResponse<Bitmap>{
            override fun onSuccess(bitmap: Bitmap) {
                bitmaps.add(bitmap)

                if(bitmaps.size == urls.size) {
                    vp.adapter = ScreenSlidePagerAdapter2(bitmaps)
                    userPostRecyclerViewAdapter.addItems(PostView(bitmaps,view,vp))
                }
            }
            override fun onFail() {
                Log.e(TAG, "get bitmap failed")
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        content = inflater.inflate(R.layout.activity_get_user_post,null)

        var recyclerView = content.findViewById(R.id.user_post_recycler_view) as RecyclerView
        recyclerView.adapter = userPostRecyclerViewAdapter
        recyclerView.layoutManager = GridLayoutManager(activity, 1)

        var name = arguments?.let{it.getString("name")?.let {name->name
        }?: ""}?: ""
        GetUserPostsTask(0x7fffffff, name, object: OnResponse<UserPostData>{
            override fun onSuccess(item: UserPostData) {
                setPostDataView(item)
            }

            override fun onFail() {
                Log.e(TAG, "get post data task by name ,retrofit field")
            }

        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

        return content
    }

    inner class UserFragmentRecyclerViewAdapter :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var postData = ArrayList<View>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.image_view_pager_fragment, parent, false)
            view.layoutParams = LinearLayoutCompat.LayoutParams(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels)
            return PostViewHolder(view)
        }

        inner class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var userProfile:ImageView = view.findViewById(R.id.user_post_profile)
            var userId: TextView = view.findViewById(R.id.post_id_tv)
            var viewPager: ViewPager2 = view.findViewById(R.id.pager)
            var likeAcounts: LinearLayout = view.findViewById(R.id.like_accounts)
            var idTextView: TextView = view.findViewById(R.id.id_text_view)
            var con: TextView = view.findViewById(R.id.user_post_content)
            var postTime: TextView = view.findViewById(R.id.post_time)

            fun onBind(v: PostView) {
                try {
                    viewPager.adapter = v.vp.adapter
                }
                catch(e:Exception) {Log.d(TAG, e.message)}
            }
        }

        fun addItems(postData: View) {
            this.postData.add(postData)
            notifyDataSetChanged()
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var holder = holder as PostViewHolder
            holder.viewPager.id = position+1

            holder.onBind(postData[position] as PostView)
        }

        override fun getItemCount(): Int {
            return postData.size
        }
    }

    inner class ScreenSlidePagerAdapter2(var images:ArrayList<Bitmap>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        inner class UserPostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var imageView: ImageView = view.findViewById(R.id.fragmentImage)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.view_pager, parent, false)
            return UserPostViewHolder(view)
        }


        override fun getItemCount(): Int {
            return images.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var holder = holder as UserPostViewHolder
            holder.imageView.setImageBitmap(images[position])
        }
    }
}
