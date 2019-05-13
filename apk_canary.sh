echo 开始对apk进行检测
mkdir ./publish
java -jar matrix-apk-canary-0.5.1.jar --config ./apk_canary.json
#./publish/apk-checker-result
echo "./publish/apk-checker-result.html"
open ./publish/apk-checker-result.html
echo 请认真阅读分析报告。优化技巧请查看 https://github.com/Tencent/matrix/wiki/Matrix-Android-ApkChecker