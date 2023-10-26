package com.kiylx.retrofit2_ext

import android.app.Application
import android.util.Log
import com.kiylx.libx.http.kotlin.common.OkhttpClientProvider
import com.kiylx.libx.http.okhttp_logger.Level
import com.kiylx.libx.http.okhttp_logger.LoggingInterceptor
import com.kiylx.retrofit2_ext.example2_doc.HeaderInterceptor
import okhttp3.Dispatcher
import java.util.concurrent.TimeUnit

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        configOkHttp()
    }

    /**
     * 配置全局的okhttpclient
     */
    fun configOkHttp() {
        OkhttpClientProvider.configOkHttpClient {
            val dispatcher = Dispatcher()
            dispatcher.maxRequests = 1
            dispatcher(dispatcher)
            configCache(this@MyApplication)//配置缓存策略
            var loggerInterceptor: LoggingInterceptor? = null
            val isDebug = true
            if (isDebug) {
                loggerInterceptor = LoggingInterceptor.Builder()
                    .setLevel(Level.BASIC)
                    .log(Log.VERBOSE)
                    .singleTag(true)
                    .tag("tty1-HttpLogger")
                    .build()
            }

            this
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(HeaderInterceptor())
            if (isDebug) {//debug模式添加日志打印
                loggerInterceptor?.let {
                    this.addInterceptor(it)
                }
            }
        }
    }

}