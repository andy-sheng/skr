
FlutterBoost.instance().init

FlutterActivityAndFragmentDelegate.onAttach
	FlutterBoost.instance().doInitialFlutter();
		createEngine
			mEngine = new FlutterEngine(mPlatform.getApplication().getApplicationContext());

    FlutterBoost.instance().boostPluginRegistry();
    	mPlatform.registerPlugins(mRegistry); // 注册我们自己的插件
    		GeneratedPluginRegistrant.registerWith(mRegistry) // 注册boost 自己的插件
    		"boostPluginRegistry"

main.dart App builder
	flutter_boost.dart init
			addPostFrameCallback invokeMethod('pageOnstart') 拿到pageInfo
					container_coordinator.dart nativeContainerDidShow
							container_manager.dart showContainer


01-13 12:44:54.452 I/flutter ( 9374): FlutterBoost#onShownContainerChanged old:null now:default
01-13 12:44:54.462 I/flutter ( 9374): syncMyInfo 耗时 222
01-13 12:44:54.463 I/flutter ( 9374): {uid: 1705476, userNickname: 程思敏链家, avatar: http://res-static.inframe.mobi/avatar/7fc1ba572a78bae303d7ea8781d5f060}
01-13 12:44:54.469 I/flutter ( 9374): FlutterBoost#ContainerObserver#2 didPush
01-13 12:44:54.470 I/flutter ( 9374): FlutterBoost#BoostContainerLifeCycleObserver container:RelayResultPage lifeCycle:ContainerLifeCycle.Appear
01-13 12:44:54.470 I/flutter ( 9374): FlutterBoost#native containner did show,
01-13 12:44:54.470 I/flutter ( 9374): manager dump:
01-13 12:44:54.470 I/flutter ( 9374): onstage#:
01-13 12:44:54.470 I/flutter ( 9374):   {uniqueId=1578890693660-183912740,name=RelayResultPage}
01-13 12:44:54.470 I/flutter ( 9374): offstage#:
01-13 12:44:54.470 I/flutter ( 9374):   {uniqueId=default,name=default}
01-13 12:44:54.482 I/flutter ( 9374): FlutterBoost#ManagerNavigatorObserver didPush
01-13 12:44:54.488 I/flutter ( 9374): FlutterBoost#build widget:RelayResultPage for page:RelayResultPage(1578890693660-183912740)
01-13 12:44:54.491 I/flutter ( 9374): ViewStateModel---constructor--->RelayResultData
01-13 12:44:54.534 I/flutter ( 9374): url=http://dev.game.inframe.mobi/v1/relaygame/result params={roomID: null}
01-13 12:44:54.553 D/CommonFlutterPlugin( 9374): [9374]:httpGet被SkrMethodChannelHandler处理
01-13 12:44:54.560 I/flutter ( 9374): MyUserInfoManager.avatar http://res-static.inframe.mobi/avatar/7fc1ba572a78bae303d7ea8781d5f060
01-13 12:44:54.560 I/flutter ( 9374): dataModel.targetAvatar null
01-13 12:44:54.609 I/flutter ( 9374): http://res-static.inframe.mobi/avatar/7fc1ba572a78bae303d7ea8781d5f060?x-oss-process=image/resize,w_160
01-13 12:44:54.653 I/flutter ( 9374): MiddleAnimArea initState score=0.0
01-13 12:44:54.658 I/flutter ( 9374): MiddleAnimArea didChangeDependencies
01-13 12:44:54.658 I/flutter ( 9374): MiddleAnimArea build
01-13 12:44:54.835 I/flutter ( 9374): FlutterBoost#onShownContainerChanged old:default now:1578890693660-183912740
01-13 12:44:54.841 I/flutter ( 9374): httpGet 耗时 290
01-13 12:44:54.843 I/flutter ( 9374):
01-13 12:44:54.843 I/flutter ( 9374): ApiResult{errno: 3, errmsg: strconv.ParseUint: parsing "": invalid syntax, traceId: , data: {}}
01-13 12:44:54.873 I/flutter ( 9374): MiddleAnimArea build
01-13 12:44:54.962 I/flutter ( 9374): MiddleAnimArea build

