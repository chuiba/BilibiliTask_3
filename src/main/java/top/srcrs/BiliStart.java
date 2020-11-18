package top.srcrs;

import com.alibaba.fastjson.JSONObject;
import top.srcrs.domain.Config;
import top.srcrs.domain.Data;
import top.srcrs.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 启动类，程序运行开始的地方
 * @author srcrs
 * @Time 2020-10-13
 */
public class BiliStart {
    /** 获取日志记录器对象 */
    private static final Logger LOGGER = LoggerFactory.getLogger(BiliStart.class);
    /** 获取DATA对象 */
    private static final Data DATA = Data.getInstance();
    /** 访问成功 */
    private static final String SUCCESS = "0";
    /** 获取Config配置的对象 */
    private static final Config CONFIG = Config.getInstance();
    public static void main(String[] args) {
        /*
         * 存储所有 class 全路径名
         * 因为测试的时候发现，在 windows 中是按照字典排序的
         * 但是在 Linux 中并不是字典排序我就很迷茫
         * 因为部分任务是需要有顺序的去执行
         */
        final List<String> list = new ArrayList<>();
        if(args.length == 0){
            LOGGER.error("💔请在Github Secrets中添加你的Cookie信息");
            return;
        }
        /* 账户信息是否失效 */
        boolean flag = true;
        DATA.setCookie(args[0],args[1],args[2]);
        /* 读取yml文件配置信息 */
        ReadConfig.transformation("/config.yml");
        /* 如果用户账户有效 */
        if(check()){
            flag =false;
            LOGGER.info("【用户名】: {}",hideString(DATA.getUname(),1,1,'*'));
            LOGGER.info("【硬币】: {}",DATA.getMoney());
            LOGGER.info("【经验】: {}",DATA.getCurrentExp());
            PackageScanner pack = new PackageScanner() {
                @Override
                public void dealClass(Class<?> klass) {
                    try{
                        list.add(klass.getName());
                    } catch (Exception e){
                        LOGGER.error("💔扫描class目录出错 : " + e);
                    }
                }
            };
            /* 动态执行task包下的所有java代码 */
            pack.scannerPackage("top.srcrs.task");
            Collections.sort(list);
            for(String s : list){
                try{
                    Object object = Class.forName(s).newInstance();
                    Method method = object.getClass().getMethod("run", (Class<?>[]) null);
                    method.invoke(object);
                } catch (Exception e){
                    LOGGER.error("💔反射获取对象错误 : " + e);
                }
            }
            /* 当用户等级为Lv6时，升级到下一级 next_exp 值为 -- 代表无穷大 */
            String maxLevel = "6";
            if(maxLevel.equals(DATA.getCurrentLevel())){
                LOGGER.info("【升级预计】: 当前等级为: Lv" + maxLevel + " ,已经是最高等级");
                LOGGER.info("【温馨提示】: 可在配置文件中关闭每日投币操作");
            } else{
                LOGGER.info("【升级预计】: 当前等级为: Lv"
                        + DATA.getCurrentLevel() + " ,预计升级到下一级还需要: "
                        + getNextLevel() +" 天");
            }
            LOGGER.info("本次任务运行完毕。");

        } else {
            LOGGER.info("💔账户已失效，请在Secrets重新绑定你的信息");
        }
        /* 当用户只推送 server 酱或钉钉时，需要做一下判断*/
        if(args.length==4){
            /* 如果该字符串包含钉钉推送链接信息，则证明是钉钉推送 */
            String ding = "https://oapi.dingtalk.com/robot/send";
            if(args[3].contains(ding)){
                SendDingTalk.send(args[3]);
            } else{
                SendServer.send(args[3]);
            }
        }
        /* 此时数组的长度为4，就默认填写的是填写的钉钉 webHook 链接 */
        if(args.length==5){
            SendDingTalk.send(args[4]);
        }
        /* 当用户失效工作流执行失败，github将会给邮箱发送运行失败信息 */
        if(flag){
            throw new RuntimeException("💔账户已失效，请在Secrets重新绑定你的信息");
        }
    }

    /**
     * 检查用户的状态
     * @return boolean
     * @author srcrs
     * @Time 2020-10-13
     */
    public static boolean check(){
        JSONObject jsonObject = Request.get("https://api.bilibili.com/x/web-interface/nav");
        JSONObject object = jsonObject.getJSONObject("data");
        String code = jsonObject.getString("code");
        if(SUCCESS.equals(code)){
            /* 用户名 */
            DATA.setUname(object.getString("uname"));
            /* 账户的uid */
            DATA.setMid(object.getString("mid"));
            /* vip类型 */
            DATA.setVipType(object.getString("vipType"));
            /* 硬币数 */
            DATA.setMoney(object.getString("money"));
            /* 经验 */
            DATA.setCurrentExp(object.getJSONObject("level_info").getString("current_exp"));
            /* 大会员状态 */
            DATA.setVipStatus(object.getString("vipStatus"));
            /* 钱包B币卷余额 */
            DATA.setCouponBalance(object.getJSONObject("wallet").getString("coupon_balance"));
            /* 升级到下一级所需要的经验 */
            DATA.setNextExp(object.getJSONObject("level_info").getString("next_exp"));
            /* 获取当前的等级 */
            DATA.setNextExp(object.getJSONObject("level_info").getString("current_level"));
            return true;
        }
        return false;
    }

    /**
     * 计算到下一级所需要的天数
     * 未包含今日所获得经验数
     * @return int 距离升级到下一等级还需要几天
     * @author srcrs
     * @Time 2020-11-17
     */
    private static int getNextLevel(){
        /* 当前经验数 */
        int currentExp = Integer.parseInt(DATA.getCurrentExp());
        /* 到达下一级所需要的经验数 */
        int nextExp = Integer.parseInt(DATA.getNextExp());
        /* 获取当前硬币数量 */
        int num1 = (int)Double.parseDouble(DATA.getMoney());
        /* 获取配置中每日投币数量 */
        int num2 = CONFIG.getCoin();
        /* 避免投币数设置成负数异常 */
        num2 = Math.max(num2,0);
        /* 实际每日能需要投币数 */
        int num = Math.min(num1,num2);
        /* 距离升级到下一级所需要的天数 */
        int nextNum = 0;
        while(currentExp < nextExp){
            nextNum += 1;
            num1 += 1;
            currentExp += (15+num*10);
            num1 -= num;
            num = Math.min(num1,num2);
        }
        return nextNum;
    }
    public static String hideString(String str, int startLen, int endLen, char replaceChar)
    {
        int length = str.length() - startLen - endLen;
        String startStr = str.substring(0, startLen);
        String endStr = str.substring(str.length() - endLen);
        StringBuilder hideStr = new StringBuilder();
        length = Math.min(length, 3);
        while(length--!=0){
            hideStr.append(replaceChar);
        }
        return startStr + hideStr + endStr;
    }
}
