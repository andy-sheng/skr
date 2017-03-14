package com.wali.live.livesdk.live.livegame.view.panel;

import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.HostChannelManager;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;
import com.wali.live.component.view.panel.BaseBottomPanel;
import com.wali.live.livesdk.R;
import com.wali.live.statistics.StatisticsKey;

import static com.wali.live.statistics.StatisticsKey.AC_APP;
import static com.wali.live.statistics.StatisticsKey.KEY;
import static com.wali.live.statistics.StatisticsKey.TIMES;

/**
 * Created by yangli on 17-2-14.
 *
 * @module 设置面板
 */
public class GameSettingPanel extends BaseBottomPanel<LinearLayout, RelativeLayout>
        implements View.OnClickListener {
    public static final boolean DEFAULT_FORBID_GIFT = false;
    public static final boolean DEFAULT_FORBID_SYS = false;
    public static final boolean DEFAULT_FORBID_CHAT = false;

    @Nullable
    private LiveRoomChatMsgManager mLiveRoomChatMsgManager;

    private View mForbidGiftTv;
    private View mForbidSystemTv;
    private View mForbidChatTv;

    public GameSettingPanel(@NonNull RelativeLayout parentView,
                            @NonNull LiveRoomChatMsgManager liveRoomChatMsgManager) {
        super(parentView);
        mLiveRoomChatMsgManager = liveRoomChatMsgManager;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.game_setting_control_panel;
    }

    @Override
    protected void inflateContentView() {
        super.inflateContentView();
        mForbidGiftTv = $(R.id.forbid_gift_tv);
        mForbidSystemTv = $(R.id.forbid_system_tv);
        mForbidChatTv = $(R.id.forbid_chat_tv);

        if (mLiveRoomChatMsgManager != null) {
            mForbidGiftTv.setSelected(!mLiveRoomChatMsgManager.isHideGiftMsg());
            mForbidSystemTv.setSelected(!mLiveRoomChatMsgManager.isHideSysMsg());
            mForbidChatTv.setSelected(!mLiveRoomChatMsgManager.isHideChatMsg());
        } else {
            mForbidGiftTv.setSelected(!LiveRoomChatMsgManager.DEFAULT_FORBID_GIFT);
            mForbidSystemTv.setSelected(!LiveRoomChatMsgManager.DEFAULT_FORBID_SYS);
            mForbidChatTv.setSelected(!LiveRoomChatMsgManager.DEFAULT_FORBID_CHAT);
        }

        mForbidGiftTv.setOnClickListener(this);
        mForbidSystemTv.setOnClickListener(this);
        mForbidChatTv.setOnClickListener(this);
    }

    @Override
    public void onOrientation(boolean isLandscape) {
        super.onOrientation(isLandscape);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mContentView.getLayoutParams();
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        if (mIsLandscape) {
            layoutParams.width = PANEL_WIDTH_LANDSCAPE;
        } else {
            layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        }
    }

    @Override
    protected void onAnimationStart() {
        super.onAnimationStart();
        if (mIsShow) {
            mContentView.setTranslationX(0);
            mContentView.setTranslationY(mContentView.getHeight());
        }
    }

    @Override
    protected void onAnimationValue(@FloatRange(from = 0.0, to = 1.0) float value) {
        mContentView.setAlpha(value);
        mContentView.setTranslationX(0);
        mContentView.setTranslationY(mContentView.getHeight() * (1.0f - value));
    }

    @Override
    public void onClick(View v) {
        if (mLiveRoomChatMsgManager == null) {
            return;
        }
        int id = v.getId();
        boolean isSelected = !v.isSelected();
        v.setSelected(isSelected);
        int msgType = 0;
        if (id == R.id.forbid_gift_tv) {
            mLiveRoomChatMsgManager.setHideGiftMsg(!isSelected);
            ToastUtils.showToast(isSelected ? R.string.game_cancel_forbid_gift : R.string.game_forbid_gift_toast);
            msgType = StatisticsKey.KEY_LIVESDK_MSG_GIFT;
        } else if (id == R.id.forbid_system_tv) {
            mLiveRoomChatMsgManager.setHideSysMsg(!isSelected);
            ToastUtils.showToast(isSelected ? R.string.game_cancel_forbid_system : R.string.game_forbid_system_toast);
            msgType = StatisticsKey.KEY_LIVESDK_MSG_SYSTEM;
        } else if (id == R.id.forbid_chat_tv) {
            mLiveRoomChatMsgManager.setHideChatMsg(!isSelected);
            ToastUtils.showToast(isSelected ? R.string.game_cancel_forbid_chat : R.string.game_forbid_chat_toast);
            msgType = StatisticsKey.KEY_LIVESDK_MSG_CHAT;
        }
        if (msgType > 0) {
            StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                    String.format(StatisticsKey.KEY_LIVESDK_PLUG_FLOW_CLICK_SET_SPECIFIC, msgType, HostChannelManager.getInstance().getChannelId()),
                    TIMES, "1");
        }
    }

}
