//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget.provider;

import io.rong.imkit.model.ConversationProviderTag;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imkit.widget.provider.IContainerItemProvider.ConversationProvider;
import io.rong.imkit.widget.provider.PrivateConversationProvider;

@ConversationProviderTag(
        conversationType = "system",
        portraitPosition = 1
)
public class SystemConversationProvider extends PrivateConversationProvider implements ConversationProvider<UIConversation> {
  public SystemConversationProvider() {
  }

  public String getTitle(String id) {
    String name;
    if (RongUserInfoManager.getInstance().getUserInfo(id) == null) {
      name = id;
    } else {
      name = RongUserInfoManager.getInstance().getUserInfo(id).getName();
    }

    return name;
  }
}
