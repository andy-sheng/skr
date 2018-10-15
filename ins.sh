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
		echo "编译app release"
		./gradlew :app:assembleRelease
		adb install -r app/build/outputs/apk/release/app-release.apk
	else
		echo "./gradlew :app:assembleDebug"
		./gradlew :app:assembleDebug
		adb install -r app/build/outputs/apk/debug/app-debug.apk
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
