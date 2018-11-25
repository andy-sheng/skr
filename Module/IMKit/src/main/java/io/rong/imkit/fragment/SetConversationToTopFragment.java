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
import io.rong.imkit.model.Event.ConversationTopEvent;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.RongIMClient.ResultCallback;
import io.rong.imlib.model.Conversation;

public class SetConversationToTopFragment extends BaseSettingFragment {
  private static String TAG = io.rong.imkit.fragment.SetConversationToTopFragment.class.getSimpleName();

  public SetConversationToTopFragment() {
  }

  protected void initData() {
    if (RongContext.getInstance() != null) {
      RongContext.getInstance().getEventBus().register(this);
    }

    RongIM.getInstance().getConversation(this.getConversationType(), this.getTargetId(), new ResultCallback<Conversation>() {
      public void onSuccess(Conversation conversation) {
        if (conversation != null) {
          io.rong.imkit.fragment.SetConversationToTopFragment.this.setSwitchBtnStatus(conversation.isTop());
        }

      }

      public void onError(ErrorCode e) {
      }
    });
  }

  protected boolean setSwitchButtonEnabled() {
    return true;
  }

  protected String setTitle() {
    return this.getString(R.string.rc_setting_set_top);
  }

  public boolean handleMessage(Message msg) {
    return false;
  }

  protected void onSettingItemClick(View v) {
    RLog.i(TAG, "onSettingItemClick, " + v.toString());
  }

  protected int setSwitchBtnVisibility() {
    return 0;
  }

  protected void toggleSwitch(boolean toggle) {
    if (this.getConversationType() != null && !TextUtils.isEmpty(this.getTargetId())) {
      RongIM.getInstance().setConversationToTop(this.getConversationType(), this.getTargetId(), toggle, (ResultCallback)null);
    } else {
      RLog.e(TAG, "toggleSwitch() args is null");
    }

  }

  public void onEventMainThread(ConversationTopEvent conversationTopEvent) {
    if (conversationTopEvent != null && conversationTopEvent.getTargetId().equals(this.getTargetId()) && conversationTopEvent.getConversationType().getValue() == this.getConversationType().getValue()) {
      this.setSwitchBtnStatus(conversationTopEvent.isTop());
    }

  }

  public void onDestroy() {
    if (RongContext.getInstance() != null) {
      RongContext.getInstance().getEventBus().unregister(this);
    }

    super.onDestroy();
  }
}
