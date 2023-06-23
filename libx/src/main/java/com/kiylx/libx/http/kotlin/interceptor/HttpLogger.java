package com.kiylx.libx.http.kotlin.interceptor;


import android.util.Log;
import okhttp3.logging.HttpLoggingInterceptor;

//打印okhttp请求
public class HttpLogger implements HttpLoggingInterceptor.Logger {
    private StringBuilder mMessage = new StringBuilder();
    private static final String TAG = "tty2-HttpLogger";

    public static HttpLoggingInterceptor newInstance() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLogger());
        httpLoggingInterceptor.level(HttpLoggingInterceptor.Level.BODY);
        return httpLoggingInterceptor;
    }

    @Override
    public void log(String message) {
        // 请求或者响应开始
        if (message.startsWith("--> POST")) {
            mMessage.setLength(0);
        }
        // 以{}或者[]形式的说明是响应结果的json数据，需要进行格式化
        if ((message.startsWith("{") && message.endsWith("}"))
                || (message.startsWith("[") && message.endsWith("]"))) {
            message = JsonUtil.formatJson(message);
        }
        mMessage.append(message.concat("\n"));
        // 请求或者响应结束，打印整条日志
        if (message.startsWith("<-- END HTTP")) {
            Log.d(TAG, mMessage.toString());
        }
    }
}