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
        return getWbiSignWithWks(params, false);
    }

    /**
     * 获取带w_ks参数的WBI签名参数
     * @param params 请求参数
     * @param addSelf 是否添加w_ks参数
     */
    public static Map<String, String> getWbiSignWithWks(Map<String, Object> params, boolean addSelf) {
        try {
            // 更新密钥
            updateKeys();

            // 生成混合密钥
            String mixinKey = getMixinKey(imgKey + subKey);

            // 添加时间戳
            long wts = System.currentTimeMillis() / 1000;
            params.put("wts", wts);

            // 新增 w_ks 参数支持
            if (addSelf) {
                String wKs = swapString(imgKey + subKey, 2);
                params.put("w_ks", wKs);
            }

            // 排序参数并构建查询字符串
            String query = buildSortedQuery(params);

            // 计算MD5
            String wRid = md5(query + mixinKey);

            Map<String, String> result = new HashMap<>();
            result.put("w_rid", wRid);
            result.put("wts", String.valueOf(wts));
            if (addSelf) {
                result.put("w_ks", params.get("w_ks").toString());
            }

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
            // 添加超时保护，最多尝试3次
            JSONObject navResp = null;
            int retries = 0;
            while (retries < 3) {
                try {
                    navResp = Request.get("https://api.bilibili.com/x/web-interface/nav");
                    break;
                } catch (Exception e) {
                    retries++;
                    log.warn("WBI密钥更新重试 {}/3: {}", retries, e.getMessage());
                    if (retries >= 3) throw e;
                    Thread.sleep(1000); // 等待1秒后重试
                }
            }

            if (navResp != null && "0".equals(navResp.getString("code"))) {
                JSONObject data = navResp.getJSONObject("data");
                JSONObject wbiImg = data.getJSONObject("wbi_img");

                String imgUrl = wbiImg.getString("img_url");
                String subUrl = wbiImg.getString("sub_url");

                // 提取文件名（去掉扩展名）
                imgKey = getFileName(imgUrl);
                subKey = getFileName(subUrl);

                lastUpdateTime = currentTime;
                log.info("WBI密钥更新成功: imgKey={}, subKey={}", imgKey.substring(0, 8) + "...", subKey.substring(0, 8) + "...");
            } else {
                throw new RuntimeException("导航API返回错误: " + (navResp != null ? navResp.getString("message") : "无响应"));
            }

        } catch (Exception e) {
            log.error("💔WBI密钥更新失败: ", e);
            // 使用默认值避免完全失败
            if (imgKey.isEmpty()) {
                imgKey = "7cd084941338484aae1ad9425b84077c";
                subKey = "4932caff0ff746eab6f01bf08b70ac45";
                log.warn("使用默认WBI密钥");
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
     * 构建排序后的查询字符串
     */
    private static String buildSortedQuery(Map<String, Object> params) {
        return params.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                String value = String.valueOf(entry.getValue());
                // 过滤特殊字符
                value = value.replaceAll("[!'()*]", "");
                return entry.getKey() + "=" + value;
            })
            .collect(Collectors.joining("&"));
    }

    /**
     * swapString函数 - 2024年新增的WBI算法
     * @param str 输入字符串
     * @param depth 递归深度
     * @return 处理后的字符串
     */
    private static String swapString(String str, int depth) {
        if (str.length() % 2 != 0) return str;
        if (depth == 0) return str;
        if (str.length() == Math.pow(2, depth)) {
            return new StringBuilder(str).reverse().toString();
        }
        
        String left = str.substring(0, str.length() / 2);
        String right = str.substring(str.length() / 2);
        return swapString(right, depth - 1) + swapString(left, depth - 1);
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