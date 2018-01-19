#!/bin/bash
#rm -f ../main/java-gen/com/wali/live/proto/
genPath=../main/java-gen/
#rm -f ../main/java-gen/com/wali/live/proto/
#处理单个proto文件，不需要的请注释
protoc --java_out=$genPath ./LiveSummit.proto

