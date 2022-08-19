package com.dubaipolice.model

import java.io.Serializable

data class HLSLinkResponse(
    val data: HLSLink,
    val message: String,
    val success: Boolean
)
data class HLSLink(
    val link: String,
    val pid: Int,
    val isCameraUrl : Boolean = false
) : Serializable