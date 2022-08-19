package com.dubaipolice.model

data class GroupInfoResponse(
    val data: GroupData,
    val message: String,
    val success: Boolean
)
data class GroupData(
    val groupDetails: GroupDetails
)
data class GroupDetails(
    val Language: Language,
    val alert_level: Int,
    val created_at: String,
    val created_by: Int,
    val group_id: Int,
    val image: String?,
    val members: List<Member>,
    val name: String,
    val is_call_active: Boolean,
    val organization_id: Int,
    val room_jid: String,
    val shared: Boolean,
    val status: String,
    val updated_at: String,
    val mute: Boolean
)

data class Language(
    val code: String,
    val id: Int,
    val name: String
)

data class Member(
    val member_id: Int,
    val organization_id: Int,
    val role_id: Int,
    val user: User
)

data class User(
    val first_name: String,
    val jid: Any,
    val last_name: String,
    val profile_image: String,
    val user_id: Int,
    val username: String,
    val lastKnownLocation: LastKnownLocation

)
data class LastKnownLocation(
    val created_at: String,
    val latitude: String,
    val longitude: String
)