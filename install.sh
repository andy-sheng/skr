if [[ $1 == "debug" ]]; then
	./gradlew :livesdkapp:assembleDebug
	adb install -r  livesdkapp/build/outputs/apk/livesdkapp-debug.apk
else
	cd livesdkapp
	# 应用商店 meng_1254_48_android
	./ship_to.sh $1
	#./gradlew :livesdkapp:assembleRelease
	cd ..
	date1=`date +%m_%d_%H_%M`
	cp livesdkapp/build/outputs/apk/livesdkapp-release.apk ~/Downloads/livesdkapp-$1-$date1.apk
	adb install -r  livesdkapp/build/outputs/apk/livesdkapp-release.apk
fi


adb shell am start -n com.mi.liveassistant/com.wali.live.MainActivity