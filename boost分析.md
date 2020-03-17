
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


