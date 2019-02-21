cd ../../wire/
./gradlew :wire-compiler:build

rm /Users/chengsimin/dev/livesdk/livesdk/proto/wire-compiler-2.3.0-SNAPSHOT-jar-with-dependencies.jar
mv wire-compiler/build/libs/wire-compiler-2.3.0-SNAPSHOT-jar-with-dependencies.jar  /Users/chengsimin/dev/livesdk/livesdk/proto/

cd ../livesdk/livesdk

java -jar -Dfile.encoding=UTF-8 ./proto/wire-compiler-2.3.0-SNAPSHOT-jar-with-dependencies.jar \
--proto_path=./proto --java_out=./commoncore/src/main/java-gen-pb/ Common.proto

./gradlew assemblechannel_testDebug