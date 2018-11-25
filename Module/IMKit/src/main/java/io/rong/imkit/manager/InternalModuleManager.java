//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.manager;

import android.content.Context;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import io.rong.common.RLog;
import io.rong.imkit.manager.IExternalModule;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imlib.model.Conversation.ConversationType;

public class InternalModuleManager {
  private static final String TAG = "InternalModuleManager";
  private static IExternalModule callModule;

  private InternalModuleManager() {
  }

  public static io.rong.imkit.manager.InternalModuleManager getInstance() {
    return io.rong.imkit.manager.InternalModuleManager.SingletonHolder.sInstance;
  }

  public static void init(Context context) {
    RLog.i("InternalModuleManager", "init");

    try {
      String moduleName = "io.io.rong.callkit.RongCallModule";
      Class<?> cls = Class.forName(moduleName);
      Constructor<?> constructor = cls.getConstructor();
      callModule = (IExternalModule)constructor.newInstance();
      callModule.onCreate(context);
    } catch (Exception var4) {
      RLog.i("InternalModuleManager", "Can not find RongCallModule.");
    }

  }

  public void onInitialized(String appKey) {
    RLog.i("InternalModuleManager", "onInitialized");
    if (callModule != null) {
      callModule.onInitialized(appKey);
    }

  }

  public List<IPluginModule> getExternalPlugins(ConversationType conversationType) {
    List<IPluginModule> pluginModules = new ArrayList();
    if (callModule != null && (conversationType.equals(ConversationType.PRIVATE) || conversationType.equals(ConversationType.DISCUSSION) || conversationType.equals(ConversationType.GROUP))) {
      pluginModules.addAll(callModule.getPlugins(conversationType));
    }

    return pluginModules;
  }

  public void onConnected(String token) {
    RLog.i("InternalModuleManager", "onConnected");
    if (callModule != null) {
      callModule.onConnected(token);
    }

  }

  public void onLoaded() {
    RLog.i("InternalModuleManager", "onLoaded");
    if (callModule != null) {
      callModule.onViewCreated();
    }

  }

  static class SingletonHolder {
    static io.rong.imkit.manager.InternalModuleManager sInstance = new io.rong.imkit.manager.InternalModuleManager();

    SingletonHolder() {
    }
  }
}
