#入职须知
1. 找段兵营分配个企业邮箱帐号
2. 下载 Tunnelblick VPN ,找段兵营要个vpn的帐号，代码仓库在阿里云上，代码的下载与提交都需要vpn。vpn配置会在邮箱里

# 代码下载

项目目录结构如下
livesdk
	app
	skr_flutter
	pbgit

1. 撕歌主项目代码 地址 http://git.inframe.club/chengsimin/livesdk 。访问不了找相关人员配置Member加权限
2. 撕歌项目flutter部分代码 地址 http://git.inframe.club/skrer/skr_flutter 。 访问不了找相关人员配置Member加权限
3. 撕歌项目pbgit部分代码 地址 http://git.inframe.club/skrer/pb . PbGit 定义 下行信令消息 的Protocol buffer 协议。 房间内的状态机信令信息都在这。

# 编译环境配置
1. 代码下载完成后
2. 下载NDK，版本为 android-ndk-r15c 。因为音视频引擎原因，只支持这个版本
3. 配置 flutter 编译环境
	1. 下载 flutter sdk。
	2. 使用 flutter version ，切换flutter 分支为 1.9.1+hotfix.6 ，因为我们使用的一个重要的路由组件 flutter boost 只支持这个版本。
	3. 切换到 skr_flutter 目录下，运行 flutter pub get 确保生成 .android 目录

在AS中运行编译或者使用 ./ins.sh 脚本运行编译。


# 项目结构介绍
livesdk --- 撕歌项目
	app --- 空壳app，也有一些flutter methodchannel 代码。以及一些全局事件的处理代码
	baselibray --- 业务无关的基础库
		AndResGuard --- APK包大小压缩库
		android-gif-drawable --- 著名的开源gif库
		arcCloud --- 对演唱进行打分的库，花钱买的
		avstatistics --- 引擎统计
		commonsdk --- 基础库，图片压缩，播放器，音视频缓存工具，各种工具类，图片相册浏览等等等很多基础库，想到的功能几乎都有，不用在业务模块中再写基础库
		enginesdk --- 引擎库
		mediaengine --- 引擎库
		effectsdk --- 抖音魔法表情库
		my-utils-gradle --- gradle插件库，目前没用
		replugin-xxxx --- 360 replugin 插件库，目前没用，不参与编译
	commoncore --- 业务相关，但几乎所有App都有的业务。比如帐号，scheme，权限管理的封装，app的升级的等
	Component --- 业务基础组件库，比如app风格的统一弹窗，统一的歌词渲染控件，通知管理等等
	Module --- 业务模块
		Club --- 家族模块
		Feeds --- 帖子浏览
		Home --- 主页，设置，个人主页等
		IMKit --- 消息模块，对融云的封装，长链接信令的入口也在这
		PlayWays --- 所有游戏的主要玩法都在这，抢唱，排位，双人合唱，主题房等 最重要的模块
		    一共有4种房间类型
		    Grab 抢唱房 也叫一唱到底 我们最先做的功能，拉活的保证，就是一个房间7个人抢歌唱
		    Party 主题房 就是一个主持人6个嘉宾的那种 也是我们的主打功能 营收主要来源
		    Race 排位赛 蒙面排位
		    Relay 接唱 也叫双人合唱 就是 1v1 唱歌聊天
		Posts --- 帖子的制作与发布
	commonservice --- 定义了Module 对外暴露的接口		
	skr_flutter --- 一些逻辑简单的页面为了节约人力，统一用flutter写，android ios 公用

# 项目架构解析
	1. Module 之间无编译依赖，完全解耦，支持插拔
	2. Module间的通信借助ARouter依赖注入，接口在 commonservice 中暴露
	3. Module的初始化在 XXXConfiguration 中进行，Application在commonsdk层，BaseApplication，在onCreate时会通过反射找出各个Module的Configuration，并执行初始化。

# 相关地址
1. jira地址，测试在上面开bug。负责人，陈墨   https://www.bugclose.com/console.html
2. 石墨文档地址。产品的需求文档在上面。负责人，丁一   https://shimo.im/dashboard
3. 蓝湖地址。设计的设计图在上面。负责人，陈晋涛   https://lanhuapp.com/web/#/item
4. wiki地址 http://wiki.inframe.club/confluence/pages/viewpage.action?pageId=3670683

5. 服务器接口文档 http://doc.inframe.club/#/Ocean/
    1. battlegame.json 团战
    2. partygame.json 主题房游戏
    3. partyroom.json 主题房房间相关 其他模块类似 如 racegame raceroom 是排位赛 relaygame relayroom 是双人合唱

6. 客户端所有scheme地址 http://wiki.inframe.club/confluence/pages/viewpage.action?pageId=3670683
7. 企业邮箱地址 https://qiye.aliyun.com/alimail/
8. bugly 崩溃管理平台。https://bugly.qq.com/v2/product/apps/75917797f3?pid=1 
9. http://op.inframe.mobi/report/feedback 用户反馈后台线上环境，查看线上用户反馈的地方
   http://test.op.inframe.mobi/feedback/list  用户反馈后台test环境， dev sandbox环境类似
   用户名 Admin 密码 123456


以上任何地址如果没有权限，在大群里找相关责任人添加权限。

# 代码查看技巧
使用
adb shell dumpsys activity activities | sed -En -e '/Running activities/,/Run #0/p'
查看当前手机页面属于哪个Activity，然后再找这个activity看代码

# 打包上传流程
### 环境介绍
dev 开发环境 初期开发一般在这个环境，服务器的接口也会先部署在这个环境
test 测试环境 一般是测试在这个环境做功能验证回归
sandbox 沙盒环境 一般是服务器用
default 线上缺省环境 这个渠道不会覆盖任何现有的线上渠道，一般用作自升级的包。比如当前用户渠道是MI_SHOP，来自小米应用商店，
升级包配置的是 DEFAULT 渠道的包，升级安装后，渠道号还为 MI_SHOP。

### 打包发版流程
1. 打测试包 一般给测试 test 环境的包，命令为 ./upload.sh TEST build 。会上传包到蒲公英后台 https://www.pgyer.com/my ，输出二维码。
2. 测试bug全部解决修复后，会使用 ./ins.sh app release all 打全渠道包，并将 DEFAULT 上传阿里云 oss 后台，生成url链接。
https://signin.aliyun.com/skrer.onaliyun.com/login.htm
将生成的链接丢给 段兵营 配置升级。线上用户就会收到升级提示
3. 观察一段时间新版本 bugly 线上的崩溃率 ，修复崩溃
4. 版本稳定后，./ins.sh app release all 打全渠道包 。并将所有渠道包通过U盘拷贝给运营同学，运营同学会将包上传到各应用商店

