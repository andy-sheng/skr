
rm -rf guard-res-apk
apkPath=app/build/outputs/apk/channel_default/release/app-channel_default-release.apk

java -jar baseLibrary/AndResGuard/AndResGuard-cli-1.2.15.jar $apkPath \
-config baseLibrary/AndResGuard/config.xml \
-out guard-res-apk \
-7zip /usr/local/bin/7z \
-zipalign /Users/chengsimin/my_dev_utils/android-sdk-macosx-24.4/build-tools/28.0.2/zipalign \
-signatureType v1 \
-signature app/zq.keystore zq123456 zq123456 release_key


adb install -r guard-res-apk/app-channel_default-release_signed_7zip_aligned.apk


exit

对资源路径进行混淆，极致压缩。


注意：
1、若想通过 getIdentifier 方式获得资源，需要放置白名单中。
 部分手机桌面快捷图标的实现有问题，务必将程序桌面icon加入白名单。
2、对于一些第三方sdk,例如友盟，可能需要将部分资源添加到白名单中。
