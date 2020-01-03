package com.common.flutter.boost;

import android.content.Context;

import com.common.log.MyLog;
import com.common.utils.U;
import com.idlefish.flutterboost.*;

import java.util.Map;

import com.idlefish.flutterboost.interfaces.INativeRouter;

import io.flutter.embedding.android.FlutterView;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugins.GeneratedPluginRegistrant;

public class BoostInit {


    public static void init() {

        INativeRouter router = new INativeRouter() {
            @Override
            public void openContainer(Context context, String url, Map<String, Object> urlParams, int requestCode, Map<String, Object> exts) {
                MyLog.d("FlutterBoost", "openContainer" + " context=" + context + " url=" + url + " urlParams=" + urlParams + " requestCode=" + requestCode + " exts=" + exts);
                String assembleUrl = Utils.assembleUrl(url, urlParams);
                FlutterPageRouter.openPageByUrl(context, assembleUrl, urlParams);
            }

        };

        FlutterBoost.BoostPluginsRegister pluginsRegister = new FlutterBoost.BoostPluginsRegister() {

            @Override
            public void registerPlugins(PluginRegistry mRegistry) {
                MyLog.d("FlutterBoost", "registerPlugins" + " mRegistry=" + mRegistry);
                GeneratedPluginRegistrant.registerWith(mRegistry);
                TextPlatformViewPlugin.register(mRegistry.registrarFor("TextPlatformViewPlugin"));
            }
        };

        Platform platform = new FlutterBoost
                .ConfigBuilder(U.app(), router)
                .isDebug(true)
                .whenEngineStart(FlutterBoost.ConfigBuilder.ANY_ACTIVITY_CREATED)
                .renderMode(FlutterView.RenderMode.texture)
                .pluginsRegister(pluginsRegister)
                .build();

        FlutterBoost.instance().init(platform);


    }
}
