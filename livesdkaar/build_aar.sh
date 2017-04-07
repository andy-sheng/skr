#!/bin/bash

gradle assembleRelease --info

if [ ! -d "../aardemo/libs" ]; then
  mkdir "../aardemo/libs"
fi
cp build/outputs/aar/livesdkaar-release.aar ../aardemo/libs/livesdk.aar