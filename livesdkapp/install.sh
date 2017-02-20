echo $1$2
if [ x"$1" == x"2" ] ; then 
	echo "gradle build"
	cd .
	gradle build
fi 

if [ "$1" == "2" -o "$1" == "1" ] ; then 
	adb install -r build/outputs/apk/watchsdklite-debug.apk
fi 



if [ "$2" == "2" ] ; then 
	echo "gradle build"
	cd /Users/chengsimin/dev/walilive/MiLiveSdkManager/app
	gradle build
fi 

if [ "$2" == "2" -o "$2" == "1" ] ; then 
	adb install -r	/Users/chengsimin/dev/walilive/MiLiveSdkManager/app/build/outputs/apk/app-debug.apk
fi 


