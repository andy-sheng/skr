./gradlew baseLibrary:my-utils-gradle:uploadArchives
./gradlew clean :app:assembleDebug 
cp app/build/outputs/apk/channel_mishop/debug/app-channel_mishop-debug.apk ~/Downloads/test.apk
cd ~/Downloads
adb install -r ~/Downloads/test.apk
~/my_dev_utils/apk_decompile/run.sh  ~/Downloads/test.apk