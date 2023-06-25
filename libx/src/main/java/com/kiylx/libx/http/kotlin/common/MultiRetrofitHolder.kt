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
 * @param baseUrl baseurl
 * @param json 自定义Json的配置
 * ```
 * prettyPrint = true //json格式化
 * isLenient = true //宽松解析，json格式异常也可解析，如：{name:"小红",age:"18"} + Person(val name:String,val age:Int) ->Person("小红",18)
 * ignoreUnknownKeys = true //忽略未知键，如{"name":"小红","age":"18"} ->Person(val name:String)
 * coerceInputValues =  true //强制输入值，如果json属性与对象格式不符，则使用对象默认值，如：{"name":"小红","age":null} + Person(val name:String = "小绿"，val age:Int = 18) ->Person("小红",18)
 * encodeDefaults =  true //编码默认值,默认情况下，默认值的属性不会参与序列化，通过设置encodeDefaults = true,可让默认属性参与序列化(可参考上述例子)
 * explicitNulls =  true //序列化时是否忽略null
 * allowStructuredMapKeys =  true //允许结构化映射(map的key可以使用对象)
 * allowSpecialFloatingPointValues =  true //特殊浮点值：允许Double为NaN或无穷大
```
 */
open class Retrofit2Holder(private val baseUrl: String, private val json: Json = Json) {
    val mOkHttpClient: OkHttpClient by lazy { createOkHttpClient() }
    val mRetrofit: Retrofit by lazy { createRetrofit(mOkHttpClient) }
    private val contentType = "application/json".toMediaType()

    @OptIn(ExperimentalSerializationApi::class)
    open fun createRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val builder = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(json.asConverterFactory(contentType))
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
    var reCreate = false
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
            if (reCreate) {
                builder.block()
            } else {
                return
            }
        } else {
            builder = OkHttpClient.Builder()
            builder.block()
        }

    }
}
