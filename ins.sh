#修改 BuildModule
changeBuildModule(){
    echo "changeBuildModule$1"
	if [[ $1 = true ]]; then
		sed -ig 's/isBuildModule=false/isBuildModule=true/' gradle.properties
		echo "sed -ig 's/isBuildModule=true/isBuildModule=false/' gradle.properties"
	else
		sed -ig 's/isBuildModule=true/isBuildModule=false/' gradle.properties
		echo "sed -ig 's/isBuildModule=false/isBuildModule=true/' gradle.properties"
	fi
	rm gradle.propertiesg
}

#得到 BuildModule
getBuildModule(){
	result=`grep isBuildModule=true gradle.properties`
	echo $result
	if [[ $result = "isBuildModule=true" ]]; then
		isBuildModule=true
	else
		isBuildModule=false
	fi
}

echo "运行示例 ./ins.sh app release all  或 ./ins.sh modulechannel 编译组件module"
if [ $# -le 0 ] ; then 
	echo "输入需要编译的模块名" 
	exit 1; 
fi 

getBuildModule

echo 当前isBuildModule=$isBuildModule

if [[ $1 = "app" ]]; then
	if [[ $isBuildModule = false ]]; then
		#如果是app 并且 之前的 isBuildModule 为false，则直接编译
		echo "直接编译"
	else
		echo "先clean再编译"
		changeBuildModule false
		./gradlew clean
	fi
	if [[ $2 = "release" ]]; then
		echo "编译app release  加 --profile 会输出耗时报表"
		#./gradlew :app:assembleRelease --profile
		if [[ $3 = "all" ]];then
		    echo "编译release所有渠道"
		    ./gradlew :app:assembleRelease
		    adb install -r app/build/outputs/apk/channel_mishop/release/app-channel_mishop-release.apk
		else
		    echo "只编译release default渠道"
		    ./gradlew :app:assemblechannel_defaultRelease --stacktrace
		    adb install -r app/build/outputs/apk/channel_default/release/app-channel_default-release.apk
		fi
	elif [[ $2 = "test" ]]; then
	   echo "./gradlew :app:assemblechannel_testDebug"
	   echo "只编译test debug渠道"
       	./gradlew :app:assemblechannel_testDebug
       	adb install -r app/build/outputs/apk/channel_test/debug/app-channel_test-debug.apk
	else
		echo "./gradlew :app:assemblechannel_defaultDebug"
		echo "只编译default debug渠道"
		if [[ $2 = "clean" ]]; then
		    echo "clean一下"
		    ./gradlew :app:clean
		fi
		./gradlew :app:assemblechannel_defaultDebug --stacktrace
		adb install -r app/build/outputs/apk/channel_default/debug/app-channel_default-debug.apk
	fi
else
	if [[ $isBuildModule = false ]]; then
		#如果是其他module 并且 之前的 isBuildModule 为false，则clean在编译
		echo "先clean再编译"
		changeBuildModule true
		./gradlew clean
	else
		echo "直接编译"
	fi
	if [[ $2 = "release" ]]; then
		echo "module分支不能编译release版本"
	else
		echo "./gradlew :$1:assembleDebug"
		./gradlew :$1:assembleDebug
		adb install -r $1/build/outputs/apk/debug/$1-debug.apk
	fi
fi
