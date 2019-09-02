
echo "systemtrace 支持的模块如下 说明文档 https://zhuanlan.zhihu.com/p/27331842"
$ANDROID_SDK/platform-tools/systrace/systrace.py -l
$ANDROID_SDK/platform-tools/systrace/systrace.py -t 10 sched gfx view wm am app webview -a com.zq.live
open ./trace.html


#要达到这个目的，我们只需要在期望开始和结束的地方加上自定义的Label就可以了。
#比如你要分析App的冷启动过程，那就在Application类的attachBaseContext调用`Trace.beginSection("Boot Procedure")`，
#然后在App首页的`onWindowFocusChanged` 或者你认为别的合适的启动结束点调用`Trace.endSection`就可以到启动过程的信息