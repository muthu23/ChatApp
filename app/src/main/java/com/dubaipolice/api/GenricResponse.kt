package com.dubaipolice.api

class GenricResponse<T> {
    var statusCode: Int? = null
    var result: T? = null
        private set
    var error: GenricError? = null

    fun setResult(result: T) {
        this.result = result
    }
}


