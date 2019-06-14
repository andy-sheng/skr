#! /bin/bash
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

getTestModuleEnable(){
	result=`grep compileModuleTest=true gradle.properties`
	echo $result
	if [[ $result = "compileModuleTest=true" ]]; then
		testModuleEnable=true
	else
		testModuleEnable=false
	fi
}

changeMatrixModule(){
    echo "changeMatrixEnable $1"
	if [[ $1 = true ]]; then
		sed -ig 's/MatrixEnable=false/MatrixEnable=true/' gradle.properties
		echo "sed -ig 's/MatrixEnable=true/MatrixEnable=false/' gradle.properties"
	else
		sed -ig 's/MatrixEnable=true/MatrixEnable=false/' gradle.properties
		echo "sed -ig 's/MatrixEnable=false/MatrixEnable=true/' gradle.properties"
	fi
	rm gradle.propertiesg
}

getMatrixEnable(){
	result=`grep MatrixEnable=true gradle.properties`
	echo $result
	if [[ $result = "MatrixEnable=true" ]]; then
		MatrixEnable=true
	else
		MatrixEnable=false
	fi
}

#获得设备id并保存到数组
getDeviceId(){
	adb devices
    devstr=`adb devices`
    #字符串截取
    devstr=${devstr#*"List of devices attached"}
    #device 替换为空格
    devices=(${devstr//"device"/ })
}


#将apk安装到所有设备上
installApkForAllDevices(){
    echo "注意包大小优化"
    ls -al $1
	for data in ${devices[@]}  
	do
	    if [ ${data} = "90bf00ba" ]; then
	        myphone="90bf00ba"
	    fi
    	echo "安装 $1 到 ${data}"
    	echo "adb -s ${data} install -r $1"
    	adb -s ${data} install -r $1

        if [ $matrix = true ]; then
            if [ $release = true ]; then
                echo adb push app/build/outputs/mapping/release/methodMapping.txt /sdcard/ZQ_LIVE/matrix_method.txt
                adb shell rm /sdcard/ZQ_LIVE/matrix_method.txt
                adb -s ${data} push app/build/outputs/mapping/release/methodMapping.txt /sdcard/ZQ_LIVE/matrix_method.txt
            else
                echo adb push app/build/outputs/mapping/debug/methodMapping.txt /sdcard/ZQ_LIVE/matrix_method.txt
                adb shell rm /sdcard/ZQ_LIVE/matrix_method.txt
                adb -s ${data} push app/build/outputs/mapping/debug/methodMapping.txt /sdcard/ZQ_LIVE/matrix_method.txt
            fi
        fi

    	if [[ $testModuleEnable = true ]]; then
    		adb -s ${data} shell am start -n com.zq.live/com.wali.live.moduletest.activity.TestSdkActivity
    	else
    		adb -s ${data} shell am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.zq.live/com.module.home.HomeActivity
    	fi
	done
}

#遍历文件夹
function walk()  
{  
  for file in `ls $1`  
  do  
    local path=$1"/"$file  
    if [ -d $path ]  
     then  
      echo "DIR $path"  
      walk $path  
    else  
      echo "FILE $path"
	  #echo "filename: ${path%.*}"
	  ext=${path##*.}
	  echo $ext
	  if [[ $ext = "apk" ]]; then
	  		movePackage $path
	  fi
    fi  
  done  
} 

#移动apk
movePackage(){
	echo "movePackage $1"
	path=$1
	date1=`date +%m_%d_%H_%M`
	pre=${path%.*}
	name=${pre##*/}
	ext=${path##*.}
	echo name:$name ext:$ext date1:$date1
	echo mv $path ${name}_${date1}.${ext}
	#mv $path ./publish/${name}_${date1}.${ext}
	mv $path ./publish/${name}.${ext}
}


function findChannel()  
{  
  for file in `ls app/build/outputs/channels`  
  do  
    local path=app/build/outputs/channels/$file  
    if [ -d $path ]  
     then  
      echo "DIR $path"  
      walk $path  
    else  
      echo "FILE $path"
	  #echo "filename: ${path%.*}"
	  ext=${path##*.}
	  echo $ext
	  if [[ $ext = "apk" ]]; then
	  	if [[ $path == *"-$1-"* ]]; then
	  		if [[ $path == *"-$2-"* ]]; then
	  			installApkPath=$path
	  		fi
		fi
	  fi
    fi  
  done  
}

echo "运行示例 ./ins.sh app release all  或 ./ins.sh modulechannel 编译组件module "
echo "运行示例 ./ins.sh app release matrix 开启matrix性能监控"
echo "运行示例 ./ins.sh app release apkcanary 开启apk包体静态检查"
if [ $# -le 0 ] ; then 
	echo "输入需要编译的模块名" 
	exit 1; 
fi


for p in $*               #在$*中遍历参数，此时每个参数都是独立的，会遍历$#次
do
    if [[ $p = release ]]; then
        release=true;
    elif [[ $p = all ]]; then
        all=true
    elif [[ $p = dev ]]; then
        dev=true
    elif [[ $p = test ]]; then
        test=true
    elif [[ $p = sandbox ]]; then
        sandbox=true
    elif [[ $p = matrix ]]; then
        matrix=true
    elif [[ $p = apkcanary ]]; then
        apkcanary=true
    elif [[ $p = clean ]]; then
        clean=true
    fi
done

echo release=$release
echo all=$all
echo dev=$dev
echo test=$test
echo sandbox=$sandbox
echo matrix=$matrix
echo apkcanary=$apkcanary
echo clean=$clean

getBuildModule

echo 当前isBuildModule=$isBuildModule

getTestModuleEnable

echo testModuleEnable=$testModuleEnable

if [ $matrix = true ]; then
    clean=true
    changeMatrixModule true
else
    changeMatrixModule false
fi

getMatrixEnable

echo MatrixEnable=$MatrixEnable

getDeviceId

echo ${devices[@]}

echo rm -rf app/build/outputs/channels
rm -rf app/build/outputs/channels


if [[ $1 = "app" ]]; then
	if [[ $isBuildModule = false ]]; then
		#如果是app 并且 之前的 isBuildModule 为false，则直接编译
		echo "直接编译"
	else
		echo "先clean再编译"
		changeBuildModule false
		./gradlew clean
	fi
	if [[ $release = true ]]; then
		echo "编译app release  加 --profile 会输出耗时报表"
		./gradlew clean
		if [[ $all = true ]];then
		    echo "编译release所有渠道 ./gradlew :app:assembleReleaseChannels"
		    ./gradlew :app:assembleReleaseChannels
		    ./apk_canary.sh
            #拷贝所有包到主目录
            rm -rf ./publish
            mkdir ./publish
			walk app/build/outputs/channels
			if [ $matrix = true ]; then
                cp app/build/outputs/mapping/debug/methodMapping.txt ./publish/matrix_method.txt
            fi
			echo "拷贝完毕"
			if [ $MatrixEnable = true ];then
			    echo "注意在 release all 版本中开启了 Matrix，确认是否为期望的操作"
			fi
		else
		    echo "只编译release default渠道 ./gradlew :app:assembleReleaseChannels --stacktrace"
		    ./gradlew :app:assembleReleaseChannels --stacktrace
		    if [ $apkcanary = true ]; then
		        ./apk_canary.sh
		    fi
		    if [ $dev = true ]; then
		        findChannel DEV release
		    elif [ $test = true ];then
		        findChannel TEST release
		    elif [ $sandbox = true ];then
		        findChannel SANDBOX release
		    else
		        findChannel DEFAULT release
		    fi
		    installApkForAllDevices $installApkPath
            #myandroidlog.sh  com.zq.live
		fi
	else
		echo "编译app debug  加 --profile 会输出耗时报表 ./gradlew :app:assembleDebugChannels --stacktrace"
		if [[ $clean = true ]]; then
		    echo "clean一下"
		    ./gradlew :app:clean
		fi
		rm -rf app/build/outputs/apk
		./gradlew :app:assembleDebugChannels --stacktrace
        if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
            if [ $apkcanary = true ]; then
		           ./apk_canary.sh
		    fi
		    if [ $dev = true ]; then
		        findChannel DEV debug
		    elif [ $test = true ];then
		        findChannel TEST debug
		    elif [ $sandbox = true ];then
		        findChannel SANDBOX debug
		    else
		        findChannel DEFAULT debug
		    fi
		    installApkForAllDevices $installApkPath
		    if [ $myphone = "90bf00ba" ]; then
		        echo "过滤自己手机日志"
	            myandroidlog.sh  -s 90bf00ba com.zq.live
	        else
	            myandroidlog.sh  com.zq.live
	        fi
		else
		   echo "app/build/outputs/apk/debug 为空"
        fi
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
