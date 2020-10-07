package org.example;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RelatedLive {
    // 获取日志记录对象
    public static final Logger LOGGER = LoggerFactory.getLogger(DailyTasks.class);
    public RelatedLive(){
        this.run();
    }
    public void run(){
        LOGGER.info("正在执行直播相关"+"-------->"+"稍等");
        Function function = Function.FUNCTION;
        // B站直播签到
        try {
            JSONObject json = function.xliveSign();
            if("0".equals(json.getString("code"))){
                LOGGER.info("直播签到成功"+"-------->"+json);
            } else{
                LOGGER.info("今天已经签过"+"-------->"+json);
            }
        } catch (Exception e){
            LOGGER.error("直播签到异常"+"-------->"+e);
        }
        // 银瓜子兑换成硬币
        try{
            JSONObject jsonObject = function.xliveGetStatus();
            if(!("0".equals(jsonObject.getString("code")))){
                LOGGER.info("获取瓜子信息异常，跳过所有操作"+"-------->"+jsonObject.getString("msg"));
                return;
            }
            if("0".equals(jsonObject.getJSONObject("data").getString("silver_2_coin_left"))){
                LOGGER.info("今天的额度已经兑换完");
                return;
            }
            JSONObject info = function.silver2coin();
            if(!("0".equals(info.getString("code")))){
                LOGGER.info("银瓜子兑换硬币失败");
            }
            LOGGER.info("银瓜子兑换一个硬币"+"-------->"+"成功");
        } catch (Exception e){
            LOGGER.info("银瓜子兑换硬币错误"+"-------->"+e);
            return;
        }
        LOGGER.info("直播相关"+"-------->"+"完成");
    }
}
