package com.module.home.flutter

import android.app.Activity
import android.arch.lifecycle.Lifecycle
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.fastjson.JSONObject
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiResult
import com.common.rxretrofit.httpGet
import com.common.utils.U
import com.module.RouterConstants
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.android.SplashScreen
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterShellArgs
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformPlugin
import io.flutter.view.FlutterView


/**
 * Flutter目前不提供在View级别使用Flutter的便捷API，因此如果可能，应避免使用 FlutterView。
 * 但是如果需要的话，显示 FlutterView 在技术上是可行的。
 * 确保使用io.flutter.embedding.android.FlutterView 而不是 io.flutter.view.FlutterView。
 * 您可以像其他任何Android View 一样实例化新的 FlutterView
 */
@Route(path = RouterConstants.ACTIVITY_FLUTTER)
class BaseFlutterActivity : io.flutter.embedding.android.FlutterActivity() {
    val TAG = "BaseFlutterActivity"

    companion object {
        fun goTest(activity: Activity) {

        }
    }
//    val EXTRA_INITIAL_ROUTE = "initial_route"
//    val EXTRA_BACKGROUND_MODE = "background_mode"
//    val EXTRA_CACHED_ENGINE_ID = "cached_engine_id"
//    val EXTRA_DESTROY_ENGINE_WITH_ACTIVITY = "destroy_engine_with_activity"
//    .putExtra(EXTRA_CACHED_ENGINE_ID, cachedEngineId)
//    .putExtra(EXTRA_DESTROY_ENGINE_WITH_ACTIVITY, destroyEngineWithActivity)
//    .putExtra(EXTRA_BACKGROUND_MODE, backgroundMode);
//    .putExtra(EXTRA_INITIAL_ROUTE, initialRoute)

    /**
     * 配置引擎
     */
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        EventChannel(flutterEngine.dartExecutor, "skr_flutter/event_channel").setStreamHandler(
                object : EventChannel.StreamHandler {
                    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                        MyLog.d(TAG, "onListen arguments = $arguments, events = $events")
                    }

                    override fun onCancel(arguments: Any?) {
                        MyLog.d(TAG, "onCancel arguments = $arguments")
                    }
                }
        )

        MethodChannel(flutterEngine.dartExecutor, "skr_flutter/method_channel")
                .setMethodCallHandler { call, result ->
                    MyLog.d(TAG, "threadId=" + Thread.currentThread())
                    when {
                        call.method == "getDeviceID" -> result.success(U.getDeviceUtils().deviceID)
                        call.method == "getChannel" -> result.success(U.getChannelUtils().channel)
                        call.method == "httpGet" -> {
                            var url = call.argument<String>("url")
                            var params = call.argument<HashMap<String, Any?>>("params")
                            httpGet(url!!, params) { r ->
                                result.success(r)
                            }
                        }
                        else -> result.notImplemented()
                    }
                }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun shouldAttachEngineToActivity(): Boolean {
        return super.shouldAttachEngineToActivity()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun getRenderMode(): io.flutter.embedding.android.FlutterView.RenderMode {
        return super.getRenderMode()
    }


    override fun getLifecycle(): Lifecycle {
        return super.getLifecycle()
    }

    override fun provideFlutterEngine(context: Context): FlutterEngine? {
        return super.provideFlutterEngine(context)
    }

    override fun onPostResume() {
        super.onPostResume()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
    }

    override fun onFlutterUiDisplayed() {
        super.onFlutterUiDisplayed()
    }

    override fun providePlatformPlugin(activity: Activity?, flutterEngine: FlutterEngine): PlatformPlugin? {
        return super.providePlatformPlugin(activity, flutterEngine)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun getFlutterEngine(): FlutterEngine? {
        return super.getFlutterEngine()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun shouldDestroyEngineWithHost(): Boolean {
        return super.shouldDestroyEngineWithHost()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
    }

    override fun getCachedEngineId(): String? {
        return super.getCachedEngineId()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onFlutterUiNoLongerDisplayed() {
        super.onFlutterUiNoLongerDisplayed()
    }

    override fun cleanUpFlutterEngine(flutterEngine: FlutterEngine) {
        super.cleanUpFlutterEngine(flutterEngine)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun getActivity(): Activity {
        return super.getActivity()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun provideSplashScreen(): SplashScreen? {
        return super.provideSplashScreen()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
    }

    override fun getAppBundlePath(): String {
        return super.getAppBundlePath()
    }

    override fun getContext(): Context {
        return super.getContext()
    }

    override fun getTransparencyMode(): io.flutter.embedding.android.FlutterView.TransparencyMode {
        return super.getTransparencyMode()
    }

    override fun getDartEntrypointFunctionName(): String {
        return super.getDartEntrypointFunctionName()
    }

    override fun getInitialRoute(): String {
        return super.getInitialRoute()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun getFlutterShellArgs(): FlutterShellArgs {
        return super.getFlutterShellArgs()
    }
}