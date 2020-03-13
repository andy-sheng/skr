# 搭建编译环境

1. 搭建Android 开发环境
   
   下载耗时较长，此时可同时进行后续步骤
   * [下载 Android studio](https://developer.android.google.cn/studio/)
   * [下载ndk-r15c](https://developer.android.google.cn/ndk/downloads/older_releases)
   * 配置环境
   ```sh
    # Android
    ANDROID_HOME=<你的sdk目录>
    export PATH=$PATH:$ANDROID_HOME/tools
    export PATH=$PATH:$ANDROID_HOME/platform-tools
    export NDK_HOME=<你的ndk目录>/android-ndk-r15c
    export PATH=$PATH:$NDK_HOME
   ```
   * 使用 gradle 5.4.1
    ```sh
    # gradle-wrapper.properties 配置
    distributionUrl=https\://services.gradle.org/distributions/gradle-5.4.1-all.zip
    ```
2. 获取代码
   * 登录 VPN
  
    安装公司邮件中提供的 Tunnelblick （VPN 工具），使用自己的 VPN账号登录
   * 获取 livesdk 代码： git clone http://git.inframe.club/chengsimin/livesdk.git
   * 在 livesdk 目录下获取 skr_flutter 代码： http://git.inframe.club/skrer/skr_flutter
   * 示例
    ```sh
    # 创建自己的工作目录
    mkdir Android
    # 获取 livesdk 代码 
    git clone http://git.inframe.club/chengsimin/livesdk.git
    # 切换 livesdk 到 zq_DB分支
    cd livesdk 
    git checkout zq_DB
    # 在livesdk目录下获取 skr_flutter 代码 
    git clone http://git.inframe.club/skrer/skr_flutter.git
    # 切换 skr_flutter 到 zq_DB分支 
    cd skr_flutter
    git checkout zq_DB
    ```
    * zq_DB 分支为开发使用的分支
3. 安装Flutter v1.9.1+hotfix.6
   * 在 [这里下载Flutter v1.9.1+hotfix.6](https://flutter.dev/docs/development/tools/sdk/releases?tab=macos#macos)
   * 解压到指定目录，配置环境变量
    ```sh
    export PATH=/Users/gxf/tools/flutter/bin:$PATH
    ```
4. 编译 skr_flutter 
   * 执行编译后会生成 .android 目录
   ```sh
   # 启动编译
   flutter pub get
   ```
   * 如果编译缓慢可以使用翻墙VPN或者配置国内镜像源
   ```sh
   export PUB_HOSTED_URL=https://pub.flutter-io.cn\nexport FLUTTER_STORAGE_BASE_URL=https://storage.flutter-io.cn
   ```

5. 编译 livesdk 工程
   ```sh
   # 使用命令行编译安装
   ./ins.sh app dev clean online refresh

   # 编译失败，可通过以下命令查看详细错误信息

   ./gradlew assembleDebug --stacktrace --info
   
   ```

6. 项目结构

    查看 [livesdk 工程 README](./readme.md)
