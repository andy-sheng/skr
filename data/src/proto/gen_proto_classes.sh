#!/bin/bash
#rm -f ../main/java-gen/com/wali/live/proto/
genPath=../main/java-gen/
protoc --java_out=$genPath ./RankList.proto
protoc --java_out=$genPath ./Rank.proto

