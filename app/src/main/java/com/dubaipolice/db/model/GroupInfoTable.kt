package com.dubaipolice.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "group_info")
class GroupInfoTable : Serializable {

    @PrimaryKey(autoGenerate = true)
    public var id = 0

    @ColumnInfo(name = "access_level")
    public var accessLevel: String? = null

    @ColumnInfo(name = "alert_level") //1 grey, 2 yellow, 3 red
    public var alertLevel: String? = null

    @ColumnInfo(name = "created_timestamp")
    public var createdTimestamp: String? = null //NSDate

    @ColumnInfo(name = "group_id")
    public var groupId: Int? = null

    @ColumnInfo(name = "group_jid")
    public var groupJid: String? = null

    @ColumnInfo(name = "group_name")
    public var groupName: String? = null

    @ColumnInfo(name = "callActive")
    public var callActive: String = "false"

    @ColumnInfo(name = "status")
    public var groupActiveStatus: String = "true"

    @ColumnInfo(name = "historyTimestamp")
    public var historyTimestamp: String? = null //NSDate

    @ColumnInfo(name = "image_url")
    public var imageUrl: String? = null

    //======LAST MESSAGE DATA===========================

    @ColumnInfo(name = "unixTimestamp") //only unixtimestamp utc with millseconds
    public var unixTimestamp: String?= null

    @ColumnInfo(name = "last_message")
    public var lastMessage: String? = null

    @ColumnInfo(name = "unread_count")
    public var unreadCount: Int? = null

    //new columns added for read receipt
    @ColumnInfo(name = "delivered_at")
    public var deliveredAt: String?= null //NSDate

    @ColumnInfo(name = "displayed_at")
    public var displayedAt: String?= null //NSDate

    @ColumnInfo(name = "type") //by default 1 for txt msg
    public var type: String?= null

    @ColumnInfo(name = "message_id")
    public var messageId: String?= null

    @ColumnInfo(name = "sent_at")
    public var sentAt: String?= null //NSDate

    @ColumnInfo(name = "sender_jid")
    public var senderJid: String?= null

    @ColumnInfo(name = "last_sender_name")
    public var lastSenderName: String =""

    //=========LAST MSG ENDS HERE================

    @ColumnInfo(name = "reference_user_id")
    public var refrenceUserId: Int? = null

    @ColumnInfo(name = "updated_timestamp")
    public var updatedTimestamp: String? = null //NSDate

    @ColumnInfo(name = "creator_name")
    public var creatorName: String? = null

}