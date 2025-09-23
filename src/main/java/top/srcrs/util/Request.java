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
        waitFor();
        HttpUriRequest httpGet = getBaseBuilder(HttpGet.METHOD_NAME)
                .setUri(url)
                .addParameters(getPairList(pJson))
                .build();
        return clientExe(httpGet);
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
        waitFor();
        log.info("⏰等待完成，开始执行请求");
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
        waitFor();
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
        waitFor();
        HttpUriRequest httpPost = getBaseBuilder(HttpPost.METHOD_NAME)
                .addHeader("accept", "application/json, text/plain, */*")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("charset", "UTF-8")
                .setUri(url)
                .addParameters(getPairList(pJson))
                .build();
        return clientExe(httpPost);
    }

    /**
     * 发送POST请求，不包含bili_ticket（用于获取bili_ticket本身，避免循环调用）
     */
    public static JSONObject postWithoutBiliTicket(String url, JSONObject pJson) {
        waitFor();
        HttpUriRequest httpPost = getBaseBuilder(HttpPost.METHOD_NAME, false)
                .addHeader("accept", "application/json, text/plain, */*")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("charset", "UTF-8")
                .setUri(url)
                .addParameters(getPairList(pJson))
                .build();
        return clientExe(httpPost);
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
                throw new RuntimeException("API响应为空");
            }

            // 检查是否是HTML错误页面（通常以 < 开头）
            if(respContent.trim().startsWith("<")) {
                log.error("💔{}请求返回HTML错误页面: {}", request.getMethod(), respContent.substring(0, Math.min(100, respContent.length())));
                throw new RuntimeException("API返回HTML错误页面，可能是认证失败或API不可用");
            }

            try {
                return JSON.parseObject(respContent);
            } catch (Exception parseException) {
                log.error("💔{}请求JSON解析失败，响应内容: {}", request.getMethod(), respContent.substring(0, Math.min(200, respContent.length())));
                throw new RuntimeException("JSON解析失败: " + parseException.getMessage(), parseException);
            }
        } catch (Exception e) {
            log.error("💔{}请求错误 : ", request.getMethod(), e);
            throw new RuntimeException("API请求失败: " + e.getMessage(), e);
        }
    }

    /**
     * 增加等待时间，解决风控问题
     * 优化等待时间为 0-1 秒钟，避免过长延迟
     * @author chuiba (updated from srcrs)
     * @Time 2025-01-21
     */
    public static void waitFor() {
        try{
            // 减少等待时间，从0-3秒改为0-1秒
            Thread.sleep(new Random().nextInt(1000));
        } catch (Exception e){
            log.warn("等待过程中出错",e);
        }
    }
}
