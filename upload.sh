apiKey=3dd7d2a8ab6591ca44fb8cbbe1333785
#userKey=5b7c796c9534766eb5ea9570088f0487
#echo $apiKey

curl -F 'file=@app/build/outputs/apk/release/app-release.apk' \
 -F '_api_key='$apiKey'' \
 -F 'buildUpdateDescription=萨达介绍了肯德基阿斯科利简单' \
  https://www.pgyer.com/apiv2/app/upload
