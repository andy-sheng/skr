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


附属一些sdk库的账号以及密码
蒲公英sdk 763585627@qq.com s2343288
百度地图sdk 15718887533 s2343288