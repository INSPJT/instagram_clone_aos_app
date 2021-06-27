package com.cookandroid.instagramclone

import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_user_profile.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class FullSIzeMatcher(private val resources: Resources) : ImageSizeMatcher{
    override val convertImageSize = {Pair(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels)}
}

class NDividedSIzeMatcher(private val resources: Resources, private val n: Int) : ImageSizeMatcher {
    override val convertImageSize = {
        val width = resources.displayMetrics.widthPixels/n
        Pair(width,width)
    }
}

class PostViewHolderMaker(override val bitmapGetter: BitmapManagerInterface, override val matcher: ImageSizeMatcher) : ViewHolderMake {
    override fun setViewHolder(viewHolder: RecyclerView.ViewHolder, postData: PostDTO) {
        (viewHolder as PostViewHolder).setInformation(postData)
    }

    override fun makeViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.image_view_pager_fragment, parent, false)
        val (width, height) = matcher.convertImageSize()
        view.layoutParams = LinearLayoutCompat.LayoutParams(width, height)
        return PostViewHolder(view, bitmapGetter)
    }
}

class ImageViewHolderMaker(override val bitmapGetter: BitmapManagerInterface, override val matcher: ImageSizeMatcher) : ViewHolderMake, BitmapManagerInterface by bitmapGetter {
    override fun makeViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.add_viewholder,parent, false)//ImageView(parent.context)
        val (width, height) = matcher.convertImageSize()
        view.layoutParams = RelativeLayout.LayoutParams(width,height)

        return ImageViewHolder(view, parent)
    }

    override fun setViewHolder(viewHolder: RecyclerView.ViewHolder, postData: PostDTO) {
        try {
            if(postData.mediaUrls.isNotEmpty()) {
                var urls = postData.getUrls()
                bitmapGetter.getBitmapFromUrl(
                    urls[0],
                    (viewHolder as ImageViewHolder).parent,
                    object : OnResponse<Pair<String, Bitmap>> {
                        override fun onFail() {
                            Log.e("SD", "get bitmap from url failed (image view holder maker)")
                        }

                        override fun onSuccess(item: Pair<String, Bitmap>) {
                            viewHolder.imgView.setImageBitmap(item.second)
                            viewHolder.multiImg.visibility = if(urls.size > 1) View.VISIBLE else View.INVISIBLE
                        }
                    })
            }
        } catch (e: Exception){
            Log.e("SD", "image view holder maker  get bitmap from url failed ${e.message}")
        }
    }
}

class GetUserPostActivity : Fragment() {
    val TAG = "get user Post"
    var bitmapManager = BitmapManager()
    var postInfo = ArrayList<PostDTO>()

    private val sharedManager: SharedManager by lazy { SharedManager(requireContext()) }

    lateinit var content: View
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        content = inflater.inflate(R.layout.fragment_user_profile, null)

        var recyclerView = content.findViewById(R.id.profile_recycler_view) as RecyclerView
        recyclerView.adapter = PostRecyclerView(
            postInfo,
            ImageViewHolderMaker(bitmapManager, NDividedSIzeMatcher(resources, 3))
        )
        recyclerView.layoutManager = GridLayoutManager(activity, 3)

        var name = arguments?.getParcelable("user") ?: ProfileResponse()
        name?.let { user ->

            GetUserPostsTask(
                user = user.displayId ?: "",
                onResponse = object : OnResponse<PostDTO> {
                    override fun onSuccess(item: PostDTO) {
                        postInfo.add(item)
                        recyclerView.adapter?.notifyDataSetChanged()
                    }

                    override fun onFail() {
                        Log.e(TAG, "get post data task by name ,retrofit field")
                    }

                }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

            var memberService = InternetCommunication.getRetrofitString()
                .create(MemberController::class.java)

            var followBtn = content.findViewById<Button>(R.id.follow_btn)
            var unfollowBtn = content.findViewById<Button>(R.id.unfollow_btn)
            val logoutBtn = content.findViewById<Button>(R.id.logout_btn)

            followBtn.setOnClickListener{
                memberService.follow(user.displayId ?: "").enqueue(object:
                    Callback<String> {
                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Log.e("SD", "follow failed ${t.message}")
                    }

                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        val message = when(response.code()){
                            200 -> {
                                followBtn.text = "팔로우 완료"
                                unfollowBtn.text = "언팔로우 미완"
                                "${user.displayId} follow Success"
                            }
                            else-> "fail( ${response.body()} ) failed"
                        }
                        Log.e("SD", message)
                    }
                })
            }

            logoutBtn.setOnClickListener { // 여기 부분은 다시 고민좀 해야 할거같지만 이정도면 괜찮을듯 -> layout 을 수정해야함
                Log.d("login 화면으로" , "login 화면으로 이동")
                val lastUser = User("","","",0,"","","","")
                sharedManager.saveCurrentUser(lastUser)
                val intent = Intent(getActivity(),LoginActivity::class.java)
                startActivity(intent)
            }

            unfollowBtn.setOnClickListener{
                memberService.unfollow(user.displayId ?: "").enqueue(object:
                    Callback<String> {
                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Log.e("SD", "unfollow failed ${t.message}")
                    }

                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        val message = when(response.code()){
                            200 -> {
                                followBtn.text = "팔로우 미완"
                                unfollowBtn.text = "언팔로우 완료"
                                "${user.displayId} unfollow Success"
                            }
                            else-> "fail( ${response.body()} ) failed"
                        }
                        Log.e("SD", message)
                    }
                })
            }
        }

        return content
    }
}
