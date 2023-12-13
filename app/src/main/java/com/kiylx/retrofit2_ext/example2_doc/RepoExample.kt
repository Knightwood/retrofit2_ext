package com.kiylx.retrofit2_ext.example2_doc

import com.kiylx.libx.http.kotlin.basic.Resource
import com.kiylx.libx.http.kotlin.basic.handleApi
import com.kiylx.libx.http.kotlin.basic2.Resource2
import com.kiylx.libx.http.kotlin.basic2.handleApi2
import com.kiylx.libx.http.kotlin.basic3.handleApi3
import com.kiylx.libx.http.kotlin.common.*
import com.kiylx.libx.http.kotlin.common.okhttp_helper.MediaTypeStr
import com.kiylx.libx.http.kotlin.common.okhttp_helper.OkhttpPartHelper
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.kiylx.retrofit2_ext.example2_doc.bean.BaseData
import com.kiylx.retrofit2_ext.example2_doc.bean.User
import com.kiylx.retrofit2_ext.example2_doc.bean.User2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File


class RepoExample {
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
            println((tmp.responseData as BaseData).code)
        }

        override suspend fun ExceptionErr(tmp: RawResponse.Error) {
            //网络请求出错，可以获取异常信息
        }
    }

    //<editor-fold desc="get请求">
    suspend fun login(): Resource<User> {
        return handleApi(mainApi.login("token iiiiiii"), errorHandler = globalErrHandler)
    }

    //</editor-fold>

    //<editor-fold desc="post json">
    /**
     * post json请求
     * 使用现有的entity版
     */
    suspend fun placeUpload1(): RawResponse<User> {
        return handleApi3(
            mainApi.placeUpload1(
                User(200, "msg", "data")
            )
        )
    }

    /**
     * post json请求
     * 自己构造body版本
     */
    suspend fun placeUpload2(): RawResponse<User> {
        val body = buildJsonObject {
            put("data", "data")
            put("username", "username")
        }
        return handleApi3(
            mainApi.placeUpload2(
                body.toString().toRequestBody("application/json;".toMediaTypeOrNull())
            )
        )
    }

    //</editor-fold>

    //<editor-fold desc="post 带文件">

    suspend fun uploadVoiceMsg(filePath: String) {
        val file = File(filePath)
        var part: MultipartBody.Part? = null
        if (file.exists()) {
            part =
                OkhttpPartHelper.filePackToPart(file, MediaTypeStr.image, "img")
        }

        handleApi3(
            mainApi.uploadVoiceMsg(
                12,
                "username",
                "xx发送一段消息，请注意查收~",
                part
            )
        )
    }


    suspend fun upload(): Resource2<User2> = withContext(Dispatchers.IO) {
        val file = File("")//假设有一个文件
        //手动创建一个MultipartBody.Part
        // create RequestBody instance from file
        val requestFile: RequestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        // MultipartBody.Part is used to send also the actual file name
        val body = MultipartBody.Part.createFormData("picture", file.getName(), requestFile)

        //或者用此方法生成MultipartBody.Part
        val body2 = OkhttpPartHelper.filePackToPart(file, MediaTypeStr.image, "pic")

        // add another part within the multipart request
        val descriptionString = "hello, this is description speaking"

        val description: RequestBody = descriptionString.toRequestBody()
        return@withContext handleApi2(mainApi.post3(description, body))
    }

    //</editor-fold>


}
