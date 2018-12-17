//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget.provider;

import android.net.Uri;
import android.view.View;

import io.rong.imkit.RongContext;
import io.rong.imkit.model.ConversationKey;
import io.rong.imkit.model.ConversationProviderTag;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.widget.provider.IContainerItemProvider.ConversationProvider;
import io.rong.imkit.widget.provider.PrivateConversationProvider;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.PublicServiceProfile;

@ConversationProviderTag(
        conversationType = "public_service",
        portraitPosition = 1
)
public class PublicServiceConversationProvider extends PrivateConversationProvider implements ConversationProvider<UIConversation> {
    private ConversationKey mKey;

    public PublicServiceConversationProvider() {
    }

    public String getTitle(String id) {
        this.mKey = ConversationKey.obtain(id, ConversationType.PUBLIC_SERVICE);
        PublicServiceProfile info = RongContext.getInstance().getPublicServiceInfoFromCache(this.mKey.getKey());
        String name;
        if (info != null) {
            name = info.getName();
        } else {
            name = "";
        }

        return name;
    }

    public Uri getPortraitUri(String id) {
        this.mKey = ConversationKey.obtain(id, ConversationType.PUBLIC_SERVICE);
        PublicServiceProfile info = RongContext.getInstance().getPublicServiceInfoFromCache(this.mKey.getKey());
        Uri uri;
        if (info != null) {
            uri = info.getPortraitUri();
        } else {
            uri = null;
        }

        return uri;
    }

    public void bindView(View view, int position, UIConversation data) {
        super.bindView(view, position, data);
    }
}
