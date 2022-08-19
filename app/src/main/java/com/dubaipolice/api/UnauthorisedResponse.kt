package com.dubaipolice.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class UnauthorisedResponse {

    @SerializedName("success")
    @Expose
    var isSuccess = false

    /*  @SerializedName("status_code")
      @Expose
      var statusCode = 0*/

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