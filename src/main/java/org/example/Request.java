package org.example;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.example.domain.Cookie;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Request {

    private Cookie ce = new Cookie();

    private static final Request REQUEST = new Request();

    public static Request getInstance(){
        return REQUEST;
    }
    private Request(){}

    public JSONObject get(String URL) throws Exception {
        java.net.URL url = new URL(URL);
        //得到connection对象。
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        //设置请求方式
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Referer", "https://www.bilibili.com/");//设置请求头
        connection.setRequestProperty("Connection","keep-alive");
        connection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36");
        connection.setRequestProperty("Cookie", "bili_jct="+ce.getBili_jct()+";SESSDATA="+ce.getSESSDATA()+";DedeUserID="+ce.getDedeUserID());//设置请求头
        //连接
        connection.connect();
        //得到响应码
        int responseCode = connection.getResponseCode();
        if(responseCode == HttpURLConnection.HTTP_OK){
            //得到响应流
            InputStream inputStream = connection.getInputStream();
            //将响应流转换成字符串
            String result = new Scanner(inputStream).useDelimiter("\\Z").next();//将流转换为字符串。
            JSONObject jsonObject = JSONObject.parseObject(result);
            inputStream.close();
            return jsonObject;
        }
        else{
            return JSONObject.parseObject("");
        }
    }

    public JSONObject post(String URL, String Body) throws Exception{
        URL url = new URL(URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setRequestProperty("connection","keep-alive");
        connection.setRequestProperty("referer","https://www.bilibili.com/");
        connection.setRequestProperty("accept","application/json, text/plain, */*");
        connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
        connection.setRequestProperty("charset","UTF-8");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36");
        connection.setRequestProperty("Cookie", "bili_jct="+ce.getBili_jct()+";SESSDATA="+ce.getSESSDATA()+";DedeUserID="+ce.getDedeUserID());
        connection.connect();

//        String body = "kw="+name+"&tbs="+tbs+"&sign="+enCodeMd5("kw="+name+"tbs="+tbs+"tiebaclient!!!");

        String body = Body;

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
        writer.write(body);
        writer.close();

        int responseCode = connection.getResponseCode();
        InputStream inputStream = null;
        if(responseCode < 400){
            //得到正确的响应流
            inputStream = connection.getInputStream();
        }else{
            //得到错误的响应流
            inputStream = connection.getErrorStream();
        }
        
        //将响应流转换成字符串
        String result = new Scanner(inputStream).useDelimiter("\\Z").next();//将流转换为字符串。
        JSONObject jsonObject = JSON.parseObject(result);
        inputStream.close();
        return jsonObject;
        
		/*
        if(responseCode == HttpURLConnection.HTTP_OK){
            //得到响应流
            InputStream inputStream = connection.getInputStream();
            //将响应流转换成字符串
            String result = new Scanner(inputStream).useDelimiter("\\Z").next();//将流转换为字符串。
            JSONObject jsonObject = JSON.parseObject(result);
            inputStream.close();
            return jsonObject;
        }
        else{
            return JSONObject.parseObject("");
        }*/
    }
}
