package com.example.musicplayer.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * 音乐搜索 API 接口（Retrofit）
 * 连接本地 Node.js 服务器或网易云 API
 */
public interface MusicApiService {

    @GET("search")
    Call<SearchResponse> searchSongs(
            @Query("keywords") String keywords,
            @Query("limit") int limit
    );

    @GET("song/url")
    Call<SongUrlResponse> getSongUrl(@Query("id") long id);

    @GET("song/detail")
    Call<SongDetailResponse> getSongDetail(@Query("ids") String ids);

    /** 歌曲 URL 响应 */
    class SongUrlResponse {
        public java.util.List<UrlData> data;
        public int code;

        public static class UrlData {
            public long id;
            public String url;
            public int br;
            public String type;
        }
    }

    /** 歌曲详情响应 */
    class SongDetailResponse {
        public java.util.List<SearchResponse.Song> songs;
        public int code;
    }
}
