package top.srcrs.task.live;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.srcrs.Task;
import top.srcrs.util.Request;


/**
 * 进行直播签到
 * @author srcrs
 * @Time 2020-10-13
 */
public class BiLiveTask implements Task {
    /** 获取日志记录器对象 */
    private static final Logger LOGGER = LoggerFactory.getLogger(BiLiveTask.class);
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
            LOGGER.info("【直播签到】: {}",msg);
            /* 直播签到后等待5秒
            ** 为防止礼物未到到账，而无法送出
            */
            Thread.sleep(5000);
        } catch (Exception e){
            LOGGER.error("💔直播签到错误 : " + e);
        }
    }

    /**
     * B站直播进行签到
     * @return JSONObject
     * @author srcrs
     * @Time 2020-10-13
     */
    public JSONObject xliveSign(){
        return Request.get("https://api.live.bilibili.com/xlive/web-ucenter/v1/sign/DoSign");
    }

}
