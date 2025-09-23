package top.srcrs.util;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import top.srcrs.domain.UserData;

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
    private static final long TICKET_VALIDITY = 2 * 60 * 60 * 1000; // 2小时有效期
    private static final int MAX_RETRIES = 3; // 最大重试次数

    /**
     * 获取bili_ticket
     */
    public static String getBiliTicket() {
        long currentTime = System.currentTimeMillis();
        
        // 检查缓存是否有效
        if (!cachedTicket.isEmpty() && 
            (currentTime - lastUpdateTime) < TICKET_VALIDITY) {
            return cachedTicket;
        }

        // 获取新的ticket
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                long timestamp = currentTime / 1000;
                String message = "ts" + timestamp;
                String hexSign = hmacSha256(message, HMAC_KEY);

                JSONObject params = new JSONObject();
                params.put("key_id", KEY_ID);
                params.put("hexsign", hexSign);
                params.put("context[ts]", timestamp);
                params.put("csrf", UserData.getInstance().getBiliJct());

                JSONObject response = Request.postWithoutBiliTicket(
                    "https://api.bilibili.com/bapis/bilibili.api.ticket.v1.Ticket/GenWebTicket", 
                    params
                );

                if ("0".equals(response.getString("code"))) {
                    JSONObject data = response.getJSONObject("data");
                    cachedTicket = data.getString("ticket");
                    lastUpdateTime = currentTime;
                    log.info("bili_ticket更新成功");
                    return cachedTicket;
                } else {
                    log.warn("bili_ticket API返回错误: {} - {}", 
                        response.getString("code"), response.getString("message"));
                    // API返回错误码，不进行重试
                    break;
                }
            } catch (Exception e) {
                log.warn("bili_ticket获取失败，重试 {}/{}: {}", i + 1, MAX_RETRIES, e.getMessage());
                if (i < MAX_RETRIES - 1) {
                    try {
                        Thread.sleep(1000 * (i + 1)); // 递增等待时间：1s, 2s, 3s
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        log.warn("bili_ticket获取失败，使用空值");
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