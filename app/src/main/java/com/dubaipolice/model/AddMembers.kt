package com.dubaipolice.model

data class AddMembers(
    val group_id: Int = 0,
    val users: List<Int> = emptyList()
)