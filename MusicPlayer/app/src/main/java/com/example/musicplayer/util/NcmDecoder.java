package com.example.musicplayer.util;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * NCM 文件解密器 — 将网易云 .ncm 加密文件转为原始 MP3/FLAC
 *
 * 算法来源: ncmdump (taurusxin/ncmdump) 移植为纯 Java 实现
 */
public class NcmDecoder {

    // AES-128-ECB 固定密钥
    private static final byte[] CORE_KEY = hexToBytes("687a4852416d736f356b496e62617857");
    private static final byte[] META_KEY = hexToBytes("2331346c6a3338333433346a626c3932");

    /**
     * 解密 NCM 文件
     * @param input  ncm 文件输入流
     * @return { musicData, imageData, format("mp3"/"flac") }
     */
    public static DecodeResult decode(InputStream input) throws Exception {
        DataInputStream dis = new DataInputStream(input);

        // 1. 读取文件头 (10 bytes)
        byte[] header = new byte[10];
        dis.readFully(header);

        // 2. 读取加密的 RC4 密钥长度 (4 bytes LE)
        int keyLen = readIntLE(dis);
        if (keyLen <= 0 || keyLen > 256) {
            throw new IllegalStateException("无效的 NCM 文件: keyLen=" + keyLen);
        }

        // 3. 读取 RC4 密钥数据
        byte[] keyData = new byte[keyLen];
        dis.readFully(keyData);

        // 4. 解密 RC4 密钥: 每字节 XOR 0x64 → AES-128-ECB → 去掉"neteasecloudmusic"填充
        for (int i = 0; i < keyData.length; i++) {
            keyData[i] ^= 0x64;
        }
        byte[] decryptedKey = aesEcbDecrypt(keyData, CORE_KEY);
        String keyStr = new String(decryptedKey, "UTF-8");
        keyStr = keyStr.replace("neteasecloudmusic", "");
        byte[] rc4Key = keyStr.getBytes("UTF-8");

        // 5. 跳过音乐信息部分 (JSON 元数据，不关心)
        int infoLen = readIntLE(dis);
        dis.skipBytes(infoLen);

        // 6. 跳过 5 字节 gap
        dis.skipBytes(5);

        // 7. 读取专辑封面
        int coverLen = readIntLE(dis);
        byte[] coverData = new byte[coverLen];
        dis.readFully(coverData);

        // 8. 剩余全是 RC4 加密的音乐数据
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int len;
        while ((len = dis.read(buf)) > 0) {
            baos.write(buf, 0, len);
        }
        dis.close();
        byte[] musicData = rc4Crypt(baos.toByteArray(), rc4Key);

        // 9. 检测音频格式
        String format = detectFormat(musicData);

        return new DecodeResult(musicData, coverData, format);
    }

    /** 解密结果 */
    public static class DecodeResult {
        public final byte[] musicData;
        public final byte[] coverData;
        public final String format; // "mp3" or "flac"

        DecodeResult(byte[] music, byte[] cover, String fmt) {
            this.musicData = music;
            this.coverData = cover;
            this.format = fmt;
        }
    }

    // ===== 加密原语 =====

    /** AES-128-ECB 解密 */
    private static byte[] aesEcbDecrypt(byte[] data, byte[] key) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        return cipher.doFinal(data);
    }

    /** RC4 解密（加密和解密是同一个操作） */
    private static byte[] rc4Crypt(byte[] data, byte[] key) {
        int[] S = new int[256];
        for (int i = 0; i < 256; i++) S[i] = i;

        int j = 0;
        for (int i = 0; i < 256; i++) {
            j = (j + S[i] + (key[i % key.length] & 0xFF)) % 256;
            int tmp = S[i];
            S[i] = S[j];
            S[j] = tmp;
        }

        byte[] out = new byte[data.length];
        int i2 = 0;
        j = 0;
        for (int k = 0; k < data.length; k++) {
            i2 = (i2 + 1) % 256;
            j = (j + S[i2]) % 256;
            int tmp = S[i2];
            S[i2] = S[j];
            S[j] = tmp;
            out[k] = (byte) (data[k] ^ S[(S[i2] + S[j]) % 256]);
        }
        return out;
    }

    /** 检测音频格式 */
    private static String detectFormat(byte[] data) {
        if (data.length < 4) return "mp3";
        // FLAC magic: "fLaC"
        if ((data[0] & 0xFF) == 0x66 && (data[1] & 0xFF) == 0x4C &&
            (data[2] & 0xFF) == 0x61 && (data[3] & 0xFF) == 0x43) {
            return "flac";
        }
        return "mp3";
    }

    // ===== 工具方法 =====

    private static int readIntLE(DataInputStream dis) throws Exception {
        int b0 = dis.readUnsignedByte();
        int b1 = dis.readUnsignedByte();
        int b2 = dis.readUnsignedByte();
        int b3 = dis.readUnsignedByte();
        return b0 | (b1 << 8) | (b2 << 16) | (b3 << 24);
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
