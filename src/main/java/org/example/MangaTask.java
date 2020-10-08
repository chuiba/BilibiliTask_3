package org.example;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MangaTask {
    // 获取日志记录对象
    public static final Logger LOGGER = LoggerFactory.getLogger(DailyTasks.class);
    public MangaTask() throws Exception {
        this.run();
    }
    public void run() throws Exception {
        Function function = Function.FUNCTION;
        LOGGER.info("正在开始漫画签到"+"-------->"+"稍等");
        try {
            JSONObject jsonObject = function.mangaClockIn("android");
            if("0".equals(jsonObject.getString("code"))){
                LOGGER.info("漫画签到成功"+"-------->"+jsonObject);
            }
            else{
                LOGGER.warn("漫画签到失败"+"-------->"+jsonObject);
            }
        } catch (Exception e){
            LOGGER.error("漫画签到失败"+"-------->"+e);
        }
        LOGGER.info("漫画操作"+"-------->"+"完成");
    }
}
