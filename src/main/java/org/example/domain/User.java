package org.example.domain;

import java.net.URLEncoder;

public class User {
    private static final User user = new User();
    // 登录账户的用户名
    private String uname;
    // 登录账户的uid
    private String mid;
    // 代表账户的类型
    private String vipType;

    private User(){};

    public static User getInstance(){
        return user;
    }

    // 获取账户的用户名
    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    // 获取账户的uid
    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    // 获取账户的类型
    public String getVipType() {
        return vipType;
    }

    public void setVipType(String vipType) {
        this.vipType = vipType;
    }
}
