package top.srcrs.util;

import org.junit.jupiter.api.Test;

/**
 * ...
 *
 * @author : Ali
 * @date : 2020/11/21
 */
class SendServerTest {

    @Test
    void sendTest() {
        if(StringUtil.isNotBlank(System.getenv("SCKEY"))){
            SendServer.send(System.getenv("SCKEY"));
        }
    }

}
