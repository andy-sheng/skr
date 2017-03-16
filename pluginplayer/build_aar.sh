#!/bin/bash

gradle assembleRelease

if [ ! -d "../pluginplayerdemo/libs" ]; then
  mkdir "../pluginplayerdemo/libs"
fi
cp build/outputs/aar/pluginplayer-release.aar ../pluginplayerdemo/libs/pluginplayer.aar