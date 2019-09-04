
echo "
systemtrace 支持的模块如下 说明文档 https://zhuanlan.zhihu.com/p/27331842
如果要测试界面流畅度，我们一般只关注图形性能。因此必須选择Graphics和View gfx view
要查看具体数据可以通过快捷键完成，W/S 放大/缩小 A/D 左移/右移。

f	放大当前选定区域
m	标记当前选定区域
v	高亮VSync
g	切换是否显示60hz的网格线
0	恢复trace到初始态，这里是数字0而非字母o

在每个app进程，都有一个Frames行，正常情况以绿色的圆点表示。当圆点颜色为黄色或者红色时，
意味着这一帧超过16.6ms（即发现丢帧），这时需要通过放大那一帧进一步分析问题。对于Android 5.0(API level 21)或者更高的设备，
该问题主要聚焦在UI Thread和Render Thread这两个线程当中。对于更早的版本，则所有工作在UI Thread。
"
rm ./trace.html
$ANDROID_SDK/platform-tools/systrace/systrace.py -l
$ANDROID_SDK/platform-tools/systrace/systrace.py -t 10 gfx view app sched -a com.zq.live
#$ANDROID_SDK/platform-tools/systrace/systrace.py -t 10 sched gfx view wm am app webview dalvik -a com.zq.live
echo 打开报告
#sleep 15
open ./trace.html


#要达到这个目的，我们只需要在期望开始和结束的地方加上自定义的Label就可以了。
#比如你要分析App的冷启动过程，那就在Application类的attachBaseContext调用`Trace.beginSection("Boot Procedure")`，
#然后在App首页的`onWindowFocusChanged` 或者你认为别的合适的启动结束点调用`Trace.endSection`就可以到启动过程的信息