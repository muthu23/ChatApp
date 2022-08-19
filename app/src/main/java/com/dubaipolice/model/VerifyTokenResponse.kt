package com.dubaipolice.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class VerifyTokenResponse {

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

        @SerializedName("is_valid")
        @Expose
        var isValid: Boolean? = null

        @SerializedName("token")
        @Expose
        var token: String? = null

    }

}