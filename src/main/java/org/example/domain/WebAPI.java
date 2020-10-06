package org.example.domain;

public class WebAPI {
    // 用于判断用户账号是否可用
    private String URL_1 = "https://api.bilibili.com/x/web-interface/nav";
    // 获取经验信息
    private String URL_2 = "https://account.bilibili.com/home/reward";
    // 获取硬币剩余数
    private String URL_3 = "https://api.bilibili.com/x/web-interface/nav?build=0&mobi_app=web";
    // 给指定av号视频投币
    private String URL_4 = "https://api.bilibili.com/x/web-interface/coin/add";
    // 分享指定av号视频
    private String URL_5 = "https://api.bilibili.com/x/web-interface/share/add";
    // B站上报观看进度
    private String URL_6 = "http://api.bilibili.com/x/v2/history/report";
    // B站首页推荐视频
    private String URL_7 = "https://www.bilibili.com";
    // 获取B站分区视频信息，暂时不知道又什么用。
    private String URL_8 = "https://api.bilibili.com/x/web-interface/dynamic/region";
    // B站排行榜rid分区，day最近几天（3，7）
    // rid代表哪个分区，day代表最近几天，需要携带两个参数
    private String URL_9 = "https://api.bilibili.com/x/web-interface/ranking";
    // 转发B站动态
    private String URL_10 = "https://api.vc.bilibili.com/dynamic_repost/v1/dynamic_repost/repost";
    // 评论动态
    private String URL_11 = "https://api.bilibili.com/x/v2/reply/add";
    // 评论动态并转发
    private String URL_12 = "https://api.vc.bilibili.com/dynamic_repost/v1/dynamic_repost/reply";
    // 关注或取关up主
    private String URL_13 = "https://api.vc.bilibili.com/feed/v1/feed/SetUserFollow";
    // 改变关注的状态
    private String URL_14 = "https://api.bilibili.com/x/relation/modify";
    // 移动关注的up主的分组
    private String URL_15 = "https://api.bilibili.com/x/relation/tags/addUsers?cross_domain=true";
    // 获取指定账户的关注者
    private String URL_16 = "https://api.bilibili.com/x/relation/followings";
    // 取B站话题信息
    private String URL_17 = "https://api.bilibili.com/x/tag/info";
    // 取B站话题列表
    private String URL_18 = "https://api.vc.bilibili.com/topic_svr/v1/topic_svr/topic_new";
    // 获取动态内容
    private String URL_19 = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/get_dynamic_detail";
    // 获取B站用户最新动态数据
    private String URL_20 = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/dynamic_new";
    // 取B站用户动态历史数据
    private String URL_21 = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/dynamic_history";
    // 取B站用户自己的动态列表，生成器
    private String URL_22 = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history";
    // 是删除自己的动态
    private String URL_23 = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/rm_dynamic";
    // 获取指定抽奖信息
    private String URL_24 = "https://api.vc.bilibili.com/lottery_svr/v1/lottery_svr/lottery_notice";
    // 获取指定账户关注信息
    private String URL_25 = "https://api.bilibili.com/x/relation/stat";
    // 获取指定账户空间信息
    private String URL_26 = "https://api.bilibili.com/x/space/acc/info";
    // 获取账户钱包信息
    private String URL_27 = "https://pay.bilibili.com/paywallet/wallet/getUserWallet";
    // 用B币给up主充电
    private String URL_28 = "https://api.bilibili.com/x/ugcpay/trade/elec/pay/quick";
    // 充电订单状态查询
    private String URL_29 = "https://api.bilibili.com/x/ugcpay/trade/elec/pay/order/status";
    // B站直播签到
    private String URL_30 = "https://api.live.bilibili.com/xlive/web-ucenter/v1/sign/DoSign";
    // B站直播获取金瓜子状态
    private String URL_31 = "https://api.live.bilibili.com/pay/v1/Exchange/getStatus";
    // 银瓜子兑换硬币
    private String URL_32 = "https://api.live.bilibili.com/pay/v1/Exchange/silver2coin";

    public String getURL_32() {
        return URL_32;
    }

    public String getURL_31() {
        return URL_31;
    }

    public String getURL_30() {
        return URL_30;
    }

    public String getURL_29() {
        return URL_29;
    }

    public String getURL_28() {
        return URL_28;
    }

    public String getURL_27() {
        return URL_27;
    }

    public String getURL_26() {
        return URL_26;
    }

    public String getURL_25() {
        return URL_25;
    }

    public String getURL_24() {
        return URL_24;
    }

    public String getURL_23() {
        return URL_23;
    }

    public String getURL_22() {
        return URL_22;
    }

    public String getURL_21() {
        return URL_21;
    }

    public String getURL_20() {
        return URL_20;
    }

    public String getURL_19() {
        return URL_19;
    }

    public String getURL_18() {
        return URL_18;
    }

    public String getURL_17() {
        return URL_17;
    }

    public String getURL_16() {
        return URL_16;
    }

    public String getURL_15() {
        return URL_15;
    }

    public String getURL_14() {
        return URL_14;
    }

    public String getURL_13() {
        return URL_13;
    }

    public String getURL_12() {
        return URL_12;
    }

    public String getURL_11() {
        return URL_11;
    }

    public String getURL_10() {
        return URL_10;
    }

    public String getURL_9() {
        return URL_9;
    }

    public String getURL_8() {
        return URL_8;
    }

    public String getURL_7() {
        return URL_7;
    }

    public String getURL_6() {
        return URL_6;
    }

    public String getURL_5() {
        return URL_5;
    }

    public String getURL_4() {
        return URL_4;
    }

    public String getURL_3() {
        return URL_3;
    }

    public String getURL_2() {
        return URL_2;
    }

    public String getURL_1() {
        return URL_1;
    }

}
