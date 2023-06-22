package com.kiylx.libx.http.kotlin.common

abstract class BaseErrorHandler {
    /**
     * 网络请求非200
     */
    abstract fun FailedErr(tmp: RawResponse.Error)

    abstract fun <T> OnSuccess(tmp: RawResponse.Success<T>)

    /**
     * 网络请求出错
     */
    abstract fun ExceptionErr(tmp: RawResponse.Error)
}