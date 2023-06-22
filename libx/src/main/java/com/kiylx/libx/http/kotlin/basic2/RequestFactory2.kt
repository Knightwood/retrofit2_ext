package com.kiylx.libx.http.kotlin.basic2

import com.kiylx.libx.http.kotlin.common.BaseErrorHandler
import com.kiylx.libx.http.kotlin.common.handleRequest
import com.kiylx.libx.http.kotlin.common.ErrorType.*
import com.kiylx.libx.http.kotlin.common.RawResponse
import kotlinx.coroutines.CoroutineScope
import retrofit2.Call

/**
 * 创建者 kiylx
 * 创建时间 2022/5/7 20:06
 * packageName：com.crystal.aplayer.module_base.base.http.okhttp
 * 描述：
 */
suspend inline fun <reified T : Any> handle2(action: Call<T>): Resource2<T> {
    return handleApi2(action, null)
}

suspend inline infix fun <reified T : Any> CoroutineScope.handleWith2(action: Call<T>): Resource2<T> {
    return handleApi2(action, null)
}

suspend inline fun <reified T : Any, reified E : BaseErrorHandler> handleApi2(
    action: Call<T>,
    errorHandler: E?,
): Resource2<T> {
    val rawResponse = handleRequest {
        action.execute()
    }
    return when (rawResponse) {
        is RawResponse.Error -> {
            errorHandler?.let {
                when (rawResponse.errorMsg.errorType) {
                    NETWORK_ERROR -> it.ExceptionErr(rawResponse)
                    SERVICE_ERROR -> it.FailedErr(rawResponse)
                }
            }
            Resource2.RequestError(rawResponse)
        }
        is RawResponse.Success -> {
            errorHandler?.OnSuccess(rawResponse)
            val info = rawResponse.responseData
            Resource2.Success(info)
        }
    }
}

