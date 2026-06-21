package com.example.musicplayer.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 在线搜索模拟器 —— 模拟网易云音乐搜索结果
 * 实际接入 API 只需替换此类
 */
public class OnlineSearchHelper {

    private static final Random random = new Random();

    // 模拟歌曲库（歌手 + 歌曲名）
    private static final Map<String, List<String[]>> SONG_DB = new HashMap<>();

    static {
        SONG_DB.put("陈奕迅", Arrays.asList(
            new String[]{"十年", "黑白灰", "https://music.163.com/song?id=65538"},
            new String[]{"富士山下", "What's Going On...?", "https://music.163.com/song?id=65528"},
            new String[]{"浮夸", "U-87", "https://music.163.com/song?id=65534"},
            new String[]{"红玫瑰", "认了吧", "https://music.163.com/song?id=65536"},
            new String[]{"好久不见", "认了吧", "https://music.163.com/song?id=65533"},
            new String[]{"爱情转移", "认了吧", "https://music.163.com/song?id=65535"},
            new String[]{"K歌之王", "打得火热", "https://music.163.com/song?id=65530"}
        ));
        SONG_DB.put("周杰伦", Arrays.asList(
            new String[]{"晴天", "叶惠美", "https://music.163.com/song?id=186016"},
            new String[]{"七里香", "七里香", "https://music.163.com/song?id=186001"},
            new String[]{"稻香", "魔杰座", "https://music.163.com/song?id=185925"},
            new String[]{"夜曲", "十一月的萧邦", "https://music.163.com/song?id=185926"},
            new String[]{"青花瓷", "我很忙", "https://music.163.com/song?id=185924"},
            new String[]{"告白气球", "周杰伦的床边故事", "https://music.163.com/song?id=186010"}
        ));
        SONG_DB.put("林俊杰", Arrays.asList(
            new String[]{"江南", "第二天堂", "https://music.163.com/song?id=185822"},
            new String[]{"修炼爱情", "因你而在", "https://music.163.com/song?id=185824"},
            new String[]{"不为谁而作的歌", "和自己对话", "https://music.163.com/song?id=185825"},
            new String[]{"可惜没如果", "新地球", "https://music.163.com/song?id=185820"}
        ));
        SONG_DB.put("邓紫棋", Arrays.asList(
            new String[]{"光年之外", "光年之外", "https://music.163.com/song?id=185809"},
            new String[]{"泡沫", "Xposed", "https://music.163.com/song?id=185810"},
            new String[]{"倒数", "另一个童话", "https://music.163.com/song?id=185811"}
        ));
        SONG_DB.put("薛之谦", Arrays.asList(
            new String[]{"演员", "绅士", "https://music.163.com/song?id=32507038"},
            new String[]{"绅士", "绅士", "https://music.163.com/song?id=32507039"},
            new String[]{"天外来物", "天外来物", "https://music.163.com/song?id=32507040"},
            new String[]{"像风一样", "渡", "https://music.163.com/song?id=32507041"}
        ));
        SONG_DB.put("五月天", Arrays.asList(
            new String[]{"倔强", "神的孩子都在跳舞", "https://music.163.com/song?id=386538"},
            new String[]{"突然好想你", "后青春期的诗", "https://music.163.com/song?id=386539"},
            new String[]{"恋爱ing", "为爱而生", "https://music.163.com/song?id=386540"}
        ));
        // 流行
        SONG_DB.put("流行", Arrays.asList(
            new String[]{"起风了", "起风了", ""},
            new String[]{"后来", "我等你", ""},
            new String[]{"体面", "体面", ""},
            new String[]{"芒种", "芒种", ""},
            new String[]{"少年", "少年", ""}
        ));
        // 摇滚
        SONG_DB.put("摇滚", Arrays.asList(
            new String[]{"海阔天空", "乐与怒", ""},
            new String[]{"光辉岁月", "命运派对", ""},
            new String[]{"不再犹豫", "犹豫", ""}
        ));
        // 民谣
        SONG_DB.put("民谣", Arrays.asList(
            new String[]{"南山南", "南山南", ""},
            new String[]{"成都", "成都", ""},
            new String[]{"理想三旬", "理想三旬", ""}
        ));
        // 电子
        SONG_DB.put("电子", Arrays.asList(
            new String[]{"Faded", "Different World", ""},
            new String[]{"Alone", "Alone", ""}
        ));
    }

    /**
     * 模拟在线搜索
     */
    public static List<OnlineSongResult> search(String keyword) {
        List<OnlineSongResult> results = new ArrayList<>();
        String kw = keyword.toLowerCase().trim();

        // 精确匹配歌手
        for (Map.Entry<String, List<String[]>> entry : SONG_DB.entrySet()) {
            if (entry.getKey().contains(keyword) || keyword.contains(entry.getKey())) {
                for (String[] info : entry.getValue()) {
                    results.add(new OnlineSongResult(
                        info[0], entry.getKey(), info[1],
                        "https://music.163.com/song?id=" + random.nextInt(999999)
                    ));
                }
            }
        }

        // 模糊匹配歌名
        for (Map.Entry<String, List<String[]>> entry : SONG_DB.entrySet()) {
            for (String[] info : entry.getValue()) {
                if (info[0].toLowerCase().contains(kw) && !containsResult(results, info[0], entry.getKey())) {
                    results.add(new OnlineSongResult(
                        info[0], entry.getKey(), info[1],
                        "https://music.163.com/song?id=" + random.nextInt(999999)
                    ));
                }
            }
        }

        return results;
    }

    private static boolean containsResult(List<OnlineSongResult> list, String title, String artist) {
        for (OnlineSongResult r : list) {
            if (r.title.equals(title) && r.artist.equals(artist)) return true;
        }
        return false;
    }

    // 公开免费 mp3 测试音源（稳定可播）
    private static final String[] DEMO_MP3_URLS = {
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3"
    };

    private static int urlIndex = 0;

    /** 获取一个可播放的 demo mp3 地址 */
    private static String getDemoMp3Url() {
        String url = DEMO_MP3_URLS[urlIndex % DEMO_MP3_URLS.length];
        urlIndex++;
        return url;
    }

    /**
     * 在线歌曲结果
     */
    public static class OnlineSongResult {
        public String title;
        public String artist;
        public String album;
        public String url;
        public String coverUrl;

        public OnlineSongResult(String title, String artist, String album, String url) {
            this.title = title;
            this.artist = artist;
            this.album = album;
            this.url = (url != null && !url.isEmpty()) ? url : getDemoMp3Url();
        }
    }
}
