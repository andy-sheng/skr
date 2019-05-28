//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget.provider;

import android.net.Uri;

import io.rong.imkit.RongContext;
import io.rong.imkit.model.ConversationKey;
import io.rong.imkit.model.ConversationProviderTag;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.widget.provider.IContainerItemProvider.ConversationProvider;
import io.rong.imkit.widget.provider.PrivateConversationProvider;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.PublicServiceProfile;

@ConversationProviderTag(
        conversationType = "app_public_service",
        portraitPosition = 1
)
public class AppServiceConversationProvider extends PrivateConversationProvider implements ConversationProvider<UIConversation> {
    public AppServiceConversationProvider() {
    }

    public String getTitle(String id) {
        ConversationKey mKey = ConversationKey.obtain(id, ConversationType.APP_PUBLIC_SERVICE);
        PublicServiceProfile info = RongContext.getInstance().getPublicServiceInfoFromCache(mKey.getKey());
        String name;
        if (info != null) {
            name = info.getName();
        } else {
            name = "";
        }

        return name;
    }

    public Uri getPortraitUri(String id) {
        ConversationKey mKey = ConversationKey.obtain(id, ConversationType.APP_PUBLIC_SERVICE);
        PublicServiceProfile info = RongContext.getInstance().getPublicServiceInfoFromCache(mKey.getKey());
        Uri uri;
        if (info != null) {
            uri = info.getPortraitUri();
        } else {
            uri = null;
        }

        return uri;
    }
}
