if [[ $1 == "debug" ]]; then
	./gradlew :livesdkapp:assembleDebug
	adb install -r  livesdkapp/build/outputs/apk/livesdkapp-debug.apk
else
	./gradlew :livesdkapp:assembleRelease
	date1=`date +%Y_%m_%d-%H_%M_%S`
	cp livesdkapp/build/outputs/apk/livesdkapp-release.apk ~/Downloads/livesdkapp-release$date1.apk
	adb install -r  livesdkapp/build/outputs/apk/livesdkapp-release.apk
fi


adb shell am start -n com.mi.liveassistant/com.wali.live.MainActivity