//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Message;

public class ConversationTypeFilter {
    io.rong.imkit.model.ConversationTypeFilter.Level mLevel;
    List<ConversationType> mTypes = new ArrayList();

    public static io.rong.imkit.model.ConversationTypeFilter obtain(ConversationType... conversationType) {
        return new io.rong.imkit.model.ConversationTypeFilter(conversationType);
    }

    public static io.rong.imkit.model.ConversationTypeFilter obtain(io.rong.imkit.model.ConversationTypeFilter.Level level) {
        return new io.rong.imkit.model.ConversationTypeFilter(level);
    }

    public static io.rong.imkit.model.ConversationTypeFilter obtain() {
        return new io.rong.imkit.model.ConversationTypeFilter();
    }

    private ConversationTypeFilter(ConversationType... type) {
        this.mTypes.addAll(Arrays.asList(type));
        this.mLevel = io.rong.imkit.model.ConversationTypeFilter.Level.CONVERSATION_TYPE;
    }

    private ConversationTypeFilter() {
        this.mLevel = io.rong.imkit.model.ConversationTypeFilter.Level.ALL;
    }

    private ConversationTypeFilter(io.rong.imkit.model.ConversationTypeFilter.Level level) {
        this.mLevel = level;
    }

    public io.rong.imkit.model.ConversationTypeFilter.Level getLevel() {
        return this.mLevel;
    }

    public List<ConversationType> getConversationTypeList() {
        return this.mTypes;
    }

    public boolean hasFilter(Message message) {
        if (this.mLevel == io.rong.imkit.model.ConversationTypeFilter.Level.ALL) {
            return true;
        } else if (this.mLevel == io.rong.imkit.model.ConversationTypeFilter.Level.CONVERSATION_TYPE) {
            return this.mTypes.contains(message.getConversationType());
        } else {
            return false;
        }
    }

    public static enum Level {
        ALL,
        CONVERSATION_TYPE,
        NONE;

        private Level() {
        }
    }
}
