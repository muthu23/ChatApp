package com.dubaipolice.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.dubaipolice.db.model.MemberInfo
import com.dubaipolice.db.model.MemberInfoTable

@Dao
interface MemberInfoTableDao {


    @Query("SELECT * FROM member_info WHERE group_id = :group_id")
    fun getAll(group_id: String): LiveData<List<MemberInfoTable>>

    @Query("SELECT * FROM member_info WHERE group_id = :group_id")
    fun getAllMembers(group_id: String): List<MemberInfoTable>

    @Query("SELECT DISTINCT user_jid FROM member_info")
    fun getAllGroupMembers(): List<MemberInfo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(memberInfoTable: List<MemberInfoTable>)

    @Delete
    fun delete(memberInfoTable: List<MemberInfoTable>)

    @Query("DELETE FROM member_info")
    fun deleteAllMembers()

    @Query("DELETE FROM member_info WHERE user_jid =:userJid AND group_id=:group_id")
    fun deleteMember(userJid: String, group_id: String)

    @Query("DELETE FROM member_info WHERE group_jid=:groupJid")
    fun deleteMemberForDeletedGroup(groupJid: String)

    @Query("UPDATE member_info SET latitude=:latitude, longitude=:longitude, createdAt=:createdAt WHERE user_jid =:user_jid")
    fun updateMembersData(latitude: String?, longitude: String?, createdAt: String, user_jid: String)

    @Query("UPDATE member_info SET first_name=:firstName, last_name=:lastName, role_id=:roleId WHERE user_jid =:user_jid AND group_jid = :groupJid")
    fun updateMembersDetails(firstName: String?, lastName: String?, roleId: Int?, user_jid: String, groupJid: String)

    @Query("SELECT * FROM member_info WHERE group_id = :group_id AND longitude IS NOT NULL")
    fun getAllMembersLocation(group_id: String): LiveData<List<MemberInfoTable>>



}