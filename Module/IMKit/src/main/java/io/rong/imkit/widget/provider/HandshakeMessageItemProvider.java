//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget.provider;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.view.ViewGroup;

import io.rong.imkit.emoticon.AndroidEmoji;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.provider.IContainerItemProvider.MessageProvider;
import io.rong.message.HandshakeMessage;

@ProviderTag(
        messageContent = HandshakeMessage.class,
        showPortrait = false,
        centerInHorizontal = true,
        hide = true
)
public class HandshakeMessageItemProvider extends MessageProvider<HandshakeMessage> {
  public HandshakeMessageItemProvider() {
  }

  public View newView(Context context, ViewGroup group) {
    return null;
  }

  public Spannable getContentSummary(HandshakeMessage data) {
    return null;
  }

  public Spannable getContentSummary(Context context, HandshakeMessage data) {
    return data != null && data.getContent() != null ? new SpannableString(AndroidEmoji.ensure(data.getContent())) : null;
  }

  public void onItemClick(View view, int position, HandshakeMessage content, UIMessage message) {
  }

  public void bindView(View v, int position, HandshakeMessage content, UIMessage data) {
  }
}
