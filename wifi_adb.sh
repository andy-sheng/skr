#获得设备id并保存到数组
getIp(){
    ip=`adb -s $1 shell ifconfig | grep -e "inet addr" | grep "Bcast:"`
    echo $ip
    #字符串截取
    ip=${ip#*"inet addr:"}
    echo $ip
    ip=${ip%"Bcast"*} 
    echo $ip
    ip=`echo $ip`
    #device 替换为空格
    #devices=(${devstr//"device"/ })
}

getIp $1

echo "adb -s $1 tcpip $2"
adb -s $1 tcpip $2
echo "adb -s $1 connect $ip:$2"
adb -s $1 connect $ip:$2
