//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget.provider;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.rong.imkit.R;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM.ConversationBehaviorListener;
import io.rong.imkit.RongIM.ConversationClickListener;
import io.rong.imkit.emoticon.AndroidEmoji;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.AutoLinkTextView;
import io.rong.imkit.widget.ILinkClickListener;
import io.rong.imkit.widget.LinkTextViewMovementMethod;
import io.rong.imkit.widget.provider.IContainerItemProvider.MessageProvider;
import io.rong.imlib.model.Message.MessageDirection;
import io.rong.message.TextMessage;

@ProviderTag(
        messageContent = TextMessage.class,
        showReadState = true
)
public class TextMessageItemProvider extends MessageProvider<TextMessage> {
  private static final String TAG = "TextMessageItemProvider";

  public TextMessageItemProvider() {
  }

  public View newView(Context context, ViewGroup group) {
    View view = LayoutInflater.from(context).inflate(R.layout.rc_item_text_message, (ViewGroup)null);
    io.rong.imkit.widget.provider.TextMessageItemProvider.ViewHolder holder = new io.rong.imkit.widget.provider.TextMessageItemProvider.ViewHolder();
    holder.message = (AutoLinkTextView)view.findViewById(16908308);
    view.setTag(holder);
    return view;
  }

  public Spannable getContentSummary(TextMessage data) {
    return null;
  }

  public Spannable getContentSummary(Context context, TextMessage data) {
    if (data == null) {
      return null;
    } else {
      String content = data.getContent();
      if (content != null) {
        if (content.length() > 100) {
          content = content.substring(0, 100);
        }

        return new SpannableString(AndroidEmoji.ensure(content));
      } else {
        return null;
      }
    }
  }

  public void onItemClick(View view, int position, TextMessage content, UIMessage message) {
  }

  public void bindView(final View v, int position, TextMessage content, final UIMessage data) {
    io.rong.imkit.widget.provider.TextMessageItemProvider.ViewHolder holder = (io.rong.imkit.widget.provider.TextMessageItemProvider.ViewHolder)v.getTag();
    if (data.getMessageDirection() == MessageDirection.SEND) {
      holder.message.setBackgroundResource(R.drawable.rc_ic_bubble_right);
    } else {
      holder.message.setBackgroundResource(R.drawable.rc_ic_bubble_left);
    }

    final AutoLinkTextView textView = holder.message;
    if (data.getTextMessageContent() != null) {
      int len = data.getTextMessageContent().length();
      if (v.getHandler() != null && len > 500) {
        v.getHandler().postDelayed(new Runnable() {
          public void run() {
            textView.setText(data.getTextMessageContent());
          }
        }, 50L);
      } else {
        textView.setText(data.getTextMessageContent());
      }
    }

    holder.message.setMovementMethod(new LinkTextViewMovementMethod(new ILinkClickListener() {
      public boolean onLinkClick(String link) {
        ConversationBehaviorListener listener = RongContext.getInstance().getConversationBehaviorListener();
        ConversationClickListener clickListener = RongContext.getInstance().getConversationClickListener();
        boolean result = false;
        if (listener != null) {
          result = listener.onMessageLinkClick(v.getContext(), link);
        } else if (clickListener != null) {
          result = clickListener.onMessageLinkClick(v.getContext(), link, data.getMessage());
        }

        if (listener == null && clickListener == null || !result) {
          String str = link.toLowerCase();
          if (str.startsWith("http") || str.startsWith("https")) {
            Intent intent = new Intent("io.io.rong.imkit.intent.action.webview");
            intent.setPackage(v.getContext().getPackageName());
            intent.putExtra("url", link);
            v.getContext().startActivity(intent);
            result = true;
          }
        }

        return result;
      }
    }));
  }

  private static class ViewHolder {
    AutoLinkTextView message;
    boolean longClick;

    private ViewHolder() {
    }
  }
}
