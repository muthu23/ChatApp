package com.dubaipolice.model

data class GroupMediaResponse(
    val current_page: Int,
    val data: MediaData,
    val items: Int,
    val message: String,
    val page_count: Int,
    val success: Boolean
)
data class MediaData(
    val attachmentList: List<Attachment>
)

data class Attachment(
    val attachmentThumbUrl: String,
    val attachmentUrl: String,
    val filename: String = "Sample",
    val filetype: String = "Doc",
    val created_at: String,
    val id: Int,
    val media_type: String,
    val updated_at: String,
    val duration: String
)