package com.cookandroid.instagramclone

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class PostData(
    var content: String,
    var mediaUrls: ArrayList<String>
)

data class CommentView(
    var author: Author,
    var content: String,
    var created:String,
    var id: Long,
    var isLike: Boolean,
    var likeLength: Long
)

data class Author(
    var displayId: String,
    var isFollowedByMe: Boolean,
    var profileImageUrl: String?
)

data class GetPostData(
    var id:Int,
    var author: Author,
    var createdAt: String,
    var modifiedAt: String,
    var isLike: Boolean,
    var commentPreview: ArrayList<CommentView>?,
    var viewCount: Int,
    var images: ArrayList<String>,
    var body: String,
    var likeUser: ArrayList<Author>?,
    var likeLength: Int,
    var commentLength: Int
)