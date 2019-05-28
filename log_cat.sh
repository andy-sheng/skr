if [[ $1 == "" ]]; then
	rm -rf logs/
	adb pull /sdcard/ZQ_LIVE/logs ./

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
	for file in $1/*
	do
	    if test -f $file
	    then
	        python decode_mars_nocrypt_log_file.py $file
	    fi
	done
	echo sublime $1
	sublime $1
fi

echo 解析xlog歌词 ./logcat.sh ~/Downloads/logs  或者 ./logcat.sh 拉取sdcard中的歌词
echo 请使用 UTF-8 的编码，打开日志文件查看