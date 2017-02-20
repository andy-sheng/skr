#!/bin/bash

lib_path="app/src/main/jniLibs/armeabi-v7a"
lib_suffix="so"
lib_files=("libbroadcast" "libconferencemanager" "libffmpeg" "libgnustl_shared" "libopenh264" "libwebrtc" "libplayer")

echo "renaming .so files"
for elem in ${lib_files[@]}
do
	dst_file="$lib_path/$elem.$lib_suffix"
	src_file="$dst_file.*"
	echo move $src_file to $dst_file
	mv $src_file $dst_file
done

lib_path="app/libs"
lib_suffix="jar"
lib_files=("broadcaster" "conferencemanager" "lmidevicemanager" "player" "webrtc" "xplatform_util")

echo "renaming .jar files"
for elem in ${lib_files[@]}
do
	dst_file="$lib_path/$elem.$lib_suffix"
	src_file="$dst_file.*"
	echo move $src_file to $dst_file
	mv $src_file $dst_file
done

