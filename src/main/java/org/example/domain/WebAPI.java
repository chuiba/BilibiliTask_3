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
    // 专栏编辑草稿
    private String URL_33 = "https://api.bilibili.com/x/article/creative/draft/addupdate";
    // 专栏正式发表地址
    private String URL_34 = "https://api.bilibili.com/x/article/creative/article/submit";
    // 删除专栏
    private String URL_35 = "https://member.bilibili.com/x/web/draft/delete";
    // 获取专栏内容
    private String URL_36 = "https://api.bilibili.com/x/article/creative/draft/view";
    // 上传本地图片
    private String URL_37 = "https://api.bilibili.com/x/article/creative/article/upcover";
    // 根据bv号获取视频信息
    private String URL_38 = "https://api.bilibili.com/x/article/cards";
    // 根据mc号获取漫画信息
    private String URL_39 = "https://api.bilibili.com/x/article/mangas";
    // 获取B站活动列表,生成器
    private String URL_40 = "https://www.bilibili.com/activity/page/list";
    // 获取B站活动列表
    private String URL_41 = "https://member.bilibili.com/x/app/h5/activity/videoall";
    // 增加B站活动的参与次数
    private String URL_42 = "https://api.bilibili.com/x/activity/lottery/addtimes";
    // 参与B站的活动
    private String URL_43 = "https://api.bilibili.com/x/activity/lottery/do";
    // 获取B站活动次数
    private String URL_44 = "https://api.bilibili.com/x/activity/lottery/mytimes";
    // B站直播模拟客户端打开宝箱领取银瓜子
    private String URL_45 = "https://api.live.bilibili.com/lottery/v1/SilverBox/getAward";
    // B站直播模拟客户端获取时间宝箱
    private String URL_46 = "https://api.live.bilibili.com/lottery/v1/SilverBox/getCurrentTask";
    // B站直播获取背包礼物
    private String URL_47 = "https://api.live.bilibili.com/xlive/web-room/v1/gift/bag_list";
    // B站直播获取首页前10条直播
    private String URL_48 = "https://api.live.bilibili.com/relation/v1/AppWeb/getRecommendList";
    // B站直播送出背包礼物
    private String URL_49 = "https://api.live.bilibili.com/gift/v2/live/bag_send";
    // B站直播获取房间信息
    private String URL_50 = "https://api.live.bilibili.com/xlive/web-room/v1/index/getInfoByRoom";
    // B站直播 直播间心跳
    private String URL_51 = "https://live-trace.bilibili.com/xlive/rdata-interface/v1/heartbeat/webHeartBeat";
    // B站直播 心跳(大约2分半一次)
    private String URL_52 = "https://api.live.bilibili.com/relation/v1/Feed/heartBeat";
    // B站直播 用户在线心跳(很少见)
    private String URL_53 = "https://api.live.bilibili.com/User/userOnlineHeart";
    // 模拟B站漫画客户端签到
    private String URL_54 = "https://manga.bilibili.com/twirp/activity.v1.Activity/ClockIn";
    // 获取钱包信息
    private String URL_55 = "https://manga.bilibili.com/twirp/user.v1.User/GetWallet";
    // 站友日漫画卷兑换
    private String URL_56 = "https://manga.bilibili.com/twirp/activity.v1.Activity/Comrade";
    // 获取漫画购买信息
    private String URL_57 = "https://manga.bilibili.com/twirp/comic.v1.Comic/GetEpisodeBuyInfo";

    public String getURL_57() {
        return URL_57;
    }

    public String getURL_56() {
        return URL_56;
    }

    public String getURL_55() {
        return URL_55;
    }

    public String getURL_54() {
        return URL_54;
    }

    public String getURL_53() {
        return URL_53;
    }

    public String getURL_52() {
        return URL_52;
    }

    public String getURL_51() {
        return URL_51;
    }

    public String getURL_50() {
        return URL_50;
    }

    public String getURL_49() {
        return URL_49;
    }

    public String getURL_48() {
        return URL_48;
    }

    public String getURL_47() {
        return URL_47;
    }

    public String getURL_46() {
        return URL_46;
    }

    public String getURL_45() {
        return URL_45;
    }

    public String getURL_44() {
        return URL_44;
    }

    public String getURL_43() {
        return URL_43;
    }

    public String getURL_42() {
        return URL_42;
    }

    public String getURL_41() {
        return URL_41;
    }

    public String getURL_40() {
        return URL_40;
    }

    public String getURL_39() {
        return URL_39;
    }

    public String getURL_38() {
        return URL_38;
    }

    public String getURL_37() {
        return URL_37;
    }

    public String getURL_36() {
        return URL_36;
    }

    public String getURL_35() {
        return URL_35;
    }

    public String getURL_34() {
        return URL_34;
    }

    public String getURL_33() {
        return URL_33;
    }

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
