package org.example;

import org.example.domain.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App
{
    // 获取日志记录对象
    public static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main( String[] args ) throws Exception {
        Cookie cookie = new Cookie();
        cookie.setBili_jct(args[0]);
        cookie.setDedeUserID(args[1]);
        cookie.setSESSDATA(args[2]);
        // 检查用户是否可用
        LOGGER.info("正在进行检查账户是否可用");
        Function function = Function.FUNCTION;
        // 检查用户状态
        try{
            boolean check = function.check();
            if(check){
                LOGGER.info("账户状态正常");
                // 执行每日任务获取经验
                new DailyTasks();
                // 执行直播相关
                new RelatedLive();
                // 执行漫画签到
                new MangaTask();
                LOGGER.info("所有操作完成");

            } else{
                LOGGER.warn("账户状态异常");
            }
        } catch (Exception e){
            LOGGER.error("账户状态异常,中止所有操作"+"-------->"+e);
            return;
        }
    }
}
