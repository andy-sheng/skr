// 注意 会修改 jar 包的 activity ，编译完可能要 checkout 一下

changePLuginBuildModule(){
    echo "changeBuildModule$1"
	if [[ $1 = true ]]; then
		sed -ig 's/repluginHostEnable=true/repluginHostEnable=false/' gradle.properties
		sed -ig 's/repluginPluginEnable=false/repluginPluginEnable=true/' gradle.properties
	else
		sed -ig 's/repluginHostEnable=false/repluginHostEnable=true/' gradle.properties
		sed -ig 's/repluginPluginEnable=true/repluginPluginEnable=false/' gradle.properties
	fi
	rm gradle.propertiesg
}

changePLuginBuildModule true

exit
./gradlew replugin-plugin-gradle:uploadArchives
echo 开始clean
./gradlew plugindemo:clean
echo 开始编译
./gradlew plugindemo:assembleDebug > a.txt
sublime a.txt
changePLuginBuildModule false
adb install -r plugindemo/build/outputs/apk/channel_replugin_plugin/debug/plugindemo-channel_replugin_plugin-debug.apk
mv plugindemo/build/outputs/apk/channel_replugin_plugin/debug/plugindemo-channel_replugin_plugin-debug.apk ~/Downloads/replugin.apk
adb push ~/Downloads/replugin.apk /sdcard/replugin.apk
cd ~/Downloads

~/my_dev_utils/apk_decompile/run.sh  ~/Downloads/replugin.apk