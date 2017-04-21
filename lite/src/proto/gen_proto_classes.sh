#!/bin/bash
rm -rf ../main/java-gen/com/mi/liveassistant/proto/
genPath=../main/java-gen/
protoc --java_out=$genPath ./LiveCommon.proto
protoc --java_out=$genPath ./Live.proto

