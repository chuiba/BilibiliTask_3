package top.srcrs.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
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
        waitFor();
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

    private static RequestBuilder getBaseBuilder(final String method) {
        // 获取bili_ticket并添加到Cookie中
        String biliTicket = BiliTicket.getBiliTicket();
        String cookie = USER_DATA.getCookie();
        if (!biliTicket.isEmpty()) {
            cookie += "bili_ticket=" + biliTicket + ";";
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
        try (CloseableHttpClient client = HttpClients.createDefault()) {
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
     * 暂时先设置为每次请求预等待 0-3 秒钟
     * @author srcrs
     * @Time 2020-11-28
     */
    public static void waitFor() {
        try{
            Thread.sleep(new Random().nextInt(4)*1000);
        } catch (Exception e){
            log.warn("等待过程中出错",e);
        }
    }
}
