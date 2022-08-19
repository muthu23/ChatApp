package com.dubaipolice.model

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class CallerData : Serializable {

    @SerializedName("groupName")
    @Expose
    var groupName: String? = null

    @SerializedName("groupJid")
    @Expose
    var groupJid: String? = null

    @SerializedName("groupId")
    @Expose
    var groupId: String? = null

    @SerializedName("senderName")
    @Expose
    var senderName: String? = null

    @SerializedName("senderProfileImage")
    @Expose
    var senderProfileImage: String? = null

}