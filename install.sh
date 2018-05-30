if [[ $1 == "debug" ]]; then
	./gradlew :livesdkapp:assembleDebug
	adb install -r  livesdkapp/build/outputs/apk/livesdkapp-debug.apk
else
	./gradlew :livesdkapp:assembleRelease
adb install -r  livesdkapp/build/outputs/apk/livesdkapp-release.apk
fi


