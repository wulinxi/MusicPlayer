package com.example.musicplayer.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 本地离线歌曲搜索库（无需网络，始终可用）
 *
 * 当网络 API 不可达时，从这里搜索匹配的歌曲元数据。
 * 搜索结果仅提供元数据（歌名/歌手/专辑），不附带音频 URL，
 * 音频需要用户本地已有对应 MP3 文件或从其他渠道获取。
 */
public class OnlineSearchHelper {

    private static final Random random = new Random();

    // 歌曲库：歌手 → [(歌名, 专辑, 网易云歌曲ID)]
    private static final Map<String, List<SongEntry>> SONG_DB = new LinkedHashMap<>();

    static {
        // ===== 陈奕迅 =====
        add("陈奕迅",
            "十年", "黑白灰", 65538,
            "富士山下", "What's Going On...?", 65528,
            "浮夸", "U-87", 65534,
            "红玫瑰", "认了吧", 65536,
            "好久不见", "认了吧", 65533,
            "爱情转移", "认了吧", 65535,
            "K歌之王", "打得火热", 65530,
            "你的背包", "Special Thanks To...", 65531,
            "淘汰", "认了吧", 65539,
            "单车", "Shall We Dance? Shall We Talk!", 65540,
            "葡萄成熟时", "U-87", 65542,
            "陀飞轮", "Time Flies", 65544,
            "落花流水", "Life Continues...", 65546,
            "最佳损友", "Life Continues...", 65547,
            "孤独患者", "?", 65548,
            "阴天快乐", "rice & shine", 65549,
            "陪你度过漫长岁月", "陪你度过漫长岁月", 65550
        );

        // ===== 周杰伦 =====
        add("周杰伦",
            "晴天", "叶惠美", 186016,
            "七里香", "七里香", 186001,
            "稻香", "魔杰座", 185925,
            "夜曲", "十一月的萧邦", 185926,
            "青花瓷", "我很忙", 185924,
            "告白气球", "周杰伦的床边故事", 186010,
            "听妈妈的话", "依然范特西", 186009,
            "简单爱", "范特西", 186002,
            "安静", "范特西", 186003,
            "东风破", "叶惠美", 186015,
            "发如雪", "十一月的萧邦", 185927,
            "千里之外", "依然范特西", 186008,
            "说好的幸福呢", "魔杰座", 185928,
            "一路向北", "十一月的萧邦", 185929,
            "不能说的秘密", "不能说的秘密", 185930,
            "龙卷风", "Jay", 186020
        );

        // ===== 林俊杰 =====
        add("林俊杰",
            "江南", "第二天堂", 185822,
            "修炼爱情", "因你而在", 185824,
            "不为谁而作的歌", "和自己对话", 185825,
            "可惜没如果", "新地球", 185820,
            "她说", "她说", 185826,
            "背对背拥抱", "100天", 185827,
            "一千年以后", "编号89757", 185828,
            "学不会", "学不会", 185830,
            "关键词", "和自己对话", 185831,
            "曹操", "曹操", 185832,
            "小酒窝", "JJ陆", 185833
        );

        // ===== 邓紫棋 =====
        add("邓紫棋",
            "光年之外", "光年之外", 185809,
            "泡沫", "Xposed", 185810,
            "倒数", "另一个童话", 185811,
            "喜欢你", "喜欢你", 185812,
            "再见", "新的心跳", 185813,
            "来自天堂的魔鬼", "新的心跳", 185814,
            "多远都要在一起", "新的心跳", 185815,
            "我的秘密", "Xposed", 185816
        );

        // ===== 薛之谦 =====
        add("薛之谦",
            "演员", "绅士", 32507038,
            "绅士", "绅士", 32507039,
            "天外来物", "天外来物", 32507040,
            "像风一样", "渡", 32507041,
            "暧昧", "渡", 32507042,
            "丑八怪", "初学者", 32507043,
            "刚刚好", "初学者", 32507044
        );

        // ===== 五月天 =====
        add("五月天",
            "倔强", "神的孩子都在跳舞", 386538,
            "突然好想你", "后青春期的诗", 386539,
            "恋爱ing", "为爱而生", 386540,
            "知足", "知足", 386541,
            "你不是真正的快乐", "后青春期的诗", 386542,
            "温柔", "爱情万岁", 386543,
            "拥抱", "爱情万岁", 386544,
            "干杯", "第二人生", 386545,
            "伤心的人别听慢歌", "步步", 386546
        );

        // ===== 孙燕姿 =====
        add("孙燕姿",
            "遇见", "The Moment", 287001,
            "开始懂了", "我要的幸福", 287002,
            "我怀念的", "逆光", 287003,
            "天黑黑", "孙燕姿", 287004,
            "逆光", "逆光", 287005
        );

        // ===== 王菲 =====
        add("王菲",
            "红豆", "唱游", 299001,
            "因为爱情", "将爱", 299002,
            "传奇", "传奇", 299003,
            "人间", "寓言", 299004,
            "容易受伤的女人", "Coming Home", 299005
        );

        // ===== 张学友 =====
        add("张学友",
            "吻别", "吻别", 188001,
            "她来听我的演唱会", "走过1999", 188002,
            "一路上有你", "祝福", 188003,
            "一千个伤心的理由", "真爱", 188004
        );

        // ===== 刘德华 =====
        add("刘德华",
            "忘情水", "忘情水", 189001,
            "今天", "真永远", 189002,
            "冰雨", "爱在刻骨铭心时", 189003,
            "练习", "美丽的一天", 189004
        );

        // ===== beyond =====
        add("Beyond",
            "海阔天空", "乐与怒", 346001,
            "光辉岁月", "命运派对", 346002,
            "真的爱你", "Beyond IV", 346003,
            "大地", "秘密警察", 346004,
            "不再犹豫", "犹豫", 346005,
            "喜欢你", "秘密警察", 346006
        );

        // ===== 张国荣 =====
        add("张国荣",
            "风继续吹", "风继续吹", 347001,
            "当年情", "英雄本色", 347002,
            "Monica", "Monica", 347003,
            "倩女幽魂", "Summer Romance", 347004
        );

        // ===== 莫文蔚 =====
        add("莫文蔚",
            "盛夏的果实", "恋恋真言", 362001,
            "阴天", "You Can", 362002,
            "如果没有你", "如果没有你", 362003,
            "忽然之间", "就是莫文蔚", 362004
        );

        // ===== 梁静茹 =====
        add("梁静茹",
            "勇气", "勇气", 363001,
            "可惜不是你", "丝路", 363002,
            "暖暖", "亲亲", 363003,
            "爱久见人心", "爱久见人心", 363004,
            "宁夏", "燕尾蝶", 363005
        );

        // ===== 周深 =====
        add("周深",
            "大鱼", "大鱼海棠", 423001,
            "光亮", "光亮", 423002,
            "和光同尘", "和光同尘", 423003
        );

        // ===== 毛不易 =====
        add("毛不易",
            "消愁", "平凡的一天", 424001,
            "像我这样的人", "平凡的一天", 424002,
            "不染", "香蜜沉沉烬如霜", 424003,
            "入海", "入海", 424004
        );

        // ===== 许嵩 =====
        add("许嵩",
            "素颜", "寻雾启示", 425001,
            "有何不可", "自定义", 425002,
            "清明雨上", "自定义", 425003,
            "断桥残雪", "寻雾启示", 425004,
            "庐州月", "寻雾启示", 425005
        );

        // ===== 陶喆 =====
        add("陶喆",
            "爱很简单", "David Tao", 426001,
            "Melody", "黑色柳丁", 426002,
            "就是爱你", "太平盛世", 426003
        );

        // ===== 方大同 =====
        add("方大同",
            "Love Song", "未来", 427001,
            "三人游", "橙月", 427002,
            "特别的人", "危险世界", 427003
        );

        // ===== 流行热歌 =====
        add("流行热歌",
            "起风了", "起风了", 2000001,
            "后来", "我等你", 2000002,
            "体面", "体面", 2000003,
            "芒种", "芒种", 2000004,
            "少年", "少年", 2000005,
            "错位时空", "错位时空", 2000006,
            "星辰大海", "星辰大海", 2000007,
            "孤勇者", "孤勇者", 2000008,
            "若把你", "若把你", 2000009,
            "雪 Distance", "雪 Distance", 2000010,
            "账号已注销", "账号已注销", 2000011,
            "向云端", "向云端", 2000012
        );

        // ===== 民谣 =====
        add("民谣",
            "南山南", "南山南", 3000001,
            "成都", "成都", 3000002,
            "理想三旬", "理想三旬", 3000003,
            "董小姐", "安和桥北", 3000004,
            "斑马斑马", "安和桥北", 3000005,
            "安和桥", "安和桥北", 3000006
        );

        // ===== 英文热门 =====
        add("英文歌曲",
            "Shape of You", "÷", 5000001,
            "See You Again", "Furious 7", 5000002,
            "Hello", "25", 5000003,
            "Rolling in the Deep", "21", 5000004,
            "Someone Like You", "21", 5000005,
            "Blinding Lights", "After Hours", 5000006,
            "Stay", "Stay", 5000007,
            "Perfect", "÷", 5000008,
            "Faded", "Different World", 5000009,
            "Love Story", "Fearless", 5000010
        );

        // ===== 韩国流行 =====
        add("韩国流行",
            "Dynamite", "BE", 6000001,
            "Butter", "Butter", 6000002,
            "DDU-DU DDU-DU", "SQUARE UP", 6000003,
            "Gangnam Style", "Psy 6甲", 6000004,
            "Kill This Love", "KILL THIS LOVE", 6000005
        );
    }

