package com.dubaipolice.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dubaipolice.db.model.MessageInfoTable

@Dao
interface MessageInfoTableDao {

    @Query("SELECT * FROM message_info")
    fun getAll(): List<MessageInfoTable?>?

    @Query("SELECT * FROM message_info WHERE group_jid = :groupJid order by unixTimestamp asc")
    fun getChatList(groupJid: String): List<MessageInfoTable>?

    @Query("SELECT * FROM message_info WHERE sender_jid = :senderJid AND sent_at IS null AND created_at IS NOT null order by unixTimestamp asc")
    fun getUnuploadedChatList(senderJid: String): List<MessageInfoTable>?

    @Query("SELECT * FROM message_info WHERE message_id = :messageId")
    fun checkMessageDuplicate(messageId: String): List<MessageInfoTable>?

    @Insert
    fun insert(messageInfoTable: MessageInfoTable?)

    @Query("UPDATE message_info SET sent_at= :sentAt WHERE message_id = :messageId")
    fun updateMessageSent(sentAt: String, messageId: String)

    @Query("UPDATE message_info SET delivered_at= :deliveredAt WHERE message_id = :messageId")
    fun updateMessageDelivered(deliveredAt: String, messageId: String)

    @Query("UPDATE message_info SET displayed_at= :displayedAt WHERE message_id = :messageId")
    fun updateMessageDisplayed(displayedAt: String, messageId: String)

    //1 means true 1 and 0 means false
    //get unread chat count for each group
    @Query("SELECT COUNT(*) FROM message_info WHERE sender_jid != :senderJid AND group_jid = :groupJid AND read_status=0")
    fun getUnreadChatCount(senderJid: String, groupJid: String) : String?

    //get unread chats
    @Query("SELECT * FROM message_info WHERE sender_jid != :senderJid AND group_jid = :groupJid AND read_status=0")
    fun getUnreadChats(senderJid: String, groupJid: String): List<MessageInfoTable?>?

    @Query("UPDATE message_info SET read_status= 1 WHERE message_id = :messageId")
    fun updateMessageReadForSenderSide(messageId: String)

    @Query("DELETE FROM message_info WHERE group_jid=:groupJid")
    fun deleteChatForDeletedGroup(groupJid: String)

    @Query("DELETE FROM message_info")
    fun deleteAllChats()

    @Query("DELETE FROM message_info WHERE message_id=:messageId")
    fun deleteMessageHavingNoFile(messageId: String)

    @Query("UPDATE message_info SET media_url= :mediaUrl, media_thumb_image_url= :mediaThumbUrl WHERE message_id = :messageId")
    fun updateUploadedMediaUrl(mediaUrl: String, mediaThumbUrl: String, messageId: String)

    @Query("UPDATE message_info SET media_url_local= :mediaUrlLocal WHERE message_id = :messageId")
    fun updateDownloadedMediaUrl(mediaUrlLocal: String, messageId: String)

}