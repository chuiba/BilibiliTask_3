package top.srcrs.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * 给钉钉机器人推送消息
 * @author srcrs
 * @Time 2020-11-16
 */
public class SendDingTalk {
    /** 获取日志记录器对象 */
    private static final Logger LOGGER = LoggerFactory.getLogger(SendDingTalk.class);

    /**
     * 发送消息给用户，如果绑定了微信，会发送到微信上。
     * @param dingTalk 需要从钉钉群机器人获取
     * @author srcrs
     * @Time 2020-10-22
     */
    public static void send(String dingTalk){
        /* 将要推送的数据 */
        String desp = ReadLog.getString("logs/logback.log");
        JSONObject markdownJson = new JSONObject();
        markdownJson.put("title", "BilibiliTask运行结果");
        markdownJson.put("text", desp);
        JSONObject bodyJson = new JSONObject();
        bodyJson.put("msgtype", "markdown");
        bodyJson.put("markdown", markdownJson);

        StringEntity entityBody = new StringEntity(bodyJson.toString(), StandardCharsets.UTF_8);
        HttpPost httpPost = new HttpPost(dingTalk);
        httpPost.addHeader("Content-Type","application/json;charset=utf-8");
        httpPost.setEntity(entityBody);
        HttpResponse resp ;
        String respContent;
        try(CloseableHttpClient client = HttpClients.createDefault()){
            resp = client.execute(httpPost);
            HttpEntity entity;
            entity = resp.getEntity();
            respContent = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            int success = 200;
            if(resp.getStatusLine().getStatusCode() == success){
                LOGGER.info("【钉钉推送】: 正常✔");
            } else{
                LOGGER.info("【钉钉推送】: 失败, 原因为: {}❌", respContent);
            }
        } catch (Exception e){
            LOGGER.error("💔钉钉通知错误 : ", e);
        }
    }
}
