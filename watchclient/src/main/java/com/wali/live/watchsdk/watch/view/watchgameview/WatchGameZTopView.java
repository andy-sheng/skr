package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.base.utils.toast.ToastUtils;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.watchsdk.R;

/**
 * Created by vera on 2018/8/7.
 * 直播顶部浮层 包括返回按钮 分享 横屏时的关注按钮、下载等等
 * 内部处理横竖屏样式
 */

public class WatchGameZTopView extends RelativeLayout implements View.OnClickListener,
        IComponentView<WatchGameZTopView.IPresenter, WatchGameZTopView.IView>,PortraitLineUpButtons.OnPortraitButtonClickListener {

    @Nullable
    protected IPresenter mPresenter;
    private boolean mIsLandscape = false; // 是否是横屏

    // 竖屏下展示的控件
    private ImageView mPortraitBackBtn;
    private PortraitLineUpButtons mPortraitLinUpButtons;

    // 横屏下展示的控件

    public WatchGameZTopView(Context context) {
        super(context);
        initLayout(context);
    }

    public WatchGameZTopView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout(context);
    }

    private void initLayout(Context context) {
        if (mIsLandscape) {
            // 加载横屏
            inflate(context, R.layout.watch_z_top_lanscape_layout, this);
            bindLandscapeViews();
        } else {
            // 加载竖屏
            inflate(context, R.layout.watch_z_top_portrait_layout, this);
            bindPortraitViews();
        }
    }

    private void bindPortraitViews() {
        mPortraitBackBtn = (ImageView) findViewById(R.id.portrait_back_btn);
        mPortraitBackBtn.setOnClickListener(this);

        mPortraitLinUpButtons = (PortraitLineUpButtons) findViewById(R.id.portrait_line_up_buttons);
        // 分享
        mPortraitLinUpButtons.addButton(R.drawable.live_video_function_icon_share, R.id.game_watch_portrait_share);
        // 更多
        mPortraitLinUpButtons.addButton(R.drawable.live_video_function_icon_more, R.id.game_watch_portrait_more);
        // 暂停　播放
        mPortraitLinUpButtons.addButton(R.drawable.live_video_function_icon_suspended, R.id.game_watch_portrait_suspended);
        // 全屏
        mPortraitLinUpButtons.addButton(R.drawable.live_video_function_icon_fullscreen, R.id.game_watch_portrait_fullscreen);
        mPortraitLinUpButtons.setOnButtonClickListener(this);
    }

    private void bindLandscapeViews() {

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.portrait_back_btn) {
            ToastUtils.showToast("竖屏下点击返回键");
        }
    }

    @Override
    public void onPortraitButtonClick(View v) {
        int id = v.getId();
        if (id == R.id.game_watch_portrait_share) {
            ToastUtils.showToast("点击分享");
        } else if (id == R.id.game_watch_portrait_more) {
            ToastUtils.showToast("点击更多");
        } else if (id == R.id.game_watch_portrait_suspended) {
            if (v.isSelected()) {

            } else {

            }
            ToastUtils.showToast("点击暂停|播放");
        } else if (id == R.id.game_watch_portrait_fullscreen) {
            ToastUtils.showToast("点击全屏");
            mPresenter.forceRotate();
        }
    }

    @Override
    public IView getViewProxy() {
        return null;
    }

    @Override
    public void setPresenter(IPresenter iPresenter) {
        this.mPresenter = iPresenter;
    }


    public interface  IPresenter {
        void forceRotate();
    }

    public interface IView extends IViewProxy {

    }
}
