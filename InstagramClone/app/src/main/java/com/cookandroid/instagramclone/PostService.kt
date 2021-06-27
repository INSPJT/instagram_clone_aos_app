package com.cookandroid.instagramclone

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.LayoutInflaterCompat
import androidx.core.view.MarginLayoutParamsCompat
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.*
import java.io.File

//for retrofit
interface PostService{
    @Headers("accpet: text/plain",
        "content-type: application/json")
    @POST("/posts")
    fun post(
        @Body body: PostData
    ) : Call<String>

    @GET("/posts/{post_id}")
    fun getPost(
        @Path("post_id") id: String
    ): Call<GetPostData>
}

//get post by id
class GetUserPostsTask(var lastId: Long = 0x7fffffffffffffffL, var user: String="", var onResponse: OnResponse<PostDTO>, var onAllSucess: ()->Unit = {}): AsyncTask<Void, Void, Unit>() {
    companion object{
        val TAG = "GetUserPostTask"
    }
    var isEnd = false
    var isContinue = false

    override fun doInBackground(vararg p0: Void?) {
        var retrofit = InternetCommunication.getRetrofitGson()
        var retrofitService = retrofit.create(ProfileController::class.java)
        var service = if(user == "") retrofitService.getUserPosts(lastId) else retrofitService.getUserPosts(user, lastId)
        Log.d(TAG, "start")

        service.enqueue(object: Callback<FeedDto> {
            override fun onFailure(call: Call<FeedDto>, t: Throwable) {
                lastId = -1
                isEnd = true
                Log.d("get posts task", "failed ${t.message}")
            }

            override fun onResponse(
                call: Call<FeedDto>,
                response: Response<FeedDto>
            ) {
                lastId = -1
                val message = when (response.code()) {
                    200 -> {
                        val data = response.body()
                        isContinue = data?.let{data->
                            data.posts.forEach { post ->
                                try {
                                    onResponse.onSuccess(post)
                                    lastId = post.id
                                } catch(e:Exception) {
                                    Log.d(TAG, "set post data view failed")
                                }
                            }

                            !data.hasNext
                        } ?: {
                            Log.e("SD", "$TAG data is null")
                            false
                        }()
                        isEnd = true
                        "Success"
                    }
                    401 -> {
                        isEnd = true
                        "Unauthorized"
                    }
                    404 -> {
                        isEnd = true
                        "Not Found"
                    }
                    else -> {
                        isEnd = true
                        "Unknown Error"
                    }
                }
                Log.d(TAG, message)
            }
        })
        while(!isEnd) Thread.sleep(100)
    }

    override fun onPostExecute(result: Unit?) {
        super.onPostExecute(result)
        onAllSucess()
        if(isContinue) {
            GetUserPostsTask(lastId, user, onResponse, onAllSucess).execute()
        }
    }
}


interface ViewHolderMake{
    val bitmapGetter: BitmapManagerInterface
    val matcher: ImageSizeMatcher
    fun makeViewHolder(parent:ViewGroup): RecyclerView.ViewHolder
    fun setViewHolder(viewHolder: RecyclerView.ViewHolder, postData:PostDTO)
}

class ImageViewHolder(val view: View, val parent: ViewGroup) : RecyclerView.ViewHolder(view){
    var imgView = view.findViewById<ImageView>(R.id.viewHolder_img)
    var multiImg = view.findViewById<TextView>(R.id.viewHolder_check)

    init{
        imgView.setImageResource(R.drawable.ic_wait)
        multiImg.layoutParams = RelativeLayout.LayoutParams(view.layoutParams.width/5,view.layoutParams.width/5)
        var layout = multiImg.layoutParams as RelativeLayout.LayoutParams
        layout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        layout.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        multiImg.visibility= View.INVISIBLE
    }
}

class PostViewHolder(val view: View, private val bitmapGetter: BitmapManagerInterface) : RecyclerView.ViewHolder(view), BitmapManagerInterface by bitmapGetter {
    var userProfile:ImageView = view.findViewById(R.id.user_post_profile)
    var userId: TextView = view.findViewById(R.id.post_id_tv)
    var viewPager: ViewPager2 = view.findViewById(R.id.pager)
    var likeAcounts: LinearLayout = view.findViewById(R.id.like_accounts)
    var idTextView: TextView = view.findViewById(R.id.id_text_view)
    var con: TextView = view.findViewById(R.id.user_post_content)
    var postTime: TextView = view.findViewById(R.id.post_time)
    var urlToIter = HashMap<String, Int>()
    var postInfo = PostDTO()
    lateinit var bitmaps:  Array<Bitmap>

    fun setInformation(data: PostDTO) {
        postInfo = data
        bitmaps = Array(data.mediaUrls.size){ BitmapFactory.decodeResource(view.resources, R.drawable.ic_wait )}
        viewPager.adapter = ScreenSlidePagerAdapter2(bitmaps)

        var urls = data.getUrls()
        for(idx in urls.indices) {
            urlToIter[urls[idx]] = idx
        }

        bitmapGetter.getBitmapFromUrl(urls, view, object: OnResponse<Pair<String,Bitmap>> {
            override fun onSuccess(item: Pair<String, Bitmap>) {
                try {
                    var pos = urlToIter[item.first]!!
                    bitmaps[pos] = item.second
                } catch(e: Exception) {
                    Log.e("SD", "post view holder get bitmap from url failed ${e.message}")
                }
                viewPager.adapter?.notifyDataSetChanged()
            }

            override fun onFail() {
                Log.e("SD", "get bit form url has faield")
            }
        })
    }

    inner class ScreenSlidePagerAdapter2(var images:Array<Bitmap>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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

        fun updateViewHolder(pos: Int, bitmap: Bitmap){
            images[pos] = bitmap
            notifyDataSetChanged()
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var holder = holder as UserPostViewHolder
            holder.imageView.setImageBitmap(images[position])
        }
    }
}

//for postView Size match
interface ImageSizeMatcher {
    val convertImageSize: ()->Pair<Int, Int>
}
class PostRecyclerView(private var posts: ArrayList<PostDTO>, private var viewHolderMaker: ViewHolderMake)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    ViewHolderMake by viewHolderMaker {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return viewHolderMaker.makeViewHolder(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        setViewHolder(holder, posts[position])
    }

    override fun getItemCount(): Int {
        return posts.size
    }
}

