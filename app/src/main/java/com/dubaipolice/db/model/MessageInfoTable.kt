package com.dubaipolice.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "message_info")
class MessageInfoTable : Serializable {

    @PrimaryKey(autoGenerate = true)
    public var id = 0

    @ColumnInfo(name = "created_at")
    public var createdAt: String? = null //NSDate

    @ColumnInfo(name = "delivered_at")
    public var deliveredAt: String? = null //NSDate

    @ColumnInfo(name = "displayed_at")
    public var displayedAt: String? = null //NSDate

    @ColumnInfo(name = "type") //by default 1 for txt msg
    public var type: String? = null

    @ColumnInfo(name = "group_id")
    public var groupId: String? = null

    @ColumnInfo(name = "group_image_url")
    public var groupImageUrl: String? = null

    @ColumnInfo(name = "group_jid")
    public var groupJid: String? = null

    @ColumnInfo(name = "group_name")
    public var groupName: String? = null

    @ColumnInfo(name = "read_status")
    public var readStatus: Int = 0 //1 means true 1 and 0 means false

    @ColumnInfo(name = "message_id")
    public var messageId: String? = null

    @ColumnInfo(name = "message_text")
    public var messageText: String? = null

    @ColumnInfo(name = "received_at")
    public var receivedAt: String? = null //NSDate

    @ColumnInfo(name = "reference_user_id")
    public var refrenceUserId: Int? = null

    @ColumnInfo(name = "sender_id")
    public var senderId: String? = null

    @ColumnInfo(name = "sender_image_url")
    public var senderImageUrl: String? = null

    @ColumnInfo(name = "sender_jid")
    public var senderJid: String? = null

    @ColumnInfo(name = "sender_name")
    public var senderName: String = ""

    @ColumnInfo(name = "sender_first_name")
    public var senderFirstName: String? = null

    @ColumnInfo(name = "sender_last_name")
    public var senderLastName: String? = null

    @ColumnInfo(name = "sent_at")
    public var sentAt: String? = null //NSDate

    @ColumnInfo(name = "timestamp") //all local time with formatted date time and only unixtimestamp utc with millseconds
    public var timestamp: String? = null

    @ColumnInfo(name = "unixTimestamp") //only unixtimestamp utc with millseconds
    public var unixTimestamp: String? = null

    //media and doc data
    @ColumnInfo(name = "media_url")
    public var mediaUrl: String? = null

    @ColumnInfo(name = "media_url_local")
    public var mediaUrlLocal: String? = null

    @ColumnInfo(name = "media_thumb_image_url")
    public var mediaThumbImageUrl: String? = null

    @ColumnInfo(name = "media_size")
    public var mediaSize: String? = null

    @ColumnInfo(name = "media_duration")
    public var mediaDuration: String? = null

    //doc data
    @ColumnInfo(name = "doc_mime_type")
    public var docMimeType: String? = null

    //location data
    @ColumnInfo(name = "latitude")
    public var latitude: String? = null

    @ColumnInfo(name = "longitude")
    public var longitude: String? = null

    //contact data
    @ColumnInfo(name = "contact_first_name")
    public var contactFirstName: String? = null

    @ColumnInfo(name = "contact_last_name")
    public var contactLastName: String? = null

    @ColumnInfo(name = "contact_middle_name")
    public var contactMiddleName: String? = null

    @ColumnInfo(name = "contact_phone")
    public var contactPhone: String? = null

    @ColumnInfo(name = "contact_email")
    public var contactEmail: String? = null

}