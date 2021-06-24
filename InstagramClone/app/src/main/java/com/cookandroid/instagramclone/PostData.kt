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
    var displayId: String = "",
    var isFollowedByMe: Boolean =false,
    var profileImageUrl: String? = null
)

data class GetPostData(
    var id:Int,
    var author: Author,
    var createdAt: String,
    var modifiedAt: String,
    var isLike: Boolean,
    var images: ArrayList<String>,
    var body: String,
    var likeCount: Int,
    var commentCount: Int
)

data class PostDTO(
    var id: Long=0,
    var author: Author = Author(),
    var images: ArrayList<String> = ArrayList(),
    var body: String? = null,
    var likeLength: Int = 0,
    var likeUser: Author? = null,
    var viewCount: Int = 0,
    var commentLength: Int = 0,
    var createdAt: String = "",
    var modifiedAt: String = "",
    var isLike: Boolean = false
){
    constructor(data: UserPostData) : this() {
        id = data.postId
        data.mediaUrls.forEach{images.add(it.url)}
        body = data.content
        likeLength = data.likeCount
        commentLength = data.commentCount
        createdAt = data.createdAt
        modifiedAt = data.modifiedAt
        isLike = data.isLike
    }
}

