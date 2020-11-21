package top.srcrs.task.live;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.srcrs.Task;
import top.srcrs.domain.Config;
import top.srcrs.domain.UserData;
import top.srcrs.util.Request;

/**
 * B站直播送出即将过期的礼物
 * @author srcrs
 * @Time 2020-10-13
 */
public class GiveGiftTask implements Task {
    /** 获取日志记录器对象 */
    private static final Logger LOGGER = LoggerFactory.getLogger(GiveGiftTask.class);
    UserData userData = UserData.getInstance();
    Config config = Config.getInstance();

    @Override
    public void run(){
        try{
            /* 从配置类中读取是否需要执行赠送礼物 */
            if(!config.isGift()){
                LOGGER.info("【送即将过期礼物】: " + "自定义配置不送出即将过期礼物✔");
                return;
            }
            /* 直播间 id */
            String roomId = "";
            /* 直播间 uid 即 up 的 id*/
            String uid = "";
            /* B站后台时间戳为10位 */
            long nowTime = System.currentTimeMillis()/1000;
            /* 获得礼物列表 */
            JSONArray jsonArray = xliveGiftBagList();
            /* 判断是否有过期礼物出现 */
            boolean flag = true;
            for(Object object : jsonArray){
                JSONObject json = (JSONObject) object;
                long expireAt = Long.parseLong(json.getString("expire_at"));
                /* 礼物还剩1天送出 */
                /* 永久礼物到期时间为0 */
                if((expireAt-nowTime) < 87000 && expireAt != 0){
                    /* 如果有未送出的礼物，则获取一个直播间 */
                    if("".equals(roomId)){
                        JSONObject uidAndRid = getuidAndRid();
                        uid = uidAndRid.getString("uid");
                        roomId = uidAndRid.getString("roomId");
                    }
                    JSONObject jsonObject3 = xliveBagSend(
                            roomId,
                            uid,
                            json.getString("bag_id"),
                            json.getString("gift_id"),
                            json.getString("gift_num"),
                            "0",
                            "0", "pc");
                    if("0".equals(jsonObject3.getString("code"))){
                        /* 礼物的名字 */
                        String giftName = jsonObject3.getJSONObject("data").getString("gift_name");
                        /* 礼物的数量 */
                        String giftNum = jsonObject3.getJSONObject("data").getString("gift_num");
                        LOGGER.info("【送即将过期礼物】: 给直播间 - {} - {} - 数量: {}✔",roomId,giftName,giftNum);
                        flag = false;
                    }
                    else{
                        LOGGER.warn("【送即将过期礼物】: 失败, 原因 : {}❌", jsonObject3);
                    }
                }
            }
            if(flag){
                LOGGER.info("【送即将过期礼物】: " + "当前无即将过期礼物❌");
            }
        } catch (Exception e){
            LOGGER.error("💔赠送礼物异常 : ", e);
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
     * @param roomId up 主的 uid
     * @return JSONObject
     * @author srcrs
     * @Time 2020-10-13
     */
    public String xliveGetRoomUid(String roomId){
        String param = "?room_id="+roomId;
        return Request.get("https://api.live.bilibili.com/xlive/web-room/v1/index/getInfoByRoom" + param)
                .getJSONObject("data")
                .getJSONObject("room_info")
                .getString("uid");
    }

    /**
     * 根据 uid 获取其 roomid
     * @param mid 即 uid
     * @return String 返回一个直播间id
     * @author srcrs
     * @Time 2020-11-20
     */
    public String getRoomInfoOld(String mid) {
        String param = "?mid="+mid;
        return Request.get("http://api.live.bilibili.com/room/v1/Room/getRoomInfoOld"+param)
                .getJSONObject("data")
                .getString("roomid");
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
     * @param bizId roomId
     * @param ruid uid 用户id
     * @param bagId 背包id
     * @param giftId 礼物id
     * @param giftNum 礼物数量
     * @param stormBeatId 未知意思
     * @param price 价格
     * @param platform 设备标识
     * @return JSONObject
     * @author srcrs
     * @Time 2020-10-13
     */
    public JSONObject xliveBagSend(
            String bizId,
            String ruid,
            String bagId,
            String giftId,
            String giftNum,
            String stormBeatId,
            String price, String platform){
        String body = "uid=" + userData.getMid()
                      + "&gift_id=" + giftId
                      + "&ruid=" + ruid
                      + "&send_ruid=0"
                      + "&gift_num=" + giftNum
                      + "&bag_id=" + bagId
                      + "&platform=" + platform
                      + "&biz_code=" + "live"
                      + "&biz_id=" + bizId
                      + "&storm_beat_id=" + stormBeatId
                      + "&price=" + price
                      + "&csrf=" + userData.getBiliJct();
        return Request.post("https://api.live.bilibili.com/gift/v2/live/bag_send", body);
    }

    /**
     * 获取一个包含 uid 和 RooId 的 json 对象
     * @return JSONObject 返回一个包含 uid 和 RooId 的 json 对象
     * @author srcrs
     * @Time 2020-11-20
     */
    public JSONObject getuidAndRid(){
        /* 直播间 id */
        String roomId;
        /* 直播间 uid 即 up 的 id*/
        String uid;
        if(config.getUpLive() != null){
            /* 获取指定up的id */
            uid = config.getUpLive();
            roomId = getRoomInfoOld(uid);
            String status = "0";
            if(status.equals(roomId)){
                LOGGER.info("【获取直播间】: 自定义up {} 无直播间", uid);
                /* 随机获取一个直播间 */
                roomId = xliveGetRecommend();
                uid = xliveGetRoomUid(roomId);
                LOGGER.info("【获取直播间】: 随机直播间");
            } else{
                LOGGER.info("【获取直播间】: 自定义up {} 的直播间", uid);
            }

        } else{
            /* 随机获取一个直播间 */
            roomId = xliveGetRecommend();
            uid = xliveGetRoomUid(roomId);
            LOGGER.info("【获取直播间】: " + "随机直播间");
        }
        JSONObject json = new JSONObject();
        json.put("uid",uid);
        json.put("roomId",roomId);
        return json;
    }
}
