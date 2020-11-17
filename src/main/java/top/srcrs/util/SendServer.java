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
 * 将日志消息发送到用户的 server 酱 （微信）
 * @author srcrs
 * @Time 2020-10-22
 */
public class SendServer {
    /** 获取日志记录器对象 */
    private static final Logger LOGGER = LoggerFactory.getLogger(SendServer.class);

    /**
     * 发送消息给用户，如果绑定了微信，会发送到微信上。
     * @param sckey 需要从server酱的官网注册获取
     * @author srcrs
     * @Time 2020-10-22
     */
    public static void send(String sckey){
        /* 将要推送的数据 */
        String desp = ReadLog.getString("logs/logback.log");
        String body = "text=" + "BilibiliTask运行结果" + "&desp="+desp;
        StringEntity entityBody = new StringEntity(body,"UTF-8");
        HttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("https://sc.ftqq.com/" + sckey + ".send");
        httpPost.addHeader("Content-Type","application/x-www-form-urlencoded");
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
                LOGGER.info("【server酱推送】: 正常✔");
            } else{
                LOGGER.info("【server酱推送】: 失败, 原因为: " + respContent + "❌");
            }
        } catch (Exception e){
            LOGGER.error("💔server酱发送错误 : " + e);
        }
    }
}
