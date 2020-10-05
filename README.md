# Bilibili助手

👯欢迎访问。✨😄📫

本项目将要完成的功能 

* [x] 自动获取经验(投币、点赞、分享视频) 
* [ ] 自动转发互动抽奖
* [ ] 参与官方抽奖活动(activity)
* [ ] 直播辅助(直播签到，~~直播挂机~~，直播自动送出快过期礼物) 
* [ ] 自动兑换银瓜子为硬币 
* [ ] 自动领取大会员每月权益(B币劵，优惠券) 
* [ ] 漫画辅助脚本(漫画APP签到，自动花费即将过期漫读劵，自动积分兑换漫画福利券) 
* [ ] 定时清理无效动态(转发的过期抽奖，失效动态) 

# 使用方法

## 1.fork本项目

默认fork本项目。

## 2.需要的参数

本项目想成功运行需要三个参数，分别是SESSDATA，bili_jct，DedeUserID

## 3.如何获取需要的参数

b站首页（任意一个页面都行）--> 按下F12 --> Application --> Cookies --> https://www.bilibili.com

然后找到需要所需要的参数对应的数据，找不到可能是你的账号没有登录。

![](img/1.png)

## 4.Secrets的格式

需要把上面的SESSDATA，bili_jct，DedeUserID隐私数据添加到Secrets中。

新的Secrets的Name和Value格式如下：

Name | Value
- | -
BILI_JCT | xxxxx
DEDEUSERID | xxxxx
SESSDATA | xxxxx

从上一步骤获取的参数，替换对应的xxxxx，一共需要添加三个键值对。

![](img/2.png)

## 5.运行一次工作流

当填写完上面数据之后，再创建wiki，就可以运行一次工作流。

Wiki --> Create the first --> Save Page

然后查看actions，显示对勾就说明运行成功了。以后会在每天的10：30进行运行，自动完成每日任务。

## 2020-10-05

+ 完成了自动获取经验功能

+ 完善对接api接口

如果你觉得本项目对你有帮助，那么就请给个star吧😄

本项目参考了[happy888888/BiliExp](https://github.com/happy888888/BiliExp) ，万分感谢。改用java重写，未来可能会增加更多的功能，也有可能达不到参考项目的功能。