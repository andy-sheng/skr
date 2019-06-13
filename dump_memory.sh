#输入进程名，会dump出 prof 到downloads目录
heap_dump_location='/data/local/tmp/tmp.hprof'
packageName=com.zq.live
echo try dump $packageName

getPid(){
	pid=`adb  shell ps | grep $packageName |grep -v :| awk '{print $2}'`
}

dump_heap() {
  adb shell rm $heap_dump_location
  adb shell am dumpheap $pid $heap_dump_location
  echo "adb shell am dumpheap $pid $heap_dump_location"
  echo "Heap dump started, we have no idea when it's done, so take a look at logs, and when is done use pull_heap_dump"
}

pull_heap_dump() {
  echo "adb pull $heap_dump_location $outputfile"
  adb pull $heap_dump_location $outputfile
}

size1=0
size2=0

check(){
	size=`adb shell ls -al  /data/local/tmp/tmp.hprof | awk '{print $4}'`
	size2=$size1
	size1=$size
	echo check size is $size $size1 $size2
}

getPid

echo pid:$pid

#force GC
#adb root
adb shell kill -10 $pid

sleep 2s

dump_heap
sleep 5s
#while (($size1==0||$size2==0||$size1!=$size2))
#do
#  sleep 4s
#  check
#done
rm -rf memory
mkdir memory
date1=`date +%Y_%m_%d-%H_%M_%S`
outputfile="memory/$packageName$date1.hprof"
echo $outputfile

pull_heap_dump


#convert
sleep 2s
fff=convert
outputfile2="memory/$packageName$date1$fff.hprof"
$ANDROID_SDK/platform-tools/hprof-conv  $outputfile $outputfile2
echo "$ANDROID_SDK/platform-tools/hprof-conv  $outputfile $outputfile2"
open memory
#java -jar hprof_bitmap_dump.jar $outputfile
echo java -jar hprof_bitmap_dump.jar $outputfile2
java -jar hprof_bitmap_dump.jar $outputfile2
echo 可以使用MAT 查看 内存泄漏 与 大Bitmap占用等。如果要查看bitmap,必须使用 8.0 以下的手机,8.0系统bitmap内存在native中 \
bitmap命名格式为 bitmap-宽x高-是否为res-地址.png,请分析bitmap是否必须

#/Users/chengsimin/my_dev_utils/android-sdk-macosx-24.4/platform-tools/hprof-conv  /Users/chengsimin/Downloads/com.wali.live2018_05_11-16_05_27.hprof /Users/chengsimin/Downloads/com.wali.live2018_05_11-16_05_27_convert.hprof