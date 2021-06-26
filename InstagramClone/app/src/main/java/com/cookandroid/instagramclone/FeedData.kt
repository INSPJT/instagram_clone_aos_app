package com.cookandroid.instagramclone

import android.os.AsyncTask

data class FeedSorted(
    val sorted: Boolean,
    val unsorted: Boolean,
    val empty: Boolean
)

data class Pageable(
    val sort: FeedSorted,
    val offset: Int,
    val pageNumber: Int,
    val pageSize: Int,
    val paged: Boolean,
    val unpaged: Boolean
)

data class FeedDto(
    val posts: ArrayList<PostDTO>,
    val hasNext: Boolean
)
