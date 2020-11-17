package top.srcrs.task.daily;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.srcrs.Task;
import top.srcrs.domain.Data;
import top.srcrs.util.Request;

import java.util.Random;

/**
 * 完成B站每日任务，观看，分享视频
 * @author srcrs
 * @Time 2020-10-13
 */
public class DailyTask implements Task {
    /** 获取日志记录器对象 */
    private static final Logger LOGGER = LoggerFactory.getLogger(DailyTask.class);
    /** 获取DATA对象 */
    Data data = Data.getInstance();

    @Override
    public void run() {
        try {
            JSONObject reward = getReward();
            /* 今天是否完成分享视频任务 */
            boolean isShare = reward.getBoolean("share_av");
            /* 今天是否完成观看视频任务 */
            boolean isWatch = reward.getBoolean("watch_av");
            /* 如果模拟观看视频和分享视频还未做完。
               这里做了一个小小的优化，如果这两个任务都完成，就不必再发送请求获取视频了。
            */
            if(isWatch&&isShare){

                LOGGER.info("【模拟观看视频】: " + "今日已经观看过视频❌");
                LOGGER.info("【分享视频】: " + "今日已经分享过视频❌");
                return;
            }
            /* 获取B站推荐视频 */
            JSONArray regions = getRegions("6", "1");
            if(isWatch){
                LOGGER.info("【模拟观看视频】: " + "今日已经观看过视频❌");
            } else{
                String aid = regions.getJSONObject(5).getString("aid");
                /* 随机观看时间 */
                int time = new Random().nextInt(duration(aid)-2) + 2;
                String cid = regions.getJSONObject(5).getString("cid");
                JSONObject report = report(aid, cid, ""+time);
                LOGGER.info("【模拟观看视频】: {}", "0".equals(report.getString("code")) ? "成功✔" : "失败❌");
            }
            if(isShare){
                LOGGER.info("【分享视频】: " + "今日已经分享过视频❌");
            } else{
                JSONObject share = share(regions.getJSONObject(5).getString("aid"));
                LOGGER.info("【分享视频】: {}", "0".equals(share.getString("code")) ? "成功✔" : "失败❌");
            }
        } catch (Exception e) {
            LOGGER.error("💔每日任务异常 : " + e);
        }
    }

    /**
     * 获取B站推荐视频
     *
     * @param ps  代表你要获得几个视频
     * @param rid B站分区推荐视频
     * @return JSONArray
     * @author srcrs
     * @Time 2020-10-13
     */
    public JSONArray getRegions(String ps, String rid) {
        String params = "?ps=" + ps + "&rid=" + rid;
        JSONObject jsonObject = Request.get("https://api.bilibili.com/x/web-interface/dynamic/region" + params);
        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("archives");
        JSONArray jsonRegions = new JSONArray();
        for (Object object : jsonArray) {
            JSONObject json = (JSONObject) object;
            JSONObject cache = new JSONObject();
            cache.put("title", json.getString("title"));
            cache.put("aid", json.getString("aid"));
            cache.put("bvid", json.getString("bvid"));
            cache.put("cid", json.getString("cid"));
            jsonRegions.add(cache);
        }
        return jsonRegions;
    }

    /**
     * 模拟观看视频
     * @param aid     视频 aid 号
     * @param cid     视频 cid 号
     * @param progres 模拟观看的时间
     * @return JSONObject
     * @author srcrs
     * @Time 2020-10-13
     */
    public JSONObject report(String aid, String cid, String progres) {
        String body = "aid=" + aid
                + "&cid=" + cid
                + "&progres=" + progres
                + "&csrf=" + data.getBiliJct();
        return Request.post("http://api.bilibili.com/x/v2/history/report", body);
    }

    /**
     * 分享指定的视频
     *
     * @param aid 视频的aid
     * @return JSONObject
     * @author srcrs
     * @Time 2020-10-13
     */
    public JSONObject share(String aid) {
        String body = "aid=" + aid + "&csrf=" + data.getBiliJct();
        return Request.post("https://api.bilibili.com/x/web-interface/share/add", body);
    }

    /**
     * 获取每日得到经验信息
     * @return JSONObject
     * @author srcrs
     * @Time 2020-10-13
     */
    public JSONObject getReward() {
        return Request.get("https://account.bilibili.com/home/reward").getJSONObject("data");
    }

    /**
     * 获取视频的播放时间 (单位 秒)
     * @param aid 视频的 aid 号
     * @return int 视频的播放时间
     * @author srcrs
     * @Time 2020-11-17
     */
    private int duration(String aid){
        String param = "?aid="+aid;
        return Request.get("https://api.bilibili.com/x/player/pagelist"+param)
                .getJSONArray("data")
                .getJSONObject(0)
                .getIntValue("duration");
    }
}
