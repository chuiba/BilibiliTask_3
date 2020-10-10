package org.example;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.example.domain.WebAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DailyTasks {
    // 获取日志记录对象
    private static final Logger LOGGER = LoggerFactory.getLogger(DailyTasks.class);
    Integer reward;
    WebAPI webAPI = new WebAPI();
    public DailyTasks() throws Exception {
        this.run();
    }

    public void run(){
        LOGGER.info("正在执行每日任务"+"-------->"+"稍等");
        Function function = Function.getInstance();
        // 获取经验信息，进而获取需要投币数
        Integer coin_throw_num = null;
        try{
            reward = Integer.parseInt(function.getReward().getString("coins_av"));
            coin_throw_num = (50-reward)/10;
        } catch (Exception e){
            LOGGER.error("获取经验信息异常"+"-------->"+e);
        }
        // 代表硬币剩余数
        Integer coin_num;
        try{
            coin_num = function.getCoin();
            coin_throw_num = coin_num >= coin_throw_num ? coin_throw_num : coin_num;
        } catch (Exception e){
            LOGGER.error("获取硬币数量失败"+"-------->"+e);
        }
        // 获取分区视频信息
        JSONArray regions=null;
        try{
            regions = function.getRegions("6", "1");
        } catch (Exception e){
            LOGGER.error("获取分区视频失败"+"-------->"+e);
        }
        // 执行投币
        try{
            for(int i=0;i<coin_throw_num;i++){
                JSONObject json = function.throwCoin(regions.getJSONObject(i).getString("aid"),"1","1");
                LOGGER.info("投币信息"+"-------->"+json);
            }
        } catch (Exception e){
            LOGGER.error("投币异常"+"-------->"+e);
        }
        // 模拟视频观看
        try{
            JSONObject json = function.report(regions.getJSONObject(5).getString("aid"),regions.getJSONObject(5).getString("cid"),"300");
            LOGGER.info("模拟观看视频进度"+"-------->"+json);
        } catch (Exception e){
            LOGGER.error("模拟观看视频异常"+"-------->"+e);
        }
        // 分享视频
        try{
            JSONObject json = function.share(regions.getJSONObject(5).getString("aid"));
            LOGGER.info("分享视频结果"+"-------->"+json);
        } catch (Exception e){
            LOGGER.error("分享视频失败"+"-------->"+e);
        }
        LOGGER.info("每日任务"+"-------->"+"完成");
    }
}
