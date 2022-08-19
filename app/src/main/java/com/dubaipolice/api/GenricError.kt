package com.dubaipolice.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class GenricError {
    @SerializedName("success")
    @Expose
    var success: Boolean? = null

/*    @SerializedName("status_code")
    @Expose
    var statusCode: Int? = null*/

    @SerializedName("message")
    @Expose
    var message: String? = null

    /*@SerializedName("error")
    @Expose
    var error: Data? = null*/

    @SerializedName("data")
    @Expose
    var error: Data? = null

    inner class Data {

    }
}

