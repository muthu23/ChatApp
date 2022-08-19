package com.dubaipolice.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class GetAllCmsListResponse {

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

        @SerializedName("id")
        @Expose
        var id: Int? = null

        @SerializedName("name")
        @Expose
        var name: String? = null

        @SerializedName("color_scheme")
        @Expose
        var colorScheme: String? = null

        @SerializedName("font_scheme")
        @Expose
        var fontScheme: String? = null

        @SerializedName("created_at")
        @Expose
        var createdAt: String? = null

        @SerializedName("updated_at")
        @Expose
        var updatedAt: String? = null


    }

}