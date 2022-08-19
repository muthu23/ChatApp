package com.dubaipolice.model
data class NotificationsListResponse(
    val current_page: Int,
    val data: List<Notifications>,
    val items: Int,
    val message: String,
    val page_count: Int,
    val success: Boolean
)
data class Notifications(
    val id: Int,
    val sender_id: Int,
    val receiver_id: Int,
    val title: String,
    val body: String,
    var is_read: Boolean,
    val created_at: String?,
    val updated_at: String?,
)