package com.dubaipolice.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class GroupListResponse {

    @SerializedName("success")
    @Expose
    var isSuccess = false

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("data")
    @Expose
    var data: Data? = null

    //Data Class
    inner class Data {

        @SerializedName("groupList")
        @Expose
        var groupDetails: ArrayList<GroupDetails>? = null

        inner class GroupDetails {
            @SerializedName("group_id")
            @Expose
            var groupId: Int? = null

            @SerializedName("name")
            @Expose
            var groupName: String? = null

            @SerializedName("room_jid")
            @Expose
            var groupJid: String? = null

            @SerializedName("image")
            @Expose
            var image: String? = null

            @SerializedName("alert_level") //red >> yellow >> grey
            @Expose
            var alertLevel: String? = null

            @SerializedName("created_at")
            @Expose
            var createdAt: String? = null

            @SerializedName("updated_At")
            @Expose
            var updatedAt: String? = null

            @SerializedName("organization_id")
            @Expose
            var organizationId: Int? = null

            @SerializedName("is_call_active")
            @Expose
            var callActive: String = "false"

            @SerializedName("status")
            @Expose
            var groupActiveStatus: String = "true"

           /* @SerializedName("members")
            @Expose
            var memberDetails: ArrayList<MemberDetails>? = null*/

            inner class MemberDetails {

                @SerializedName("member_id")
                @Expose
                var memberId: Int? = null

                @SerializedName("role_id")
                @Expose
                var roleId: Int? = null

                @SerializedName("organization_id")
                @Expose
                var organizationId: Int? = null

                @SerializedName("user")
                @Expose
                var user: User? = null

            }

            inner class User {

                @SerializedName("user_id")
                @Expose
                var userId: Int? = null

                @SerializedName("first_name")
                @Expose
                var firstName: String? = null

                @SerializedName("last_name")
                @Expose
                var lastName: String? = null

                @SerializedName("username")
                @Expose
                var userName: String? = null

                @SerializedName("profile_image")
                @Expose
                var profileImage: String? = null

                @SerializedName("jid")
                @Expose
                var userJid: String? = null


            }

        }
    }

}