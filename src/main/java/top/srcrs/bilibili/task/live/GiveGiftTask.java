package top.srcrs.bilibili.task.live;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.srcrs.bilibili.Task;
import top.srcrs.bilibili.domain.Config;
import top.srcrs.bilibili.domain.Data;
import top.srcrs.bilibili.util.Request;

/**
 * B站直播送出即将过期的礼物
 * @author srcrs
 * @Time 2020-10-13
 */
public class GiveGiftTask implements Task {
    /** 获取日志记录器对象 */
    private static final Logger LOGGER = LoggerFactory.getLogger(GiveGiftTask.class);
    Data data = Data.getInstance();
    Config config = Config.getInstance();

    @Override
    public void run(){
        try{
            giveGift();
        } catch (Exception e){
            LOGGER.error("赠送礼物异常 -- "+e);
        }
    }

    public void giveGift(){
        /** 从配置类中读取是否需要执行赠送礼物 */
        if(config.isGift()){
            /** 获取一个直播间的room_id */
            String roomId = xliveGetRecommend();
            /** 通过room_id获取uid */
            String uid = xliveGetRoomUid(roomId);
            /** B站后台时间戳为10位 */
            long nowTime = System.currentTimeMillis()/1000;
            /** 获得礼物列表 */
            JSONArray jsonArray = xliveGiftBagList();;
            for(Object object : jsonArray){
                JSONObject json = (JSONObject) object;
                long expireAt = Long.valueOf(json.getString("expire_at"));
                if((expireAt-nowTime)<172800){
                    JSONObject jsonObject3 = xliveBagSend(roomId, uid, json.getString("bag_id"), json.getString("gift_id"), json.getString("gift_num"), "0", "0", "pc");
                    if("0".equals(jsonObject3.getString("code"))){
                        /** 礼物的名字 */
                        String giftName = jsonObject3.getJSONObject("data").getString("gift_name");
                        /** 礼物的数量 */
                        String giftNum = jsonObject3.getJSONObject("data").getString("gift_num");
                        LOGGER.info("送礼物给 -- {} -- {} -- 数量: {}",roomId,giftName,giftNum);

                    }
                    else{
                        LOGGER.warn("礼物送出失败 -- "+jsonObject3);
                    }
                }
            }
        }
    }

    /**
     * 获取一个直播间的room_id
     * @return JSONObject
     * @author srcrs
     * @Time 2020-10-13
     */
    public String xliveGetRecommend(){
        return Request.get("https://api.live.bilibili.com/relation/v1/AppWeb/getRecommendList")
                .getJSONObject("data")
                .getJSONArray("list")
                .getJSONObject(6)
                .getString("roomid");
    }

    /**
     * B站获取直播间的uid
     * @param room_id 房间的id
     * @return JSONObject
     * @author srcrs
     * @Time 2020-10-13
     */
    public String xliveGetRoomUid(String room_id){
        String param = "?room_id="+room_id;
        return Request.get("https://api.live.bilibili.com/xlive/web-room/v1/index/getInfoByRoom" + param)
                .getJSONObject("data")
                .getJSONObject("room_info")
                .getString("uid");
    }

    /**
     * B站直播获取背包礼物
     * @return JSONObject
     * @author srcrs
     * @Time 2020-10-13
     */
    public JSONArray xliveGiftBagList(){
        return Request.get("https://api.live.bilibili.com/xlive/web-room/v1/gift/bag_list")
                .getJSONObject("data")
                .getJSONArray("list");
    }

    /**
     * B站直播送出背包的礼物
     * @param biz_id roomId
     * @param ruid uid 用户id
     * @param bag_id 背包id
     * @param gift_id 礼物id
     * @param gift_num 礼物数量
     * @param storm_beat_id
     * @param price
     * @param platform 设备标识
     * @return JSONObject
     * @author srcrs
     * @Time 2020-10-13
     */
    public JSONObject xliveBagSend(String biz_id, String ruid, String bag_id, String gift_id, String gift_num, String storm_beat_id, String price, String platform){
        String body = "uid="+data.getMid()
                +"&gift_id="+gift_id
                +"&ruid="+ruid
                +"&send_ruid=0"
                +"&gift_num="+gift_num
                +"&bag_id="+bag_id
                +"&platform="+platform
                +"&biz_code="+"live"
                +"&biz_id="+biz_id
                +"&storm_beat_id="+storm_beat_id
                +"&price="+price
                +"&csrf="+data.getBili_jct();
        return Request.post("https://api.live.bilibili.com/gift/v2/live/bag_send", body);
    }
}
