package top.srcrs.task.daily;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.srcrs.Task;
import top.srcrs.domain.Config;
import top.srcrs.domain.Data;
import top.srcrs.util.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 进行视频投币
 * @author srcrs
 * @Time 2020-10-13
 */
public class ThrowCoinTask implements Task {
    /** 获取日志记录器对象 */
    private static final Logger LOGGER = LoggerFactory.getLogger(ThrowCoinTask.class);
    /** 获取DATA对象 */
    private static final Data DATA = Data.getInstance();
    Config config = Config.getInstance();

    @Override
    public void run() {
        try {
            /* 今天投币获得了多少经验 */
            int reward = getReward();
            /* navData */
            JSONObject navData = getNav();
            /* 还剩多少个硬币 */
            int num2 = (int)navData.getDoubleValue("money");
            /* 配置类中设置投币数 */
            int num3 = config.getCoin();
            /* 计算今天需要投 num1 个硬币
               当今日已经投过 num3 个硬币将不再进行投币
               否则则应该投 (num3-num1) 个硬币
            */
            int num1 = (num3*10 - reward) / 10;
            /* 避免设置投币数为负数异常 */
            num3 = Math.max(num3,0);
            /* 实际需要投 num个硬币 */
            int num = Math.min(num3,Math.min(num1,num2));
            LOGGER.info("【投币计算】: 当前硬币数: " + num2
                    + " ,自定义投币数: " + num3
                    + " ,今日已投币: " + reward/10
                    + " ,还需投币: "+num1
                    + " ,实际投币: "+num);
            if(num == 0){
                LOGGER.info("【投币】: " + "当前无需执行投币操作❌");
            }
            /* 获取视频信息，优先级为:
                     自定义配置 up 主发布的最新视频(前 30 条) >
                     当前用户动态列表投稿视频(已关注 up 主视频投稿都会在动态列表出现)(前 20 条) >
                     随机从分区热门视频中获取(前六条)
            */
            List<String> videoAid = new ArrayList<>();
            /* 获取自定义配置中 up 主投稿的30条最新视频 */
            if(config.getUpList() == null && num > 0){
                LOGGER.info("【优先投币up】: " + "未配置优先投币up主");
            } else{
                if(num - videoAid.size() > 0){
                    for(String up : config.getUpList()){
                        videoAid.addAll(spaceSearch(up,num - videoAid.size()));
                        LOGGER.info("【优先投币up " + up + " 】: "
                                + "成功获取到: " + videoAid.size() + " 个视频");
                    }
                }
            }
            /* 获取当前用户最新的20条动态投稿视频列表 */
            if(num - videoAid.size() > 0){
                videoAid.addAll(dynamicNew(num - videoAid.size()));
                LOGGER.info("【用户动态列表】: " + "成功获取到: " + videoAid.size() + " 个视频");
            }
            /* 获取分区视频 */
            if(num - videoAid.size() > 0){
                videoAid.addAll(getRegions("6", "1",num - videoAid.size()));
                LOGGER.info("【分区热门视频】: " + "成功获取到: " + videoAid.size() + " 个视频");
            }
            /* 给每个视频投 1 个币,点 1 个赞 */
            for (int i = 0; i < num; i++) {
                /* 视频的aid */
                String aid = videoAid.get(i);
                JSONObject json = throwCoin(aid, "1", "1");
                /* 输出的日志消息 */
                String msg ;
                if ("0".equals(json.getString("code"))) {
                    msg = "硬币-1✔";
                } else {
                    msg = json.getString("message") + "❌";
                }
                LOGGER.info("【投币】: 给视频 - av{} - {}", aid, msg);
                /* 投完币等待1-2秒 */
                Thread.sleep(new Random().nextInt(1000)+1000);
            }
            update(navData);
        } catch (Exception e) {
            LOGGER.info("💔投币异常 : " + e);
        }
    }

    /**
     * 给视频投币
     * @param aid         视频 aid 号
     * @param num         投币数量
     * @param selectLike 是否点赞
     * @return JSONObject 返回投币的结果
     * @author srcrs
     * @Time 2020-10-13
     */
    public JSONObject throwCoin(String aid, String num, String selectLike) {

        String body = "aid=" + aid
                + "&multiply=" + num
                + "&select_like=" + selectLike
                + "&cross_domain=" + "true"
                + "&csrf=" + DATA.getBiliJct();
        return Request.post("https://api.bilibili.com/x/web-interface/coin/add", body);
    }

    /**
     * 获取今天投币所得经验
     * @return JSONObject
     * @author srcrs
     * @Time 2020-10-13
     */
    public Integer getReward() {
        JSONObject jsonObject = Request.get("https://account.bilibili.com/home/reward");
        return Integer.parseInt(jsonObject.getJSONObject("data").getString("coins_av"));
    }

