package com.dubaipolice.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LoginResponse {
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

        @SerializedName("otp")
        @Expose
        var otp: String? = null

        @SerializedName("userDetails")
        @Expose
        var userDetails: UserDetails? = null

        inner class UserDetails {
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

            @SerializedName("sound_file")
            @Expose
            var soundFile: String? = null

            @SerializedName("profile_image")
            @Expose
            var profileImage: String? = null

            @SerializedName("jid")
            @Expose
            var xmppUserJid: String? = null

            @SerializedName("jid_password")
            @Expose
            var xmppUserPassword: String? = null

            @SerializedName("token")
            @Expose
            var token: String? = null

            @SerializedName("passcode_generated")
            @Expose
            var isPasscodeGenerated: Boolean? = null

            @SerializedName("phone_number")
            @Expose
            var phoneNumber: Boolean? = null

            @SerializedName("Language")
            @Expose
            var language: Language? = null

            @SerializedName("Role")
            @Expose
            var role: Role? = null

            @SerializedName("Organization")
            @Expose
            var organization: Organization? = null

            @SerializedName("Status")
            @Expose
            var status: Status? = null

            inner class Language {

                @SerializedName("language_id")
                @Expose
                var languageId: Int? = null

                @SerializedName("code") //EN
                @Expose
                var languageCode: String? = null

                @SerializedName("name") //English
                @Expose
                var languageName: String? = null

            }

            inner class Role {

                //1 end user, 2 super user, 3 sub admin, 4 it admin, 5 admin
                @SerializedName("role_id")
                @Expose
                var roleId: Int? = null

                @SerializedName("user_type") //End User / Super User
                @Expose
                var userType: String? = null

                @SerializedName("status")  //true / false
                @Expose
                var status: Boolean? = null

            }

            inner class Organization {

                //1 end user, 2 super user, 3 sub admin, 4 it admin, 5 admin
                @SerializedName("organization_id")
                @Expose
                var organizationId: Int? = null

                @SerializedName("name") //End User / Super User
                @Expose
                var name: String? = null


            }


            inner class Status {

                //2 for active
                @SerializedName("status_id")
                @Expose
                var statusId: Int? = null

                @SerializedName("name") //active
                @Expose
                var statusName: String? = null

            }

        }
    }
}
