package top.srcrs.task.daily;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import top.srcrs.Task;
import top.srcrs.domain.UserData;
import top.srcrs.util.Request;

import java.util.Random;

/**
 * 完成B站每日任务，观看，分享视频
 *
 * @author srcrs
 * @Time 2020-10-13
 */
@Slf4j
public class DailyTask implements Task {
    /**
     * 获取DATA对象
     */
    UserData userData = UserData.getInstance();

    @Override
    public void run() {
        try {
            JSONObject reward = getReward();
            log.info("📊每日任务状态: {}", reward.toJSONString());

            /* 今天是否完成分享视频任务 */
            boolean isShare = reward.getBooleanValue("share_av");
            /* 今天是否完成观看视频任务 */
            boolean isWatch = reward.getBooleanValue("watch_av");
            /* 如果模拟观看视频和分享视频还未做完。
               这里做了一个小小的优化，如果这两个任务都完成，就不必再发送请求获取视频了。
            */
            if (isWatch && isShare) {

                log.info("【模拟观看视频】: " + "今日已经观看过视频❌");
                log.info("【分享视频】: " + "今日已经分享过视频❌");
                return;
            }
            /* 获取B站推荐视频 */
            JSONArray regions = getRegions("6", "1");
            if (isWatch) {
                log.info("【模拟观看视频】: " + "今日已经观看过视频❌");
            } else {
                String aid = regions.getJSONObject(5).getString("aid");
                /* 随机观看时间 */
                int time = new Random().nextInt(duration(aid) - 2) + 2;
                String cid = regions.getJSONObject(5).getString("cid");
                JSONObject report = report(aid, cid, "" + time);
                log.info("【模拟观看视频】: {}", "0".equals(report.getString("code")) ? "成功✔" : "失败❌");
            }
            if (isShare) {
                log.info("【分享视频】: " + "今日已经分享过视频❌");
            } else {
                JSONObject share = share(regions.getJSONObject(5).getString("aid"));
                log.info("【分享视频】: {}", "0".equals(share.getString("code")) ? "成功✔" : "失败❌");
            }
        } catch (Exception e) {
            log.error("💔每日任务异常 : ", e);
            throw new RuntimeException("每日任务执行失败", e);
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
        try {
            JSONObject pJson = new JSONObject();
            pJson.put("ps", ps);
            pJson.put("rid", rid);
            pJson.put("fresh_type", "3");
            pJson.put("version", "1");
            pJson.put("fresh_idx_1h", "1");
            pJson.put("fetch_row", "1");
            pJson.put("fresh_idx", "1");
            pJson.put("brush", "0");
            pJson.put("homepage_ver", "1");
            pJson.put("ps", "12");
            
            // 优先使用新的推荐API
            JSONObject jsonObject = Request.getWithWbi(
                "https://api.bilibili.com/x/web-interface/wbi/index/top/feed/rcmd", 
                pJson
            );
            
            if ("0".equals(jsonObject.getString("code"))) {
                JSONObject data = jsonObject.getJSONObject("data");
                if (data.containsKey("item")) {
                    return processRecommendVideos(data.getJSONArray("item"));
                }
            }
            
            // 降级到分区API
            log.warn("推荐API失败，使用分区API");
            return getRegionVideos(ps, rid);
            
        } catch (Exception e) {
            log.error("获取视频列表失败: ", e);
            return getBackupVideoList();
        }
    }

    /**
     * 新增方法处理推荐视频数据
     */
    private JSONArray processRecommendVideos(JSONArray items) {
        JSONArray result = new JSONArray();
        for (Object item : items) {
            JSONObject video = (JSONObject) item;
            JSONObject processed = new JSONObject();
            processed.put("title", video.getString("title"));
            processed.put("aid", video.getString("id"));
            processed.put("bvid", video.getString("bvid"));
            processed.put("cid", video.getString("cid"));
            result.add(processed);
        }
        return result;
    }

    /**
     * 获取分区视频（降级方案）
     */
    private JSONArray getRegionVideos(String ps, String rid) {
        try {
            JSONObject pJson = new JSONObject();
            pJson.put("ps", ps);
            pJson.put("rid", rid);
            
            JSONObject jsonObject = Request.getWithWbi(
                "https://api.bilibili.com/x/web-interface/dynamic/region", 
                pJson
            );
            
            if ("0".equals(jsonObject.getString("code"))) {
                JSONObject data = jsonObject.getJSONObject("data");
                if (data.containsKey("archives")) {
                    JSONArray archives = data.getJSONArray("archives");
                    JSONArray result = new JSONArray();
                    
                    for (Object object : archives) {
                        JSONObject json = (JSONObject) object;
                        JSONObject cache = new JSONObject();
                        cache.put("title", json.getString("title"));
                        cache.put("aid", json.getString("aid"));
                        cache.put("bvid", json.getString("bvid"));
                        cache.put("cid", json.getString("cid"));
                        result.add(cache);
                        
                        if (result.size() >= Integer.parseInt(ps)) {
                            break;
                        }
                    }
                    return result;
                }
            }
        } catch (Exception e) {
            log.error("分区API调用失败: ", e);
        }
        
        return getBackupVideoList();
    }

    /**
     * 备用视频列表（避免完全失败）
     */
    private JSONArray getBackupVideoList() {
        JSONArray backupList = new JSONArray();
        // 添加一些固定的热门视频作为备用
        JSONObject backup = new JSONObject();
        backup.put("title", "备用视频");
        backup.put("aid", "1");
        backup.put("bvid", "BV1xx411c7mD");
        backup.put("cid", "1");
        backupList.add(backup);
        return backupList;
    }

    /**
     * 模拟观看视频
     *
     * @param aid     视频 aid 号
     * @param cid     视频 cid 号
     * @param progres 模拟观看的时间
     * @return JSONObject
     * @author srcrs
     * @Time 2020-10-13
     */
    public JSONObject report(String aid, String cid, String progres) {
        JSONObject pJson = new JSONObject();
        pJson.put("aid", aid);
        pJson.put("cid", cid);
        pJson.put("progres", progres);
        pJson.put("csrf", userData.getBiliJct());
        return Request.post("https://api.bilibili.com/x/v2/history/report", pJson);
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
        JSONObject pJson = new JSONObject();
        pJson.put("aid", aid);
        pJson.put("csrf", userData.getBiliJct());
        return Request.post("https://api.bilibili.com/x/web-interface/share/add", pJson);
    }

    /**
     * 获取每日得到经验信息
     * 使用新的API端点和WBI签名认证
     *
     * @return JSONObject
     * @author chuiba (updated from srcrs)
     * @Time 2025-01-21
     */
    public JSONObject getReward() {
        try {
            // 使用新的WBI签名API获取每日任务状态
            JSONObject params = new JSONObject();
            JSONObject response = Request.getWithWbi("https://api.bilibili.com/x/member/web/exp/reward", params);

            if ("0".equals(response.getString("code"))) {
                return response.getJSONObject("data");
            } else {
                log.warn("每日任务API返回错误: {} - {}", response.getString("code"), response.getString("message"));
                // 如果新API失败，尝试使用导航API获取基础信息
                return getBasicExpInfo();
            }
        } catch (Exception e) {
            log.warn("新API调用失败，尝试使用导航API: ", e);
            return getBasicExpInfo();
        }
    }

    /**
     * 从导航API获取基础经验信息（备用方案）
     */
    private JSONObject getBasicExpInfo() {
        try {
            JSONObject navResp = Request.get("https://api.bilibili.com/x/web-interface/nav");
            if ("0".equals(navResp.getString("code"))) {
                JSONObject data = navResp.getJSONObject("data");

                // 构造兼容的返回格式
                JSONObject result = new JSONObject();
                result.put("login", true); // 能获取到导航信息说明已登录
                result.put("watch_av", false); // 默认为未完成，让程序尝试执行
                result.put("share_av", false); // 默认为未完成，让程序尝试执行
                result.put("coins_av", 0); // 默认为0，让程序尝试投币

                return result;
            }
        } catch (Exception e) {
            log.error("导航API调用失败: ", e);
        }

        // 返回默认值避免空指针
        JSONObject defaultResult = new JSONObject();
        defaultResult.put("login", false);
        defaultResult.put("watch_av", false);
        defaultResult.put("share_av", false);
        defaultResult.put("coins_av", 0);
        return defaultResult;
    }

    /**
     * 获取视频的播放时间 (单位 秒)
     *
     * @param aid 视频的 aid 号
     * @return int 视频的播放时间
     * @author srcrs
     * @Time 2020-11-17
     */
    private int duration(String aid) {
        JSONObject pJson = new JSONObject();
        pJson.put("aid", aid);
        return Request.get("https://api.bilibili.com/x/player/pagelist", pJson)
                      .getJSONArray("data")
                      .getJSONObject(0)
                      .getIntValue("duration");
    }
}
