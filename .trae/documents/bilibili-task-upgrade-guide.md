# Bilibili 打卡项目升级指南

## 📋 项目概述

本文档基于对 GitHub 上活跃的 bilibili 打卡项目调研，以及对当前项目代码的深入分析，提供了一个完整的项目升级方案。目标是将现有项目升级为符合最新 bilibili API 规范的现代化打卡系统。

## 🔍 1. 当前项目状态分析

### 1.1 项目基本信息
- **项目类型**: Java Gradle 项目，基于 srcrs/BilibiliTask 的兼容性修复版
- **Java 版本**: 支持 Java 11+
- **主要依赖**: Fastjson2 2.0.43, Apache HttpClient 4.5.14
- **部署方式**: GitHub Actions 自动化执行

### 1.2 现有功能模块
- ✅ **每日任务**: 登录、观看视频、分享视频、投币
- ✅ **直播功能**: 直播签到、银瓜子兑换、礼物赠送
- ✅ **大会员功能**: B币券领取和使用
- ✅ **漫画签到**: 自动漫画签到
- ✅ **多种推送**: 支持微信、钉钉、Telegram等推送方式

### 1.3 技术架构现状
- **网络请求**: 基于 Apache HttpClient 的 Request 工具类
- **WBI 签名**: 已实现 WbiSignature 工具类
- **bili_ticket**: 已实现 BiliTicket 工具类
- **配置管理**: 基于 YAML 的配置系统

## ⚠️ 2. 发现的API兼容性问题

### 2.1 关键问题清单

#### 🔴 高优先级问题
1. **WBI 签名算法更新**
   - 问题: 2024年后 WBI 签名算法有所变化，增加了 `w_ks` 参数和 `swapString` 操作
   - 影响: 部分需要 WBI 签名的 API 可能失效
   - 状态: 当前实现基本正确，但缺少最新的算法更新

2. **API 端点变化**
   - 问题: 部分 API 端点已更新或废弃
   - 影响: 可能导致请求失败或返回错误数据
   - 状态: 需要逐一验证和更新

3. **bili_ticket 验证**
   - 问题: 新增的 bili_ticket 验证机制
   - 影响: 某些 API 调用需要额外的票据验证
   - 状态: 已实现基础版本，需要完善

#### 🟡 中优先级问题
1. **用户代理字符串**
   - 问题: 固定的 UA 可能被识别为机器人
   - 影响: 增加被风控的风险
   - 状态: 需要实现动态 UA 生成

2. **请求频率控制**
   - 问题: 缺乏智能的请求间隔控制
   - 影响: 可能触发频率限制
   - 状态: 需要优化等待策略

### 2.2 具体API问题分析

#### 每日任务相关API
- `https://api.bilibili.com/x/member/web/exp/reward` - ✅ 正常
- `https://api.bilibili.com/x/web-interface/wbi/index/top/feed/rcmd` - ⚠️ 需要WBI签名
- `https://api.bilibili.com/x/v2/history/report` - ✅ 正常
- `https://api.bilibili.com/x/web-interface/share/add` - ✅ 正常

#### 投币相关API
- `https://api.bilibili.com/x/web-interface/coin/add` - ⚠️ 需要WBI签名
- `https://api.bilibili.com/x/web-interface/nav` - ✅ 正常

#### 直播相关API
- `https://api.live.bilibili.com/xlive/web-ucenter/v1/sign/DoSign` - ✅ 正常
- `https://api.live.bilibili.com/pay/v1/Exchange/silver2coin` - ✅ 正常

## 📊 3. 最新bilibili API变化总结

### 3.1 WBI签名机制演进

基于调研发现，bilibili在2023年3月引入WBI签名机制，并在2024年进行了算法更新：

#### 原始WBI算法（2023年3月）
```
1. 获取 img_key 和 sub_key
2. 生成 mixin_key
3. 添加 wts 时间戳
4. 参数排序和编码
5. 计算 MD5 得到 w_rid
```

#### 更新后的WBI算法（2024年）
```
1. 获取 img_key 和 sub_key
2. 生成 mixin_key
3. 添加 wts 时间戳
4. 可选添加 w_ks 参数（通过 swapString 函数处理）
5. 参数排序和编码
6. 计算 MD5 得到 w_rid
```

