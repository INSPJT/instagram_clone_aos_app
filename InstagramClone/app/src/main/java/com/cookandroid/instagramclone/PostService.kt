package com.cookandroid.instagramclone

import retrofit2.Call
import retrofit2.http.*

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

