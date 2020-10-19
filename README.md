<div align="center"> 
<h1 align="center">Bilibili助手</h1>
<img src="https://img.shields.io/github/issues/srcrs/BilibiliTask?color=green">
<img src="https://img.shields.io/github/stars/srcrs/BilibiliTask?color=yellow">
<img src="https://img.shields.io/github/forks/srcrs/BilibiliTask?color=orange">
<img src="https://img.shields.io/github/license/srcrs/BilibiliTask?color=ff69b4">
<img src="https://img.shields.io/github/search/srcrs/BilibiliTask/main?color=blue">
</div>

# 简介

👯✨😄📫

哔哩哔哩(B站)自动完成每日任务，
投币，点赞，直播签到，自动兑换银瓜子为硬币，自动送出即将过期礼物，漫画App签到，大会员领取B币卷等。每天获得65点经验，助你快速升级到Lv6。

开源不易，如果本项目对你有帮助，那么就请给个star吧。😄

# 功能

* [x] 自动获取经验(投币、点赞、分享视频) 
* [x] 直播辅助(直播签到，自动送出即将过期的礼物) 
* [x] 自动兑换银瓜子为硬币 
* [x] 自动领取年度大会员每月权益(B币劵，权益礼包) 
* [x] 月底自动用B币卷给自己充电
* [x] 漫画辅助脚本(漫画APP签到) 
* [x] 加入配置文件，用户可自定义执行
* [x] 账户失效提醒

# 目录

- [简介](#简介)
- [功能](#功能)
- [目录](#目录)
- [使用方法](#使用方法)
  - [1.fork本项目](#1fork本项目)
  - [2.准备需要的参数](#2准备需要的参数)
  - [3.将获取到参数填到Secrets](#3将获取到参数填到secrets)
  - [4.开启actions](#4开启actions)
  - [5.运行一次工作流](#5运行一次工作流)
  - [配置文件说明](#配置文件说明)
- [如何拉取最新代码](#如何拉取最新代码)
- [更新日志](#更新日志)
  - [2020-10-19](#2020-10-19)
  - [2020-10-17](#2020-10-17)
  - [2020-10-13](#2020-10-13)
  - [2020-10-08](#2020-10-08)
  - [2020-10-07](#2020-10-07)
  - [2020-10-06](#2020-10-06)
  - [2020-10-05](#2020-10-05)
- [参考项目](#参考项目)

# 使用方法

## 1.fork本项目

项目地址：[srcrs/BilibiliTask](https://github.com/srcrs/BilibiliTask)

## 2.准备需要的参数

本项目成功运行需要三个参数，分别是`SESSDATA`，`bili_jct`，`DedeUserID`

- 打开`b`站首页（任意一个页面都行）--> 按下`F12` --> `Application` --> `Cookies` --> `https://www.bilibili.com`

- 找到需要所需要的参数对应的数据，找不到可能是你的账号没有登录。

![](img/1.png)

## 3.将获取到参数填到Secrets

在`Secrets`中的`Name`和`Value`格式如下：

Name | Value
-|-
BILI_JCT | xxxxx
DEDEUSERID | xxxxx
SESSDATA | xxxxx

上一步获取的参数，替换对应的`xxxxx`，一共需要添加三个键值对。

![](img/2.png)

## 4.开启actions

默认actions处于禁止状态，在`Actions`中开启`Actions`功能，把那个绿色的长按钮点一下。

![](img/3.png)

## 5.运行一次工作流

项目创建wiki则会触发一次工作流。

+ `Wiki` --> `Create the first` --> `Save Page`

+ 查看`actions`，显示对勾就说明运行成功了。以后会在每天的`10：30`执行，自动完成每日任务。

![](img/4.png)

## 配置文件说明

配置文件的位置在`src/main/resource/config.yml`。

```yml
coin: 5 #每天需要投币的数量，范围为[0,5]
gift: true #送出即将过期礼物,默认送出为true
s2c: true #银币转换硬币,默认转换为true
autoCharge: true #是否允许月底B币卷给自己充电
``` 

# 如何拉取最新代码

1、查看是否有源头仓库的别名和地址

```sh
$ git remote -v
origin  https://github.com/cmdcs/BilibiliTask.git (fetch)
origin  https://github.com/cmdcs/BilibiliTask.git (push)
upstream  https://github.com/srcrs/BilibiliTask (fetch)
upstream  https://github.com/srcrs/BilibiliTask (push)
```

`origin`是你的仓库地址，`upstream`是你`fork`的源头仓库。通常第一次是没有`upstream`的。

2、添加源头仓库

```sh
git remote add upstream https://github.com/srcrs/BilibiliTask
```

3、把上游仓库main分支的更新拉取到本地

```sh
git pull upstream main
```

4、将更新后的代码推送到你的仓库

```sh
git push origin main 
```

由于添加有配置文件config.yml，有可能会覆盖你自定义的config.yml，需要注意。

# 更新日志
## 2020-10-19

+ 增加年度大会员每月1号领取B币卷

+ 月底自动用B币卷给自己充电

+ 在配置项中添加是否月底用B币卷给自己充电开关，默认开启

由于我本身不是年度大会员，无法测试是否可以正常领取年度大会员权益和B币卷，
出错的时候麻烦给我提一个issues，我会及时解决的。

## 2020-10-17

+ 优化日志显示

+ 增加账户失效提醒

账户失效会导致任务流运行失败，github会下发运行失败邮件提醒。

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

# 参考项目

[happy888888/BiliExp](https://github.com/happy888888/BiliExp)