    /**
     * 获取账号信息(经验，硬币数，B币卷等等)
     * @return JSONObject 返回账户 json 数据
     * @author srcrs
     * @Time 2020-11-17
     */
    public JSONObject getNav() {
        return Request.get("https://api.bilibili.com/x/web-interface/nav?build=0&mobi_app=web")
                .getJSONObject("data");
    }

    /**
     * 获取B站分区视频信息
     * @param ps  获取视频的数量
     * @param rid 分区号
     * @return JSONArray
     * @author srcrs
     * @Time 2020-10-13
     */
    public List<String> getRegions(String ps, String rid, int num) {
        String params = "?ps=" + ps + "&rid=" + rid;
        JSONObject jsonObject = Request.get("https://api.bilibili.com/x/web-interface/dynamic/region" + params);
        JSONArray archives = jsonObject.getJSONObject("data").getJSONArray("archives");
        List<String> videoAid = new ArrayList<>();
        for (Object object : archives) {
            JSONObject archive = (JSONObject) object;
            String aid = archive.getString("aid");
            String mid = archive.getJSONObject("owner").getString("mid");
            if(isThrowCoins(aid)){
                /* 可能会碰到自己的视频 */
                if(!(DATA.getMid().equals(mid))){
                    videoAid.add(aid);
                }
            }
            if(videoAid.size()>=num){
                break;
            }
        }
        return videoAid;
    }

    /**
     * 更新 Data 实体类中的账户信息
     * @param navData 用户的数据信息
     * @author srcrs
     * @Time 2020-11-17
     */
    private void update(JSONObject navData){
        /* 考虑到需要计算还剩几天升级，需要更新 Data 类中的结果 */
        /* 更新Data实体类中硬币剩余数 */
        DATA.setMoney(navData.getString("money"));
        /* 更新Data实体类中的经验数 */
        DATA.setCurrentExp(navData.getJSONObject("level_info").getString("current_exp"));
        /* 更新Data实体类中的等级 */
        DATA.setCurrentLevel(navData.getJSONObject("level_info").getString("current_level"));
        /* 更新Data实体类中的升级到下一级所需要的经验数 */
        DATA.setNextExp(navData.getJSONObject("level_info").getString("next_exp"));
    }

    /**
     * 获取当前用户最新的20条动态投稿视频列表
     * @return List<String> 返回将要投币视频的aid
     * @author srcrs
     * @Time 2020-11-17
     */
    private List<String> dynamicNew(int num){
        String param = "?uid="+DATA.getMid()+"&type_list=8";
        JSONObject dynamic = Request.get("https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/dynamic_new"+param);
        List<String> videoAid = new ArrayList<>();
        String success = "0";
        String key = "code";
        if(success.equals(dynamic.getString(key))){
            JSONArray cards = dynamic.getJSONObject("data").getJSONArray("cards");
            for(Object object : cards){
                JSONObject card = (JSONObject) object;
                String aid = card.getJSONObject("desc").getString("rid");
                String mid = card.getJSONObject("desc").getString("rid");
                if(isThrowCoins(aid)){
                    /* 可能会碰到自己的视频 */
                    if(!(DATA.getMid().equals(mid))){
                        videoAid.add(aid);
                    }
                }
                if(videoAid.size()>=num){
                    break;
                }
            }
        }
        return videoAid;
    }

    /**
     * 判断视频是否被投币
     * @param aid 视频的 aid 号
     * @return boolean 是否投币过
     * @author srcrs
     * @Time 2020-11-17
     */
    private boolean isThrowCoins(String aid){
        String param = "?aid="+aid;
        JSONObject object = Request.get("http://api.bilibili.com/x/web-interface/archive/coins"+param);
        int multiply = object.getJSONObject("data").getIntValue("multiply");
        return multiply == 0;
    }

    /**
     * 获取 up 主的最新30条视频投稿信息
     * 从中挑选从未投币的视频进行投币
     * @param uid up 主的 uid 号
     * @return List<String> 返回未投币的5个视频
     * @param num 需要投币的数量
     * @author srcrs
     * @Time 2020-11-17
     */
    private List<String> spaceSearch(String uid,int num){
        String param = "?mid="+uid;
        JSONObject spaceVideo = Request.get("https://api.bilibili.com/x/space/arc/search"+param);
        List<String> videoAid = new ArrayList<>();
        String success = "0";
        String key = "code";
        if(success.equals(spaceVideo.getString(key))){
            JSONArray vList = spaceVideo.getJSONObject("data")
                    .getJSONObject("list")
                    .getJSONArray("vlist");
            for(Object object : vList){
                JSONObject video = (JSONObject) object;
                String aid = video.getString("aid");
                String mid = video.getString("mid");
                if(isThrowCoins(aid)){
                    /* 可能会碰到自己的视频 */
                    if(!(DATA.getMid().equals(mid))){
                        videoAid.add(aid);
                    }
                }
                if(videoAid.size()>=num){
                    break;
                }
            }
        }
        return videoAid;
    }
}
