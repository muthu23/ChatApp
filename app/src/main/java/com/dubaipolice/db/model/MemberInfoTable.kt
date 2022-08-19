package com.dubaipolice.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(tableName = "member_info")
class MemberInfoTable : Serializable {

    @PrimaryKey(autoGenerate = true)
    var id = 0

    @ColumnInfo(name = "access_level") // skip
    var accessLevel: String? = null

    @ColumnInfo(name = "email") // skip
    var email: String? = null

    @ColumnInfo(name = "group_id")
    var groupId: Int? = null

    @ColumnInfo(name = "group_jid")
    var groupJid: String? = null

    @ColumnInfo(name = "group_name")
    var groupName: String? = null

    @ColumnInfo(name = "last_activity_seconds") // skip
    var lastActivitySeconds: Int? = null

    @ColumnInfo(name = "image_url")
    var imageUrl: String? = null

    @ColumnInfo(name = "is_online") // skip
    var isOnline: Boolean? = null

    @ColumnInfo(name = "user_jid")
    var userJid: String? = null

    @ColumnInfo(name = "ejabbered_id") // skip
    var ejabberedId: String? = null

    @ColumnInfo(name = "reference_user_id") // skip
    var refrenceUserId: Int? = null

    @ColumnInfo(name = "thumb_url") // skip
    var thumbUrl: String? = null

    @ColumnInfo(name = "user_id")
    var userId: Int? = null

    @ColumnInfo(name = "role_id")
    var roleId: Int? = null

    @ColumnInfo(name = "user_name")
    var userName: String? = null

    @ColumnInfo(name = "first_name")
    var firstName: String? = null

    @ColumnInfo(name = "last_name")
    var lastName: String? = null

    @ColumnInfo(name = "group_access_level") // skip
    var groupAccessLevel: String? = null

    @ColumnInfo(name = "latitude")
    var latitude: String? = null

    @ColumnInfo(name = "longitude")
    var longitude: String? = null

    @ColumnInfo(name = "createdAt")
    var createdAt: String = ""


}