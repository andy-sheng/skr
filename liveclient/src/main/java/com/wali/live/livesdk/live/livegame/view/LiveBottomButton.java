package com.wali.live.livesdk.live.livegame.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import com.mi.live.data.account.channel.HostChannelManager;
import com.thornbirds.component.view.IOrientationListener;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;
import com.wali.live.component.view.BaseBottomButton;
import com.wali.live.livesdk.R;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.watchsdk.auth.AccountAuthManager;

import static com.wali.live.statistics.StatisticsKey.AC_APP;
import static com.wali.live.statistics.StatisticsKey.KEY;
import static com.wali.live.statistics.StatisticsKey.TIMES;

/**
 * Created by yangli on 16-8-29.
 *
 * @module 底部按钮视图, 游戏直播
 */
public class LiveBottomButton extends BaseBottomButton<LiveBottomButton.IPresenter, LiveBottomButton.IView> {
    private static final String TAG = "LiveBottomButton";

    protected View mCommentBtn;
    protected View mSettingBtn;
    protected View mMuteBtn;
    protected View mShareBtn;

    private boolean mEnableShare;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public LiveBottomButton(@NonNull RelativeLayout contentContainer, boolean enableShare) {
        super(contentContainer);
        mEnableShare = enableShare;
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

        addShareBtn();

        orientChild();
    }

    private void addShareBtn() {
        if (mEnableShare) {
            mShareBtn = createImageView(R.drawable.live_icon_share_btn);
            addCreatedView(mShareBtn, R.id.share_btn);

            mRightBtnSetPort.add(1, mShareBtn);
        }
    }

    public void updateMuteAudio(boolean isMute) {
        mMuteBtn.setSelected(isMute);
    }

    @Override
    protected void orientSelf() {
        // 无论横竖屏，都在右下角，采用竖屏位置参数
    }

    @Override
    public void onClick(View view) {
        if (mPresenter == null) {
            return;
        }
        int id = view.getId();
        String msgType = "";
        if (id == R.id.comment_btn) {
            mPresenter.showInputView();
            msgType = StatisticsKey.KEY_LIVESDK_PLUG_FLOW_CLICK_SENDMESSAGE;
        } else if (id == R.id.setting_btn) {
            mPresenter.showSettingPanel();
            msgType = StatisticsKey.KEY_LIVESDK_PLUG_FLOW_CLICK_SET;
        } else if (id == R.id.mute_btn) {
            boolean isSelected = !view.isSelected();
            view.setSelected(isSelected);
            mPresenter.muteAudio(isSelected);
            msgType = StatisticsKey.KEY_LIVESDK_PLUG_FLOW_CLICK_SILENT;
        } else if (id == R.id.share_btn) {
            if (AccountAuthManager.triggerActionNeedAccount(getContext())) {
                mPresenter.showShareView();
            }
        }
        if (!TextUtils.isEmpty(msgType)) {
            StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                    String.format(msgType, HostChannelManager.getInstance().getChannelId()),
                    TIMES, "1");
        }
    }

    @Override
    public IView getViewProxy() {
        /**
         * 局部内部类，用于Presenter回调通知该View改变状态
         */
        class ComponentView implements IView {
            @Override
            public void onOrientation(boolean isLandscape) {
                LiveBottomButton.this.onOrientation(isLandscape);
            }

            @Nullable
            @Override
            public <T extends View> T getRealView() {
                return (T) mContentContainer;
            }

            @Override
            public void updateMuteBtn(boolean isMute) {
                mMuteBtn.setSelected(isMute);
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
        /**
         * 显示输入界面
         */
        void showInputView();

        /**
         * 显示设置面板
         */
        void showSettingPanel();

        /**
         * 禁音/取消禁音
         */
        void muteAudio(boolean isMute);

        /**
         * 显示分享面板
         */
        void showShareView();

        /**
         * 获取share状态
         */
        boolean isEnableShare();
    }

    public interface IView extends IViewProxy, IOrientationListener {
        /**
         * 更新禁音按钮状态
         */
        void updateMuteBtn(boolean isMute);

    }
}
