package top.srcrs.bilibili.task.bigvip;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.srcrs.bilibili.Task;
import top.srcrs.bilibili.domain.Data;
import top.srcrs.bilibili.util.Request;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * 每个月1号，年度大会员领取B币卷，领取会员权益。
 * @author srcrs
 * @Time 2020-10-19
 */
public class CollectVipGift implements Task {
    /** 获取日志记录器对象 */
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectVipGift.class);
    /** 获取DATA对象 */
    Data data = Data.getInstance();

    @Override
    public void run() {
        try{

        } catch (Exception e){
            LOGGER.error("领取年度大会员礼包错误 -- "+e);
        }
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
        Integer day = cal.get(Calendar.DATE);
        Integer vipType = queryVipStatusType();

        /** 每个月1号，年度大会员领取权益 */
        if(day==1&&vipType==2){
            vipPrivilege(1);
            vipPrivilege(2);
        }
    }

    /**
     * 领取年度大会员B卷和大会员福利/权益
     * @param type
     * @author srcrs
     * @Time 2020-10-19
     */
    public void vipPrivilege(Integer type) {
        String body = "type=" + type
                + "&csrf=" + data.getBili_jct();
        JSONObject jsonObject = Request.post("https://api.bilibili.com/x/vip/privilege/receive", body);
        Integer code = jsonObject.getInteger("code");
        if (0 == code) {
            if (type == 1) {
                LOGGER.info("领取年度大会员每月赠送的B币券 -- 成功");
            } else if (type == 2) {
                LOGGER.info("领取大会员福利/权益 -- 成功");
            }

        } else {
            LOGGER.warn("领取年度大会员每月赠送的B币券/大会员福利 -- 失败 -- " + jsonObject.getString("message"));
        }
    }

    /**
     * 检查用户的会员状态。如果是会员则返回其会员类型。
     * 0 不是会员
     * 1 是大会员
     * 2 是年度大会员
     * @return Integer
     * @author srcrs
     * @Time 2020-10-19
     */
    public Integer queryVipStatusType() {
        if ("1".equals(data.getVipStatus())) {
            return Integer.parseInt(data.getVipType());
        } else {
            return 0;
        }
    }
}
