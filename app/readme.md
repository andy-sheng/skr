host application  宿主 app。
每个module可以单独运行
可以改变 gradle.properties 属性决定哪个编译生效


运行collect任务，发现会在build/multi-dex目录下单独生成manifest_keep.txt文件，
该文件其实就是通过上述规则扫描AndroidManifest生成。manifest_keep.txt保留的是所有需要放入主Dex里的类。
还没完，接下来transformClassesWithMultidexlist任务会根据manifest_keep.txt生成必要依赖列表maindexlist.txt，
这里面所有类才是真正放入主Dex里的。bingo，现在非常清楚，我们只需要控制进入manifest_keep.txt中的类即可，
最终其类的依赖关系由系统帮我们生成即可，安全绿色可靠！


Android 5.0之前，安卓系统采用的是Dalvik虚拟机，采用的是JIT技术（Just-in-time compilation，即时编译，运行时编译DEX字节码文件，这也是以前为什么安卓手机用户总是诟病Android系统比iOS系统运行卡顿的原因），限制每个APK文件只能包含一个DEX文件（即classes.dex）。为了绕开这个限制，Google给我们提供了multidex support library兼容包，帮助我们实现应用程序加载多个DEX文件，并且这个兼容包作为程序的主DEX文件，管理者其他DEX文件的访问。

Android 5.0之后，安卓系统改用了ART虚拟机（Android RunTime），采用的是OAT技术（Ahead-of-time，预编译，在应用安装的时候扫描应用中的所有DEX文件，并编译成一个.oat格式的文件供安卓设备执行，所以相比Dalvik虚拟机下的应用，安装时间较长）。因此可以理解为，使用ART虚拟机下的安卓系统自动支持APK文件中多个DEX的加载。所以我用乐视手机（Android 6.0）上apk正常运行，而在三星（Android4.4.2）却无法运行，报找不到类；
