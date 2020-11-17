package top.srcrs.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * 读取日志配置文件
 * @author srcrs
 * @Time 2020-11-16
 */
public class ReadLog {
    /** 获取日志记录器对象 */
    private static final Logger LOGGER = LoggerFactory.getLogger(ReadLog.class);

    /**
     * 读取输出到文件中的日志
     * @param pathName 日志文件的名字，包括路径
     * @return String 将日志拼接成了字符串
     * @author srcrs
     * @Time 2020-10-22
     */
    public static String getString(String pathName){
        /* str代表要发送的数据 */
        StringBuilder str = new StringBuilder();
        FileReader reader ;
        BufferedReader br ;
        try{
            reader = new FileReader(pathName);
            br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null){
                str.append(line).append("\n\n");
            }
            reader.close();
            br.close();
        } catch (Exception e){
            LOGGER.error("💔读日志文件时出错 : " + e);
        }
        return str.toString();
    }
}
