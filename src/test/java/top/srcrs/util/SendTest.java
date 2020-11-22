package top.srcrs.util;

import org.junit.jupiter.api.Test;

/**
 * ...
 *
 * @author : Ali
 * @date : 2020/11/21
 */
class SendTest {

    @Test
    void SendServerTest() {
        if(StringUtil.isNotBlank(System.getenv("SCKEY"))){
            SendServer.send(System.getenv("SCKEY"));
        }
    }

    @Test
    void SendPushPlusTest() {
        if(StringUtil.isNotBlank(System.getenv("PUSHPLUSTK"))){
            SendPushPlus.send(System.getenv("PUSHPLUSTK"));
        }
    }

}
