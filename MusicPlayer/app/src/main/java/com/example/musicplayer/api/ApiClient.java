package com.example.musicplayer.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit 网络客户端 → 连接本地音乐 API 服务器
 */
public class ApiClient {

    // 本地 API 服务器地址（D:/music-api-server/server.js）
    // 模拟器用 10.0.2.2 访问本机 localhost
    private static final String BASE_URL = "http://10.0.2.2:3000/";

    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static MusicApiService getMusicApi() {
        return getClient().create(MusicApiService.class);
    }
}
