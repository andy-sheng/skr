#! /bin/bash
echo "./logcat.sh 捞取手机日志"
echo "./logcat.sh dir 解析文件夹内所有日志"
echo "./logcat.sh a.txt  反混淆 a.txt"

if [[ $1 == "" ]]; then
    adb shell am broadcast -a com.zq.live.FLUSH_LOG
    sleep 2
	rm -rf logs/
	adb pull /sdcard/Android/data/com.zq.live/files/logs ./

	for file in ./logs/*
	do
	    if test -f $file
	    then
	        python decode_mars_nocrypt_log_file.py $file
	    fi
	done
	echo sublime ./logs/
	sublime ./logs/
else
	if test -f $1 
	then
        #遍历文件夹 所有maping文件
        for file in `ls ./publish_mapping`
        do
        mapping_path="./publish_mapping/"$file
        if [ -d $mapping_path ]
        then
           echo "DIR $mapping_path"
        else
            if [[ $mapping_path =~ "mapping" ]]
            then
                # 使用R8解混淆
                java -jar ./r8_retrace/lib/retrace.jar $mapping_path $1
            else
              echo "不包含"
            fi
        fi
        done
	else
		for file in $1/*
		do
		    if test -f $file
		    then
		    	if [[ $file == *".xlog" ]]; then
		    		python decode_mars_nocrypt_log_file.py $file
		    	fi
		    fi
		done
		echo sublime $1
	fi
fi

echo "结束"