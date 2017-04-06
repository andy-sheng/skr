#!/bin/bash

gradle assembleRelease --info

if [ ! -d "../livesdkapp/libs" ]; then
  mkdir "../livesdkapp/libs"
fi
cp build/outputs/aar/livesdkaar-release.aar ../aardemo/libs/livesdk.aar