//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.view.KeyEvent;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import io.rong.common.RLog;
import io.rong.imkit.IExtensionModule;
import io.rong.imkit.RongExtension;
import io.rong.imkit.emoticon.EmojiTab;
import io.rong.imkit.emoticon.IEmojiItemClickListener;
import io.rong.imkit.emoticon.IEmoticonTab;
import io.rong.imkit.manager.InternalModuleManager;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imkit.plugin.ImagePlugin;
import io.rong.imkit.widget.provider.FilePlugin;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Message;

public class DefaultExtensionModule implements IExtensionModule {
    private static final String TAG = io.rong.imkit.DefaultExtensionModule.class.getSimpleName();
    private EditText mEditText;
    private Stack<EditText> stack;
    String[] types = null;

    public DefaultExtensionModule() {
    }

    public void onInit(String appKey) {
        this.stack = new Stack();
    }

    public void onConnect(String token) {
    }

    public void onAttachedToExtension(RongExtension extension) {
        this.mEditText = extension.getInputEditText();
        Context context = extension.getContext();
        RLog.i(TAG, "attach " + this.stack.size());
        this.stack.push(this.mEditText);
        Resources resources = context.getResources();

        try {
            this.types = resources.getStringArray(resources.getIdentifier("rc_realtime_support_conversation_types", "array", context.getPackageName()));
        } catch (NotFoundException var5) {
            RLog.i(TAG, "not config rc_realtime_support_conversation_types in rc_config.xml");
        }

    }

    public void onDetachedFromExtension() {
        RLog.i(TAG, "detach " + this.stack.size());
        if (this.stack.size() > 0) {
            this.stack.pop();
            this.mEditText = this.stack.size() > 0 ? (EditText) this.stack.peek() : null;
        }

    }

    public void onReceivedMessage(Message message) {
    }

    public List<IPluginModule> getPluginModules(ConversationType conversationType) {
        List<IPluginModule> pluginModuleList = new ArrayList();
        IPluginModule image = new ImagePlugin();
        pluginModuleList.add(image);

        if (conversationType.equals(ConversationType.GROUP) || conversationType.equals(ConversationType.DISCUSSION) || conversationType.equals(ConversationType.PRIVATE)) {
            pluginModuleList.addAll(InternalModuleManager.getInstance().getExternalPlugins(conversationType));
        }

        // todo 屏蔽文件发送的入口
//        IPluginModule file = new FilePlugin();
//        pluginModuleList.add(file);
        return pluginModuleList;
    }

    public List<IEmoticonTab> getEmoticonTabs() {
        EmojiTab emojiTab = new EmojiTab();
        emojiTab.setOnItemClickListener(new IEmojiItemClickListener() {
            public void onEmojiClick(String emoji) {
                int start = io.rong.imkit.DefaultExtensionModule.this.mEditText.getSelectionStart();
                io.rong.imkit.DefaultExtensionModule.this.mEditText.getText().insert(start, emoji);
            }

            public void onDeleteClick() {
                io.rong.imkit.DefaultExtensionModule.this.mEditText.dispatchKeyEvent(new KeyEvent(0, 67));
            }
        });
        List<IEmoticonTab> list = new ArrayList();
        list.add(emojiTab);
        return list;
    }

    public void onDisconnect() {
    }
}
