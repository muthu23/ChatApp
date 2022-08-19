package com.dubaipolice.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.dubaipolice.db.model.GroupInfoTable
import com.dubaipolice.db.model.MemberInfoTable

@Dao
interface GroupInfoTableDao {

    @Query("SELECT * FROM group_info order by unixTimestamp asc")
    fun getAll(): List<GroupInfoTable>?

    @Query("SELECT * FROM group_info order by alert_level desc, unixTimestamp desc")
    fun getAllLiveData(): LiveData<List<GroupInfoTable>>?

    @Query("UPDATE group_info SET last_message=:lastMessage, unixTimestamp=:unixTimestamp, message_id=:messageId, type= :messageType, sender_jid= :senderJid,last_sender_name= :senderName, sent_at= null, delivered_at=null, displayed_at=null WHERE group_jid = :groupJid")
    fun updateMessage(lastMessage: String, unixTimestamp: String, messageId: String, messageType: String, senderJid: String, senderName: String, groupJid: String)

    @Query("UPDATE group_info SET sent_at= :sentAt WHERE message_id = :messageId")
    fun updateMessageSent(sentAt: String, messageId: String)

    @Query("UPDATE group_info SET delivered_at= :deliveredAt WHERE message_id = :messageId")
    fun updateMessageDelivered(deliveredAt: String, messageId: String)

    @Query("UPDATE group_info SET displayed_at= :displayedAt WHERE message_id = :messageId")
    fun updateMessageDisplayed(displayedAt: String, messageId: String)

    @Query("UPDATE group_info SET image_url=:image, group_name=:groupName, alert_level=:alertLevel WHERE group_jid = :groupJid")
    fun updateGroupData(image: String?, groupName: String?, alertLevel: String, groupJid: String)

    @Query("UPDATE group_info SET image_url=:image, group_name=:groupName, alert_level=:alertLevel, callActive=:callActive, status=:groupActiveStatus WHERE group_jid = :groupJid")
    fun updateGroupDataFromHome(image: String?, groupName: String?, alertLevel: String, callActive: String, groupActiveStatus:String, groupJid: String)


    @Query("UPDATE group_info SET callActive=:isCallActive WHERE group_jid = :groupJid")
    fun updateGroupCallStatus(isCallActive: String, groupJid: String)

    @Query("UPDATE group_info SET status=:groupActiveStatus WHERE group_jid = :groupJid")
    fun updateGroupActiveStatus(groupActiveStatus: String, groupJid: String)

    @Query("UPDATE group_info SET alert_level=:alertLevel WHERE group_jid = :groupJid")
    fun updateGroupAlertLevel(alertLevel: String, groupJid: String)

    @Query("UPDATE group_info SET unread_count=:unreadCount WHERE group_jid = :groupJid")
    fun updateGroupUnreadChatCount(unreadCount: Int, groupJid: String)

    @Query("DELETE FROM group_info WHERE group_jid=:groupJid")
    fun deleteGroup(groupJid: String)

    @Query("DELETE FROM group_info")
    fun deleteAllGroups()

    @Insert
    fun insert(groupInfoTable: GroupInfoTable?)





}