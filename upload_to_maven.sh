
echo "运行示例 ./upload_to_maven.sh debug"
echo "运行示例 ./upload_to_maven.sh release"
echo "运行示例 ./upload_to_maven.sh all"
if [ $# -le 0 ] ; then 
	echo "输入buildType模式" 
	exit 1; 
fi

changeBuildType(){
    echo "changeBuildModule$1"
	if [[ $1 = debug ]]; then
		sed -ig 's/buildTypeForMaven=release/buildTypeForMaven=debug/' gradle.properties
	else
		sed -ig 's/buildTypeForMaven=debug/buildTypeForMaven=release/' gradle.properties
	fi
	rm gradle.propertiesg
}

changeDependLibraryFromServer(){
	sed -ig 's/dependLibraryFromServer=false/dependLibraryFromServer=true/' gradle.properties
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

upload(){
	./gradlew :baseLibrary:android-gif-drawable:uploadArchives
	./gradlew :baseLibrary:commonsdk:uploadArchives
	./gradlew :baseLibrary:doraemonkit:uploadArchives
	./gradlew :baseLibrary:arcCloud:uploadArchives
	./gradlew :baseLibrary:effectsdk:uploadArchives
	./gradlew :baseLibrary:enginesdk:uploadArchives
	./gradlew :baseLibrary:mediaengine:uploadArchives
	./gradlew :commoncore:uploadArchives
	./gradlew :commonservice:uploadArchives
}

changeDependLibraryFromServer 
cat gradle.properties

echo $1
if [[ $1 = debug ]]; then
	changeBuildType debug
	upload
elif [[ $1 = release ]]; then
	changeBuildType release
	upload
elif [[ $1 = all ]]; then
	changeBuildType debug
	upload	
	changeBuildType release
	upload
fi
