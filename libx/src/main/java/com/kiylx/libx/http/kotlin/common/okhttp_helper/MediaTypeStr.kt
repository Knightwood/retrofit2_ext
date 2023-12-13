package com.kiylx.libx.http.kotlin.common.okhttp_helper
/**
 * 常见的post请求头
 */
class PostRequestHeaderStr {
    companion object {
        const val www_form_urlencoded = "application/x-www-form-urlencoded"
        const val form_data: String = "multipart/form-data"
        const val application_json: String = "application/json"
    }
}

/**
 * 常用的mediaType 字符串
 */
class MediaTypeStr {
    companion object {
        //application开头的
        const val application_json: String = "application/json"//json字符串
        const val application_xml = "application/xml"
        const val application_pdf = "application/pdf"
        const val application_word = "application/msword"
        const val application_stream = "application/octet-stream" //二进制流
        const val application_urlencoded = "application/x-www-form-urlencoded" //表单发送默认格式
        const val application_zip = "application/zip"
        const val application_epub = "application/epub+zip"
        const val application_7zip = "application/x-7z-compressed"
        const val application_rar = "application/x-rar-compressed"

        //文本
        const val text: String = "text/*"
        const val text_html: String = "text/html"// HTML格式
        const val text_plain: String = "text/plain"//纯文本格式
        const val text_xml: String = "text/xml"// XML格式

        //图像
        const val image: String = "image/*"//图片类型，不区分子类型
        const val png = "image/png" //png图片格式
        const val jpeg = "image/jpeg" //jpg图片格式
        const val gif = "image/gif" //gif图片格式

        //媒体
        //音频
        const val audio = "audio/*"
        const val audio_wav="audio/x-wav"
        const val audio_m3u="audio/x-mpegurl"
        const val audio_aac="audio/x-aac"
        const val audio_mp4="audio/mp4"
        const val audio_mid="audio/midi"

        //视频
        const val video = "video/*"
        const val video_flv="video/x-flv"
        const val video_mp4="video/mp4"
        const val video_mpeg="video/mpeg"
        const val video_h264="video/h264"
    }
}
