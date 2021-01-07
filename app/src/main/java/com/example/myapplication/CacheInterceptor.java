package com.example.myapplication;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Response;

public class CacheInterceptor implements Interceptor {
    @NotNull
    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());

        CacheControl cacheControl = new CacheControl.Builder()
                .maxAge(15, TimeUnit.MINUTES) // 15 minutes cache
                .build();

        return response.newBuilder()
                .removeHeader("Pragma")
                .removeHeader("Cache-Control")
                .header("Cache-Control", cacheControl.toString())
                .build();
    }
}
