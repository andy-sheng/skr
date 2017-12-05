package com.wali.live.livesdk.live.liveshow.view;

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
import com.wali.live.livesdk.live.liveshow.view.button.MagicControlBtnView;
import com.wali.live.livesdk.live.liveshow.view.button.PlusControlBtnView;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.watchsdk.auth.AccountAuthManager;
import com.wali.live.watchsdk.view.MsgCtrlBtnView;

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

    protected View mPlusBtn;
    protected View mMagicBtn;
    protected View mSettingBtn;
    protected View mShareBtn;
    private MsgCtrlBtnView mMsgCntBtn;
    private View mFansBtn;
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
        mPlusBtn = new PlusControlBtnView(getContext());
        addCreatedView(mPlusBtn, R.id.plus_btn);

        mMagicBtn = new MagicControlBtnView(getContext());
        addCreatedView(mMagicBtn, R.id.magic_btn);

        mSettingBtn = createImageView(R.drawable.live_icon_set_btn);
        addCreatedView(mSettingBtn, R.id.setting_btn);

        mMsgCntBtn = new MsgCtrlBtnView(getContext());
        addCreatedView(mMsgCntBtn, R.id.msg_ctrl_btn);

        mFansBtn = createImageView(R.drawable.game_live_icon_pet);
        addCreatedView(mFansBtn, R.id.vip_fans_btn);
        mFansBtn.setVisibility(View.GONE);

        // 横竖屏时按钮排列顺序
        mLeftBtnSetPort.add(mPlusBtn);
        mRightBtnSetPort.add(mSettingBtn);
        mRightBtnSetPort.add(mMagicBtn);
        mRightBtnSetPort.add(mMsgCntBtn);
        mRightBtnSetPort.add(mFansBtn);

        mBottomBtnSetLand.add(mPlusBtn);
        mBottomBtnSetLand.add(mSettingBtn);
        mBottomBtnSetLand.add(mMagicBtn);
        mBottomBtnSetLand.add(mMsgCntBtn);
        mBottomBtnSetLand.add(mFansBtn);

        addShareBtn();

        orientChild();
    }

    private void addShareBtn() {
        if (mEnableShare) {
            mShareBtn = createImageView(R.drawable.live_icon_share_btn);
            addCreatedView(mShareBtn, R.id.share_btn);

            mRightBtnSetPort.add(0, mShareBtn);
            mBottomBtnSetLand.add(mShareBtn);
        }
    }

    @Override
    public void onClick(View view) {
        if (mPresenter == null) {
            return;
        }
        int id = view.getId();
        String msgType = "";
        if (id == R.id.plus_btn) {
            mPresenter.showPlusPanel(); // TODO 增加打点
        } else if (id == R.id.setting_btn) {
            mPresenter.showSettingPanel();
            msgType = StatisticsKey.KEY_LIVESDK_PLUG_FLOW_CLICK_SET;
        } else if (id == R.id.magic_btn) {
            mPresenter.showMagicPanel(); // TODO 增加打点
        } else if (id == R.id.share_btn) {
            mPresenter.showShareView();
        } else if (id == R.id.msg_ctrl_btn) {
            mPresenter.showMsgCtrlView();
        } else if (id == R.id.vip_fans_btn) {
            mPresenter.showVipFansView();
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
            public <T extends View> T getRealView() {
                return (T) mContentContainer;
            }

            @Override
            public void onOrientation(boolean isLandscape) {
                LiveBottomButton.this.onOrientation(isLandscape);
            }

            @Override
            public void onUpdateUnreadCount(int unreadCount) {
                mMsgCntBtn.setMsgUnreadCnt(unreadCount);
            }

            @Override
            public void showFansIcon() {
                mFansBtn.setVisibility(View.VISIBLE);
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
        /**
         * 显示直播加面板
         */
        void showPlusPanel();

        /**
         * 显示设置面板
         */
        void showSettingPanel();

        /**
         * 显示美妆面板
         */
        void showMagicPanel();

        /**
         * 显示分享面板
         */
        void showShareView();

        /**
         * 显示私信面板
         */
        void showMsgCtrlView();

        /**
         * 显示粉丝团管理界面
         */
        void showVipFansView();
    }

    public interface IView extends IViewProxy, IOrientationListener {
        /**
         * 更新私信未读数
         */
        void onUpdateUnreadCount(int unreadCount);

        /**
         * 显示粉丝团入口按钮
         */
        void showFansIcon();
    }
}