新增的 `swapString` 函数：
```javascript
function swapString(e, t) {
    if (e.length % 2) return e;
    if (0 === t) return e;
    if (e.length === Math.pow(2, t)) 
        return e.split("").reverse().join();
    var r = e.slice(0, e.length / 2),
        n = e.slice(e.length / 2);
    return "".concat(swapString(n, t - 1)).concat(swapString(r, t - 1))
}
```

### 3.2 bili_ticket机制

bili_ticket是bilibili新增的验证机制，用于增强API安全性：

- **获取方式**: 通过专门的API端点获取
- **使用方式**: 添加到Cookie中
- **有效期**: 通常为几小时
- **适用范围**: 部分敏感API调用

### 3.3 API端点更新

#### 推荐视频API
- 旧: `https://api.bilibili.com/x/web-interface/dynamic/region`
- 新: `https://api.bilibili.com/x/web-interface/wbi/index/top/feed/rcmd`

#### 用户信息API
- 增强: 需要更多的验证参数（dm_img_str, dm_img_inter等）

## 🔧 4. 具体的代码更新方案

### 4.1 WBI签名算法升级

#### 当前实现评估
现有的 `WbiSignature.java` 实现了基础的WBI签名算法，但缺少最新的更新。

#### 升级方案
```java
// 在 WbiSignature.java 中添加新的方法
public static Map<String, String> getWbiSignWithWks(Map<String, Object> params, boolean addSelf) {
    try {
        updateKeys();
        String mixinKey = getMixinKey(imgKey + subKey);
        long wts = System.currentTimeMillis() / 1000;
        params.put("wts", wts);
        
        // 新增 w_ks 参数支持
        if (addSelf) {
            String wKs = swapString(imgKey + subKey, 2);
            params.put("w_ks", wKs);
        }
        
        // 其余逻辑保持不变
        String query = buildSortedQuery(params);
        String wRid = md5(query + mixinKey);
        
        Map<String, String> result = new HashMap<>();
        result.put("w_rid", wRid);
        result.put("wts", String.valueOf(wts));
        if (addSelf) {
            result.put("w_ks", params.get("w_ks").toString());
        }
        
        return result;
    } catch (Exception e) {
        log.error("WBI签名生成失败: ", e);
        return new HashMap<>();
    }
}

// 添加 swapString 方法
private static String swapString(String str, int depth) {
    if (str.length() % 2 != 0) return str;
    if (depth == 0) return str;
    if (str.length() == Math.pow(2, depth)) {
        return new StringBuilder(str).reverse().toString();
    }
    
    String left = str.substring(0, str.length() / 2);
    String right = str.substring(str.length() / 2);
    return swapString(right, depth - 1) + swapString(left, depth - 1);
}
```

### 4.2 bili_ticket机制完善

#### 当前实现评估
现有的 `BiliTicket.java` 提供了基础实现，需要增强错误处理和缓存机制。

#### 升级方案
```java
// 在 BiliTicket.java 中添加改进
public class BiliTicket {
    private static String cachedTicket = "";
    private static long lastUpdateTime = 0;
    private static final long TICKET_VALIDITY = 2 * 60 * 60 * 1000; // 2小时
    private static final int MAX_RETRIES = 3;
    
    public static String getBiliTicket() {
        long currentTime = System.currentTimeMillis();
        
        // 检查缓存是否有效
        if (!cachedTicket.isEmpty() && 
            (currentTime - lastUpdateTime) < TICKET_VALIDITY) {
            return cachedTicket;
        }
        
        // 获取新的ticket
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                JSONObject params = new JSONObject();
                params.put("csrf", UserData.getInstance().getBiliJct());
                
                JSONObject response = Request.postWithoutBiliTicket(
                    "https://api.bilibili.com/bapis/bilibili.api.ticket.v1.Ticket/GenWebTicket", 
                    params
                );
                
                if ("0".equals(response.getString("code"))) {
                    JSONObject data = response.getJSONObject("data");
                    cachedTicket = data.getString("ticket");
                    lastUpdateTime = currentTime;
                    log.info("bili_ticket更新成功");
                    return cachedTicket;
                }
            } catch (Exception e) {
                log.warn("bili_ticket获取失败，重试 {}/{}: {}", i + 1, MAX_RETRIES, e.getMessage());
                if (i < MAX_RETRIES - 1) {
                    try {
                        Thread.sleep(1000 * (i + 1)); // 递增等待时间
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        log.warn("bili_ticket获取失败，使用空值");
        return "";
    }
}
```

### 4.3 API端点更新

