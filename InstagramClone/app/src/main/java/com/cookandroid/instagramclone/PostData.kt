package com.cookandroid.instagramclone

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class PostData(
    var content: String,
    var mediaUrls: ArrayList<String>
)
