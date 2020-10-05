package org.example;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.example.domain.Cookie;
import org.example.domain.User;
import org.example.domain.WebAPI;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Function {
    WebAPI webAPI = new WebAPI();
    private Cookie ce = new Cookie();
    User user = User.getInstance();
    // 获取日志记录对象
    public static final Logger LOGGER = LoggerFactory.getLogger(Function.class);
    public static final Function FUNCTION = new Function();

    // 检查用户是否有效
    public void check() throws Exception {
        JSONObject jsonObject = new Request(webAPI.getURL_1()).get();
        JSONObject object = jsonObject.getJSONObject("data");
        if("0".equals(jsonObject.getString("code"))){
            User user = User.getInstance();
            user.setUname(object.getString("uname"));
            user.setUname(object.getString("mid"));
            user.setUname(object.getString("vipType"));
        }
    }

    private Function(){};

    // 获取经验信息
    public JSONObject getReward() throws Exception {
        JSONObject jsonObject = new Request(webAPI.getURL_2()).get();
        JSONObject json = jsonObject.getJSONObject("data");
        return json;
    }

    // 获取b站指定视频的链接的aid和cid
    // 使用的是手机的UA，电脑UA获取不到aid和cid
    public JSONObject getID(String URL) throws Exception {
        Document doc = Jsoup.connect("https://www.bilibili.com/video/BV11a4y1576H")
                .header("Referer","https://www.bilibili.com/")
                .header("Connection","keep-alive")
                .header("User-Agent","Mozilla/5.0 (Linux; U; Android 7.1.2; zh-cn; Redmi 4X Build/N2G47H) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/71.0.3578.141 Mobile Safari/537.36 XiaoMi/MiuiBrowser/11.8.14")
          //      .header("Cookie","bili_jct="+ce.getBili_jct()+";SESSDATA="+ce.getSESSDATA()+";DedeUserID="+ce.getDedeUserID())
                .get();
        // 这里的思路先获取到包含aid和cid的那一段js
        // 然后根据正则匹配到aid和cid，然后依次保存到arr字符串数组
        Elements select = doc.select("#app > div > div > div.m-video-player > div:nth-child(3) > script:nth-child(2)");
        String arr[] = {"",""};
        int index=0;
        for(Element e : select){
            String INPUT = e.toString();
            String REGEX = "(aid|cid): [\\d]{9},";
            Pattern p = Pattern.compile(REGEX);
            Matcher m = p.matcher(INPUT);
            while(m.find()){
                arr[index++]=m.group().substring(5,14);
            }
        }
        // 将结果以json对象返回
        return JSONObject.parseObject("{\"aid\": "+arr[0]+",\"cid\": "+arr[1]+"}");
    }

    // 获取硬币数
    public Integer getCoin() throws Exception {
        JSONObject jsonObject = new Request(webAPI.getURL_3()).get();
        int money = (int)(Double.parseDouble(jsonObject.getJSONObject("data").getString("money")));
        return money;
    }
    // 给指定av号视频投币
    public JSONObject throwCoin(String aid,String num,String select_like) throws Exception{

        String body="aid="+aid+"&multiply="+num+"&select_like="+select_like+"&cross_domain="+"true"+"&csrf="+ce.getBili_jct();
        JSONObject post = new Request(webAPI.getURL_4(), body).post();
        System.out.println(post);
        return post;
    }
    // 分享指定av号视频
    public JSONObject share(String aid) throws Exception {
        String body = "aid="+aid+"&csrf="+ce.getBili_jct();
        JSONObject post = new Request(webAPI.getURL_5(), body).post();
        return post;
    }
    // B站上报观看进度
    public JSONObject report(String aid,String cid,String progres) throws Exception{
        String body = "aid="+aid+"&cid="+cid+"&progres="+progres+"&csrf="+ce.getBili_jct();
        JSONObject post = new Request(webAPI.getURL_6(), body).post();
        return post;
    }
    // 获取首页的推荐视频列表
    public JSONArray getRecommend() throws Exception{
        Document doc = Jsoup.connect(webAPI.getURL_7())
                .header("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36")
                .get();
        JSONArray jsonArray = new JSONArray();
        for(int i=1;i<=10;i++) {
            jsonArray.add("https:" + doc.select("#reportFirst1 > div.recommend-box > div:nth-child(" + i + ") > div.info-box > a").attr("href"));
        }
        return jsonArray;
    }
    // 获取B站分区视频信息
    public JSONArray getRegions(String ps,String rid) throws Exception{
        JSONObject jsonObject = new Request(webAPI.getURL_8() + "?ps=" + ps + "&rid=" + rid).get();
        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("archives");
        JSONArray jsonRegions = new JSONArray();
        for(Object object : jsonArray){
            JSONObject json = (JSONObject)object;
            JSONObject cache = new JSONObject();
            cache.put("title",json.getString("title"));
            cache.put("aid",json.getString("aid"));
            cache.put("bvid",json.getString("bvid"));
            cache.put("cid",json.getString("cid"));
            jsonRegions.add(cache);
        }
        return jsonRegions;
    }
    // 获取B站分区视频排行榜信息
    public JSONArray getRankings(String rid,String day) throws Exception {
        JSONObject jsonObject = new Request(webAPI.getURL_9()+"?rid="+rid+"&day="+day).get();
        return jsonObject.getJSONObject("data").getJSONArray("list");
    }
    // 转发B站动态
    public void repost(String dynamic_id,String content,String extension) throws Exception {
        String body = "uid="+user.getMid()+"&dynamic_id="+dynamic_id+"&content="+content+"&extension.emoji_type="+extension+"&csrf_token="+ce.getBili_jct();
        JSONObject post = new Request(webAPI.getURL_10(), body).post();
        System.out.println(post);
    }
}
