package com.module.msg;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imkit.plugin.ImagePlugin;
import io.rong.imlib.model.Conversation;

public class MyExtensionModule extends DefaultExtensionModule {

    @Override
    public List<IPluginModule> getPluginModules(Conversation.ConversationType conversationType) {
        List<IPluginModule> pluginModuleList = new ArrayList<>();
        IPluginModule image = new ImagePlugin();
//        IPluginModule location = new DefaultLocationPlugin();
//        IPluginModule audio = new AudioPlugin();
//        IPluginModule video = new VideoPlugin();
//        IPluginModule file = new FilePlugin();

        if (conversationType.equals(Conversation.ConversationType.PRIVATE)) {
            pluginModuleList.add(image);
//            pluginModuleList.add(location);
//            pluginModuleList.add(audio);
//            pluginModuleList.add(video);
//            pluginModuleList.add(file);
        } 

        return pluginModuleList;
    }
}
