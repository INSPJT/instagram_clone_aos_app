package com.cookandroid.instagramclone

import android.os.AsyncTask
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface FeedContorller {
    @Headers("accpet: application/json")
    @GET("/feeds")
    fun feeds(
        @Query("lastId") lastPostId: Long? = null
    ): Call<FeedDto>
}

data class FeedInfo(
    val lastPostId: Long,
    val posts: ArrayList<PostDTO>,
    val last: Boolean = false
)

class GetFeedTask(var lastPostId:Long? = null, val onResponse: OnResponse<FeedInfo>): AsyncTask<Void, Void, Unit>() {
    var isEnd = false
    var isLast = true
    override fun doInBackground(vararg p0: Void?) {
        try {
            InternetCommunication.getRetrofitGson().create(FeedContorller::class.java)
                .feeds(lastPostId= lastPostId).enqueue(object : Callback<FeedDto> {
                    override fun onFailure(call: Call<FeedDto>, t: Throwable) {
                        Log.e("SD", "get feed failed ${t.message}")
                        onResponse.onFail()
                        isEnd = true
                    }

                    override fun onResponse(call: Call<FeedDto>, response: Response<FeedDto>) {
                        response.body()?.let {
                            it.posts.let { post->
                                onResponse.onSuccess(FeedInfo(lastPostId?.let { lastPostId }
                                    ?: 0, post, !it.hasNext))
                                isLast = !it.hasNext
                                lastPostId = if(post.isNotEmpty())post.last().id else 1}
                                isEnd = true
                        }
                    }
                })
        } catch(e: Exception) {
            Log.e("SD", "retrofit failed")
            onResponse.onFail()
            isEnd = true
        }
        while(!isEnd) Thread.sleep(1000)
    }

    override fun onPostExecute(result: Unit?) {
        super.onPostExecute(result)
        if(!isLast) {
            GetFeedTask(lastPostId, onResponse).executeOnExecutor(THREAD_POOL_EXECUTOR)
        }
        Log.e("SD", "isLast: $isLast")
    }
}
