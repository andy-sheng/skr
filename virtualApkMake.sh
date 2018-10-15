if [[ $1 = "withhost" ]]; then
	./ins.sh app release
fi

rm plugindemo/build/outputs/apk/beijing/release/plugindemo-beijing-release.apk
./gradlew :plugindemo:clean
./gradlew :plugindemo:assemblePlugin
adb push plugindemo/build/outputs/apk/beijing/release/plugindemo-beijing-release.apk  /sdcard/Test.apk
