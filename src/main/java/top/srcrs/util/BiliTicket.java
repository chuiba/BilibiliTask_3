package top.srcrs.util;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Bilibili Ticket生成工具类
 * 用于生成bili_ticket以降低风控概率
 *
 * @author chuiba
 * @since 2025-01-21
 */
@Slf4j
public class BiliTicket {

    private static final String HMAC_KEY = "XgwSnGZ1p";
    private static final String KEY_ID = "ec02";
    private static String cachedTicket = "";
    private static long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL = 30 * 60 * 1000; // 30分钟更新一次

    /**
     * 获取bili_ticket
     */
    public static String getBiliTicket() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < UPDATE_INTERVAL && !cachedTicket.isEmpty()) {
            return cachedTicket;
        }

        try {
            long timestamp = currentTime / 1000;
            String message = "ts" + timestamp;
            String hexSign = hmacSha256(message, HMAC_KEY);

            JSONObject params = new JSONObject();
            params.put("key_id", KEY_ID);
            params.put("hexsign", hexSign);
            params.put("context[ts]", timestamp);
            params.put("csrf", "");

            // 添加重试机制，最多尝试2次
            int retries = 0;
            while (retries < 2) {
                try {
                    JSONObject response = Request.postWithoutBiliTicket("https://api.bilibili.com/bapis/bilibili.api.ticket.v1.Ticket/GenWebTicket", params);

                    if ("0".equals(response.getString("code"))) {
                        JSONObject data = response.getJSONObject("data");
                        cachedTicket = data.getString("ticket");
                        lastUpdateTime = currentTime;
                        log.info("bili_ticket更新成功");
                        return cachedTicket;
                    } else {
                        log.warn("bili_ticket获取失败: {}", response.getString("message"));
                        break; // API返回错误，不重试
                    }
                } catch (Exception e) {
                    retries++;
                    log.warn("bili_ticket获取重试 {}/2: {}", retries, e.getMessage());
                    if (retries >= 2) throw e;
                    Thread.sleep(500); // 等待0.5秒后重试
                }
            }
        } catch (Exception e) {
            log.error("💔bili_ticket生成异常: ", e);
        }

        return "";
    }

    /**
     * HMAC-SHA256签名
     */
    private static String hmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("💔HMAC-SHA256签名失败: ", e);
            return "";
        }
    }
}