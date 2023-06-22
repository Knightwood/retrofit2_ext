package com.kiylx.retrofit2_ext.example;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Date;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 创建者 kiylx
 * 创建时间 2020/10/7 16:43
 * packageName：com.crystal.aplayer.module_base.base.http.okhttp
 * 描述：
 */
public class HeaderInterceptor implements Interceptor {
    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        String userAgent = System.getProperty("http.agent");
        Request original = chain.request();
        Request.Builder request = original.newBuilder();
        request.addHeader("model", "Android")
                .addHeader("If-Modified-Since", String.valueOf(new Date()))
                .addHeader("User-Agent", userAgent != null ? userAgent : "unknown");
        return chain.proceed(request.build());
    }
}
