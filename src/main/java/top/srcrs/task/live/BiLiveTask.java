package top.srcrs.task.live;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import top.srcrs.Task;
import top.srcrs.util.Request;

import java.util.Random;

/**
 * 进行直播签到
 * @author srcrs
 * @Time 2020-10-13
 */
@Slf4j
public class BiLiveTask implements Task {
    /** 访问成功 */
    private static final String SUCCESS = "0";

    @Override
    public void run(){
        try{
            JSONObject json = xliveSign();
            String msg ;
            String key = "code";
            /* 获取json对象的状态码code */
            if(SUCCESS.equals(json.getString(key))){
                msg = "获得" + json.getJSONObject("data").getString("text") + " ,"
                        + json.getJSONObject("data").getString("specialText") + "✔";
            } else{
                msg = json.getString("message") + "❌";
            }
            log.info("【直播签到】: {}",msg);
            /* 直播签到后等待 3-5 秒
            ** 为防止礼物未到到账，而无法送出
            */
            Thread.sleep(new Random().nextInt(2000)+3000);
        } catch (Exception e){
            log.error("💔直播签到错误 : ", e);
        }
    }

    /**
     * B站直播进行签到
     * @return JSONObject
     * @author srcrs
     * @Time 2020-10-13
     */
    public JSONObject xliveSign(){
        try {
            JSONObject response = Request.get("https://api.live.bilibili.com/xlive/web-ucenter/v1/sign/DoSign");

            // 检查响应是否有效
            if (response == null || response.toString().contains("HTML")) {
                log.warn("直播签到API返回HTML页面，可能需要不同的认证方式");
                // 构造默认失败响应
                JSONObject fallbackResponse = new JSONObject();
                fallbackResponse.put("code", "-1");
                fallbackResponse.put("message", "API访问失败，可能需要更新认证方式");
                return fallbackResponse;
            }

            return response;
        } catch (Exception e) {
            log.error("直播签到请求异常: ", e);
            // 返回默认失败响应
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("code", "-1");
            errorResponse.put("message", "请求异常: " + e.getMessage());
            return errorResponse;
        }
    }

}
