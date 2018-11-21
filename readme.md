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





