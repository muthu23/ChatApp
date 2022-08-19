package com.dubaipolice.db.model

import androidx.room.ColumnInfo
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class MemberInfo {

    var access_level: String? = null

    var email: String? = null

    var group_id: Int? = null

    var group_jid: String? = null

    var group_name: String? = null

    var last_activity_seconds: Int? = null

    var image_url: String? = null

    var user_jid: String? = null

    var ejabbered_id: String? = null

    var reference_user_id: Int? = null

    var thumb_url: String? = null

    var user_id: Int? = null

    var role_id: Int? = null

    var user_name: String? = null

    var first_name: String? = null

    var last_name: String? = null

    var group_access_level: String? = null

    var latitude: String? = null

    var longitude: String? = null


}