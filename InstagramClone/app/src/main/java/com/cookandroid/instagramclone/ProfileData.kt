package com.cookandroid.instagramclone

data class MediaUrls(
    var mediaUrlId:Int,
    var url: String,
    var type: String
)

data class UserPostData(
    var postId: Int,
    var mediaUrls: ArrayList<MediaUrls>,
    var content: String?,
    var likeCount: Int,
    var commentCount: Int,
    var createdAt: String,
    var modifiedAt: String,
    var isLike: Boolean
)