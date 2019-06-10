#输入进程名，会dump出 prof 到downloads目录
echo try send trim memory signal to com.zq.live  先保证进程在后台

getPid(){
	pid=`adb  shell ps | grep com.zq.live |grep -v :| awk '{print $2}'`
}

getPid

echo pid:$pid

adb shell am send-trim-memory  $pid  COMPLETE