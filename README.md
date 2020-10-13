# Bilibili助手

👯欢迎访问。✨😄📫

本项目将要完成的功能 

* [x] 自动获取经验(投币、点赞、分享视频) 
* [x] 直播辅助(直播签到，自动送出即将过期的礼物) 
* [x] 自动兑换银瓜子为硬币 
* [ ] 自动领取大会员每月权益(B币劵，优惠券) 
* [x] 漫画辅助脚本(漫画APP签到) 
* [x] 加入配置文件，用户可自定义执行

# 使用方法

## 1.fork本项目

项目地址：[srcrs/BilibiliTask](https://github.com/srcrs/BilibiliTask)

## 2.需要的参数

本项目想成功运行需要三个参数，分别是`SESSDATA`，`bili_jct`，`DedeUserID`

在`Secrets`中的`Name`和`Value`格式如下：

Name | Value
-|-
BILI_JCT | xxxxx
DEDEUSERID | xxxxx
SESSDATA | xxxxx

从下一步骤获取需要的参数，替换对应的`xxxxx`，一共需要添加三个键值对。

![](img/2.png)

## 3.如何获取需要的参数

`b`站首页（任意一个页面都行）--> 按下`F12` --> `Application` --> `Cookies` --> `https://www.bilibili.com`

然后找到需要所需要的参数对应的数据，找不到可能是你的账号没有登录。

![](img/1.png)

## 4.开启actions

默认actions处于禁止状态，在`Actions`中开启`Actions`，把那个绿色的长按钮点一下。

![](img/3.png)

## 5.运行一次工作流

当填写完上面数据之后，再创建`wiki`，就可以运行一次工作流。

`Wiki` --> `Create the first` --> `Save Page`

然后查看`actions`，显示对勾就说明运行成功了。以后会在每天的`10：30`执行，自动完成每日任务。

## 配置文件说明

配置文件的位置在`src/main/resource/config.yml`。

```yml
coin: 5 #每天需要投币的数量，范围为[0,5]
gift: true #送出即将过期礼物,默认送出为true
s2c: true #银币转换硬币,默认转换为true
``` 

## 2020-10-13

+ 重构代码，功能不变

+ 采用反射实现自动加载`task`包功能任务代码。

+ 加入配置文件，用户可自定义一些配置

## 2020-10-08

+ 增加自动送出即将过期的礼物

+ 增加漫画`APP`签到

+ 增加一些`api`

## 2020-10-07

+ 增添银瓜子自动兑换硬币功能

## 2020-10-06

+ 增添B站直播签到

+ 继续增添`API`

## 2020-10-05

+ 完成了自动获取经验功能

每日登录、每日观看视频、每日投币、每日分享

+ 完善对接`api`接口

如果你觉得本项目对你有帮助，那么就请给个star吧😄

本项目参考了[happy888888/BiliExp](https://github.com/happy888888/BiliExp) ，万分感谢。改用`java`重写，未来可能会增加更多的功能，也有可能达不到参考项目的功能。