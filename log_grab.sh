adb -s 90bf00ba logcat -v time | grep -e "GrabOpView" -e "GrabCorePresenter" -e "ApiManager" -e "GrabPlayerRv2" -e "SongModel"
