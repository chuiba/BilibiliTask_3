package top.srcrs.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;

/**
 * 将日志消息发送到用户的 server 酱 （微信）
 * @author srcrs
 * @Time 2020-10-22
 */
@Slf4j
public class SendServer {

    /**
     * 发送消息给用户，如果绑定了微信，会发送到微信上。
     * @param sckey 需要从server酱的官网注册获取
     * @author srcrs
     * @Time 2020-10-22
     */
    public static void send(String sckey){
        /* 将要推送的数据 */
        JSONObject pJson = new JSONObject();
        pJson.put("text", "BilibiliTask运行结果");
        pJson.put("desp", ReadLog.getString("logs/logback.log"));

        HttpUriRequest httpPost = RequestBuilder.post()
                                                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                                                .setUri("https://sc.ftqq.com/" + sckey + ".send")
                                                .addParameters(Request.getPairList(pJson))
                                                .build();

        try(CloseableHttpClient client = HttpClients.createDefault()){
            HttpResponse resp = client.execute(httpPost);
            HttpEntity entity = resp.getEntity();
            String respContent = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            int success = 200;
            if(resp.getStatusLine().getStatusCode() == success){
                log.info("【server酱推送】: 正常✔");
            } else{
                log.info("【server酱推送】: 失败, 原因为: {}❌", respContent);
            }
        } catch (Exception e){
            log.error("💔server酱发送错误 : ", e);
        }
    }

}
