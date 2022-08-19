package com.dubaipolice.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class GroupChat {

    @SerializedName("group_id")
    @Expose
    var groupId: String? = null

    @SerializedName("group_jid")
    @Expose
    var groupJid: String? = null

    @SerializedName("group_image_url")
    @Expose
    var groupImageUrl: String? = null

    @SerializedName("group_name")
    @Expose
    var groupName: String? = null

    @SerializedName("last_msg")
    @Expose
    var lastMessage: String? = null

    @SerializedName("date")
    @Expose
    var date: String? = null

}