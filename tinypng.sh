
keys=(
"-QGvZ7H-4RFzjizRMsNf-rVVI_UTnmYK"
"Zkf1LHXMWyyGcpUhsgH8bjET9j5oSw7S"
"DC7Fxhf-iEw40XfDmAb3KrWRnEPfBEDN"
"qJ2q_PJPcwPNkh-67iyBFLjja08hTaF6"
"7rsVep8rCF6ACdH9JsNxYAj3IH_-4i4e"
"zSIG8zf71W1BCqKfVZkuSKrdtUmLv2UU"
"fPGfQjiIWCQ0ldbEXpN-NrpVbuWZWe9T"
)
key_index=0
echo $key
echo "" >> tinypng_record.txt
removeQuotation(){
	val=$1
	len=${#val}
	echo ${val:1:len-2}
}

compress(){
	echo "开始压缩$png"
	conent="curl --user api:${keys[key_index]} --data-binary @$1 https://api.tinify.com/shrink"
	echo "执行:$conent"
	response2=`$conent`
	echo $response2
	#检查key是否还能用
	error=`echo $response2|jq ".error"`
	if [[ x$error != x"null" ]]; then
		echo "错误信息:$error"
		key_index=`expr $key_index + 1`
		#数组长度
		length=${#keys[@]}
		if [[ $key_index -ge $length ]]; then
			echo "key都用光了,最后的文件是$png"
			exit
		else
			compress $1
		fi
	else
		url=`echo $response2|jq ".output.url"`
		url=$(removeQuotation $url)
		echo "开始下载url:$url"
		curl $url -o $png
		#追加写
		echo "下载完成$url"
		echo "$1 " >> tinypng_record.txt
		echo ""
	fi
}

list_alldir(){  
    for file2 in `ls -a $1`  
    do  
        if [ x"$file2" != x"." -a x"$file2" != x".." ];then  
            if [ -d "$1/$file2" ];then
            	if [[ x$file2 = x"build" ]]; then
            		echo "跳过build文件夹"
            	else
            		list_alldir "$1/$file2"  
            	fi
            else
            	png="$1/$file2"
            	result=`cat tinypng_record.txt | grep $png`
            	if [[ x$result != x""  ]]; then
            		echo "跳过$png,已经被压缩过"
            		continue;
            	fi
				len=${#png}
				right=${png:len-6:len}
				if [[ x$right = x".9.png" ]]; then
					echo "跳过$png,.9.png不压缩"
            		continue;
				fi
            	right=${png##*.}
            	if [[ x"$right" = x"png" ]]; then
            		compress $png
            	fi
            fi  
        fi  
    done  
} 

list_alldir .