#### DailyTask.java 更新
```java
// 更新 getRegions 方法
public JSONArray getRegions(String ps, String rid) {
    try {
        JSONObject pJson = new JSONObject();
        pJson.put("ps", ps);
        pJson.put("rid", rid);
        pJson.put("fresh_type", "3");
        pJson.put("version", "1");
        pJson.put("fresh_idx_1h", "1");
        pJson.put("fetch_row", "1");
        pJson.put("fresh_idx", "1");
        pJson.put("brush", "0");
        pJson.put("homepage_ver", "1");
        pJson.put("ps", "12");
        
        // 优先使用新的推荐API
        JSONObject jsonObject = Request.getWithWbi(
            "https://api.bilibili.com/x/web-interface/wbi/index/top/feed/rcmd", 
            pJson
        );
        
        if ("0".equals(jsonObject.getString("code"))) {
            JSONObject data = jsonObject.getJSONObject("data");
            if (data.containsKey("item")) {
                return processRecommendVideos(data.getJSONArray("item"));
            }
        }
        
        // 降级到分区API
        log.warn("推荐API失败，使用分区API");
        return getRegionVideos(ps, rid);
        
    } catch (Exception e) {
        log.error("获取视频列表失败: ", e);
        return getBackupVideoList();
    }
}

// 新增方法处理推荐视频数据
private JSONArray processRecommendVideos(JSONArray items) {
    JSONArray result = new JSONArray();
    for (Object item : items) {
        JSONObject video = (JSONObject) item;
        JSONObject processed = new JSONObject();
        processed.put("title", video.getString("title"));
        processed.put("aid", video.getString("id"));
        processed.put("bvid", video.getString("bvid"));
        processed.put("cid", video.getString("cid"));
        result.add(processed);
    }
    return result;
}
```

#### ThrowCoinTask.java 更新
```java
// 更新投币方法，确保WBI签名
public JSONObject throwCoin(String aid, String num, String selectLike) {
    try {
        JSONObject pJson = new JSONObject();
        pJson.put("aid", aid);
        pJson.put("multiply", num);
        pJson.put("select_like", selectLike);
        pJson.put("cross_domain", "true");
        pJson.put("csrf", USER_DATA.getBiliJct());
        
        // 使用WBI签名的POST请求
        JSONObject response = Request.postWithWbi(
            "https://api.bilibili.com/x/web-interface/coin/add", 
            pJson
        );
        
        // 如果WBI请求失败，尝试普通POST（向后兼容）
        if (response == null || 
            response.toString().contains("HTML") || 
            "-352".equals(response.getString("code"))) {
            log.warn("WBI投币请求失败，尝试普通POST");
            response = Request.post(
                "https://api.bilibili.com/x/web-interface/coin/add", 
                pJson
            );
        }
        
        return response;
    } catch (Exception e) {
        log.error("投币请求异常: ", e);
        JSONObject errorResponse = new JSONObject();
        errorResponse.put("code", "-1");
        errorResponse.put("message", "请求异常: " + e.getMessage());
        return errorResponse;
    }
}
```

### 4.4 用户代理和请求优化

#### InitUserAgent.java 增强
```java
public class InitUserAgent {
    private static final String[] USER_AGENTS = {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/121.0",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Safari/605.1.15"
    };
    
    private static final Random random = new Random();
    
    public static String getOne() {
        return USER_AGENTS[random.nextInt(USER_AGENTS.length)];
    }
    
    // 根据时间生成相对稳定的UA（同一天内保持一致）
    public static String getDailyUA() {
        long daysSinceEpoch = System.currentTimeMillis() / (1000 * 60 * 60 * 24);
        int index = (int) (daysSinceEpoch % USER_AGENTS.length);
        return USER_AGENTS[index];
    }
}
```

#### Request.java 请求间隔优化
```java
// 在 Request.java 中添加智能等待
private static final Random random = new Random();
private static long lastRequestTime = 0;
private static final long MIN_INTERVAL = 1000; // 最小间隔1秒
private static final long MAX_INTERVAL = 3000; // 最大间隔3秒

private static void waitFor() {
    long currentTime = System.currentTimeMillis();
    long timeSinceLastRequest = currentTime - lastRequestTime;
    
    if (timeSinceLastRequest < MIN_INTERVAL) {
        long waitTime = MIN_INTERVAL + random.nextInt((int)(MAX_INTERVAL - MIN_INTERVAL));
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    lastRequestTime = System.currentTimeMillis();
}
```

