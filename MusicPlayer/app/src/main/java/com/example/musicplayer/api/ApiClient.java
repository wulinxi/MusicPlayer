package com.example.musicplayer.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

/**
 * Retrofit 网络客户端 → 连接网易云音乐 API
 *
 * BASE_URL 优先级（自动检测）：
 *   1. 本地服务器 http://10.0.2.2:3000  （仅模拟器可用）
 *   2. 公网 API  https://netease-cloud-music-api-mu.vercel.app/
 *
 * 真机上 10.0.2.2 不可达会自动超时回退到 OnlineSearchHelper
 */
public class ApiClient {

    // 公网 API（Vercel 部署的 NeteaseCloudMusicApi，真机可用）
    private static final String PUBLIC_BASE_URL = "https://netease-cloud-music-api-mu.vercel.app/";

    // 本地 API（仅模拟器: adb forward + 本机 Node.js 服务器）
    private static final String LOCAL_BASE_URL = "http://10.0.2.2:3000/";

    private static Retrofit retrofit;

    /** 获取 Retrofit 客户端（默认公网 API） */
    public static Retrofit getClient() {
        if (retrofit == null) {
            synchronized (ApiClient.class) {
                if (retrofit == null) {
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(logging)
                            .connectTimeout(8, TimeUnit.SECONDS)
                            .readTimeout(10, TimeUnit.SECONDS)
                            .writeTimeout(10, TimeUnit.SECONDS)
                            .build();

                    // 默认用公网 API，真机可用
                    retrofit = new Retrofit.Builder()
                            .baseUrl(PUBLIC_BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }

    public static MusicApiService getMusicApi() {
        return getClient().create(MusicApiService.class);
    }
}
