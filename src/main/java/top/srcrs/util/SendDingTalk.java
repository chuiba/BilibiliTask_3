package top.srcrs.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        String body = "{\n" +
                "     \"msgtype\": \"markdown\",\n" +
                "     \"markdown\": {\n" +
                "         \"title\":\"BilibiliTask运行结果\",\n" +
                "         \"text\":\""+desp+"\"\n" +
                "     }" +
                " }";
        StringEntity entityBody = new StringEntity(body,"UTF-8");
        HttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(dingTalk);
        httpPost.addHeader("Content-Type","application/json;charset=utf-8");
        httpPost.setEntity(entityBody);
        HttpResponse resp ;
        String respContent;
        try{
            resp = client.execute(httpPost);
            HttpEntity entity;
            entity = resp.getEntity();
            respContent = EntityUtils.toString(entity, "UTF-8");
            int success = 200;
            if(resp.getStatusLine().getStatusCode() == success){
                LOGGER.info("【钉钉推送】: 正常✔");
            } else{
                LOGGER.info("【钉钉推送】: 失败, 原因为: " + respContent + "❌");
            }
        } catch (Exception e){
            LOGGER.error("💔钉钉通知错误 : " + e);
        }
    }
}
