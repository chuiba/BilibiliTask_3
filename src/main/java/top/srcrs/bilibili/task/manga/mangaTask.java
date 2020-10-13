package top.srcrs.bilibili.task.manga;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.srcrs.bilibili.Task;
import top.srcrs.bilibili.task.live.BLiveTask;
import top.srcrs.bilibili.util.Request;

/**
 * 完成漫画任务，暂时只实现了签到
 * @author srcrs
 * @Time 2020-10-13
 */
public class mangaTask implements Task {
    /** 获取日志记录器对象 */
    private static final Logger LOGGER = LoggerFactory.getLogger(mangaTask.class);

    @Override
    public void run(){
        try{
            JSONObject jsonObject = mangaClockIn("android");
            LOGGER.info("漫画签到,-{}",jsonObject);
        } catch (Exception e){
            LOGGER.error("漫画签到异常,-{}",e);
        }
    }

    /**
     * 模拟漫画app签到
     * @param platform
     * @return JSONObject
     * @author srcrs
     * @Time 2020-10-13
     */
    public JSONObject mangaClockIn(String platform){
        String body = "platform="+platform;
        JSONObject post = Request.post("https://manga.bilibili.com/twirp/activity.v1.Activity/ClockIn", body);
        return post;
    }
}
