package com.dubaipolice.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CommonResponse {

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

    }

}