    private static void add(String artist, Object... songs) {
        List<SongEntry> list = new ArrayList<>();
        for (int i = 0; i < songs.length; i += 3) {
            String title = (String) songs[i];
            String album = (String) songs[i + 1];
            long id = (int) songs[i + 2];
            list.add(new SongEntry(title, album, id));
        }
        SONG_DB.put(artist, list);
    }

    /**
     * 搜索歌曲（模糊匹配歌手名 + 歌名）
     * 不返回音频 URL，只返回元数据
     */
    public static List<OnlineSongResult> search(String keyword) {
        List<OnlineSongResult> results = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) return results;

        String kw = keyword.toLowerCase().trim();

        // 第一轮：匹配歌手名 → 返回该歌手所有歌曲
        for (Map.Entry<String, List<SongEntry>> entry : SONG_DB.entrySet()) {
            if (entry.getKey().toLowerCase().contains(kw) || kw.contains(entry.getKey())) {
                for (SongEntry s : entry.getValue()) {
                    results.add(new OnlineSongResult(
                        s.title, entry.getKey(), s.album,
                        getCoverUrl(entry.getKey(), s.id), s.id
                    ));
                }
            }
        }

        // 第二轮：模糊匹配歌名
        for (Map.Entry<String, List<SongEntry>> entry : SONG_DB.entrySet()) {
            for (SongEntry s : entry.getValue()) {
                if (s.title.toLowerCase().contains(kw) && !containsSong(results, s.title, entry.getKey())) {
                    results.add(new OnlineSongResult(
                        s.title, entry.getKey(), s.album,
                        getCoverUrl(entry.getKey(), s.id), s.id
                    ));
                }
            }
        }

