if [[ $1 == "debug" ]]; then
	./gradlew :livesdkapp:assembleDebug
	adb install -r  livesdkapp/build/outputs/apk/livesdkapp-debug.apk
else
	./gradlew :livesdkapp:assembleRelease
	cp livesdkapp/build/outputs/apk/livesdkapp-release.apk ~/Downloads/
	adb install -r  livesdkapp/build/outputs/apk/livesdkapp-release.apk
fi


adb shell am start -n com.mi.liveassistant/com.wali.live.MainActivity