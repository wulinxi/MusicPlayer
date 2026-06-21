package com.example.musicplayer.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 歌词工具类 —— 简单的歌词时间轴匹配
 */
public class LyricsHelper {

    // 预置歌词（时间秒 -> 歌词行）
    private static final Map<String, LinkedHashMap<Integer, String>> LYRICS_MAP = new LinkedHashMap<>();

    static {
        // 孤独患者
        LinkedHashMap<Integer, String> gd = new LinkedHashMap<>();
        gd.put(0,  "孤独患者 - 陈奕迅");
        gd.put(12, "我不唱声嘶力竭的情歌");
        gd.put(17, "不表示没有心碎的时刻");
        gd.put(22, "我不曾摊开伤口任宰割");
        gd.put(27, "愈合 就无人晓得 我内心挫折");
        gd.put(35, "活像个孤独患者 自我拉扯");
        gd.put(41, "外向的孤独患者 有何不可");
        gd.put(50, "我不要声嘶力竭的情歌");
        gd.put(55, "来提示我需要你的时刻");
        gd.put(60, "表面镇定并不是保护色");
        gd.put(65, "反而是要你懂得 我不知为何");
        gd.put(73, "活像个孤独患者 自我拉扯");
        gd.put(79, "外向的孤独患者 需要认可");
        LYRICS_MAP.put("孤独患者", gd);

        // 最佳损友
        LinkedHashMap<Integer, String> zj = new LinkedHashMap<>();
        zj.put(0,  "最佳损友 - 陈奕迅");
        zj.put(8,  "朋友 我当你一秒朋友");
        zj.put(12, "朋友 我当你一世朋友");
        zj.put(17, "奇怪 过去再不堪回首");
        zj.put(21, "怀缅 时时其实还有");
        zj.put(28, "从前共你 促膝把酒倾通宵都不够");
        zj.put(34, "我有痛快过 你有没有");
        zj.put(42, "很多东西今生只可给你");
        zj.put(46, "保守直到永久");
        zj.put(50, "别人如何明白透");
        zj.put(55, "实实在在 踏入过我宇宙");
        zj.put(60, "即使相处到 有个裂口");
        zj.put(67, "来年陌生的 是昨日最亲的某某");
        LYRICS_MAP.put("最佳损友", zj);

        // 盲婚哑嫁
        LinkedHashMap<Integer, String> mh = new LinkedHashMap<>();
        mh.put(0,  "盲婚哑嫁 - 陈奕迅");
        mh.put(10, "若你我可抱着睡 连命也甘心短几岁");
        mh.put(18, "如果能从来 如若你还在");
        mh.put(25, "我共你 好到可以 分享晚餐");
        mh.put(32, "曾想象 离开你 会是怎么一番境地");
        mh.put(40, "长路漫漫 如何经过");
        mh.put(47, "谁人伴我 走过这生");
        mh.put(55, "盲婚哑嫁 都因爱你");
        mh.put(62, "盲婚哑嫁 不需再记");
        LYRICS_MAP.put("盲婚哑嫁", mh);

        // 粤语残片
        LinkedHashMap<Integer, String> yy = new LinkedHashMap<>();
        yy.put(0,  "粤语残片 - 陈奕迅");
        yy.put(8,  "乔迁那日 打扫废物");
        yy.put(13, "家居仿似 开战");
        yy.put(19, "无意中发现 当天穿返学夏季衬衣");
        yy.put(28, "奇怪却是 茄汁污垢");
        yy.put(33, "渗在这衬衣 布章外边");
        yy.put(40, "极其大意 为何如此");
        yy.put(48, "那件  saddle 从何而来");
        yy.put(55, "有某只靴 有某个他");
        yy.put(62, "那夜 曾为他 着迷 着迷");
        LYRICS_MAP.put("粤语残片", yy);
    }

    /**
     * 获取当前播放秒数对应的歌词行
     */
    public static String getLyricsLine(String title, int currentSeconds) {
        // 尝试模糊匹配
        for (Map.Entry<String, LinkedHashMap<Integer, String>> entry : LYRICS_MAP.entrySet()) {
            if (title != null && title.contains(entry.getKey())) {
                LinkedHashMap<Integer, String> lyrics = entry.getValue();
                String currentLine = "";
                for (Map.Entry<Integer, String> line : lyrics.entrySet()) {
                    if (currentSeconds >= line.getKey()) {
                        currentLine = line.getValue();
                    } else {
                        break;
                    }
                }
                return currentLine.isEmpty() ? "" : currentLine;
            }
        }
        return "";
    }

    /**
     * 获取下一句歌词（有空实现滚动效果）
     */
    public static String getNextLine(String title, int currentSeconds) {
        for (Map.Entry<String, LinkedHashMap<Integer, String>> entry : LYRICS_MAP.entrySet()) {
            if (title != null && title.contains(entry.getKey())) {
                LinkedHashMap<Integer, String> lyrics = entry.getValue();
                for (Map.Entry<Integer, String> line : lyrics.entrySet()) {
                    if (line.getKey() > currentSeconds) {
                        return line.getValue();
                    }
                }
            }
        }
        return "";
    }
}
