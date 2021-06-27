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
    var nickname: String = "",
    var introduction: String?= null,
    var postCount: Long = 0,
    var followingCount: Long = 0,
    var followerCount: Long = 0,
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

data class MediaUrls(
    var mediaUrlId: Long,
    var url: String,
    var type: String
)

data class PostDTO(
    var id: Long=0,
    var author: Author = Author(),
    var mediaUrls: ArrayList<MediaUrls> = ArrayList(),
    var body: String? = null,
    var likeLength: Int = 0,
    var likeUser: Author? = null,
    var viewCount: Int = 0,
    var commentLength: Int = 0,
    var createdAt: String = "",
    var modifiedAt: String = "",
    var isLike: Boolean = false
) {
    fun getUrls(): ArrayList<String> {
        var res = ArrayList<String>()
        mediaUrls.forEach { res.add(it.url) }
        return res
    }
}
