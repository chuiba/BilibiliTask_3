package top.srcrs.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import top.srcrs.domain.UserData;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 封装的网络请求请求工具类
 *
 * @author srcrs
 * @Time 2020-10-13
 */
@Slf4j
public class Request {
    /**
     * 获取data对象
     */
    private static final UserData USER_DATA = UserData.getInstance();

    public static String UserAgent = "";
    
    // 请求频率控制相关变量
    private static final AtomicLong lastRequestTime = new AtomicLong(0);
    private static final AtomicInteger consecutiveErrors = new AtomicInteger(0);
    private static final AtomicInteger requestCount = new AtomicInteger(0);
    private static final Map<String, AtomicLong> domainLastRequest = new ConcurrentHashMap<>();
    private static final Map<String, AtomicInteger> domainErrorCount = new ConcurrentHashMap<>();
    
    // 配置参数
    private static final int BASE_INTERVAL = 800;        // 基础间隔800ms
    private static final int MAX_INTERVAL = 5000;        // 最大间隔5秒
    private static final int ERROR_PENALTY = 2000;       // 错误惩罚2秒
    private static final int MAX_REQUESTS_PER_MINUTE = 30; // 每分钟最大请求数
    private static final long MINUTE_IN_MS = 60000;      // 一分钟的毫秒数
    
    private Request() {}

    /**
     * 发送get请求
     *
     * @param url 请求的地址，包括参数
     * @param pJson 携带的参数
     * @return JSONObject
     * @author srcrs
     * @Time 2020-10-13
     */
    public static JSONObject get(String url, JSONObject pJson) {
        waitFor(url);
        HttpUriRequest httpGet = getBaseBuilder(HttpGet.METHOD_NAME)
                .setUri(url)
                .addParameters(getPairList(pJson))
                .build();
        return clientExe(httpGet, url);
    }

    /**
     * 发送get请求
     *
     * @param url 请求的地址，包括参数
     * @return JSONObject
     * @author srcrs
     * @Time 2020-10-13
     */
    public static JSONObject get(String url) {
        log.info("🔄开始GET请求: {}", url);
        return get(url, new JSONObject());
    }

