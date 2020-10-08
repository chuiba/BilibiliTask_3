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
    public boolean check() throws Exception {
        JSONObject jsonObject = new Request(webAPI.getURL_1()).get();
        JSONObject object = jsonObject.getJSONObject("data");
        if("0".equals(jsonObject.getString("code"))){
            User user = User.getInstance();
            user.setUname(object.getString("uname"));
            user.setMid(object.getString("mid"));
            user.setVipType(object.getString("vipType"));
            return true;
        }
        return false;
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

        String body="aid="+aid
                +"&multiply="+num
                +"&select_like="+select_like
                +"&cross_domain="+"true"
                +"&csrf="+ce.getBili_jct();
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
        String body = "aid="+aid
                +"&cid="+cid
                +"&progres="+progres
                +"&csrf="+ce.getBili_jct();
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
        String body = "uid="+user.getMid()
                +"&dynamic_id="+dynamic_id
                +"&content="+content
                +"&extension.emoji_type="+extension
                +"&csrf_token="+ce.getBili_jct();
        JSONObject post = new Request(webAPI.getURL_10(), body).post();
        System.out.println(post);
    }
    // 评论动态
    public JSONObject dynamicReplyAdd(String oid,String message,String type,String plat) throws Exception{
        String body = "oid="+oid
                +"&plat="+plat
                +"&type="+type
                +"&message="+message
                +"&csrf="+ce.getBili_jct();
        JSONObject post = new Request(webAPI.getURL_11(), body).post();
        return post;
    }
    // 评论动态并转发
    public JSONObject dynamicRepostReply(String rid,String content,String type,String repost_code,String From,String extension) throws Exception{
        String body="uid="+user.getMid()
                +"&rid="+rid
                +"&content="+content
                +"&extension.emoji_type="+extension
                +"&repost_code="+repost_code
                +"&from="+From
                +"&csrf_token="+ce.getBili_jct();
        JSONObject post = new Request(webAPI.getURL_12(), body).post();
        return post;
    }
    // 关注或者取关up主
    public JSONObject followed(String followid,String isfollow) throws Exception{
        //标志位，判断是否关注，以改变状态
        String flag = "true".equals(isfollow) ? "1":"0";
        String body ="type="+flag
                +"&follow="+followid
                +"&csrf_token="+ce.getBili_jct();
        System.out.println(body);
        JSONObject post = new Request(webAPI.getURL_13(), body).post();
        return post;
    }
    // 改变关注状态，与上一个API功能类似
    public JSONObject followedModify(String followid,String act,String re_src) throws Exception{
        // act值决定是否关注，值为1关注，值为2取消关注。
        String body = "fid="+followid
                +"&act="+act
                +"&re_src="+re_src
                +"&csrf="+ce.getBili_jct();
        JSONObject post = new Request(webAPI.getURL_14(), body).post();
        return post;
    }
    // 移动关注的up主的分组
    public JSONObject groupAddFollowed(String followed,String tagids) throws Exception{
        // tagids 默认分组是0，特别关注是-10;
        String body="?fids="+followed
                +"&tagids="+tagids
                +"&csrf="+ce.getBili_jct();
        JSONObject post = new Request(webAPI.getURL_15(), body).post();
        return post;
    }
    // 获取指定账户的关注者
    public JSONObject getFollowing(String uid,String pn,String ps,String order) throws Exception{
        if("0".equals(uid)){
            uid = user.getMid();
        }
        String param = "vmid="+uid
                +"&pn="+pn
                +"&ps="+ps
                +"&order="+order;
        JSONObject jsonObject = new Request(webAPI.getURL_16() + param).get();
        return jsonObject;
    }
    // 取B站的话题信息
    public JSONObject getTopicInfo(String tag_name) throws Exception{
        String param = "?tag_name="+tag_name;
        JSONObject jsonObject = new Request(webAPI.getURL_17() + param).get();
        return jsonObject;
    }
    // 取B站话题列表
