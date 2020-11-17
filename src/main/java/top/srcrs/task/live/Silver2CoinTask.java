package top.srcrs.task.live;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.srcrs.Task;
import top.srcrs.domain.Config;
import top.srcrs.domain.Data;
import top.srcrs.util.Request;

/**
 * 银瓜子兑换硬币
 * @author srcrs
 * @Time 2020-10-13
 */
public class Silver2CoinTask implements Task {
    /** 获取日志记录器对象 */
    private static final Logger LOGGER = LoggerFactory.getLogger(Silver2CoinTask.class);
    Data data = Data.getInstance();
    Config config = Config.getInstance();

    @Override
    public void run(){
        /* 获得银瓜子的数量 */
        Integer silver = getSilver();
        LOGGER.info("【银瓜子】: {}",silver);
        if(config.isS2c()){
            try{
                /* 如果银瓜子数量小于700没有必要再进行兑换 */
                int minSilver = 700;
                if(silver < minSilver){
                    LOGGER.info("【银瓜子兑换硬币】: {}","银瓜子余额不足❌");
                } else{
                    LOGGER.warn("【银瓜子兑换硬币】: {}",silver2coin().getString("msg") + "✔");
                }
            } catch (Exception e){
                LOGGER.error("💔银瓜子兑换硬币错误 : " + e);
            }
        } else{
            LOGGER.info("【银瓜子兑换硬币】: " + "自定义配置不将银瓜子兑换硬币✔");
        }
    }

    /**
     * 银瓜子兑换成硬币
     * @return JSONObject
     * @author srcrs
     * @Time 2020-10-13
     */
    public JSONObject silver2coin(){
        String body = "csrf="+data.getBiliJct();
        return Request.post("https://api.live.bilibili.com/pay/v1/Exchange/silver2coin", body);
    }

    /**
     * 获取银瓜子的数量
     * @return Integer
     * @author srcrs
     * @Time 2020-10-17
     */
    public Integer getSilver(){
        JSONObject jsonObject = Request.get("https://api.live.bilibili.com/xlive/web-ucenter/user/get_user_info");
        return Integer.parseInt(jsonObject.getJSONObject("data").getString("silver"));
    }
}
