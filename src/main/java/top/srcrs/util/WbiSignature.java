package top.srcrs.util;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * WBI签名工具类
 * 用于生成Bilibili API所需的WBI签名
 *
 * @author chuiba
 * @since 2025-01-21
 */
@Slf4j
public class WbiSignature {

    private static final int[] MIX_KEY_ENC_TAB = {
        46, 47, 18, 2, 53, 8, 23, 32, 15, 50, 10, 31, 58, 3, 45, 35, 27, 43, 5, 49,
        33, 9, 42, 19, 29, 28, 14, 39, 12, 38, 41, 13, 37, 48, 7, 16, 24, 55, 40,
        61, 26, 17, 0, 1, 60, 51, 30, 4, 22, 25, 54, 21, 56, 59, 6, 63, 57, 62, 11,
        36, 20, 34, 44, 52
    };

    private static String imgKey = "";
    private static String subKey = "";
    private static long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL = 10 * 60 * 1000; // 10分钟更新一次

    /**
     * 获取WBI签名参数
     */
    public static Map<String, String> getWbiSign(Map<String, Object> params) {
        try {
            // 更新密钥
            updateKeys();

            // 生成混合密钥
            String mixinKey = getMixinKey(imgKey + subKey);

            // 添加时间戳
            long wts = System.currentTimeMillis() / 1000;
            params.put("wts", wts);

            // 排序参数并构建查询字符串
            String query = params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

            // 计算MD5
            String wRid = md5(query + mixinKey);

            Map<String, String> result = new HashMap<>();
            result.put("w_rid", wRid);
            result.put("wts", String.valueOf(wts));

            return result;
        } catch (Exception e) {
            log.error("💔WBI签名生成失败: ", e);
            return new HashMap<>();
        }
    }

    /**
     * 更新img_key和sub_key
     */
    private static void updateKeys() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < UPDATE_INTERVAL && !imgKey.isEmpty()) {
            return;
        }

        try {
            JSONObject navResp = Request.get("https://api.bilibili.com/x/web-interface/nav");
            JSONObject data = navResp.getJSONObject("data");
            JSONObject wbiImg = data.getJSONObject("wbi_img");

            String imgUrl = wbiImg.getString("img_url");
            String subUrl = wbiImg.getString("sub_url");

            // 提取文件名（去掉扩展名）
            imgKey = getFileName(imgUrl);
            subKey = getFileName(subUrl);

            lastUpdateTime = currentTime;
            log.info("WBI密钥更新成功: imgKey={}, subKey={}", imgKey.substring(0, 8) + "...", subKey.substring(0, 8) + "...");

        } catch (Exception e) {
            log.error("💔WBI密钥更新失败: ", e);
            // 使用默认值避免完全失败
            if (imgKey.isEmpty()) {
                imgKey = "7cd084941338484aae1ad9425b84077c";
                subKey = "4932caff0ff746eab6f01bf08b70ac45";
            }
        }
    }

    /**
     * 从URL中提取文件名（不包含扩展名）
     */
    private static String getFileName(String url) {
        String fileName = url.substring(url.lastIndexOf('/') + 1);
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    /**
     * 生成混合密钥
     */
    private static String getMixinKey(String orig) {
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            if (MIX_KEY_ENC_TAB[i] < orig.length()) {
                key.append(orig.charAt(MIX_KEY_ENC_TAB[i]));
            }
        }
        return key.toString();
    }

    /**
     * 计算MD5哈希
     */
    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("💔MD5计算失败: ", e);
            return "";
        }
    }
}