    /**
     * 发送带WBI签名的GET请求
     *
     * @param url 请求的地址
     * @param params 请求参数
     * @return JSONObject
     * @author chuiba
     * @Time 2025-01-21
     */
    public static JSONObject getWithWbi(String url, JSONObject params) {
        try {
            // 转换参数格式
            Map<String, Object> paramMap = new HashMap<>();
            for (String key : params.keySet()) {
                paramMap.put(key, params.get(key));
            }

            // 获取WBI签名
            Map<String, String> wbiParams = WbiSignature.getWbiSign(paramMap);

            // 添加WBI参数
            JSONObject finalParams = new JSONObject(params);
            finalParams.put("w_rid", wbiParams.get("w_rid"));
            finalParams.put("wts", wbiParams.get("wts"));

            return get(url, finalParams);
        } catch (Exception e) {
            log.error("💔WBI请求失败: ", e);
            recordError(url);
            throw new RuntimeException("WBI请求失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送post请求
     *
     * @param url  请求的地址
     * @param pJson 携带的参数
     * @return JSONObject
     * @author srcrs
     * @Time 2020-10-13
     */
    public static JSONObject post(String url, JSONObject pJson) {
        waitFor(url);
        HttpUriRequest httpPost = getBaseBuilder(HttpPost.METHOD_NAME)
                .addHeader("accept", "application/json, text/plain, */*")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("charset", "UTF-8")
                .setUri(url)
                .addParameters(getPairList(pJson))
                .build();
        return clientExe(httpPost, url);
    }

    /**
     * 发送POST请求，不包含bili_ticket（用于获取bili_ticket本身，避免循环调用）
     */
    public static JSONObject postWithoutBiliTicket(String url, JSONObject pJson) {
        waitFor(url);
        HttpUriRequest httpPost = getBaseBuilder(HttpPost.METHOD_NAME, false)
                .addHeader("accept", "application/json, text/plain, */*")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("charset", "UTF-8")
                .setUri(url)
                .addParameters(getPairList(pJson))
                .build();
        return clientExe(httpPost, url);
    }

    /**
     * 发送带WBI签名的POST请求
     */
    public static JSONObject postWithWbi(String url, JSONObject pJson) {
        try {
            // 转换参数格式
            Map<String, Object> paramMap = new HashMap<>();
            for (String key : pJson.keySet()) {
                paramMap.put(key, pJson.get(key));
            }

            // 获取WBI签名
            Map<String, String> wbiParams = WbiSignature.getWbiSign(paramMap);

            // 添加WBI参数
            JSONObject finalParams = new JSONObject(pJson);
            finalParams.put("w_rid", wbiParams.get("w_rid"));
            finalParams.put("wts", wbiParams.get("wts"));

            return post(url, finalParams);
        } catch (Exception e) {
            log.error("💔WBI POST请求失败: ", e);
            recordError(url);
            throw new RuntimeException("WBI POST请求失败: " + e.getMessage(), e);
        }
    }

    private static RequestBuilder getBaseBuilder(final String method) {
        return getBaseBuilder(method, true);
    }

    private static RequestBuilder getBaseBuilder(final String method, boolean includeBiliTicket) {
        String cookie = USER_DATA.getCookie();

        // 只有在需要时才获取bili_ticket，避免循环调用
        if (includeBiliTicket) {
            try {
                String biliTicket = BiliTicket.getBiliTicket();
                if (!biliTicket.isEmpty()) {
                    cookie += "bili_ticket=" + biliTicket + ";";
                }
            } catch (Exception e) {
                log.warn("获取bili_ticket失败，跳过: {}", e.getMessage());
            }
        }

        return RequestBuilder.create(method)
                             .addHeader("connection", "keep-alive")
                             .addHeader("referer", "https://www.bilibili.com/")
                             .addHeader("User-Agent", UserAgent)
                             .addHeader("Cookie", cookie);
    }

    public static NameValuePair[] getPairList(JSONObject pJson) {
        return pJson.entrySet().parallelStream().map(Request::getNameValuePair).toArray(NameValuePair[]::new);
    }

    private static NameValuePair getNameValuePair(Map.Entry<String, Object> entry) {
        return new BasicNameValuePair(entry.getKey(), StringUtil.get(entry.getValue()));
    }

    public static JSONObject clientExe(HttpUriRequest request) {
        return clientExe(request, request.getURI().toString());
    }
    
    public static JSONObject clientExe(HttpUriRequest request, String url) {
        log.info("🌐开始执行HTTP请求: {} {}", request.getMethod(), request.getURI());
        // 配置超时时间
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(10000) // 连接超时10秒
                .setSocketTimeout(30000)   // 读取超时30秒
                .setConnectionRequestTimeout(5000) // 请求超时5秒
                .build();

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build()) {
            HttpResponse resp = client.execute(request);
            HttpEntity entity = resp.getEntity();
            String respContent = EntityUtils.toString(entity, StandardCharsets.UTF_8);

            // 检查响应是否为有效JSON
            if(respContent == null || respContent.trim().isEmpty()) {
                log.error("💔{}请求返回空响应", request.getMethod());
                recordError(url);
                throw new RuntimeException("API响应为空");
            }

            // 检查是否是HTML错误页面（通常以 < 开头）
            if(respContent.trim().startsWith("<")) {
                log.error("💔{}请求返回HTML错误页面: {}", request.getMethod(), respContent.substring(0, Math.min(100, respContent.length())));
                recordError(url);
                throw new RuntimeException("API返回HTML错误页面，可能是认证失败或API不可用");
            }

            try {
                JSONObject result = JSON.parseObject(respContent);
                // 检查API响应状态码
                if (result != null && result.containsKey("code")) {
                    String code = result.getString("code");
                    if ("0".equals(code)) {
                        recordSuccess(url);
                        log.debug("✅{}请求成功: {}", request.getMethod(), url);
                    } else {
                        // API返回错误码，但不一定是网络错误，根据具体错误码决定是否记录错误
                        if ("-352".equals(code) || "-403".equals(code) || "-412".equals(code)) {
                            recordError(url);
                            log.warn("⚠️{}请求API错误: {} - {}", request.getMethod(), code, result.getString("message"));
                        } else {
                            log.info("ℹ️{}请求API返回: {} - {}", request.getMethod(), code, result.getString("message"));
                        }
                    }
                } else {
                    recordSuccess(url);
                }
                return result;
            } catch (Exception parseException) {
                log.error("💔{}请求JSON解析失败，响应内容: {}", request.getMethod(), respContent.substring(0, Math.min(200, respContent.length())));
                recordError(url);
                throw new RuntimeException("JSON解析失败: " + parseException.getMessage(), parseException);
            }
        } catch (Exception e) {
            log.error("💔{}请求错误 : ", request.getMethod(), e);
            recordError(url);
            throw new RuntimeException("API请求失败: " + e.getMessage(), e);
        }
    }

    /**
     * 智能请求间隔控制
     * 根据请求频率、错误次数和域名进行动态调整
     * @author chuiba (updated from srcrs)
     * @Time 2025-01-21
     */
    public static void waitFor() {
        waitFor(null);
    }
    
    /**
     * 智能请求间隔控制（带域名参数）
     * @param url 请求URL，用于域名级别的频率控制
     */
    public static void waitFor(String url) {
        try {
            long currentTime = System.currentTimeMillis();
            String domain = extractDomain(url);
            
            // 检查全局请求频率限制
            checkGlobalRateLimit(currentTime);
            
            // 计算智能间隔
            long interval = calculateSmartInterval(domain, currentTime);
            
            // 执行等待
            if (interval > 0) {
                log.debug("⏰智能等待 {}ms (域名: {})", interval, domain != null ? domain : "全局");
                Thread.sleep(interval);
            }
            
            // 更新请求时间记录
            updateRequestTime(domain, currentTime);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("等待过程被中断", e);
        } catch (Exception e) {
            log.warn("等待过程中出错", e);
        }
    }
    
    /**
     * 提取URL中的域名
     */
    private static String extractDomain(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        try {
            if (url.startsWith("http")) {
                return url.split("/")[2];
            }
        } catch (Exception e) {
            log.debug("提取域名失败: {}", url);
        }
        return null;
    }
    
    /**
     * 检查全局请求频率限制
     */
    private static void checkGlobalRateLimit(long currentTime) throws InterruptedException {
        // 清理过期的请求计数
        if (currentTime - lastRequestTime.get() > MINUTE_IN_MS) {
            requestCount.set(0);
        }
        
        // 检查是否超过每分钟请求限制
        if (requestCount.get() >= MAX_REQUESTS_PER_MINUTE) {
            long waitTime = MINUTE_IN_MS - (currentTime - lastRequestTime.get());
            if (waitTime > 0) {
                log.warn("⚠️达到请求频率限制，等待 {}ms", waitTime);
                Thread.sleep(waitTime);
            }
            requestCount.set(0);
        }
        
        requestCount.incrementAndGet();
    }
    
    /**
     * 计算智能间隔时间
     */
    private static long calculateSmartInterval(String domain, long currentTime) {
        long baseInterval = BASE_INTERVAL;
        
        // 全局错误惩罚
        int globalErrors = consecutiveErrors.get();
        if (globalErrors > 0) {
            baseInterval += Math.min(globalErrors * ERROR_PENALTY, MAX_INTERVAL);
            log.debug("🚨全局错误惩罚: {}次错误，增加 {}ms", globalErrors, Math.min(globalErrors * ERROR_PENALTY, MAX_INTERVAL));
        }
        
        // 域名级别的错误惩罚
        if (domain != null) {
            AtomicInteger domainErrors = domainErrorCount.get(domain);
            if (domainErrors != null && domainErrors.get() > 0) {
                int penalty = Math.min(domainErrors.get() * ERROR_PENALTY / 2, MAX_INTERVAL / 2);
                baseInterval += penalty;
                log.debug("🚨域名错误惩罚 {}: {}次错误，增加 {}ms", domain, domainErrors.get(), penalty);
            }
            
            // 检查域名级别的最小间隔
            AtomicLong domainLastTime = domainLastRequest.get(domain);
            if (domainLastTime != null) {
                long timeSinceLastRequest = currentTime - domainLastTime.get();
                long minDomainInterval = baseInterval / 2; // 域名级别间隔为全局的一半
                if (timeSinceLastRequest < minDomainInterval) {
                    baseInterval = Math.max(baseInterval, minDomainInterval - timeSinceLastRequest);
                }
            }
        }
        
        // 全局最小间隔检查
        long timeSinceLastGlobalRequest = currentTime - lastRequestTime.get();
        if (timeSinceLastGlobalRequest < baseInterval) {
            baseInterval = baseInterval - timeSinceLastGlobalRequest;
        } else {
            baseInterval = 0; // 已经等待足够长时间
        }
        
        // 添加随机抖动（±20%）
        if (baseInterval > 0) {
            double jitter = 0.8 + (new Random().nextDouble() * 0.4); // 0.8 到 1.2
            baseInterval = (long) (baseInterval * jitter);
        }
        
        return Math.min(Math.max(baseInterval, 0), MAX_INTERVAL);
    }
    
    /**
     * 更新请求时间记录
     */
    private static void updateRequestTime(String domain, long currentTime) {
        lastRequestTime.set(currentTime);
        if (domain != null) {
            domainLastRequest.computeIfAbsent(domain, k -> new AtomicLong()).set(currentTime);
        }
    }
    
    /**
     * 记录请求成功，重置错误计数
     */
    public static void recordSuccess(String url) {
        consecutiveErrors.set(0);
        String domain = extractDomain(url);
        if (domain != null) {
            domainErrorCount.computeIfAbsent(domain, k -> new AtomicInteger()).set(0);
        }
        log.debug("✅请求成功，重置错误计数");
    }
    
    /**
     * 记录请求错误，增加错误计数
     */
    public static void recordError(String url) {
        int errors = consecutiveErrors.incrementAndGet();
        String domain = extractDomain(url);
        if (domain != null) {
            int domainErrors = domainErrorCount.computeIfAbsent(domain, k -> new AtomicInteger()).incrementAndGet();
            log.warn("❌请求错误，全局错误计数: {}，域名 {} 错误计数: {}", errors, domain, domainErrors);
        } else {
            log.warn("❌请求错误，全局错误计数: {}", errors);
        }
    }
}
