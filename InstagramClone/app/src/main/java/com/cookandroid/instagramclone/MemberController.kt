package com.cookandroid.instagramclone

import retrofit2.Call
import retrofit2.http.*

interface MemberController{
    @Headers("accpet: application/json",
        "content-type: application/json")
    @PUT("/member/follow/{displayId}")
    fun follow(
        @Path("displayId") displayId: String
    ): Call<String>

    @Headers("accpet: application/json",
        "content-type: application/json")
    @DELETE("/member/follow/{displayId}")
    fun unfollow(
        @Path("displayId") displayId: String
    ): Call<String>
}
