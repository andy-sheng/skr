rm -rf ./logs
adb pull /sdcard/ZQ_LIVE/logs ./
grep -r -e "Rong" -e "RC" ./logs
