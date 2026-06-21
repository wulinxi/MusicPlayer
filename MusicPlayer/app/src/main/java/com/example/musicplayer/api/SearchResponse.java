package com.example.musicplayer.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 音乐搜索响应模型（匹配网易云 API 格式）
 */
public class SearchResponse {

    public int code;

    public Result result;

    public static class Result {
        public List<Song> songs;
        @SerializedName("songCount")
        public int songCount;
    }

    public static class Song {
        public long id;
        public String name;
        public List<Artist> ar;
        public Album al;
        public int dt; // 时长（毫秒）

        public String getArtistName() {
            if (ar != null && ar.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (Artist a : ar) {
                    if (sb.length() > 0) sb.append("/");
                    sb.append(a.name);
                }
                return sb.toString();
            }
            return "未知歌手";
        }

        public String getAlbumName() {
            return (al != null && al.name != null) ? al.name : "未知专辑";
        }
    }

    public static class Artist {
        public String name;
    }

    public static class Album {
        public String name;
        @SerializedName("picUrl")
        public String picUrl;
    }
}
