package com.dubaipolice.model

data class VideoStreamResponse(
    val current_page: Int,
    val data: Data2,
    val items: Int,
    val message: String,
    val organizations: List<Any>,
    val page_count: Int,
    val success: Boolean
)

data class Data2(
    val ipstreams: List<VideoItem>,
)

data class VideoItem(
    val Organization: Organization,
    val comments: String,
    val created_at: String,
    val icon: String,
    val id: Int,
    val latitude: Any,
    val longitude: Any,
    val name: String,
    val organization_id: Int,
    val rts_link: String,
    val serial_number: String,
    val updated_at: String,
    var duration: String? = "00:15:25"
)

data class Organization(
    val id: Int,
    val name: String
)