package com.dubaipolice.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class GroupChatParticipants {

    @SerializedName("profile_pic")
    @Expose
    var profilePicture: String? = null

    @SerializedName("user_name")
    @Expose
    var userName: String? = null

    @SerializedName("is_online")
    @Expose
    var isOnline: Boolean? = null

}