package com.ktc.ecuador.utils;


import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitFactory {
    private static RetrofitFactory instance;
    private Retrofit mRetrofit;

    private RetrofitFactory(String baseUrl) {
        mRetrofit = new Retrofit.Builder().baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(initClient()).build();
    }

    private OkHttpClient initClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(initLogInterceptor())
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    private Interceptor initLogInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return interceptor;
    }

    public static RetrofitFactory getInstance(String baseUrl) {
        if (instance == null) {
            instance = new RetrofitFactory(baseUrl);
        }
        return instance;
    }

    public <T> T create(Class<T> service) {
        return mRetrofit.create(service);
    }
}
