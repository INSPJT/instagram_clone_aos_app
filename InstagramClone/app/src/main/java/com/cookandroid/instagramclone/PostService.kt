package com.cookandroid.instagramclone

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface PostService{
    @Headers("accpet: text/plain",
        "content-type: application/json")
    @POST("/posts")
    fun post(
        @Body body: PostData
    ) : Call<String>
}
