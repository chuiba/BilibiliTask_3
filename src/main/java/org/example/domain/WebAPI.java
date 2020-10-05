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

    public String getURL_5() {
        return URL_5;
    }

    public String getURL_6() {
        return URL_6;
    }

    public String getURL_1() {
        return URL_1;
    }

    public String getURL_2() {
        return URL_2;
    }

    public String getURL_3() {
        return URL_3;
    }

    public String getURL_4() {
        return URL_4;
    }
}
