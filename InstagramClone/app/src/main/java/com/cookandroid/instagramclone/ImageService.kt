package com.cookandroid.instagramclone

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ImageService {
    @Multipart
    @POST("/images")
    fun sendImage(
        @Part file: MultipartBody.Part
        ): Call<String>
}