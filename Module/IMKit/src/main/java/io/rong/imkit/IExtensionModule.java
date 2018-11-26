//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit;

import java.util.List;

import io.rong.imkit.RongExtension;
import io.rong.imkit.emoticon.IEmoticonTab;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Message;

public interface IExtensionModule {
    void onInit(String var1);

    void onConnect(String var1);

    void onAttachedToExtension(RongExtension var1);

    void onDetachedFromExtension();

    void onReceivedMessage(Message var1);

    List<IPluginModule> getPluginModules(ConversationType var1);

    List<IEmoticonTab> getEmoticonTabs();

    void onDisconnect();
}
