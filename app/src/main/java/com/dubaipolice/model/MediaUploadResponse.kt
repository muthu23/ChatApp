package com.dubaipolice.model

import java.io.Serializable

data class MediaUploadResponse(
    val data: MediaUpload,
    val message: String,
    val success: Boolean
)
data class MediaUpload(
    val attachmentThumbUrl: String,
    val attachmentUrl: String,
    val createdAt: String,
    val group_id: String,
    val id: Int,
    val updatedAt: String
)