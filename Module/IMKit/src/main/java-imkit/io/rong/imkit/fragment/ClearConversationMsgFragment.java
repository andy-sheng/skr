//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.fragment;

import android.opengl.Visibility;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import io.rong.imkit.R;
import io.rong.imkit.RongIM;
import io.rong.imkit.widget.AlterDialogFragment;
import io.rong.imkit.widget.AlterDialogFragment.AlterDialogBtnListener;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.RongIMClient.ResultCallback;
import io.rong.imlib.model.Conversation;

public class ClearConversationMsgFragment extends BaseSettingFragment implements AlterDialogBtnListener {
    private Conversation conversation;

    public ClearConversationMsgFragment() {
    }

    protected void initData() {
    }

    protected String setTitle() {
        return this.getString(R.string.rc_setting_clear_msg_name);
    }

    protected boolean setSwitchButtonEnabled() {
        return false;
    }

    protected int setSwitchBtnVisibility() {
        return View.GONE;
    }

    protected void onSettingItemClick(View v) {
        this.conversation = new Conversation();
        this.conversation.setConversationType(this.getConversationType());
        this.conversation.setTargetId(this.getTargetId());
        AlterDialogFragment dialogFragment = AlterDialogFragment.newInstance(this.getString(R.string.rc_setting_name), this.getString(R.string.rc_setting_clear_msg_prompt), this.getString(R.string.rc_dialog_cancel), this.getString(R.string.rc_dialog_ok));
        dialogFragment.setOnAlterDialogBtnListener(this);
        dialogFragment.show(this.getFragmentManager());
    }

    public void onDialogNegativeClick(AlterDialogFragment dialog) {
        dialog.dismiss();
    }

    public void onDialogPositiveClick(AlterDialogFragment dialog) {
        if (this.conversation != null) {
            RongIM.getInstance().clearMessages(this.conversation.getConversationType(), this.conversation.getTargetId(), new ResultCallback<Boolean>() {
                public void onSuccess(Boolean aBoolean) {
                    Toast.makeText(ClearConversationMsgFragment.this.getActivity(), io.rong.imkit.fragment.ClearConversationMsgFragment.this.getString(R.string.rc_setting_clear_msg_success), Toast.LENGTH_SHORT).show();
                }

                public void onError(ErrorCode e) {
                    Toast.makeText(ClearConversationMsgFragment.this.getActivity(), io.rong.imkit.fragment.ClearConversationMsgFragment.this.getString(R.string.rc_setting_clear_msg_fail), Toast.LENGTH_SHORT).show();
                }
            });
            RongIM.getInstance().clearTextMessageDraft(this.conversation.getConversationType(), this.conversation.getTargetId(), null);
        }
    }

    protected void toggleSwitch(boolean toggle) {
    }

    public boolean handleMessage(Message msg) {
        return false;
    }
}
