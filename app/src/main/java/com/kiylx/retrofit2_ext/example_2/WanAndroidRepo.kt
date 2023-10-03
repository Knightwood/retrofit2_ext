package com.kiylx.retrofit2_ext.example_2

import com.kiylx.libx.http.kotlin.basic2.Resource2
import com.kiylx.libx.http.kotlin.basic2.handleApi2
import com.kiylx.libx.http.kotlin.common.BaseErrorHandler
import com.kiylx.libx.http.kotlin.common.RawResponse
import com.kiylx.libx.http.kotlin.common.Retrofit2Holder
import retrofit2.Call
import retrofit2.http.GET

interface Api {

    @GET("/friend/json")
    fun getData(): Call<FriendData>
}

object WanAndroidRepo {
    val baseUrl = "https://www.wanandroid.com"

    //单例方式，全局唯一，要生成其他baseurl下的retrofit，要使用Retrofit2Holder
    val mainApi = Retrofit2Holder(baseUrl).create(Api::class.java)

    /**
     * 通用的错误/异常处理
     */
    val globalErrHandler = object : BaseErrorHandler() {
        override suspend fun FailedErr(tmp: RawResponse.Error) {
            //网络请求非200 http code(不是接口返回数据自定义code)，可以获取异常信息
        }

        override suspend fun <T> OnSuccess(tmp: RawResponse.Success<T>) {
            //转换成baseData,获取code和msg
            println((tmp.responseData as Data).errorCode)
        }

        override suspend fun ExceptionErr(tmp: RawResponse.Error) {
            //网络请求出错，可以获取异常信息
        }
    }

    suspend fun getData(): Resource2<FriendData> {
        return handleApi2(mainApi.getData(), errorHandler = globalErrHandler)
    }

}