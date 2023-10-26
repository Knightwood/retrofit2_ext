package com.kiylx.libx.http.kotlin.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

/**
 * 创建者 kiylx
 * 创建时间 2020/10/7 17:06
 * packageName：com.crystal.aplayer.module_base.base.http.okhttp
 * 描述：
 */
class MyHttpLoggingInterceptor : Interceptor {
    private val TAG = "tty2-HttpLoggingInterceptor"
    private val UTF8 = Charset.forName("UTF-8")
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBody = request.body
        var body: String? = null
        requestBody?.let {
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            var charset: Charset? = UTF8
            val contentType = requestBody.contentType()
            contentType?.let {
                charset = contentType.charset(UTF8)
            }
            body = buffer.readString(charset!!)
        }
        Log.d(TAG,
            " \n --------------------------------------------------- \n"
                    + "| send request: \n"
                    + "| method = ${request.method} \n"
                    + "| url = ${request.url} \n"
                    + "| request header = ${request.headers} \n"
                    + "| request params = $body \n"
                    + "--------------------------------------------------- \n"
        )
        val startNs = System.nanoTime()
        val response = chain.proceed(request)
        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        val responseBody = response.body
        val rBody: String

        val source = responseBody!!.source()
        source.request(java.lang.Long.MAX_VALUE)
        val buffer = source.buffer()

        var charset: Charset? = UTF8
        val contentType = responseBody.contentType()
        contentType?.let {
            charset = contentType.charset(UTF8)
        }
        rBody = buffer.clone().readString(charset!!)

        Log.d(TAG,
            "\n--------------------------------------------------- \n"
                    + "| received : code = ${response.code}\n"
                    + "| url = ${response.request.url}\n"
                    + "| body = $body \n"
                    + "| response $rBody \n"
                    + "--------------------------------------------------- \n")

        return response
    }
}