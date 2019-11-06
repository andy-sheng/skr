#! /bin/bash
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
    echo "dependLibraryFromServer$1"
	if [[ $1 = true ]]; then
		sed -ig 's/dependLibraryFromServer=false/dependLibraryFromServer=true/' gradle.properties
	else
		sed -ig 's/dependLibraryFromServer=true/dependLibraryFromServer=false/' gradle.properties
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

upload(){
	./gradlew :baseLibrary:android-gif-drawable:uploadArchives
	./gradlew :baseLibrary:commonsdk:uploadArchives
	./gradlew :baseLibrary:doraemonkit:uploadArchives
	./gradlew :baseLibrary:arcCloud:uploadArchives
	./gradlew :baseLibrary:effectsdk:uploadArchives
#	./gradlew :baseLibrary:enginesdk:uploadArchives
#	./gradlew :baseLibrary:mediaengine:uploadArchives
#	./gradlew :commoncore:uploadArchives
#	./gradlew :commonservice:uploadArchives
}

changeDependLibraryFromServer true
cat gradle.properties

for p in $*               #在$*中遍历参数，此时每个参数都是独立的，会遍历$#次
do
    if [[ $p = release ]]; then
        release=true;
    elif [[ $p = debug ]]; then
        debug=true
    elif [[ $p = all ]]; then
        all=true
    elif [[ $p = commonsdk ]]; then
        commonsdk=true
    elif [[ $p = commoncore ]]; then
        commoncore=true
    elif [[ $p = commonservice ]]; then
        commonservice=true
    fi
done

echo release=$release
echo debug=$debug
echo all=$all
echo commonsdk=$commonsdk
echo commoncore=$commoncore
echo commonservice=$commonservice


if [[ $debug = true ]]; then
	changeBuildType debug
	if [[ $all = true ]]; then
	    upload
	fi
	if [[ $commonsdk = true ]]; then
	    ./gradlew :baseLibrary:commonsdk:uploadArchives
	fi
	if [[ $commoncore = true ]]; then
	    ./gradlew :commoncore:uploadArchives
	fi
	if [[ $commonservice = true ]]; then
	    ./gradlew :commonservice:uploadArchives
	fi
fi
if [[ $release = true ]]; then
	changeBuildType release
	if [[ $all = true ]]; then
	    upload
	fi
	if [[ $commonsdk = true ]]; then
	    ./gradlew :baseLibrary:commonsdk:uploadArchives
	fi
	if [[ $commoncore = true ]]; then
	    ./gradlew :commoncore:uploadArchives
	fi
	if [[ $commonservice = true ]]; then
	    ./gradlew :commonservice:uploadArchives
	fi
fi

changeBuildType release
changeDependLibraryFromServer false