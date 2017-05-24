#!/bin/bash

python gen_build_settings_aar.py

# gradle assembleRelease --info
../gradlew assembleRelease --info # use this if gradle fails

if [ ! -d "../aardemo/libs" ]; then
  mkdir "../aardemo/libs"
fi
cp build/outputs/aar/livesdkaar-release.aar ../aardemo/libs/livesdk.aar

git checkout ../buildsystem/dependencies.gradle