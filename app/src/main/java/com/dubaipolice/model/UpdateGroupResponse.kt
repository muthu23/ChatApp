package com.dubaipolice.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class UpdateGroupResponse {

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

        @SerializedName("groupDetails")
        @Expose
        var groupDetails: GroupDetails? = null

        inner class GroupDetails {

            @SerializedName("image")
            @Expose
            var image: String? = null

            @SerializedName("name")
            @Expose
            var name: String? = null

            @SerializedName("alert_level")
            @Expose
            var alertLevel: Int? = null

        }

    }

}