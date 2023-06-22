package com.kiylx.retrofit2_ext

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.kiylx.immersionbar.R
import com.kiylx.libx.http.kotlin.common.OkhttpClientProvider
import com.kiylx.libx.http.okhttp_logger.Level
import com.kiylx.libx.http.okhttp_logger.LoggingInterceptor
import com.kiylx.retrofit2_ext.example.HeaderInterceptor
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    /**
     * 配置全局的okhttpclient
     */
    fun configOkHttp() {
        OkhttpClientProvider.configOkHttpClient {
            val dispatcher = Dispatcher()
            dispatcher.maxRequests = 1
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
            this.dispatcher(dispatcher)
        }
    }
}