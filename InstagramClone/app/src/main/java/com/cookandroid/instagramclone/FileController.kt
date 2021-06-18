package com.cookandroid.instagramclone

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

object FormDataUtil {
    fun getImageBody(key: String, file: File): MultipartBody.Part {
        return MultipartBody.Part.createFormData(
            key,
            file.name,
            file.asRequestBody("multipart/form-data".toMediaType())
        )
    }
}