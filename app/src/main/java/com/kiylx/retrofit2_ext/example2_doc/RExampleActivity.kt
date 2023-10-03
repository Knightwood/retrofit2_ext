package com.kiylx.retrofit2_ext.example2_doc

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kiylx.libx.http.kotlin.basic.Status.*
import com.kiylx.libx.http.kotlin.basic2.Resource2
import com.kiylx.libx.http.kotlin.common.OkhttpClientProvider
import com.kiylx.libx.http.okhttp_logger.Level
import com.kiylx.libx.http.okhttp_logger.LoggingInterceptor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.Dispatcher
import java.util.concurrent.TimeUnit

class RExampleActivity : AppCompatActivity() {
    lateinit var vm: ViewModelExample

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vm = ViewModelProvider(this)[ViewModelExample::class.java]

        //这个应该放在application中，只初始化一次即可
        configOkHttp()

        //livedata和resource的方式
        vm.loginLiveData.observe(this) {//观察结果
            when (it.status) {//根据结果改变页面状态
                SUCCESS -> TODO()
                REQUEST_ERROR -> TODO()
                LOADING -> TODO()
                LOCAL_ERR -> TODO()
            }
        }

        //resources2和stateflow的方式
        this.lifecycleScope.launch {
            // repeatOnLifecycle 每当生命周期处于 STARTED 或以后的状态时会在新的协程中
            // 启动执行代码块，并在生命周期进入 STOPPED 时取消协程。
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.uploadFlow.collectLatest { resource ->
                    when (resource) {
                        is Resource2.Loading -> TODO()
                        is Resource2.Error -> TODO()
                        is Resource2.Success -> TODO()
                        Resource2.EmptyLoading -> TODO()
                        else -> {}
                    }
                }

            }
        }
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

    override fun onStart() {
        super.onStart()
        vm.getLogin()//调用方法

    }
}