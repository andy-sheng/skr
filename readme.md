完全插件化架构
commonsdk为基础库，与具体项目无关
commoncore为账号等与项目有关，但几乎每个项目都有的功能
commonservice充当插件与插件之间交互的媒介

modulexxx为某个功能模块

app 为宿主 application module
plugindemo 为的插件demo

每个module下都有各自的readme.md对应解释

请运行 ins.sh 安装相应模块
如 ./ins.sh app
./ins.sh modulechannel

--------------------------

马上就要写业务代码了，这里我说下我的业务AndroidLibrary划分思路，你们补充啊
整体划分为Module Group 和 Component Group。
大的业务模块是紧跟业务feature的，放在Module Group中，
如经典模式，接唱模式，领唱模式 肯定是三个module，如 module:RankingMode,module:RelayMode,module:LeadingMode。
小的模块组件或者有工具属性的功能界面，在很多Module内都要用的。可以放在Component Group。
如选歌页面 component:SelectMusic，送礼相关面板与逻辑 component:Gift 等等，可能很多module都会用到。

Module
    RankingMode
    RelayMode
    LeadingMode
Component
    Gift
    SelectMusic

所有的Module Group中的模块是无编译依赖的,使用ARouter的依赖注入框架配合 commonservice 中的接口进行通信交互。
但每个Module是可以依赖 Component Group 中的N个模块。

因为Component的划分是紧耦合业务的，假如 选歌页面 只有经典模式有，那它就没必要成为一个Component。
所以前期开发只按module来，module里严格划分 java code source 和 res code source，后期有需要时可以方便地拆成Component。

尽量用Fragment写界面，有需要再包成Activity。

---------------------------

附属一些sdk库的账号以及密码
蒲公英sdk 763585627@qq.com s2343288
百度地图sdk 15718887533 s2343288
小米开发平台 程思敏的账号


自定义 gradle 插件。实现耗时方法统计
蒲公英 一键发布内测包 崩溃统计 用户反馈
带索引的联系人列表
减包
shape src 属性化
无侵入打点

-------------------------------
阿里云
zhenqukeji qwert12345

keytool -list -v  -keystore ~/dev/livesdk/livesdk/app/zq.keystore

别名: zq_android_key
创建日期: 2019-1-9
条目类型: PrivateKeyEntry
证书链长度: 1
证书[1]:
所有者: CN=chengsimin, OU=zhenqu, O=zhenqu, L=beijing, ST=beijing, C=86
发布者: CN=chengsimin, OU=zhenqu, O=zhenqu, L=beijing, ST=beijing, C=86
序列号: 45cfcac4
有效期开始日期: Wed Jan 09 11:35:25 CST 2019, 截止日期: Sun Jan 03 11:35:25 CST 2044
证书指纹:
	 MD5: 9F:9F:7C:A2:CF:43:35:BE:73:1E:AC:1A:23:D3:BD:89
	 SHA1: 5D:F4:53:66:7A:D4:64:0A:6C:67:63:41:C1:F1:15:3C:A1:46:5B:8E
	 SHA256: 2B:13:4E:1A:7E:F4:EB:98:3A:36:8E:81:0E:FC:5B:D1:46:B5:D4:96:21:87:E6:A4:A4:A7:62:78:14:F7:0C:9F
	 签名算法名称: SHA256withRSA
	 版本: 3

扩展:

#1: ObjectId: 2.5.29.14 Criticality=false
SubjectKeyIdentifier [
KeyIdentifier [
0000: AD 0B B4 CC 08 2B B8 34   82 45 F8 B6 0E A6 EF 21  .....+.4.E.....!
0010: CE C4 16 25                                        ...%
]
]



小米
账号：15910791239
密码：wujing123
 
oppo
用户：00326104568
账号：15801140410
密码：zhenqu123
邮箱：wujing@skrer.Net
 
 
Vivo
账号：zhenqukeji
密码：zhenqu.123
联系手机：15801140410
 
360
账号：15801140410
密码：zhenqu123
 
华为
账号：15801140410
密码：zhenqu123
 
应用宝
账号：3284986349
密码：zhenqukeji181105
 
百度
账号：15801140410
密码：zhenqu123
 
魅族
账号：15801140410
密码：zhenqu123

bugly
程思敏 的 QQ 扫码登录的



