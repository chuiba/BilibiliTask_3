package top.srcrs.bilibili;

import com.alibaba.fastjson.JSONObject;
import top.srcrs.bilibili.domain.Data;
import top.srcrs.bilibili.task.live.GiveGiftTask;
import top.srcrs.bilibili.util.PackageScanner;
import top.srcrs.bilibili.util.ReadConfig;
import top.srcrs.bilibili.util.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * 启动类，程序运行开始的地方
 * @author srcrs
 * @Time 2020-10-13
 */
public class BiliStart {
    /** 获取日志记录器对象 */
    private static final Logger LOGGER = LoggerFactory.getLogger(BiliStart.class);
    private static Data data = Data.getInstance();

    public static void main(String[] args) {
        data.setCookie(args[0],args[1],args[2]);
        /** 读取yml文件配置信息 */
        ReadConfig.transformation("/Bilibili.yml");
        if(check()){
            LOGGER.info("用户{}状态正常,正在执行脚本",data.getUname());
            PackageScanner pack = new PackageScanner() {
                @Override
                public void dealClass(Class<?> klass) {
                    try{
                        Object object = klass.newInstance();
                        Class clazz = object.getClass();
                        Method method = clazz.getMethod("run",null);
                        method.invoke(object);
                    } catch (Exception e){
                        LOGGER.error("反射获取对象错误,原因为-{}",e);
                    }
                }
            };
            /** 动态执行task包下的所有java代码 */
            pack.scannerPackage("top.srcrs.bilibili.task");
            LOGGER.info("本次全部操作完成");
        } else {
            LOGGER.warn("账户异常中止所有操作");
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
        if("0".equals(jsonObject.getString("code"))){
            data.setUname(object.getString("uname"));
            data.setMid(object.getString("mid"));
            data.setVipType(object.getString("vipType"));
            return true;
        }
        return false;
    }
}
