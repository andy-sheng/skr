//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget.provider;

import io.rong.imkit.model.ConversationProviderTag;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.widget.provider.IContainerItemProvider.ConversationProvider;
import io.rong.imkit.widget.provider.PrivateConversationProvider;

@ConversationProviderTag(
        conversationType = "customer_service",
        portraitPosition = 1
)
public class CustomerServiceConversationProvider extends PrivateConversationProvider implements ConversationProvider<UIConversation> {
  public CustomerServiceConversationProvider() {
  }
}
