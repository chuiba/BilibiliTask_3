package org.example;

import org.example.domain.Cookie;

public class App
{
    public static void main( String[] args ) throws Exception {
        Cookie cookie = new Cookie();
        cookie.setBili_jct(args[0]);
        cookie.setDedeUserID(args[1]);
        cookie.setSESSDATA(args[2]);
        // 执行获取经验
        new DailyTasks();
    }
}
