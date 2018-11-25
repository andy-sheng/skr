//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.manager;

import android.content.Context;

import java.util.List;

import io.rong.imkit.plugin.IPluginModule;
import io.rong.imlib.model.Conversation.ConversationType;

public interface IExternalModule {
  void onCreate(Context var1);

  void onInitialized(String var1);

  void onConnected(String var1);

  void onViewCreated();

  List<IPluginModule> getPlugins(ConversationType var1);

  void onDisconnected();
}
