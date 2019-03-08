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
	  		if [[ $path == *"-release-"* ]]; then
	  			echo 找到$path
	  			uploadFile=$path
	  		fi
		fi
	  fi
    fi  
  done  
}

function findChannel2()
{
  for file in `ls app/publish`
  do
    local path=app/publish/$file
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
	  		if [[ $path == *"-release-"* ]]; then
	  			echo 找到$path
	  			uploadFile=$path
	  		fi
		fi
	  fi
    fi
  done
}

findChannel $1
if [[ x$uploadFile = x"" ]]; then
    findChannel2 $1
fi
if [[ x$uploadFile = x"" ]]; then
    echo "请选择某个渠道的release包，输入如 ./upload.sh TEST"
fi
time=$(date "+%Y%m%d-%H:%M:%S")
apiKey=3dd7d2a8ab6591ca44fb8cbbe1333785
echo apiKey=$apiKey
echo uploadFile=$uploadFile
echo $time

downloadUrl=$(curl -F 'file=@'$uploadFile'' \
 -F '_api_key='$apiKey'' \
 -F 'buildUpdateDescription=更新时间'$time'
 1. bug修复
 2. 功能更新'\
  https://www.pgyer.com/apiv2/app/upload | jq .data.buildQRCodeURL)

echo $downloadUrl
# 去除双引号
downloadUrl=`echo $downloadUrl |sed 's/\"//g'`

open $downloadUrl