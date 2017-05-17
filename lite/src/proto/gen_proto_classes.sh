#!/bin/bash
rm -rf ../main/java-gen/com/mi/liveassistant/proto/
genPath=../main/java-gen/
protoc --java_out=$genPath ./Common.proto
protoc --java_out=$genPath ./Account.proto
protoc --java_out=$genPath ./LiveCommon.proto
protoc --java_out=$genPath ./Live.proto
protoc --java_out=$genPath ./User.proto
protoc --java_out=$genPath ./CommonChannel.proto
protoc --java_out=$genPath ./HotChannel.proto
protoc --java_out=$genPath ./Config.proto
protoc --java_out=$genPath ./CloudParams.proto
protoc --java_out=$genPath ./LiveMessage.proto
protoc --java_out=$genPath ./RedEnvelope.proto
