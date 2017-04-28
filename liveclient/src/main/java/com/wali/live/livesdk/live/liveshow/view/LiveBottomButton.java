package com.wali.live.livesdk.live.liveshow.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import com.mi.live.data.account.HostChannelManager;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;
import com.wali.live.component.view.BaseBottomButton;
import com.wali.live.component.view.IOrientationListener;
import com.wali.live.component.view.IViewProxy;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.liveshow.view.button.MagicControlBtnView;
import com.wali.live.livesdk.live.liveshow.view.button.PlusControlBtnView;
import com.wali.live.statistics.StatisticsKey;

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

    @Override
    protected String getTAG() {
        return TAG;
    }

    public LiveBottomButton(@NonNull RelativeLayout contentContainer) {
        super(contentContainer);
        initView();
    }

    protected void initView() {
        mPlusBtn = new PlusControlBtnView(getContext());
        addCreatedView(mPlusBtn, R.id.plus_btn);

        mMagicBtn = new MagicControlBtnView(getContext());
        addCreatedView(mMagicBtn, R.id.magic_btn);

        mSettingBtn = createImageView(R.drawable.live_icon_set_btn);
        addCreatedView(mSettingBtn, R.id.setting_btn);

        mShareBtn = createImageView(R.drawable.live_icon_share_btn);
        addCreatedView(mShareBtn, R.id.share_btn);

        // 横竖屏时按钮排列顺序
        mLeftBtnSetPort.add(mPlusBtn);

        mRightBtnSetPort.add(mShareBtn);
        mRightBtnSetPort.add(mSettingBtn);
        mRightBtnSetPort.add(mMagicBtn);

        mBottomBtnSetLand.add(mPlusBtn);
        mBottomBtnSetLand.add(mSettingBtn);
        mBottomBtnSetLand.add(mMagicBtn);
        mBottomBtnSetLand.add(mShareBtn);

        orientChild();
    }

    @Override
    public void onClick(View view) {
        if (mPresenter == null) {
            return;
        }
        int id = view.getId();
        String msgType = "";
        if (id == R.id.plus_btn) {
            mPresenter.showPlusPanel();
            // TODO 增加打点
        } else if (id == R.id.setting_btn) {
            mPresenter.showSettingPanel();
            msgType = StatisticsKey.KEY_LIVESDK_PLUG_FLOW_CLICK_SET;
        } else if (id == R.id.magic_btn) {
            mPresenter.showMagicPanel();
            // TODO 增加打点
        } else if (id == R.id.share_btn) {
            mPresenter.showShareView();
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
    }

    public interface IView extends IViewProxy, IOrientationListener {
    }
}
