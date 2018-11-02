time=$(date "+%Y%m%d-%H:%M:%S")
apiKey=3dd7d2a8ab6591ca44fb8cbbe1333785
uploadFile=app/build/outputs/apk/channel_default/release/app-channel_default-release.apk
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