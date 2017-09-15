package com.wali.live.watchsdk.component.view.panel;

import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IOrientationListener;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.component.view.panel.BaseBottomPanel;
import com.wali.live.watchsdk.R;

/**
 * Created by yangli on 2017/09/14.
 * <p>
 * Generated using create_panel_with_presenter.py
 *
 * @module 主播-主播连麦信息面板视图
 */
public class LinkInfoPanel extends BaseBottomPanel<RelativeLayout, RelativeLayout>
        implements View.OnClickListener, IComponentView<LinkInfoPanel.IPresenter, LinkInfoPanel.IView> {
    private static final String TAG = "LinkInfoPanel";

    public static final float DEFAULT_RATIO = 0.1875f; // 主播-主播连麦时，距离上边界的比例

    @Nullable
    protected IPresenter mPresenter;

    private int mPortWidth = PANEL_WIDTH_LANDSCAPE;
    private float mPortTranslateY = 0;

    private int mLandWidth = PANEL_WIDTH_LANDSCAPE;
    private float mLandTranslateY = 0;

    private TextView mGuestNameView;
    private View mFollowBtn;
    private View mEnterRoomView;


    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.follow_btn) {
            // TODO 关注
        } else if (i == R.id.enter_room_view) {
            // TODO 进入房间
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.link_info_panel;
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    public void setLayoutRatio(float ratio) {
        int screenWidth = GlobalData.screenWidth, screenHeight = GlobalData.screenHeight;

        int height = screenWidth, width = height * 9 / 16;
        mLandWidth = width;
        mLandTranslateY = ratio * height;

        if (screenWidth * 16 >= screenHeight * 9) {
            height = screenHeight;
            width = height * 9 / 16;
            mPortWidth = width;
            mPortTranslateY = ratio * height;
        } else {
            width = screenWidth;
            height = width * 16 / 9;
            mPortWidth = width;
            mPortTranslateY = ratio * height + (screenHeight - height) / 2;
        }
    }

    public LinkInfoPanel(@NonNull RelativeLayout parentView) {
        super(parentView);
    }

    @Override
    protected void inflateContentView() {
        super.inflateContentView();

        mGuestNameView = $(R.id.guest_name_view);
        mFollowBtn = $(R.id.follow_btn);
        mEnterRoomView = $(R.id.enter_room_view);

        $click(mFollowBtn, this);
        $click(mEnterRoomView, this);
    }

    @Override
    protected void orientSelf() {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams)
                mContentView.getLayoutParams();
        if (mIsLandscape) {
            mEnterRoomView.setVisibility(View.GONE);
            layoutParams.width = mLandWidth;
            layoutParams.topMargin = (int) mLandTranslateY;
        } else {
            mEnterRoomView.setVisibility(View.VISIBLE);
            layoutParams.width = mPortWidth;
            layoutParams.topMargin = (int) mPortTranslateY;
        }
        mContentView.setLayoutParams(layoutParams);
    }

    @Override
    protected void onAnimationValue(@FloatRange(from = 0.0, to = 1.0) float value) {
        mContentView.setAlpha(value);
    }

    @Override
    public IView getViewProxy() {
        class ComponentView implements IView {
            @Override
            public <T extends View> T getRealView() {
                return (T) mContentView;
            }

            @Override
            public void onOrientation(boolean isLandscape) {
                LinkInfoPanel.this.onOrientation(isLandscape);
            }

            @Override
            public boolean isShow() {
                return LinkInfoPanel.this.isShow();
            }

            @Override
            public void showSelf(boolean useAnimation, boolean isLandscape) {
                LinkInfoPanel.this.showSelf(useAnimation, isLandscape);
            }

            @Override
            public void hideSelf(boolean useAnimation) {
                LinkInfoPanel.this.hideSelf(useAnimation);
            }

            @Override
            public void updateLinkUserInfo(long userId, String nickName) {
                mGuestNameView.setText(nickName);
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
    }

    public interface IView extends IViewProxy, IOrientationListener {

        boolean isShow();

        void showSelf(boolean useAnimation, boolean isLandscape);

        void hideSelf(boolean useAnimation);

        void updateLinkUserInfo(long userId, String nickName);
    }
}
