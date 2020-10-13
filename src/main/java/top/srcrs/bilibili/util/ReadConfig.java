package top.srcrs.bilibili.util;

import org.yaml.snakeyaml.Yaml;
import top.srcrs.bilibili.domain.Config;

/**
 * 读取yml配置
 * @author srcrs
 * @Time 2020-10-13
 */
public class ReadConfig {

    /**
     * 将yml的配置映射到Config.java中
     * @author srcrs
     * @Time 2020-10-13
     */
    public static void transformation(String file){
        Yaml yaml = new Yaml();
        yaml.loadAs(ReadConfig.class.getResourceAsStream(file), Config.class);
    }
}
