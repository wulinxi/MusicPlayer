package com.example.musicplayer.util;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 音乐文件工具类
 * 负责将 assets/music/ 中的 mp3 文件复制到内部存储
 */
public class MusicFileHelper {

    private static final String ASSETS_MUSIC_DIR = "music";
    private static final String LOCAL_MUSIC_DIR = "music";
    private static final String LOCAL_COVERS_DIR = "covers";
    private static final String LOCAL_BG_DIR = "backgrounds";

    /**
     * 获取本地封面存储目录
     */
    public static File getCoversDir(Context context) {
        File dir = new File(context.getFilesDir(), LOCAL_COVERS_DIR);
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    /**
     * 获取本地音乐存储目录
     */
    public static File getMusicDir(Context context) {
        File dir = new File(context.getFilesDir(), LOCAL_MUSIC_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * 根据文件名获取本地完整路径
     */
    public static String getLocalMusicPath(Context context, String fileName) {
        return new File(getMusicDir(context), fileName).getAbsolutePath();
    }

    /**
     * 首次启动时将 assets/music/ 中的文件复制到内部存储
     */
    public static void copyAssetsMusicToInternal(Context context) {
        File targetDir = getMusicDir(context);

        try {
            String[] files = context.getAssets().list(ASSETS_MUSIC_DIR);
            if (files == null || files.length == 0) return;

            for (String fileName : files) {
                File targetFile = new File(targetDir, fileName);
                // 已存在则跳过
                if (targetFile.exists()) continue;

                try (InputStream in = context.getAssets().open(ASSETS_MUSIC_DIR + "/" + fileName);
                     FileOutputStream out = new FileOutputStream(targetFile)) {

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 也复制封面图片和背景图
        copyAssetsCoversToInternal(context);
        copyAssetsBackgrounds(context);
    }

    private static void copyAssetsCoversToInternal(Context context) {
        File targetDir = getCoversDir(context);
        try {
            String[] files = context.getAssets().list(LOCAL_COVERS_DIR);
            if (files == null || files.length == 0) return;
            for (String fileName : files) {
                File targetFile = new File(targetDir, fileName);
                if (targetFile.exists()) continue;
                try (InputStream in = context.getAssets().open(LOCAL_COVERS_DIR + "/" + fileName);
                     FileOutputStream out = new FileOutputStream(targetFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** 获取默认封面路径 */
    public static String getDefaultCoverPath(Context context) {
        File cover = new File(getCoversDir(context), "default_cover.jpg");
        if (cover.exists()) return cover.getAbsolutePath();
        return null;
    }

    /** 获取主页面背景路径 */
    public static String getMainBgPath(Context context) {
        File bg = new File(getBgDir(context), "111.jpg");
        if (bg.exists()) return bg.getAbsolutePath();
        return null;
    }

    private static File getBgDir(Context context) {
        File dir = new File(context.getFilesDir(), LOCAL_BG_DIR);
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    private static void copyAssetsBackgrounds(Context context) {
        File targetDir = getBgDir(context);
        try {
            String[] files = context.getAssets().list(LOCAL_BG_DIR);
            if (files == null || files.length == 0) return;
            for (String fileName : files) {
                File targetFile = new File(targetDir, fileName);
                if (targetFile.exists()) continue;
                try (InputStream in = context.getAssets().open(LOCAL_BG_DIR + "/" + fileName);
                     FileOutputStream out = new FileOutputStream(targetFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查歌曲文件是否存在
     */
    public static boolean isMusicFileExists(Context context, String fileName) {
        if (fileName == null || fileName.isEmpty()) return false;
        return new File(getMusicDir(context), fileName).exists();
    }

    /**
     * 获取 assets 中音乐文件列表
     */
    public static String[] getAssetsMusicList(Context context) {
        try {
            return context.getAssets().list(ASSETS_MUSIC_DIR);
        } catch (IOException e) {
            return new String[0];
        }
    }
}
