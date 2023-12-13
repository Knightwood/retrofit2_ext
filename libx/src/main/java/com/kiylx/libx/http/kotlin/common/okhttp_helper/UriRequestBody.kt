package com.kiylx.libx.http.kotlin.common.okhttp_helper

import android.content.ContentResolver
import android.content.res.AssetFileDescriptor
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.kiylx.libx.http.kotlin.failureThen
import com.kiylx.libx.http.kotlin.runCatchingChain
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.FileNotFoundException
import java.io.InputStream

fun Uri.getContentType(contentResolver: ContentResolver): MediaType? =
    contentResolver.getType(this)?.toMediaTypeOrNull()

/**
 *  It supports file/content/mediaStore/asset URIs. asset not tested
 */
fun Uri.createAssetFileDescriptor(contentResolver: ContentResolver): AssetFileDescriptor? {
    return contentResolver.openAssetFileDescriptor(this, "r")
}

/** It supports file/content/mediaStore URIs.
 * Will not work with providers that return sub-sections of files
 */
fun Uri.createParcelFileDescriptor(contentResolver: ContentResolver): ParcelFileDescriptor? {
    return contentResolver.openFileDescriptor(this, "r")
}

/** - It supports file/content/mediaStore/asset URIs. asset not tested
 * - When file URI is used, may get contentLength error (expected x but got y)
 *  error when uploading if contentLength header is filled from assetFileDescriptor.length */
fun Uri.createInputStreamFromContentResolver(contentResolver: ContentResolver): InputStream? {
    return contentResolver.openInputStream(this)
}

/**
 * - 媒体存储（MediaStore）：是一个系统级别的媒体数据库，提供了对设备上的媒体文件的访问。
 *  应用程序可以使用MediaStore API查询和检索设备上的媒体文件，如照片、视频和音频。
 * - 文件和文件提供者（File and File Provider）：文件和文件提供者API允许应用程序访问设备上的其他类型的文件，
 * 如文档、PDF、电子书等。应用程序可以使用文件和文件提供者API来查询、检索和操作这些文件。
 *  他们都需要与ContentProvider交互，还有FileProvider，他是ContentProvider子类，
 *  因此上述内容产生的uri本质一样，用法一样，都需要与ContentProvider交互
 *
 *
 * 通过uri打包成part
 * @param contentResolver
 * @param unknownLength true:长度不定
 *
 * 使用：例如文件上传
 *
 * api:
 * ```
 * import okhttp3.MultipartBody
 * import retrofit2.http.*
 *
 * @Multipart
 * @POST("/api/uploadImage")
 * suspend fun uploadReturnCertificateImage(@Part data: List<MultipartBody.Part>): ApiResponse<String>
 * ```
 *
 * repo:
 * ```
 * import android.content.Context
 * import android.net.Uri
 * import okhttp3.MultipartBody.Part.Companion.createFormData
 *
 * val uri: Uri = parameters.imageUri
 * val fileName = context.getFileName(uri)
 * val requestBody = uri.asRequestBody(context.contentResolver)
 *
 * uploadReturnCertificateImage(listOf(
 *             createFormData("other_param1", "xxxx"),
 *             createFormData("other_param2", "xxxx"),
 *             createFormData("image", fileName, requestBody)
 *         ))
 * ```
 */
fun Uri.asRequestBody(
    contentResolver: ContentResolver,
    unknownLength: Boolean = false,
): RequestBody {

    return object : RequestBody() {
        /** If null is given, it is binary for Streams */
        override fun contentType() = getContentType(contentResolver)

        /** 'chunked' transfer encoding will be used for big files when length not specified */
        override fun contentLength() =
            if (unknownLength) -1 else countBytes()

        /** This may get called twice if HttpLoggingInterceptor is used */
        override fun writeTo(sink: BufferedSink) {
            runCatchingChain {
                val parcelFileDescriptor = createParcelFileDescriptor(contentResolver)!!
                // when InputStream is closed, it auto closes ParcelFileDescriptor
                ParcelFileDescriptor.AutoCloseInputStream(parcelFileDescriptor)
                    .source()
                    .use { source ->
                        sink.writeAll(source)
                    }
            }.failureThen {
                val inputStream = createInputStreamFromContentResolver(contentResolver)!!
                inputStream.source()
                    .use { source ->
                        sink.writeAll(source)
                    }
            }.getOrThrow()

        }

        /**
         * count file length
         */
        private fun countBytes(): Long {
            val bytesCount: Long = runCatchingChain<Long> {
                val parcelFileDescriptor = createParcelFileDescriptor(contentResolver)!!
                parcelFileDescriptor.statSize.toLong()
            }.failureThen<Long> {
                val inputStream = createInputStreamFromContentResolver(contentResolver)!!
                inputStream.available().toLong()
            }.getOrThrow()
            return bytesCount
        }

    }
}

/**
 * 作用同[Uri.asRequestBody]，但仅用于asset文件生成RequestBody
 * 因为凡是文件生成RequestBody，绝大多数情况都不会用在apk本身的asset文件上。
 * 因此，将uri转成RequestBody分为了这两个方法
 */
fun Uri.assetFileAsRequestBody(
    contentResolver: ContentResolver,
    unknownLength: Boolean = false,
): RequestBody {
    val TAG = "assetFileAsRequestBody"
    return object : RequestBody() {
        /** If null is given, it is binary for Streams */
        override fun contentType() = getContentType(contentResolver)

        /** 'chunked' transfer encoding will be used for big files when length not specified */
        override fun contentLength() =
            if (unknownLength) -1 else countBytes()

        /** This may get called twice if HttpLoggingInterceptor is used */
        override fun writeTo(sink: BufferedSink) {
            val assetFileDescriptor = createAssetFileDescriptor(contentResolver)!!
            // when InputStream is closed, it auto closes AssetFileDescriptor
            AssetFileDescriptor.AutoCloseInputStream(assetFileDescriptor)
                .source()
                .use { source ->
                    sink.writeAll(source)
                }
        }

        /**
         * count file length
         */
        private fun countBytes(): Long {
            val assetFileDescriptor = createAssetFileDescriptor(contentResolver)
            if (assetFileDescriptor != null) {
                return assetFileDescriptor.length
            } else {
                throw FileNotFoundException("file not found,can not count length")
            }
        }

    }
}