## 🧪 5. 测试验证计划

### 5.1 单元测试计划

#### 5.1.1 WBI签名测试
```java
@Test
public void testWbiSignature() {
    Map<String, Object> params = new HashMap<>();
    params.put("mid", "123456");
    params.put("token", "");
    params.put("platform", "web");
    
    Map<String, String> result = WbiSignature.getWbiSign(params);
    
    assertNotNull(result.get("w_rid"));
    assertNotNull(result.get("wts"));
    assertEquals(32, result.get("w_rid").length());
}

@Test
public void testWbiSignatureWithWks() {
    Map<String, Object> params = new HashMap<>();
    params.put("keyword", "测试");
    
    Map<String, String> result = WbiSignature.getWbiSignWithWks(params, true);
    
    assertNotNull(result.get("w_rid"));
    assertNotNull(result.get("wts"));
    assertNotNull(result.get("w_ks"));
}
```

#### 5.1.2 bili_ticket测试
```java
@Test
public void testBiliTicket() {
    String ticket = BiliTicket.getBiliTicket();
    // ticket可能为空（如果获取失败），但不应该抛出异常
    assertNotNull(ticket);
}
```

#### 5.1.3 API调用测试
```java
@Test
public void testDailyTaskAPIs() {
    // 测试获取推荐视频
    DailyTask dailyTask = new DailyTask();
    JSONArray videos = dailyTask.getRegions("6", "1");
    assertNotNull(videos);
    assertTrue(videos.size() > 0);
    
    // 测试获取每日任务状态
    JSONObject reward = dailyTask.getReward();
    assertNotNull(reward);
}

@Test
public void testThrowCoinAPI() {
    ThrowCoinTask throwCoinTask = new ThrowCoinTask();
    
    // 测试获取投币状态
    Integer reward = throwCoinTask.getReward();
    assertNotNull(reward);
    assertTrue(reward >= 0 && reward <= 50);
}
```

### 5.2 集成测试计划

#### 5.2.1 完整流程测试
1. **环境准备**
   - 准备测试用的bilibili账号
   - 配置测试环境的Cookie信息
   - 设置测试专用的配置文件

2. **功能测试**
   ```bash
   # 运行完整的每日任务
   java -jar BILIBILI-HELPER.jar
   
   # 检查日志输出
   tail -f /var/log/bilibili-help.log
   ```

3. **验证点**
   - ✅ 账户验证成功
   - ✅ 每日任务完成（观看、分享、投币）
   - ✅ 直播签到成功
   - ✅ 银瓜子兑换成功
   - ✅ 推送消息发送成功

#### 5.2.2 错误处理测试
1. **网络异常测试**
   - 模拟网络超时
   - 模拟API返回错误
   - 验证重试机制

2. **认证失效测试**
   - 使用过期的Cookie
   - 验证错误提示和处理

3. **限流测试**
   - 快速连续请求
   - 验证限流保护机制

### 5.3 性能测试

#### 5.3.1 响应时间测试
- 单个API调用响应时间 < 5秒
- 完整任务执行时间 < 2分钟
- WBI签名生成时间 < 100ms

#### 5.3.2 稳定性测试
- 连续运行7天无异常
- 内存使用稳定，无内存泄漏
- 日志文件大小控制在合理范围

## 🚀 6. 部署和使用说明

### 6.1 环境要求

#### 6.1.1 基础环境
- **Java**: JDK 11 或更高版本
- **Gradle**: 7.0 或更高版本（项目自带wrapper）
- **操作系统**: Windows/Linux/macOS

#### 6.1.2 依赖检查
```bash
# 检查Java版本
java -version

# 检查Gradle版本
./gradlew --version
```

### 6.2 部署方式

#### 6.2.1 GitHub Actions部署（推荐）

1. **Fork项目**
   ```bash
   # 克隆项目到本地
   git clone https://github.com/your-username/BilibiliTask_3.git
   cd BilibiliTask_3
   ```

2. **配置Secrets**
   在GitHub仓库的Settings > Secrets and variables > Actions中添加：
   
   | Name | Value | 说明 |
   |------|-------|------|
   | BILI_JCT | 你的bili_jct值 | 从Cookie中获取 |
   | DEDEUSERID | 你的DedeUserID值 | 从Cookie中获取 |
   | SESSDATA | 你的SESSDATA值 | 从Cookie中获取 |
   | SCKEY | Server酱推送key | 可选 |
   | DINGTALK | 钉钉机器人webhook | 可选 |

