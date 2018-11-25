//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget.provider;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import io.rong.imkit.R;
import io.rong.imkit.activity.CSLeaveMessageActivity;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.provider.IContainerItemProvider.MessageProvider;
import io.rong.imlib.CustomServiceConfig.CSLeaveMessageType;
import io.rong.message.CSPullLeaveMessage;

@ProviderTag(
        messageContent = CSPullLeaveMessage.class,
        showPortrait = false,
        showProgress = false,
        showWarning = false,
        centerInHorizontal = true,
        showSummaryWithName = false
)
public class CSPullLeaveMsgItemProvider extends MessageProvider<CSPullLeaveMessage> {
  public CSPullLeaveMsgItemProvider() {
  }

  public void bindView(View v, int position, CSPullLeaveMessage csPullLeaveMessage, final UIMessage message) {
    io.rong.imkit.widget.provider.CSPullLeaveMsgItemProvider.ViewHolder viewHolder = (io.rong.imkit.widget.provider.CSPullLeaveMsgItemProvider.ViewHolder)v.getTag();
    if (csPullLeaveMessage != null) {
      String content = csPullLeaveMessage.getContent();
      if (!TextUtils.isEmpty(content)) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        String filter = v.getResources().getString(R.string.rc_cs_leave_message);
        int startPos = content.indexOf(filter);
        if (startPos >= 0) {
          SpannableString filterString = new SpannableString(content.substring(startPos, startPos + filter.length()));
          filterString.setSpan(new ForegroundColorSpan(v.getContext().getResources().getColor(R.color.rc_voice_color)), 0, filterString.length(), 33);
          filterString.setSpan(new io.rong.imkit.widget.provider.CSPullLeaveMsgItemProvider.Clickable(new OnClickListener() {
            public void onClick(View v) {
              io.rong.imkit.widget.provider.CSPullLeaveMsgItemProvider.this.onLeaveMessageClicked(v, message);
            }
          }), 0, filterString.length(), 17);
          String preText = content.substring(0, startPos);
          String endText = content.substring(startPos + filter.length());
          if (!preText.endsWith(" ")) {
            builder.append(preText).append(" ").append(filterString).append(endText);
          } else {
            builder.append(preText).append(filterString).append(endText);
          }
        } else {
          builder.append(content);
        }

        viewHolder.contentTextView.setText(builder);
      }
    }

  }

  private void onLeaveMessageClicked(View v, UIMessage message) {
    Intent intent;
    if (message.getCsConfig() != null && message.getCsConfig().leaveMessageConfigType.equals(CSLeaveMessageType.WEB)) {
      String action = "io.io.rong.imkit.intent.action.webview";
      intent = new Intent(action);
      intent.setPackage(v.getContext().getPackageName());
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      if (message.getCsConfig().uri != null) {
        intent.putExtra("url", message.getCsConfig().uri.toString());
      }

      v.getContext().startActivity(intent);
    } else if (message.getCsConfig() != null) {
      intent = new Intent(v.getContext(), CSLeaveMessageActivity.class);
      intent.putExtra("targetId", message.getTargetId());
      Bundle bundle = new Bundle();
      bundle.putParcelableArrayList("itemList", message.getCsConfig().leaveMessageNativeInfo);
      intent.putExtras(bundle);
      v.getContext().startActivity(intent);
    }

  }

  public Spannable getContentSummary(CSPullLeaveMessage data) {
    return null;
  }

  public void onItemClick(View view, int position, CSPullLeaveMessage content, UIMessage message) {
  }

  public void onItemLongClick(View view, int position, CSPullLeaveMessage content, UIMessage message) {
  }

  public View newView(Context context, ViewGroup group) {
    View view = LayoutInflater.from(context).inflate(R.layout.rc_item_information_notification_message, (ViewGroup)null);
    io.rong.imkit.widget.provider.CSPullLeaveMsgItemProvider.ViewHolder viewHolder = new io.rong.imkit.widget.provider.CSPullLeaveMsgItemProvider.ViewHolder();
    viewHolder.contentTextView = (TextView)view.findViewById(R.id.rc_msg);
    viewHolder.contentTextView.setMovementMethod(LinkMovementMethod.getInstance());
    view.setTag(viewHolder);
    return view;
  }

  class Clickable extends ClickableSpan implements OnClickListener {
    private final OnClickListener mListener;

    public Clickable(OnClickListener listener) {
      this.mListener = listener;
    }

    public void onClick(View view) {
      this.mListener.onClick(view);
    }
  }

  private static class ViewHolder {
    TextView contentTextView;

    private ViewHolder() {
    }
  }
}
