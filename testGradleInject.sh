./gradlew baseLibrary:my-utils-gradle:uploadArchives
./gradlew clean :app:assemblechannel_defaultDebug
cp app/build/outputs/apk/channel_default/debug/app-channel_default-debug.apk ~/Downloads/test.apk
cd ~/Downloads
adb install -r ~/Downloads/test.apk
~/my_dev_utils/apk_decompile/run.sh  ~/Downloads/test.apk