3. **启用Actions**
   - 进入Actions页面
   - 点击"I understand my workflows, go ahead and enable them"
   - 手动触发一次运行或等待定时执行

4. **验证部署**
   ```bash
   # 查看Actions运行日志
   # 确认所有任务都成功执行
   ```

#### 6.2.2 本地部署

1. **编译项目**
   ```bash
   # 编译并生成jar包
   ./gradlew shadowJar
   
   # jar包位置：build/libs/BilibiliTask-1.0.10-all.jar
   ```

2. **配置文件**
   ```yaml
   # src/main/resources/config.yml
   coin: 5
   gift: true
   s2c: true
   autoBiCoin: 1
   platform: android
   upList:
     - 477137547
   manga: true
   upLive: 477137547
   selectLike: 0
   ```

3. **设置环境变量**
   ```bash
   # Linux/macOS
   export BILI_JCT="your_bili_jct"
   export DEDEUSERID="your_dedeuserid"
   export SESSDATA="your_sessdata"
   
   # Windows
   set BILI_JCT=your_bili_jct
   set DEDEUSERID=your_dedeuserid
   set SESSDATA=your_sessdata
   ```

4. **运行程序**
   ```bash
   java -jar build/libs/BilibiliTask-1.0.10-all.jar
   ```

#### 6.2.3 Docker部署

1. **构建镜像**
   ```dockerfile
   # Dockerfile已存在，直接构建
   docker build -t bilibili-task .
   ```

2. **运行容器**
   ```bash
   docker run -d \
     --name bilibili-task \
     -e BILI_JCT="your_bili_jct" \
     -e DEDEUSERID="your_dedeuserid" \
     -e SESSDATA="your_sessdata" \
     -e SCKEY="your_sckey" \
     bilibili-task
   ```

3. **定时执行**
   ```bash
   # 使用cron定时执行
   0 10 * * * docker run --rm \
     -e BILI_JCT="your_bili_jct" \
     -e DEDEUSERID="your_dedeuserid" \
     -e SESSDATA="your_sessdata" \
     bilibili-task
   ```

### 6.3 配置说明

#### 6.3.1 基础配置
```yaml
# config.yml 详细说明
coin: 5              # 每日投币数量 [0-5]
gift: true           # 是否送出即将过期礼物
s2c: true            # 是否将银瓜子兑换为硬币
autoBiCoin: 1        # B币券使用方式 [0:不使用, 1:给自己充电, 2:兑换金瓜子]
platform: android    # 设备平台标识
manga: true          # 是否进行漫画签到
selectLike: 0        # 投币时是否点赞 [0:不点赞, 1:点赞]

# UP主优先投币列表
upList:
  - 477137547        # UP主的UID
  - 14602398

# 礼物赠送目标直播间
upLive: 477137547    # UP主的UID
```

#### 6.3.2 推送配置

**Server酱推送**
```bash
# 设置SCKEY环境变量
export SCKEY="SCT123456789"
```

**钉钉推送**
```bash
# 设置钉钉机器人webhook
export DINGTALK="https://oapi.dingtalk.com/robot/send?access_token=xxx"
```

**Telegram推送**
```bash
export TELEGRAM_BOT_TOKEN="123456789:ABCdefGHIjklMNOpqrsTUVwxyz"
export TELEGRAM_CHAT_ID="123456789"
```

### 6.4 监控和维护

#### 6.4.1 日志监控
```bash
# 查看运行日志
tail -f logs/bilibili-task.log

# 检查错误日志
grep "ERROR" logs/bilibili-task.log

# 统计成功率
grep -c "成功" logs/bilibili-task.log
```

#### 6.4.2 健康检查
```bash
#!/bin/bash
# health-check.sh

# 检查最近一次运行是否成功
LAST_RUN=$(grep "本次任务运行完毕" logs/bilibili-task.log | tail -1)
if [ -z "$LAST_RUN" ]; then
    echo "警告：最近一次运行可能失败"
    exit 1
fi

# 检查Cookie是否过期
COOKIE_ERROR=$(grep "账号未登录" logs/bilibili-task.log | tail -1)
if [ ! -z "$COOKIE_ERROR" ]; then
    echo "警告：Cookie可能已过期"
    exit 1
fi

echo "健康检查通过"
exit 0
```

