package com.dubaipolice.downloader
data class DownloadDetails(
    var notificationId: Int,
    var messageId: String,
    var fileName: String,
    var url: String,
    var groupJid: String,
    var createdTime: Int,
    var filePath: String = ""
)