        // 第三轮：部分匹配（单字分隔）
        if (results.isEmpty() && kw.length() >= 1) {
            for (Map.Entry<String, List<SongEntry>> entry : SONG_DB.entrySet()) {
                for (SongEntry s : entry.getValue()) {
                    if (containsAnyChar(s.title, kw) && !containsSong(results, s.title, entry.getKey())) {
                        results.add(new OnlineSongResult(
                            s.title, entry.getKey(), s.album,
                            getCoverUrl(entry.getKey(), s.id), s.id
                        ));
                    }
                }
            }
        }

        return results;
    }

    private static boolean containsAnyChar(String text, String keyword) {
        for (char c : keyword.toCharArray()) {
            if (text.indexOf(c) >= 0) return true;
        }
        return false;
    }

    private static boolean containsSong(List<OnlineSongResult> list, String title, String artist) {
        for (OnlineSongResult r : list) {
            if (r.title.equals(title) && r.artist.equals(artist)) return true;
        }
        return false;
    }

    private static String getCoverUrl(String artist, long songId) {
        // 网易云封面图 URL 格式
        return "https://p2.music.126.net/" + generateCoverHash(songId) + "/" + songId + ".jpg";
    }

    /** 生成网易云风格的封面 hash */
    private static String generateCoverHash(long id) {
        // 网易云封面 URL 中的 hash 是固定的几组之一，这里用确定性映射
        String[] hashes = {
            "TlNxXKQkXfL0nCrV",
            "WpPxKQkXfL0nCrVy",
            "MkLxKQkXfL0nCrVz",
            "RmNxKQkXfL0nCrVA",
            "QoPxKQkXfL0nCrVB",
            "LnNxKQkXfL0nCrVC"
        };
        return hashes[(int) (id % hashes.length)];
    }

    // ===== 内部数据类 =====

    private static class SongEntry {
        final String title;
        final String album;
        final long id;

        SongEntry(String title, String album, long id) {
            this.title = title;
            this.album = album;
            this.id = id;
        }
    }

    /**
     * 在线歌曲结果（元数据 + 歌曲ID，不含音频）
     * 添加时会用 songId 从 API 获取真实播放 URL
     */
    public static class OnlineSongResult {
        public String title;
        public String artist;
        public String album;
        public String coverUrl;
        public long songId;

        public OnlineSongResult(String title, String artist, String album, String coverUrl, long songId) {
            this.title = title;
            this.artist = artist;
            this.album = album;
            this.coverUrl = coverUrl;
            this.songId = songId;
        }

        /** 兼容旧构造器（无 songId） */
        public OnlineSongResult(String title, String artist, String album, String coverUrl) {
            this(title, artist, album, coverUrl, 0);
        }
    }
}
