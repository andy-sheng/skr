#!/bin/bash
#rm -f ../main/java-gen/com/wali/live/proto/
genPath=../main/java-gen/
protoc --java_out=$genPath ./RankList.proto
protoc --java_out=$genPath ./Rank.proto
protoc --java_out=$genPath ./LiveShow.proto
protoc --java_out=$genPath ./Feeds.proto
protoc --java_out=$genPath ./Relation.proto
protoc --java_out=$genPath ./Statistics.proto
protoc --java_out=$genPath ./CommonChannel.proto
protoc --java_out=$genPath ./Banner.proto
protoc --java_out=$genPath ./Common.proto
protoc --java_out=$genPath ./LiveCommon.proto
protoc --java_out=$genPath ./Live.proto
protoc --java_out=$genPath ./RedEnvelope.proto
