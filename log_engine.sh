adb logcat -v time | grep -e "AgoraEngineAdapter"
exit
rm -rf ./logs
adb pull /sdcard/ZQ_LIVE/logs ./
grep -r -e "Rong" -e "RC" -e "融云" -e "rcKickedByOthers" ./logs

