//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.fragment;

import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import io.rong.common.RLog;
import io.rong.imkit.R;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.Event.ConversationNotificationEvent;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.RongIMClient.ResultCallback;
import io.rong.imlib.model.Conversation.ConversationNotificationStatus;

public class SetConversationNotificationFragment extends BaseSettingFragment {
  private static final String TAG = "SetConversationNotificationFragment";

  public SetConversationNotificationFragment() {
  }

  public static io.rong.imkit.fragment.SetConversationNotificationFragment newInstance() {
    return new io.rong.imkit.fragment.SetConversationNotificationFragment();
  }

  protected void initData() {
    if (RongContext.getInstance() != null) {
      RongContext.getInstance().getEventBus().register(this);
    }

    RongIM.getInstance().getConversationNotificationStatus(this.getConversationType(), this.getTargetId(), new ResultCallback<ConversationNotificationStatus>() {
      public void onSuccess(ConversationNotificationStatus notificationStatus) {
        if (notificationStatus != null) {
          io.rong.imkit.fragment.SetConversationNotificationFragment.this.setSwitchBtnStatus(notificationStatus != ConversationNotificationStatus.DO_NOT_DISTURB);
        }

      }

      public void onError(ErrorCode errorCode) {
        io.rong.imkit.fragment.SetConversationNotificationFragment.this.setSwitchBtnStatus(!io.rong.imkit.fragment.SetConversationNotificationFragment.this.getSwitchBtnStatus());
      }
    });
  }

  protected boolean setSwitchButtonEnabled() {
    return true;
  }

  protected String setTitle() {
    return this.getString(R.string.rc_setting_conversation_notify);
  }

  public boolean handleMessage(Message msg) {
    return false;
  }

  protected void onSettingItemClick(View v) {
    RLog.i("SetConversationNotificationFragment", "onSettingItemClick, " + v.toString());
  }

  protected int setSwitchBtnVisibility() {
    return 0;
  }

  protected void toggleSwitch(boolean toggle) {
    ConversationNotificationStatus status;
    if (toggle) {
      status = ConversationNotificationStatus.NOTIFY;
    } else {
      status = ConversationNotificationStatus.DO_NOT_DISTURB;
    }

    if (this.getConversationType() != null && !TextUtils.isEmpty(this.getTargetId())) {
      RongIM.getInstance().setConversationNotificationStatus(this.getConversationType(), this.getTargetId(), status, new ResultCallback<ConversationNotificationStatus>() {
        public void onSuccess(ConversationNotificationStatus status) {
          RLog.i("SetConversationNotificationFragment", "SetConversationNotificationFragment onSuccess--");
        }

        public void onError(ErrorCode errorCode) {
          io.rong.imkit.fragment.SetConversationNotificationFragment.this.setSwitchBtnStatus(!io.rong.imkit.fragment.SetConversationNotificationFragment.this.getSwitchBtnStatus());
        }
      });
    } else {
      RLog.e("SetConversationNotificationFragment", "SetConversationNotificationFragment Arguments is null");
    }

  }

  public void onEventMainThread(ConversationNotificationEvent event) {
    if (event != null && event.getTargetId().equals(this.getTargetId()) && event.getConversationType().getValue() == this.getConversationType().getValue()) {
      this.setSwitchBtnStatus(event.getStatus() == ConversationNotificationStatus.NOTIFY);
    }

  }

  public void onDestroy() {
    if (RongContext.getInstance() != null) {
      RongContext.getInstance().getEventBus().unregister(this);
    }

    super.onDestroy();
  }
}
