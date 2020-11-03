package top.srcrs.bilibili.task.bigvip;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.srcrs.bilibili.Task;
import top.srcrs.bilibili.domain.Config;
import top.srcrs.bilibili.domain.Data;
import top.srcrs.bilibili.util.Request;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * 月底自动使用B币卷给自己充电
 * @author srcrs
 * @Time 2020-10-19
 */
public class BCoinApply implements Task {
    /** 获取日志记录器对象 */
    private static final Logger LOGGER = LoggerFactory.getLogger(BCoinApply.class);
    /** 获取DATA对象 */
    Data data = Data.getInstance();
    /** 获取用户自定义配置信息 */
    Config config = Config.getInstance();

    @Override
    public void run() {
        try{
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
            Integer day = cal.get(Calendar.DATE);
            /** B币券余额 */
            Integer couponBalance = Integer.parseInt(data.getCoupon_balance());
            if (day == 28 && couponBalance >= 2) {
                switch (config.getAutoBCoin()){
                    case "1" : doCharge(couponBalance);break;
                    case "2" : doMelonSeed(couponBalance);break;
                }
            }
        } catch (Exception e){
            LOGGER.error("使用B币卷部分异常 -- "+e);
        }
    }

    /**
     * 月底自动给自己充电。仅充会到期的B币券，低于2的时候不会充
     * @author srcrs
     * @Time 2020-10-19
     */
    public void doCharge(Integer couponBalance) {
        /**
         * 判断条件 是月底&&b币券余额大于2&&配置项允许自动充电
         */
        /** 被充电用户的userID */
        String userId = data.getMid();
        String body = "elec_num=" + couponBalance * 10
                + "&up_mid=" + userId
                + "&otype=up"
                + "&oid=" + userId
                + "&csrf=" + data.getBili_jct();

        JSONObject jsonObject = Request.post("http://api.bilibili.com/x/ugcpay/trade/elec/pay/quick", body);

        Integer resultCode = jsonObject.getInteger("code");
        if (resultCode == 0) {
            JSONObject dataJson = jsonObject.getJSONObject("data");
            LOGGER.debug(dataJson.toString());
            Integer statusCode = dataJson.getInteger("status");
            if (statusCode == 4) {
                LOGGER.info("月底了，给自己充电成功啦，送的B币券没有浪费啦");
                LOGGER.info("本次给自己充值了: " + couponBalance * 10 + "个电池");
                /** 获取充电留言token */
                String order_no = dataJson.getString("order_no");
                chargeComments(order_no);
            } else {
                LOGGER.warn("充电失败 -- " + jsonObject);
            }

        } else {
            LOGGER.warn("充电失败了啊 -- " + jsonObject);
        }
    }

    /**
     * 自动充电完，添加一条评论
     * @param token
     * @author srcrs
     * @Time 2020-10-19
     */
    public void chargeComments(String token) {

        String requestBody = "order_id=" + token
                + "&message=" + "BilibiliTask自动充电"
                + "&csrf=" + data.getBili_jct();
        JSONObject jsonObject = Request.post("http://api.bilibili.com/x/ugcpay/trade/elec/message", requestBody);
        LOGGER.debug(jsonObject.toString());
    }

    /**
     * 用B币卷兑换成金瓜子
     * @param couponBalance 传入B币卷的数量
     * @author srcrs
     * @Time 2020-11-02
     */
    public void doMelonSeed(Integer couponBalance){
        String body = "platform=pc"
                + "&pay_bp=" + couponBalance * 1000
                + "&context_id=1"
                + "&context_type=11"
                + "&goods_id=1"
                + "&goods_num=" + couponBalance
                + "&csrf=" + data.getBili_jct();
        JSONObject post = Request.post("https://api.live.bilibili.com/xlive/revenue/v1/order/createOrder", body);
        String msg = "";
        if("0".equals(post.getString("code"))){
            msg = "成功将 " + couponBalance + " B币卷兑换成 "+couponBalance*1000+" 金瓜子";
        } else{
            msg = post.getString("message");
        }
        LOGGER.info("B币卷兑换金瓜子 -- " + msg);
    }

}
