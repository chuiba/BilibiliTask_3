package top.srcrs.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;

/**
 * 将日志消息发送到 TgBot
 *
 * @author sh4wnzec
 * @Time 2020-12-25
 */
@Slf4j
public class SendTgBot {

    private SendTgBot(){}

    /**
     * 发送消息给用户，如果绑定了TelegramBot，会发送到TelegramBot上。
     * 获取的 tgbot 进行拆分， 放入arr
     * arr[0] 是用户的 chat_id，在 Tg 搜：@getidsbot 可以获取
     * arr[1] 是 Bot 的 Token，在 Tg 搜：@BotFather 创建机器人后获取
     *
     * @param tgbot
     * @author sh4wnzec
     * @Time 2020-12-25
     */
    public static void send(String tgbot) {
        String[] arr = tgbot.split("@@");
        JSONObject pJson = new JSONObject();
        pJson.put("text", ReadLog.getMarkDownString("logs/logback.log"));
        pJson.put("chat_id", arr[1]);
        HttpUriRequest httpPost = RequestBuilder.post()
                                                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                                                .setUri("https://api.telegram.org/bot" + arr[0]+"/sendMessage")
                                                .addParameters(Request.getPairList(pJson))
                                                .build();

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpResponse resp = client.execute(httpPost);
            HttpEntity entity = resp.getEntity();
            String respContent = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                log.info("【TgBot 推送】: 正常✔");
            } else {
                log.info("【TgBot 推送】: 失败, 原因为: {}❌", respContent);
            }
        } catch (Exception e) {
            log.error("💔TgBot 发送错误 : ", e);
        }
    }

}
