package com.dubaipolice.model

data class UserListResponse(
    val data: UserData,
    val message: String,
    val items: Int,
    val page_count: Int,
    val current_page: Int,
    val success: Boolean
)

data class UserData(
    val usersList: List<Users>
)

data class Users(
    val first_name: String,
    val jid: String,
    val last_name: String,
    val profile_image: String,
    val user_id: Int,
    val username: String,
    var isSelected: Boolean

)