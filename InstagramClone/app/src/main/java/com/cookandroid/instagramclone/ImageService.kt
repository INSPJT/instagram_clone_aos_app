package com.cookandroid.instagramclone

import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
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

