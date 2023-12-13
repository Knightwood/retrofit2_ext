package com.kiylx.libx.http.kotlin.common.okhttp_helper

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * MultipartBody继承自RequestBody
 * RequestBody是一个抽象类，有个writeTo抽象方法，不论是字符串还是文件，都是通过实现的writeTo方法写入okio实现功能。
 *
 * MultipartBody.Part是MultipartBody的内部类，其持有RequestBody和headers。
 * 构造MultipartBody.Part的过程中，[MultipartBody.Part.createFormData]函数需要header和RequestBody实现类。
 * 而生成RequestBody的方法，例如
 * [String.toRequestBody]、[File.asRequestBody]等都是生成一个匿名类继承RequestBody，
 * 将字符串或文件写入okio，所以字符串或文件生成RequestBody本质上是一样的。
 *
 */
object OkhttpPartHelper {

    /**
     * 将文件打包成MultipartBody.Part
     * @param data 文件
     * @param mediaType 文件的媒体类型
     * @param fieldName 参数名称
     */
    fun filePackToPart(
        data: File,
        mediaType: String,
        fieldName: String,
    ): MultipartBody.Part {
        return MultipartBody.Part.createFormData(
            fieldName,
            filename = data.name,
            data.asRequestBody(mediaType.toMediaTypeOrNull())
        )
    }

    fun stringPackToPart(
        data: String,
        fieldName: String,
    ): MultipartBody.Part {
        return MultipartBody.Part.createFormData(
            fieldName,
            data
        )
    }

    fun jsonPackToPart(
        data: String,
        fieldName: String,
    ): MultipartBody.Part {
        val entityPart = MultipartBody.Part.createFormData(
            fieldName,
            null,
            data.toRequestBody(MediaTypeStr.application_json.toMediaTypeOrNull())
        )
        return entityPart
    }

    /**
     * @param uri contentProvider提供的文件uri，例如saf选择的文件，mediaStore查询出来的文件，fileProvider提供的文件等
     * @param fieldName 将在 http请求体的 "form-data; name="后拼接上，不是文件名。
     * @param contentResolver contentResolver
     * @param unknownLength 文件长度是否未知
     */
    fun uriPackToPart(
        uri: Uri,
        fieldName: String,
        contentResolver: ContentResolver,
        unknownLength: Boolean = false,
    ): MultipartBody.Part {
        val entityPart = MultipartBody.Part.createFormData(
            fieldName,
            uriToFileName(uri, contentResolver),
            uri.asRequestBody(contentResolver, unknownLength)
        )
        return entityPart
    }

    @SuppressLint("Range")
    fun uriToFileName(
        uri: Uri,
        contentResolver: ContentResolver,
    ): String {
        return when (uri.scheme) {
            ContentResolver.SCHEME_FILE -> uri.toFile().name
            ContentResolver.SCHEME_CONTENT -> {
                val cursor = contentResolver.query(uri, null, null, null, null, null)
                cursor?.let {_cursor->
                    kotlin.runCatching {
                        _cursor.moveToFirst()
                        val displayName = _cursor.getString(_cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                        _cursor.close()
                        displayName
                    }.getOrNull()
                } ?: "${System.currentTimeMillis()}.${
                    MimeTypeMap.getSingleton()
                        .getExtensionFromMimeType(contentResolver.getType(uri))
                }}"

            }

            else -> "${System.currentTimeMillis()}.${
                MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri))
            }}"
        }
    }

}