package com.kiylx.retrofit2_ext.http

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

//这是一个添加header的拦截器示例
class KeyInterceptor :Interceptor{

    override fun intercept(chain: Interceptor.Chain): Response {
        val oldRequest: Request = chain.request()
        //拿到拥有以前的request里的url的那些信息的builder
        val builder = oldRequest
            .url
            .newBuilder()
        //得到新的url（已经追加好了参数）
        val newUrl: HttpUrl = builder.addQueryParameter("key", "key example")
            .build()
        //利用新的Url，构建新的request，并发送给服务器
        val newRequest: Request = oldRequest
            .newBuilder()
            .url(newUrl)
            .build()
        return chain.proceed(newRequest)
    }
}