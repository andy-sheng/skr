if [[ $1 = "debug" ]]; then
	./gradlew :pluginmanager:makeJar
	cp /Users/chengsimin/dev/livesdk/livesdk/pluginmanager/build/libs/liveplugin-noproguard.jar \
	/Users/chengsimin/dev/GameCenterPhone/gamecenter_knights/app/libs/liveplugin.jar
else
	./gradlew :pluginmanager:buildJar
	cp /Users/chengsimin/dev/livesdk/livesdk/pluginmanager/build/libs/liveplugin.jar \
	/Users/chengsimin/dev/GameCenterPhone/gamecenter_knights/app/libs/liveplugin.jar	
fi

