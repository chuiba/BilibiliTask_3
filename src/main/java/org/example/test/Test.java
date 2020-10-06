package org.example.test;

import com.alibaba.fastjson.JSONObject;
import org.example.Function;

public class Test {
    public static void main(String[] args) throws Exception {
        Function function = Function.FUNCTION;
        JSONObject aTrue = function.xliveSign();;
        System.out.println(aTrue);
    }
}
