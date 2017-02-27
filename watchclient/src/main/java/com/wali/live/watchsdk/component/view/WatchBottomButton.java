package com.wali.live.watchsdk.component.view;

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
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.watchsdk.R;

import static com.wali.live.statistics.StatisticsKey.AC_APP;
import static com.wali.live.statistics.StatisticsKey.KEY;
import static com.wali.live.statistics.StatisticsKey.TIMES;

/**
 * Created by yangli on 16-8-29.
 *
 * @module 底部按钮视图, 游戏直播
 */
public class WatchBottomButton extends BaseBottomButton<WatchBottomButton.IPresenter, WatchBottomButton.IView> {
    private static final String TAG = "WatchBottomButton";

    protected View mCommentBtn;
    protected View mGiftBtn;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public WatchBottomButton(@NonNull RelativeLayout contentContainer) {
        super(contentContainer);
        initView();
    }

    protected void initView() {
        mCommentBtn = createImageView(R.drawable.live_icon_comment_btn);
        addCreatedView(mCommentBtn, R.id.comment_btn);

        mGiftBtn = createImageView(R.drawable.live_icon_gift_btn);
        addCreatedView(mGiftBtn, R.id.gift_btn);

        // 横竖屏时按钮排列顺序
        mLeftBtnSetPort.add(mCommentBtn);
        mRightBtnSetPort.add(mGiftBtn);

        mBottomBtnSetLand.add(mGiftBtn);
        mBottomBtnSetLand.add(mCommentBtn);

        orientChild(mIsLandscape);
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
        } else if (id == R.id.gift_btn) {
            mPresenter.showGiftView();
            // TODO 增加打点
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
                WatchBottomButton.this.onOrientation(isLandscape);
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
         * 显示输入界面
         */
        void showInputView();

        /**
         * 显示礼物界面
         */
        void showGiftView();
    }

    public interface IView extends IViewProxy, IOrientationListener {
    }
}
