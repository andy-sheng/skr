//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget.provider;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.rong.imkit.R;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imkit.widget.provider.IContainerItemProvider.MessageProvider;
import io.rong.imlib.model.UserInfo;
import io.rong.message.RecallNotificationMessage;

@ProviderTag(
        messageContent = RecallNotificationMessage.class,
        showPortrait = false,
        showProgress = false,
        showWarning = false,
        centerInHorizontal = true,
        showSummaryWithName = false
)
public class RecallMessageItemProvider extends MessageProvider<RecallNotificationMessage> {
  public RecallMessageItemProvider() {
  }

  public void onItemClick(View view, int position, RecallNotificationMessage content, UIMessage message) {
  }

  public void bindView(View v, int position, RecallNotificationMessage content, UIMessage message) {
    io.rong.imkit.widget.provider.RecallMessageItemProvider.ViewHolder viewHolder = (io.rong.imkit.widget.provider.RecallMessageItemProvider.ViewHolder)v.getTag();
    if (content != null && message != null) {
      String information;
      if (content.getOperatorId().equals(RongIM.getInstance().getCurrentUserId())) {
        information = RongContext.getInstance().getString(R.string.rc_you_recalled_a_message);
      } else {
        UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(content.getOperatorId());
        if (userInfo != null && userInfo.getName() != null) {
          information = userInfo.getName() + RongContext.getInstance().getString(R.string.rc_recalled_a_message);
        } else {
          information = content.getOperatorId() + RongContext.getInstance().getString(R.string.rc_recalled_a_message);
        }
      }

      viewHolder.contentTextView.setText(information);
    }

  }

  public void onItemLongClick(View view, int position, RecallNotificationMessage content, UIMessage message) {
  }

  public Spannable getContentSummary(RecallNotificationMessage data) {
    return null;
  }

  public Spannable getContentSummary(Context context, RecallNotificationMessage data) {
    if (data != null && !TextUtils.isEmpty(data.getOperatorId())) {
      String information;
      if (data.getOperatorId().equals(RongIM.getInstance().getCurrentUserId())) {
        information = context.getString(R.string.rc_you_recalled_a_message);
      } else {
        UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(data.getOperatorId());
        if (userInfo != null && userInfo.getName() != null) {
          information = userInfo.getName() + context.getString(R.string.rc_recalled_a_message);
        } else {
          information = data.getOperatorId() + context.getString(R.string.rc_recalled_a_message);
        }
      }

      return new SpannableString(information);
    } else {
      return null;
    }
  }

  public View newView(Context context, ViewGroup group) {
    View view = LayoutInflater.from(context).inflate(R.layout.rc_item_information_notification_message, (ViewGroup)null);
    io.rong.imkit.widget.provider.RecallMessageItemProvider.ViewHolder viewHolder = new io.rong.imkit.widget.provider.RecallMessageItemProvider.ViewHolder();
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
