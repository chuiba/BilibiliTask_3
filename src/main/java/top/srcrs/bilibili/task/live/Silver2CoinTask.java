package top.srcrs.bilibili.task.live;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.srcrs.bilibili.Task;
import top.srcrs.bilibili.domain.Config;
import top.srcrs.bilibili.domain.Data;
import top.srcrs.bilibili.util.Request;

/**
 * 银瓜子兑换硬币
 * @author srcrs
 * @Time 2020-10-13
 */
public class Silver2CoinTask implements Task {
    /** 获取日志记录器对象 */
    private static final Logger LOGGER = LoggerFactory.getLogger(BLiveTask.class);
    Data data = Data.getInstance();
    Config config = Config.getInstance();

    @Override
    public void run() throws Exception{
        try{
            if(config.isS2c()){
                LOGGER.info("银瓜子兑换硬币,-{}",silver2coin());
            }
        } catch (Exception e){
            LOGGER.error("银瓜子兑换硬币,原因-{}",e);
        }
    }

    /**
     * 银瓜子兑换成硬币
     * @return JSONObject
     * @author srcrs
     * @Time 2020-10-13
     */
    public JSONObject silver2coin(){
        String body = "csrf_token="+data.getBili_jct();
        return Request.post("https://api.live.bilibili.com/pay/v1/Exchange/silver2coin", body);
    }
}
