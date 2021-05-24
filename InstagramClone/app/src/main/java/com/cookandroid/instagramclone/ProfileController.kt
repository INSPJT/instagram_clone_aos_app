package com.cookandroid.instagramclone

import retrofit2.Call
import retrofit2.http.*

interface ProfileController{
    @Headers("accpet: text/plain",
        "content-type: application/json")
    @GET("/member/posts")
    fun getUserPosts(
        @Query("lastPostId") lastPostId: Int
    ): Call<ArrayList<UserPostData>>


    @Headers("accpet: text/plain",
        "content-type: application/json")
    @GET("/member/{displayId}/posts")
    fun getUserPosts(
        @Query("displayId") displayId: String,
        @Query("lastPostId") lastPostId: Int
    ): Call<ArrayList<UserPostData>>
}

