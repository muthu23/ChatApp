package com.dubaipolice.wrapper

class Resource<T> private constructor(
    val status: Status, val data: T?,
    val message: String?
) {

    enum class Status {
        SUCCESS, ERROR, LOADING
    }

    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(msg: String?): Resource<T?> {
            return Resource(Status.ERROR, null, msg)
        }

        fun <T> error2(msg: String?, data: T?): Resource<T?> {
            return Resource(Status.ERROR, data, msg)
        }

        fun <T> loading(data: T?): Resource<T?> {
            return Resource(Status.LOADING, data, null)
        }
    }

}
