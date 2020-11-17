package top.srcrs.task.bigvip;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.srcrs.Task;
import top.srcrs.domain.Config;
import top.srcrs.domain.Data;
import top.srcrs.util.Request;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * 月底 (28号) 自动使用 B 币卷给自己充电
 * @author srcrs
 * @Time 2020-10-19
 */
public class BiCoinApply implements Task {
    /** 获取日志记录器对象 */
    private static final Logger LOGGER = LoggerFactory.getLogger(BiCoinApply.class);
    /** 获取DATA对象 */
    Data data = Data.getInstance();
    /** 获取用户自定义配置信息 */
    Config config = Config.getInstance();
    /** 28号代表月底 */
    private static final int END_OF_MONTH = 28;
    /** 代表获取到正确的json对象 code */
    private static final String SUCCESS = "0";

    @Override
    public void run() {
        try{
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
            int day = cal.get(Calendar.DATE);
            /* B币券余额 */
            double couponBalance = Double.parseDouble(data.getCouponBalance());
            LOGGER.info("【B币卷】: " + couponBalance);
            if(day != END_OF_MONTH){
                LOGGER.info("【使用B币卷】: " + "今日不是月底(28号)❌");
                return;
            }
            if(couponBalance <= 0){
                LOGGER.info("【使用B币卷】: " + "B币卷 <= 0 ,无法使用❌");
                return ;
            }
            switch (config.getAutoBiCoin()){
                case "1" : doCharge(couponBalance);break;
                case "2" : doMelonSeed((int) couponBalance);break;
                default: break;
            }
        } catch (Exception e){
            LOGGER.error("💔使用B币卷部分错误 : " + e);
        }
    }

    /**
     * 月底自动给自己充电。仅充会到期的B币券，低于2的时候不会充
     * @author srcrs
     * @Time 2020-10-19
     */
    public void doCharge(Double couponBalance) {
        /*
         * 判断条件 是月底&&b币券余额大于2&&配置项允许自动充电
         */
        if(couponBalance < 2){
            LOGGER.warn("【用B币卷给自己充电】: " + couponBalance + "<2 ,无法给自己充电❌");
            return ;
        }
        /* 被充电用户的userID */
        String userId = data.getMid();
        String body = "elec_num=" + couponBalance * 10
                + "&up_mid=" + userId
                + "&otype=up"
                + "&oid=" + userId
                + "&csrf=" + data.getBiliJct();

        JSONObject jsonObject = Request.post("http://api.bilibili.com/x/ugcpay/trade/elec/pay/quick", body);

        Integer resultCode = jsonObject.getInteger("code");
        if (resultCode == 0) {
            JSONObject dataJson = jsonObject.getJSONObject("data");
            LOGGER.debug(dataJson.toString());
            Integer statusCode = dataJson.getInteger("status");
            if (statusCode == 4) {
                LOGGER.info("【用B币卷给自己充电】: " + "本次给自己充值了: " + couponBalance * 10 + "个电池✔");
                /* 获取充电留言token */
                String orderNo = dataJson.getString("order_no");
                chargeComments(orderNo);
            } else {
                LOGGER.warn("【用B币卷给自己充电】: " + "失败, 原因为: " + jsonObject + "❌");
            }
        } else {
            LOGGER.warn("【用B币卷给自己充电】: " + "失败, 原因为: " + jsonObject + "❌");
        }
    }

    /**
     * 自动充电完，添加一条评论
     * @param token 订单id
     * @author srcrs
     * @Time 2020-10-19
     */
    public void chargeComments(String token) {

        String requestBody = "order_id=" + token
                + "&message=" + "BilibiliTask自动充电"
                + "&csrf=" + data.getBiliJct();
        JSONObject jsonObject = Request.post("http://api.bilibili.com/x/ugcpay/trade/elec/message", requestBody);
        LOGGER.debug(jsonObject.toString());
    }

    /**
     * 用 B 币卷兑换成金瓜子
     * @param couponBalance 传入 B 币卷的数量
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
                + "&csrf=" + data.getBiliJct();
        JSONObject post = Request.post("https://api.live.bilibili.com/xlive/revenue/v1/order/createOrder", body);
        String msg ;
        /* json对象的状态码 */
        String code = post.getString("code");
        if(SUCCESS.equals(code)){
            msg = "成功将 " + couponBalance + " B币卷兑换成 " + couponBalance*1000 + " 金瓜子✔";
        } else{
            msg = post.getString("message") + "❌";
        }
        LOGGER.info("【B币卷兑换金瓜子】: " + msg);
    }

}
