package com.dubaipolice.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ProfileResponse {

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
        @SerializedName("userDetails")
        @Expose
        var userDetails: UserDetails? = null

        inner class UserDetails {
            @SerializedName("id")
            @Expose
            var id: Int? = null

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

        }
    }

}