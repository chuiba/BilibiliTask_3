package org.example;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.example.domain.Cookie;

public class App
{
    public static void main( String[] args ) throws Exception {
        System.out.println(args[0]);
        Cookie cookie = new Cookie();
        JSONObject jsonObject = JSON.parseObject(args[0]);
        cookie.setBili_jct(jsonObject.getString("bili_jct"));
        cookie.setDedeUserID(jsonObject.getString("477137547"));
        cookie.setSESSDATA(jsonObject.getString("SESSDATA"));
        // 执行获取经验
        new DailyTasks();
    }
}