#### 6.4.3 自动更新
```bash
#!/bin/bash
# auto-update.sh

# 检查是否有新版本
cd /path/to/BilibiliTask_3
git fetch origin

LOCAL=$(git rev-parse HEAD)
REMOTE=$(git rev-parse origin/main)

if [ $LOCAL != $REMOTE ]; then
    echo "发现新版本，开始更新..."
    git pull origin main
    ./gradlew shadowJar
    echo "更新完成"
else
    echo "已是最新版本"
fi
```

### 6.5 故障排除

#### 6.5.1 常见问题

**问题1：账号验证失败**
```
错误信息：💔账户验证失败，程序退出
解决方案：
1. 检查Cookie是否正确
2. 检查Cookie是否过期
3. 重新获取Cookie信息
```

**问题2：WBI签名失败**
```
错误信息：💔WBI签名生成失败
解决方案：
1. 检查网络连接
2. 检查nav接口是否可访问
3. 重启程序重新获取密钥
```

**问题3：投币失败**
```
错误信息：投币失败，返回-352
解决方案：
1. 检查账号硬币余额
2. 检查是否已达到每日投币上限
3. 检查视频是否可投币
```

**问题4：推送失败**
```
错误信息：推送消息发送失败
解决方案：
1. 检查推送配置是否正确
2. 检查网络连接
3. 检查推送服务是否正常
```

#### 6.5.2 调试模式
```bash
# 启用详细日志
export LOG_LEVEL=DEBUG
java -jar BilibiliTask-1.0.10-all.jar

# 或者修改logback配置
# src/main/resources/logback.xml
<root level="DEBUG">
    <appender-ref ref="STDOUT" />
    <appender-ref ref="FILE" />
</root>
```

## 📈 7. 性能优化建议

### 7.1 请求优化
- 实现请求缓存机制
- 优化请求间隔策略
- 使用连接池复用HTTP连接

### 7.2 内存优化
- 及时释放大对象
- 优化JSON解析
- 控制日志文件大小

### 7.3 稳定性优化
- 增加重试机制
- 实现熔断器模式
- 添加健康检查接口

## 🔒 8. 安全建议

### 8.1 敏感信息保护
- 不要在代码中硬编码Cookie
- 使用环境变量存储敏感信息
- 定期更换Cookie信息

### 8.2 访问控制
- 限制程序运行权限
- 使用专用账号运行
- 定期检查访问日志

### 8.3 风险控制
- 避免频繁请求
- 使用真实的用户代理
- 遵守平台使用条款

## 📝 9. 更新日志

### v1.0.10 (2025-01-21)
- ✅ 升级到Java 11+支持
- ✅ 更新Fastjson到2.0.43版本
- ✅ 实现WBI签名机制
- ✅ 添加bili_ticket支持
- ✅ 优化错误处理和重试机制
- ✅ 增强日志输出和监控

### 计划中的更新
- 🔄 实现最新的WBI签名算法（w_ks支持）
- 🔄 添加更多推送方式支持
- 🔄 实现Web管理界面
- 🔄 支持多账号管理
- 🔄 添加数据统计功能

## 🤝 10. 贡献指南

### 10.1 开发环境搭建
```bash
# 克隆项目
git clone https://github.com/your-username/BilibiliTask_3.git
cd BilibiliTask_3

# 安装依赖
./gradlew build

# 运行测试
./gradlew test
```

### 10.2 提交规范
- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- style: 代码格式调整
- refactor: 代码重构
- test: 测试相关
- chore: 构建过程或辅助工具的变动

### 10.3 代码规范
- 使用Java 11+语法特性
- 遵循阿里巴巴Java开发手册
- 添加适当的注释和文档
- 编写单元测试

## ⚠️ 11. 免责声明

1. 本项目仅供学习和研究使用
2. 请遵守bilibili平台的使用条款
3. 不要滥用API接口
4. 使用本项目造成的任何后果由使用者自行承担
5. 项目作者不对任何损失负责

## 📞 12. 支持和反馈

- **GitHub Issues**: 提交bug报告和功能请求
- **讨论区**: 参与项目讨论
- **邮件联系**: 发送邮件到项目维护者

---

**最后更新**: 2025年1月21日  
**文档版本**: v1.0  
**项目版本**: v1.0.10  

> 💡 **提示**: 本文档会随着项目的更新而持续维护，建议定期查看最新版本。