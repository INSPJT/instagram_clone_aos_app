package com.cookandroid.instagramclone

import android.os.AsyncTask
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.*

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
class GetUserPostsTask(var lastId: Int = 0x7fffffff, var user: String="", var onResponse: OnResponse<UserPostData>): AsyncTask<Void, Void, Unit>() {
    companion object{
        val TAG = "GetUserPostTask"
    }
    var isEnd = false

    override fun doInBackground(vararg p0: Void?) {
        var retrofit = InternetCommunication.getRetrofitGson()
        var retrofitService = retrofit.create(ProfileController::class.java)
        var service = if(user == "") retrofitService.getUserPosts(lastId) else retrofitService.getUserPosts(user, lastId)
        Log.d(TAG, "start")

        service!!.enqueue(object: Callback<ArrayList<UserPostData>> {
            override fun onFailure(call: Call<ArrayList<UserPostData>>, t: Throwable) {
                lastId = -1
                isEnd = true
                Log.d("get posts task", "failed ${t.message}")
            }

            override fun onResponse(
                call: Call<ArrayList<UserPostData>>,
                response: Response<ArrayList<UserPostData>>
            ) {
                lastId = -1
                val message = when (response.code()) {
                    200 -> {
                        val data = response.body()
                        data?.forEach { post ->
                            try {
                                onResponse.onSuccess(post)
                                lastId = post.postId
                            } catch(e:Exception) {
                                Log.d(TAG, "set post data view failed")
                            }
                        }
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
        if(lastId != -1) {
            GetUserPostsTask(lastId, user, onResponse).execute()
        }
    }
}
