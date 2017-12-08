package com.wali.live.watchsdk.component.view.panel;

import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.utils.display.DisplayUtils;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.component.view.panel.BaseBottomPanel;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.view.WatchMenuItemView;

import java.util.ArrayList;
import java.util.List;

import static android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM;
import static android.widget.RelativeLayout.ALIGN_PARENT_RIGHT;
import static android.widget.RelativeLayout.BELOW;
import static android.widget.RelativeLayout.CENTER_VERTICAL;
import static android.widget.RelativeLayout.RIGHT_OF;
import static com.mi.milink.sdk.base.Global.getContext;

/**
 * Created by zyh on 2017/12/07.
 *
 * @module 观看端底部更多面板
 */
public class WatchMenuPanel extends BaseBottomPanel<RelativeLayout, RelativeLayout>
        implements View.OnClickListener, IComponentView<WatchMenuPanel.IPresenter, WatchMenuPanel.IView> {
    private static final String TAG = "WatchMenuPanel";
    private final static int RIGHT_MARGIN = DisplayUtils.dip2px(46.66f);

    @Nullable
    protected IPresenter mPresenter;

    protected final List<View> mBottomBtnSet = new ArrayList<>();
    private int mUnReadCnt = 0;
    private boolean mEnableShare = false;
    private boolean mEnableFans = false;

    private WatchMenuItemView mShareIv;
    private WatchMenuItemView mMsgIv;
    private WatchMenuItemView mFansIv;

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.share_btn) {
            mPresenter.showShareView();
        } else if (i == R.id.msg_ctrl_btn) {
            mPresenter.showMsgCtrlView();
        } else if (i == R.id.vip_fans_btn) {
            mPresenter.showVipFansView();
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.watch_menu_layout;
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    public WatchMenuPanel(@NonNull RelativeLayout parentView,
                          int unReadCnt,
                          boolean enableShare) {
        super(parentView);
        inflateContentView();
        mUnReadCnt = unReadCnt;
        mEnableShare = enableShare;
        initView();
    }

    private void setBackgroundResource() {
        mContentView.setBackgroundResource(mIsLandscape ? R.drawable.live_more_bg_horizontal :
                R.drawable.live_more_bg_vertical);
    }

    private void initView() {
        mMsgIv = createImageView(R.drawable.live_icon_msg_btn, R.string.private_message, R.id.msg_ctrl_btn);
        mMsgIv.setUnread(mUnReadCnt);
        mBottomBtnSet.add(mMsgIv);
        if (mEnableShare) {
            mShareIv = createImageView(R.drawable.live_menu_share_btn, R.string.watch_share_btn, R.id.share_btn);
            mBottomBtnSet.add(mShareIv);
        }
        if (mEnableFans) {
            mFansIv = createImageView(R.drawable.menu_live_icon_pet, R.string.vfan_me, R.id.vip_fans_btn);
            mBottomBtnSet.add(mFansIv);
        }
        orientChild();
        orientSelf();
    }

    protected final WatchMenuItemView createImageView(@DrawableRes int drawableResId, @StringRes int stringResId, @IdRes int id) {
        WatchMenuItemView imageView = new WatchMenuItemView(getContext());
        imageView.setImageResource(drawableResId);
        imageView.setText(stringResId);
        imageView.setId(id);
        return imageView;
    }

    private final void orientChild() {
        int size = mBottomBtnSet.size();
        for (int i = 0; i < size; i++) {
            addView((WatchMenuItemView) mBottomBtnSet.get(i), i);
        }
    }

    private final void addView(WatchMenuItemView view, int index) {
        if (view == null) {
            return;
        }
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(DisplayUtils.getScreenWidth() >
                DisplayUtils.getScreenHeight() ? DisplayUtils.getScreenHeight() / 4 :
                DisplayUtils.getScreenWidth() / 4, view.getItemHeight());
        layoutParams.alignWithParent = true;
        if (index == 0) {
        } else if (index > 0 && index < 4) {
            layoutParams.addRule(RIGHT_OF, (mBottomBtnSet.get(index - 1)).getId());
        } else if (index == 4) {
            layoutParams.addRule(BELOW, (mBottomBtnSet.get(index - 4)).getId());
        } else if (index > 4 && index < 8) {
            layoutParams.addRule(RIGHT_OF, (mBottomBtnSet.get(index - 1)).getId());
            layoutParams.addRule(BELOW, (mBottomBtnSet.get(index - 4)).getId());
        }
        mContentView.addView(view, layoutParams);
        view.setOnClickListener(this);
    }

    @Override
    public void showSelf(boolean useAnimation, boolean isLandscape) {
        boolean needStart = !mIsShow;
        super.showSelf(useAnimation, isLandscape);
        if (needStart) {
            mPresenter.startPresenter();
        }
    }

    @Override
    public void hideSelf(boolean useAnimation) {
        boolean needStop = mIsShow;
        super.hideSelf(useAnimation);
        if (needStop) {
            mPresenter.stopPresenter();
        }
    }

    @Override
    protected void orientSelf() {
        super.orientSelf();
        setBackgroundResource();
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mContentView.getLayoutParams();
        if (mIsLandscape) {
            layoutParams.removeRule(ALIGN_PARENT_BOTTOM);
            layoutParams.addRule(ALIGN_PARENT_RIGHT);
            layoutParams.addRule(CENTER_VERTICAL);
            layoutParams.rightMargin = RIGHT_MARGIN;
        } else {
            layoutParams.removeRule(ALIGN_PARENT_RIGHT);
            layoutParams.removeRule(CENTER_VERTICAL);
            layoutParams.addRule(ALIGN_PARENT_BOTTOM);
            layoutParams.rightMargin = 0;
        }
        mContentView.setLayoutParams(layoutParams);
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
                WatchMenuPanel.this.onOrientation(isLandscape);
            }

            @Override
            public void onUpdateUnreadCount(int unReadCnt) {
                mMsgIv.setUnread(unReadCnt);
            }

            @Override
            public void showFansIcon() {
                mEnableFans = true;
                if (mFansIv == null) {
                    mFansIv = createImageView(R.drawable.menu_live_icon_pet, R.string.vfan_me, R.id.vip_fans_btn);
                    mBottomBtnSet.add(mFansIv);
                    addView(mFansIv, mBottomBtnSet.size() - 1);
                }
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
        /**
         * 显示分享界面
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

        void startPresenter();

        void stopPresenter();
    }

    public interface IView extends IViewProxy {
        void onOrientation(boolean isLandscape);

        /**
         * 更新私信未读数
         */
        void onUpdateUnreadCount(int unReadCnt);

        /**
         * 顯示粉丝团按钮
         */
        void showFansIcon();
    }
}
