package com.dubaipolice.model

data class LiveStreamListResponse(
    val current_page: Int,
    val data: Data,
    val items: Int,
    val message: String,
    val page_count: Int,
    val success: Boolean
)
data class Data(
    val streamList: List<Stream>
)
data class Stream(
    val stream_key: String,
    val user_id: Int,
    val createdAt: String,
    val first_name: String,
    val last_name: String,
    val profile_image: String?,
)