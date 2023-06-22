package com.kiylx.libx.http.kotlin.common

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * 单例，且是懒加载。
 * 创建接口实例，存储不同baseurl下的retrofitholder实例
 */
object MultiRetrofitHolder {
    private var retrofitMap: HashMap<String, Retrofit2Holder> = hashMapOf()
    private val lock = ReentrantLock()

    fun <T : Retrofit2Holder> addRetrofit(baseUrl: String, retrofit2Holder: T) {
        lock.withLock {
            retrofitMap.putIfAbsent(baseUrl, retrofit2Holder)
        }
    }

    fun get(baseUrl: String): Retrofit2Holder? {
        return retrofitMap[baseUrl]
    }

    /**
     * 传入apiService接口，得到实例，如果baseurl不存在，返回null
     */
    fun <T> createServer(baseUrl: String, clazz: Class<T>): T? {
        return retrofitMap[baseUrl]?.create(clazz)
    }

}

/**
 * 只有单个retrofit
 */
class SingleServiceCreator private constructor(baseUrl: String) {
    private val retrofitHolder: Retrofit2Holder = Retrofit2Holder(baseUrl)

    /**
     * 传入apiService接口，得到实例
     */
    fun <T> create(clazz: Class<T>): T = retrofitHolder.create(clazz)

    companion object {
        @Volatile
        private var singleCreator: SingleServiceCreator? = null
        fun newInstance(baseUrl: String): SingleServiceCreator =
            singleCreator ?: synchronized(this) {
                singleCreator ?: SingleServiceCreator(baseUrl)
            }
    }

}

/**
 * 持有okhttpclient和retrofit
 * 继承并重写方法，以实现自定义
 */
open class Retrofit2Holder(val baseUrl: String) {
    val mOkHttpClient: OkHttpClient by lazy { createOkHttpClient() }
    val mRetrofit: Retrofit by lazy { createRetrofit(mOkHttpClient) }
    private val contentType = "application/json".toMediaType()

    @OptIn(ExperimentalSerializationApi::class)
    open fun createRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val builder = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(Json.asConverterFactory(contentType))
            .client(okHttpClient)
        return builder.build()
    }

    /**
     * 重写此方法以提供自定义实现，或者，修改[OkhttpClientProvider]中的实现。
     * 这两种方式，可以根据需要，使得 OkhttpClient是单例，或者不是。
     */
    open fun createOkHttpClient(): OkHttpClient {
        return OkhttpClientProvider.okHttpClient!!
    }

    /**
     * 传入apiService接口，得到实例
     */
    fun <T> create(clazz: Class<T>): T = mRetrofit.create(clazz)

}

/**
 * 单例，提供okHttpClient实例
 */
object OkhttpClientProvider {
    //每次获取okHttpClient是否都调用一次build()
    var reCreate=false
    lateinit var builder: OkHttpClient.Builder
    var okHttpClient: OkHttpClient? = null
        get() {
            if (field == null || reCreate) {
                field = builder.build()
            }
            return field
        }

    fun configOkHttpClient(block: OkHttpClient.Builder.() -> Unit) {
        if (this::builder.isInitialized) {
            return
        } else {
            builder = OkHttpClient.Builder()
            builder.block()

        }

    }
}