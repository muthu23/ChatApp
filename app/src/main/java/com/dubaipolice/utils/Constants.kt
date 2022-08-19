package com.dubaipolice.utils

class Constants {

    companion object {
        const val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=\\S+$).{6,}$"
//        const val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{6,}$"
        const val DD_MM_YYYY_FORMAT = "dd/MM/yyyy"
        const val YYYY_MM_DD_SERVER_RESPONSE_FORMAT = "yyyy-MM-dd HH:mm:ss"
        const val DD_MMM_YYYY_FORMAT = "dd MMM yyyy"
        const val MMM_DD_YYYY_FORMAT = "MMM dd yyyy"
        const val NETWORK_PAGE_SIZE = 10


    }
}