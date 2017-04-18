#!/bin/bash

if [ $# -ne 1 ]; then
  echo "Usage: ./ship_to.sh CHANNEL_ID"
  exit
fi

git checkout ../common/src/main/java/com/base/utils/Constants.java

#pushd ./
python gen_build_settings.py $1
#popd

#pushd ../
if [ $1 = "debugmi" ]; then
  gradle clean
  gradle assembleDebug --stacktrace
else
  gradle assembleRelease --stacktrace
fi

#popd

if [ ! -d "ship" ]; then
  mkdir ship
fi

#cp build/outputs/apk/app-universal-debug.apk ship/community-$1-debug.apk

if [ $1 = "debugmi" ]; then
  cp build/outputs/apk/livesdkapp-debug.apk ship/milivesdk-$1-release.apk
else
  cp build/outputs/apk/livesdkapp-release.apk ship/milivesdk-$1-release.apk
fi

git checkout ../common/src/main/java/com/base/utils/Constants.java
git checkout ./src/main/res/values/strings.xml
git checkout ./src/main/res/values-zh-rTW/strings.xml
git checkout ./src/main/res/values-zh-rCN/strings.xml
git checkout ././build.gradle
git checkout ../buildsystem/dependencies.gradle
git checkout ./src/main/AndroidManifest.xml
