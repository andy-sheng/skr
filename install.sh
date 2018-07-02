if [[ $1 == "debug" ]]; then
	./gradlew :livesdkapp:assembleDebug
	adb install -r  livesdkapp/build/outputs/apk/livesdkapp-debug.apk
else
		
	if [[ $1 == "0" ]]; then
		CHANNEL_ID="DEFAULT"
	elif [[ $1 == "1" ]]; then
		# 应用商店 meng_1254_48_android
		CHANNEL_ID="meng_1254_48_android"
	elif [[ $1 == "2" ]]; then
		# 厂包 5005_1_android
		CHANNEL_ID="5005_1_android"
	elif [[ $1 == "test" ]]; then
		CHANNEL_ID="TEST"
	else
		echo "错误的参数，0 DEFAULT | 1 meng_1254_48_android | 2 5005_1_android"
		exit
	fi

	cd livesdkapp
	./ship_to.sh $CHANNEL_ID
	#./gradlew :livesdkapp:assembleRelease
	cd ..
	date1=`date +%m_%d_%H_%M`
	cp livesdkapp/build/outputs/apk/livesdkapp-release.apk ~/Downloads/livesdkapp-$CHANNEL_ID-$date1.apk
	adb install -r  livesdkapp/build/outputs/apk/livesdkapp-release.apk
fi


adb shell am start -n com.mi.liveassistant/com.wali.live.MainActivity