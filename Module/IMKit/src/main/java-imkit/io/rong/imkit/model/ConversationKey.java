//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.model;

import android.text.TextUtils;

import io.rong.common.RLog;
import io.rong.imlib.model.Conversation.ConversationType;

public final class ConversationKey {
    public static final String SEPARATOR = "#@6RONG_CLOUD9@#";
    private String key;
    private String targetId;
    private ConversationType type;

    private ConversationKey() {
    }

    public static io.rong.imkit.model.ConversationKey obtain(String targetId, ConversationType type) {
        if (!TextUtils.isEmpty(targetId) && type != null) {
            io.rong.imkit.model.ConversationKey conversationKey = new io.rong.imkit.model.ConversationKey();
            conversationKey.setTargetId(targetId);
            conversationKey.setType(type);
            conversationKey.setKey(targetId + "#@6RONG_CLOUD9@#" + type.getValue());
            return conversationKey;
        } else {
            return null;
        }
    }

    public static io.rong.imkit.model.ConversationKey obtain(String key) {
        if (!TextUtils.isEmpty(key) && key.contains("#@6RONG_CLOUD9@#")) {
            io.rong.imkit.model.ConversationKey conversationKey = new io.rong.imkit.model.ConversationKey();
            if (key.contains("#@6RONG_CLOUD9@#")) {
                String[] array = key.split("#@6RONG_CLOUD9@#");
                conversationKey.setTargetId(array[0]);

                try {
                    conversationKey.setType(ConversationType.setValue(Integer.parseInt(array[1])));
                    return conversationKey;
                } catch (NumberFormatException var4) {
                    RLog.e("ConversationKey ", "NumberFormatException");
                    return null;
                }
            }
        }

        return null;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTargetId() {
        return this.targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public ConversationType getType() {
        return this.type;
    }

    public void setType(ConversationType type) {
        this.type = type;
    }
}
