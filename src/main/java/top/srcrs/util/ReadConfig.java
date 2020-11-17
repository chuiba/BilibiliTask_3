package top.srcrs.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import top.srcrs.domain.Config;

/**
 * 读取yml配置
 * @author srcrs
 * @Time 2020-10-13
 */
public class ReadConfig {
    /** 获取日志记录器对象 */
    private static final Logger LOGGER = LoggerFactory.getLogger(ReadConfig.class);

    /**
     * 将yml的配置映射到Config.java中
     * @author srcrs
     * @Time 2020-10-13
     */
    public static void transformation(String file){
        try{
            Yaml yaml = new Yaml();
            yaml.loadAs(ReadConfig.class.getResourceAsStream(file), Config.class);
        } catch (Exception e){
            LOGGER.info("💔配置文件转换成对象出错 : " + e);
        }
    }
}
