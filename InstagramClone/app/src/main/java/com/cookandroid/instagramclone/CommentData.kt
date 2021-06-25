package com.cookandroid.instagramclone

data class CommentDTO(
    val id: Int,
    val author: Author,
    val isLike: Boolean,
    val created: String,
    val content: String,
    val likeLength: Int
)