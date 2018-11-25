//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget.provider;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.rong.imkit.R;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.provider.IContainerItemProvider.MessageProvider;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UnknownMessage;

@ProviderTag(
        messageContent = UnknownMessage.class,
        showPortrait = false,
        showWarning = false,
        centerInHorizontal = true,
        showSummaryWithName = false
)
public class UnknownMessageItemProvider extends MessageProvider<MessageContent> {
  public UnknownMessageItemProvider() {
  }

  public void bindView(View v, int position, MessageContent content, UIMessage message) {
    io.rong.imkit.widget.provider.UnknownMessageItemProvider.ViewHolder viewHolder = (io.rong.imkit.widget.provider.UnknownMessageItemProvider.ViewHolder)v.getTag();
    viewHolder.contentTextView.setText(R.string.rc_message_unknown);
  }

  public Spannable getContentSummary(MessageContent data) {
    return null;
  }

  public Spannable getContentSummary(Context context, MessageContent data) {
    return new SpannableString(context.getResources().getString(R.string.rc_message_unknown));
  }

  public void onItemClick(View view, int position, MessageContent content, UIMessage message) {
  }

  public View newView(Context context, ViewGroup group) {
    View view = LayoutInflater.from(context).inflate(R.layout.rc_item_information_notification_message, (ViewGroup)null);
    io.rong.imkit.widget.provider.UnknownMessageItemProvider.ViewHolder viewHolder = new io.rong.imkit.widget.provider.UnknownMessageItemProvider.ViewHolder();
    viewHolder.contentTextView = (TextView)view.findViewById(R.id.rc_msg);
    viewHolder.contentTextView.setMovementMethod(LinkMovementMethod.getInstance());
    view.setTag(viewHolder);
    return view;
  }

  private static class ViewHolder {
    TextView contentTextView;

    private ViewHolder() {
    }
  }
}