//    public JSONObject getTopicList(String tag_name) throws Exception {
//        JSONObject topic_id_object = getTopicInfo(tag_name);
//        String topic_id = topic_id_object.getJSONObject("data").getString("tag_id");
//        String param = "?topic_id="+topic_id;
//        JSONObject jsonObject = new Request(webAPI.getURL_18()).get();
//
//    }
    // 获取动态内容
    public JSONObject getDynamicDetail(String dynamic_id) throws Exception{
        String param = "?dynamic_id="+dynamic_id;
        JSONObject jsonObject = new Request(webAPI.getURL_19() + param).get();
        return jsonObject;
    }
    // 取B站用户最新动态数据
    public JSONArray getDynamicNew(String type_list) throws Exception{
        String param = "?uid="+user.getMid()
                +"&type_list="+type_list;
        JSONObject jsonObject = new Request(webAPI.getURL_20() + param).get();
        return jsonObject.getJSONObject("data").getJSONArray("cards");
    }
    // 取B站用户动态数据生成器
    public JSONArray getDynamic(String uid,String type_list) throws Exception {
        if("0".equals(uid)){
            uid = user.getMid();
        }
        String param = "?uid="+uid+"&type_list="+type_list;
        JSONArray arr = new JSONArray();
        JSONObject jsonObject = new Request(webAPI.getURL_20() + param).get();
        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("cards");
        for(Object json : jsonArray){
            JSONObject object = (JSONObject)json;
            String offset = object.getJSONObject("desc").getString("dynamic_id");
            String param2 = "?uid="+user.getMid()+"&offset_dynamic_id="+offset+"&type="+type_list;
            JSONObject jsonObject1 = new Request(webAPI.getURL_21() + param2).get();
            arr.add(jsonObject1.getJSONObject("data").getJSONArray("cards"));
        }
        return arr;
    }
    // 取B站用户自己的动态列表
    public void getMyDynamic(String uid){

    }
    // 删除自己的动态
    public JSONObject removeDynamic(String dynamic_id) throws Exception{
        String body = "dynamic_id="+dynamic_id
                +"&csrf_token="+ce.getBili_jct();
        JSONObject post = new Request(webAPI.getURL_23(), body).post();
        return post;
    }
    // 获取指定的抽奖信息
    public JSONObject getLotteryNotice(String dynamic_id) throws Exception{
        String param = "?dynamic_id="+dynamic_id;
        JSONObject jsonObject = new Request(webAPI.getURL_24() + param).get();
        return jsonObject;
    }
    // 获取指定账户的关注信息
    public JSONObject getRelationStat(String uid) throws Exception{
        String param = "?vimd="+uid;
        JSONObject jsonObject = new Request(webAPI.getURL_25() + param).get();
        return jsonObject;
    }
    // 获取指定账户的空间信息
    public JSONObject getSpaceInfo(String uid) throws Exception {
        String param = "?mid="+uid;
        JSONObject jsonObject = new Request(webAPI.getURL_26() + param).get();
        return jsonObject;
    }
    // 获取钱包信息
    public JSONObject getUserWallet(String platformType) throws Exception{
        String body = "platformType="+platformType;
        JSONObject post = new Request(webAPI.getURL_27(), body).post();
        return post;
    }
    // 用b币给up主充电，num>=20
    public JSONObject elecPay(String uid,String num) throws Exception{
        String body = "elec_num="+num
                +"&up_mid="+uid
                +"&otype="+"up"
                +"&oid="+uid
                +"&csrf="+ce.getBili_jct();
        JSONObject post = new Request(webAPI.getURL_28(), body).post();
        return post;
    }
    // 充电订单状态查询
    // order_no 代表订单号
    public JSONObject elecPayStatus(String order_no) throws Exception{
        String param = "?order_no="+order_no;
        JSONObject jsonObject = new Request(webAPI.getURL_29() + param).get();
        return jsonObject;
    }
    // B站直播签到
    public JSONObject xliveSign() throws Exception{
        JSONObject jsonObject = new Request(webAPI.getURL_30()).get();
        return jsonObject;
    }
    // B站直播获取金瓜子状态
    public JSONObject xliveGetStatus() throws Exception{
        JSONObject jsonObject = new Request(webAPI.getURL_31()).get();
        return jsonObject;
    }
    // 银瓜子兑换硬币
    public JSONObject silver2coin() throws Exception{
        String body = "csrf_token="+ce.getBili_jct();
        JSONObject post = new Request(webAPI.getURL_32(), body).post();
        return post;
    }
    // 发表专栏
    public void createArticle(String title,String content,String aid,String category,String list_id,String tid,String original,String image_urls,String origin_image_urls,String submit){
        String body = "title="+title
                +"&content="+content
                +"&category="+category
                +"&list_id="+list_id
                +"&tid="+tid
                +"&reprint="+"0"
                +"&media_id="+"0"
                +"&spoiler="+"0"
                +"&original="+original
                +"&csrf="+ce.getBili_jct();

    }
    // 删除专栏
    public JSONObject deleteArticle(String aid) throws Exception{
        String body = "aid="+aid+"&csrf="+ce.getBili_jct();
        JSONObject post = new Request(webAPI.getURL_35(), body).post();
        return post;
    }
    // 获取专栏列表
    public JSONObject getArticle(String aid) throws Exception{
        String param = "?aid="+aid;
        JSONObject jsonObject = new Request(webAPI.getURL_36() + param).get();
        return jsonObject;
    }
    // 上传本地图片，返回链接
    public void articleUpcover(String file) throws Exception{

    }
    // 根据bv号获取视频信息，在专栏引用视频时使用
    public JSONObject articleCardsBvid(String bvid) throws Exception{
        String param = "?bvid="+bvid+"&cross_domain=true";
        JSONObject jsonObject = new Request(webAPI.getURL_38() + param).get();
        return jsonObject;
    }
    // 根据cv号获取专栏，在专栏引用其它专栏时使用
    public JSONObject articleCardsCvid(String cvid) throws Exception{
        String param = "?id="+cvid+"&cross_domain=true";
        JSONObject jsonObject = new Request(webAPI.getURL_38() + param).get();
        return jsonObject;
    }
    // 根据ep号获取番剧信息，在专栏引用站内番剧时使用
    // 使用时加上ep前缀
    public JSONObject articleCardsId(String epid){
        return articleCardsId(epid);
    }
    // 根据au号获取音乐信息，在专栏引用站内音乐时使用
    public JSONObject articleCardsAu(String auid) throws Exception {
        return articleCardsCvid(auid);
    }
    // 根据au号获取会员购信息，在专栏引用会员购时使用
    public JSONObject articleCardsPw(String pwid) throws Exception{
        return articleCardsCvid(pwid);
    }
    // 根据mc号获取漫画信息，在专栏引用站内漫画时使用
    public JSONObject articleMangas(String mcid) throws Exception{
        String param = "?id="+mcid+"&cross_domain=true";
        JSONObject jsonObject = new Request(webAPI.getURL_39() + param).get();
        return jsonObject;
    }
    // 根据lv号获取直播信息，在专栏引用站内直播时使用
    public JSONObject articleCardsLv(String lvid) throws Exception{
        return articleCardsCvid(lvid);
    }
    // 获取B站活动列表，生成器
    public void activityList(String plat,String mold,String http,String start_page,String end_page){

    }
    // 获取B站活动列表
    public JSONObject activityAll() throws Exception {
        JSONObject jsonObject = new Request(webAPI.getURL_41()).get();
        return jsonObject;
    }
    // 增加B站活动的参与次数
    // 活动sid，活动操作类型
    public JSONObject activityAddTimes(String sid,String action_type) throws Exception {
        String body = "sid="+sid
                +"&action_type="+action_type
                +"&csrf="+ce.getBili_jct();
        JSONObject post = new Request(webAPI.getURL_42(), body).post();
        return post;
    }
    // 参与B站活动
    public JSONObject activityDo(String sid,String type) throws Exception {
        String body = "sid="+sid
                +"&type="+type
                +"&csrf="+ce.getBili_jct();
        JSONObject post = new Request(webAPI.getURL_43(), body).post();
        return post;
    }
    // 获取B站活动次数
    public JSONObject activityMyTimes(String sid) throws Exception {
        String param = "?sid="+sid;
        JSONObject jsonObject = new Request(webAPI.getURL_44() + param).get();
        // 响应例子{"code":0,"message":"0","ttl":1,"data":{"times":0}}
        return jsonObject;
    }
    // B站直播模拟客户端打开宝箱领取银瓜子
    public JSONObject xliveGetAward(String platform) throws Exception {
        String param = "?platform="+platform;
        JSONObject jsonObject = new Request(webAPI.getURL_45() + param).get();
        return jsonObject;
    }
    // B站直播模拟客户端获取时间宝箱
    public JSONObject xliveGetCurrentTask(String platform) throws Exception {
        String param = "?platform="+platform;
        JSONObject jsonObject = new Request(webAPI.getURL_46() + param).get();
        return jsonObject;
    }
    // B站直播获取背包礼物
    public JSONObject xliveGiftBagList() throws Exception {
        JSONObject jsonObject = new Request(webAPI.getURL_47()).get();
        return jsonObject;
    }
    // B站直播获取首页前10条直播
    public JSONObject xliveGetRecommendList() throws Exception {
        JSONObject jsonObject = new Request(webAPI.getURL_48()).get();
        return jsonObject;
    }
    // B站直播送出背包礼物
    public JSONObject xliveBagSend(String biz_id,String ruid,String bag_id,String gift_id,String gift_num,String storm_beat_id,String price,String platform) throws Exception {
        String body = "uid="+user.getMid()
                +"&gift_id="+gift_id
                +"&ruid="+ruid
                +"&send_ruid=0"
                +"&gift_num="+gift_num
                +"&bag_id="+bag_id
                +"&platform="+platform
                +"&biz_code="+"live"
                +"&biz_id="+biz_id
                +"&storm_beat_id="+storm_beat_id
                +"&price="+price
                +"&csrf="+ce.getBili_jct();
        System.out.println(body);
        JSONObject post = new Request(webAPI.getURL_49(), body).post();
        return post;
    }
    // B站直播获取房间信息
    public JSONObject xliveGetRoomInfo(String room_id) throws Exception {
        String param = "?room_id="+room_id;
        JSONObject jsonObject = new Request(webAPI.getURL_50() + param).get();
        return jsonObject;
    }
    // B站直播 直播间心跳
    public void xliveWebHeartBeat(String biz_id,String last,String platform) {

    }
    // B站直播 心跳(大约2分半一次)
    public JSONObject xliveHeartBeat() throws Exception {
        JSONObject jsonObject = new Request(webAPI.getURL_52()).get();
        return jsonObject;
    }
    // B站直播 用户在线心跳(很少见)
    public JSONObject xliveUserOnlineHeart() throws Exception {
        String body = "csrf="+ce.getBili_jct();
        JSONObject post = new Request(webAPI.getURL_53(), body).post();
        return post;
    }
    // 模拟B站漫画客户端签到
    public JSONObject mangaClockIn(String platform) throws Exception {
        String body = "platform="+platform;
        JSONObject post = new Request(webAPI.getURL_54(), body).post();
        return post;
    }
    // 获取钱包信息
    public JSONObject mangaGetWallet(String platform) throws Exception {
        String param = "?platform="+platform;
        JSONObject post = new Request(webAPI.getURL_55()+param, "").post();
        return post;
    }
    // 站友日漫画卷兑换
    public JSONObject mangaComrade(String platform) throws Exception{
        String param = "?platform="+platform;
        JSONObject post = new Request(webAPI.getURL_56() + param, "").post();
        return post;
    }
    // 获取漫画购买信息
    public JSONObject mangaGetEpisodeBuyInfo(String ep_id,String platform) throws Exception {
        String param = "?platform="+platform;
        String body = "ep_id="+ep_id;
        JSONObject post = new Request(webAPI.getURL_57(), body).post();
        return post;
    }

}
