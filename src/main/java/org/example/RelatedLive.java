package org.example;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RelatedLive {
    // 获取日志记录对象
    public static final Logger LOGGER = LoggerFactory.getLogger(DailyTasks.class);
    public RelatedLive() throws Exception {
        this.run();
    }
    public void run() throws Exception {
        LOGGER.info("正在执行直播相关"+"-------->"+"稍等");
        Function function = Function.FUNCTION;
//        // B站直播签到
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
                LOGGER.info("银瓜子兑换硬币失败"+"-------->"+info);
            }
            else{
                LOGGER.info("银瓜子兑换一个硬币"+"-------->"+"成功");
            }
        } catch (Exception e){
            LOGGER.info("银瓜子兑换硬币错误"+"-------->"+e);
            return;
        }
        LOGGER.info("直播相关"+"-------->"+"完成");
        // 送出快过期的礼物
        try {
            JSONObject jsonObject = function.xliveGetRecommendList();
            String roomId =  jsonObject.getJSONObject("data").getJSONArray("list").getJSONObject(6).getString("roomid");
            JSONObject jsonObject1 = function.xliveGetRoomInfo(roomId);
            String uid = jsonObject1.getJSONObject("data").getJSONObject("room_info").getString("uid");
            long nowTime = System.currentTimeMillis()/1000;
            JSONObject jsonObject2 = function.xliveGiftBagList();
            JSONArray jsonArray = jsonObject2.getJSONObject("data").getJSONArray("list");
            for(Object object : jsonArray){
                JSONObject json = (JSONObject) object;
                long expireAt = Long.valueOf(json.getString("expire_at"));
                if((expireAt-nowTime)<172800){
                    JSONObject jsonObject3 = function.xliveBagSend(roomId, uid, json.getString("bag_id"), json.getString("gift_id"), json.getString("gift_num"), "0", "0", "pc");
                    if("0".equals(jsonObject3.getString("code"))){
                        LOGGER.info("礼物送出成功"+"-------->"+jsonObject3);
                    }
                    else{
                        LOGGER.info("礼物送出失败"+"-------->"+jsonObject3);
                    }
                }
            }
        } catch (Exception e){
            LOGGER.info("礼物送出失败"+"-------->"+e);
        }
    }
}
