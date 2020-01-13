package com.common.flutter.boost

import android.app.Activity
import android.content.Context
import com.common.flutter.plugin.CommonFlutterPlugin
import com.common.log.MyLog
import com.common.utils.U
import com.idlefish.flutterboost.FlutterBoost
import com.idlefish.flutterboost.FlutterBoostPlugin
import com.idlefish.flutterboost.containers.MyBoostFlutterActivity
import com.idlefish.flutterboost.interfaces.INativeRouter
import io.flutter.embedding.android.FlutterView
import io.flutter.plugins.GeneratedPluginRegistrant

object FlutterBoostController  {

    val TAG = "FlutterBoostController"

    private var inited = false

    var openContainerListener: ((context: Context?, url: String?, urlParams: MutableMap<String, Any>?, requestCode: Int, exts: MutableMap<String, Any>?) -> Unit)? = null

    /**
     *
     */
    fun openFlutterPage(context: Context, pageRouterName: String, params: MutableMap<String, Any>?, requestCode: Int = 0) {
        init()
        // 打开一个flutter page 页面
        val intent = MyBoostFlutterActivity.withNewEngine().url(pageRouterName)
                .params(params ?: mapOf<String, Any>())
                .backgroundMode(MyBoostFlutterActivity.BackgroundMode.opaque)
                .build(context)
        if (context is Activity) {
            context.startActivityForResult(intent, requestCode)
        } else {
            context.startActivity(intent)
        }
    }

    fun openFlutterPage(context: Context, pageRouterName: String, params: MutableMap<String, Any>?) {
        openFlutterPage(context,pageRouterName,params,0)
    }

    fun init() {
        if (inited) {
            return
        }
        val router = INativeRouter { context, url, urlParams, requestCode, exts ->
            MyLog.d("FlutterBoost", "openContainer context=$context url=$url urlParams=$urlParams requestCode=$requestCode exts=$exts")
            openContainerListener?.invoke(context, url, urlParams, requestCode, exts)
        }

        val pluginsRegister = FlutterBoost.BoostPluginsRegister { mRegistry ->
            MyLog.d("FlutterBoost", "registerPlugins mRegistry=$mRegistry")
            GeneratedPluginRegistrant.registerWith(mRegistry)
            FlutterBoostPlugin.registerWith(mRegistry.registrarFor("com.idlefish.flutterboost.FlutterBoostPlugin"))
            //用户自定义的插件也在这里注册
            TextPlatformViewPlugin.register(mRegistry.registrarFor("TextPlatformViewPlugin"))
            CommonFlutterPlugin.registerWith(mRegistry.registrarFor("com.commonsdk.SkrFlutterPlugin"))
        }

        val platform = FlutterBoost.ConfigBuilder(U.app(), router)
                .isDebug(true)
                // 任意activity启动都创建
//                .whenEngineStart(FlutterBoost.ConfigBuilder.ANY_ACTIVITY_CREATED)
                .whenEngineStart(FlutterBoost.ConfigBuilder.FLUTTER_ACTIVITY_CREATED)

                .renderMode(FlutterView.RenderMode.texture)
                .pluginsRegister(pluginsRegister)
                .lifecycleListener(object : FlutterBoost.BoostLifecycleListener{
                    override fun onEngineCreated() {
                        MyLog.d(TAG, "onEngineCreated")
                    }

                    override fun onPluginsRegistered() {
                        MyLog.d(TAG, "onPluginsRegistered")
                    }

                    override fun onEngineDestroy() {
                        MyLog.w(TAG,"onEngineDestroy")
                    }

                })
                .build()
        FlutterBoost.instance().init(platform)
        inited = true
    }
}