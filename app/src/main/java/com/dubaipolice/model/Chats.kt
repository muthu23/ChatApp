package com.dubaipolice.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Chats {
    @SerializedName("sender_id")
    @Expose
    var senderId = 0

    @SerializedName("receiver_id")
    @Expose
    var receiverId = 0

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("timestamp")
    @Expose
    var timestamp: String? = null

    constructor() {}
    constructor(senderId: Int, receiverId: Int, message: String?, timestamp: String?) {
        this.senderId = senderId
        this.receiverId = receiverId
        this.message = message
        this.timestamp = timestamp
    }



}