package com.wali.live.livesdk.live.view.bottom;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.mi.live.data.account.HostChannelManager;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;
import com.wali.live.livesdk.R;
import com.wali.live.statistics.StatisticsKey;

import static com.wali.live.statistics.StatisticsKey.AC_APP;
import static com.wali.live.statistics.StatisticsKey.KEY;
import static com.wali.live.statistics.StatisticsKey.TIMES;

/**
 * Created by yangli on 16-8-29.
 *
 * @module 底部面板
 */
public class GameBottomButton extends BaseBottomButton<GameBottomButton.IStatusListener> {
    private static final String TAG = "GameBottomButton";

    protected View mCommentBtn;
    protected View mSettingBtn;
    protected View mMuteBtn;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public GameBottomButton(@NonNull RelativeLayout contentContainer) {
        super(contentContainer);
        initView();
    }

    protected void initView() {
        mCommentBtn = createImageView(R.drawable.live_icon_comment_btn);
        addCreatedView(mCommentBtn, R.id.comment_btn);

        mSettingBtn = createImageView(R.drawable.live_icon_set_btn);
        addCreatedView(mSettingBtn, R.id.setting_btn);

        mMuteBtn = createImageView(R.drawable.live_icon_mute_btn);
        addCreatedView(mMuteBtn, R.id.mute_btn);

        // 横竖屏时按钮排列顺序
        mRightBtnSetPort.add(mMuteBtn);
        mRightBtnSetPort.add(mSettingBtn);
        mRightBtnSetPort.add(mCommentBtn);

        orientChild(mIsLandscape);
    }

    public void updateMuteAudio(boolean isMute) {
        mMuteBtn.setSelected(isMute);
    }

    @Override
    protected void orientSelf(boolean isLandscape) {
        // 无论横竖屏，都在右下角，采用竖屏位置参数
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mContentContainer.getLayoutParams();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
    }

    @Override
    public void onClick(View view) {
        if (mStatusListener == null) {
            return;
        }
        int id = view.getId();
        String msgType = "";
        if (id == R.id.comment_btn) {
            mStatusListener.showInputView();
            msgType = StatisticsKey.KEY_LIVESDK_PLUG_FLOW_CLICK_SENDMESSAGE;
        } else if (id == R.id.setting_btn) {
            mStatusListener.showSettingPanel();
            msgType = StatisticsKey.KEY_LIVESDK_PLUG_FLOW_CLICK_SET;
        } else if (id == R.id.mute_btn) {
            boolean isSelected = !view.isSelected();
            view.setSelected(isSelected);
            mStatusListener.muteAudio(isSelected);
            msgType = StatisticsKey.KEY_LIVESDK_PLUG_FLOW_CLICK_SILENT;
        }
        if (!TextUtils.isEmpty(msgType)) {
            StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                    String.format(msgType, HostChannelManager.getInstance().getmCurrentChannelId()),
                    TIMES, "1");
        }
    }

    public interface IStatusListener {
        void showInputView();

        void showSettingPanel();

        void muteAudio(boolean isMute);
    }
}
