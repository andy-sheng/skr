package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vera on 2018/8/7.
 * 直播顶部浮层 包括返回按钮 分享 横屏时的关注按钮、下载等等
 * 内部处理横竖屏样式
 */

public class WatchGameZTopView extends RelativeLayout implements View.OnClickListener,
        IComponentView<WatchGameZTopView.IPresenter, WatchGameZTopView.IView>,PortraitLineUpButtons.OnPortraitButtonClickListener {
    private final String TAG = getClass().getSimpleName();

    @Nullable
    protected IPresenter mPresenter;

    private boolean mIsLandscape = false; // 是否是横屏

    private List<View> mPortritViews; // 竖屏子View集合
    private List<View> mLandscapeViews; // 横屏子View集合

    // 竖屏下展示的控件 TODO 水印还没加上
    private ImageView mPortraitBackBtn;
    private PortraitLineUpButtons mPortraitLinUpButtons;

    // 横屏下展示的控件
    private RelativeLayout mLandscapeTopLayout; // 横屏下上半部分的布局
    private RelativeLayout mLandscapeBottomLayout; // 横屏下下半部分的布局
    private ImageView mLandscapeBackBtn;
    private TextView mLandscapeLiveTitle;
    private RelativeLayout mLandscapeAnchorLayout;
    private BaseImageView mLandscapeAnchorAvatar;
    private TextView mLandscapeAnchorNameTv;
    private TextView mLandscapeFollowBtn;
    private ImageView mLandscapeDownloadBtn;
    private ImageView mLandscapeShareBtn;
    private ImageView mLandscapeSuspend;
    private ImageView mLandscapeRefresh;
    private ImageView mLandscapeBarrageBtn;
    private ImageView getmLandscapeGiftBtn;

    public WatchGameZTopView(Context context) {
        super(context);
        setUpLayout(context, false);
    }

    public WatchGameZTopView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUpLayout(context, false);
    }

    /**
     * 根据横竖屏加载或切换不同布局
     * 仅当首次加载布局或者横竖屏切换时被调用
     * @param context
     * @param lastIsLandscape 切换前是横屏还是竖屏 首次加载传任意值都可
     */
    private void setUpLayout(Context context, boolean lastIsLandscape) {
        if (mIsLandscape) { // 切换到横屏
            if (getChildCount() > 0 && !lastIsLandscape) {
                // 切换前是竖屏
                if (mPortritViews == null) {
                    mPortritViews = new ArrayList<>();
                } else {
                    mPortritViews.clear();
                }
                // 将切换前竖屏的View保存起来
                for (int i = 0; i < getChildCount(); i ++) {
                    mPortritViews.add(getChildAt(i));
                }
            }
            // 清空当前布局上所有的子View
            removeAllViews();

            if (mLandscapeViews == null) {
                // 还没有加载过横屏布局 先加载
                inflate(context, R.layout.watch_z_top_lanscape_layout, this);
                bindLandscapeViews();
            } else {
                // 加载过横屏布局 重新add
                for (View view: mLandscapeViews) {
                    addView(view);
                }
            }
        } else { // 切换到竖屏
            if (getChildCount() > 0 && lastIsLandscape) {
                // 切换前是横屏
                if (mLandscapeViews == null) {
                    mLandscapeViews = new ArrayList<>();
                } else {
                    mLandscapeViews.clear();
                }
                // 将切换前横屏的View保存起来
                for (int i = 0; i < getChildCount(); i ++) {
                    mLandscapeViews.add(getChildAt(i));
                }
            }
            // 清空当前布局上所有的子View
            removeAllViews();

            if (mPortritViews == null) {
                // 还没有加载过竖屏布局 先加载
                inflate(context, R.layout.watch_z_top_portrait_layout, this);
                bindPortraitViews();
            } else {
                // 加载过竖屏布局 重新add
                for (View view: mPortritViews) {
                    addView(view);
                }
            }
        }
    }

    /**
     * 竖屏首次加载时bindView
     */
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

    /**
     * 横屏首次加载时bindView
     */
    private void bindLandscapeViews() {
        mLandscapeTopLayout = (RelativeLayout) findViewById(R.id.landscape_top_layout);
        mLandscapeBottomLayout = (RelativeLayout) findViewById(R.id.landscape_bottom_layout);

        mLandscapeBackBtn = (ImageView) findViewById(R.id.landscape_back_btn);
        mLandscapeBackBtn.setOnClickListener(this);

        mLandscapeLiveTitle = (TextView) findViewById(R.id.landscape_live_title);

        // 主播相关
        mLandscapeAnchorLayout = (RelativeLayout) findViewById(R.id.landscape_anchor_layout);
        mLandscapeAnchorLayout.setOnClickListener(this);
        mLandscapeAnchorAvatar = (BaseImageView) findViewById(R.id.landscape_anchor_avatar);
        mLandscapeAnchorNameTv = (TextView) findViewById(R.id.landscape_anchor_name);
        mLandscapeFollowBtn = (TextView) findViewById(R.id.landscape_follow);
        mLandscapeFollowBtn.setOnClickListener(this);

        mLandscapeDownloadBtn = (ImageView) findViewById(R.id.landscape_download);
        mLandscapeDownloadBtn.setOnClickListener(this);

        mLandscapeShareBtn = (ImageView) findViewById(R.id.landscape_share);
        mLandscapeShareBtn.setOnClickListener(this);

        mLandscapeSuspend = (ImageView) findViewById(R.id.landscape_pause_resume_btn);
        mLandscapeSuspend.setOnClickListener(this);

        mLandscapeRefresh = (ImageView) findViewById(R.id.landscape_refresh_btn);
        mLandscapeRefresh.setOnClickListener(this);

        mLandscapeBarrageBtn = (ImageView) findViewById(R.id.landscape_hide_barrage_btn);
        mLandscapeBarrageBtn.setOnClickListener(this);

        getmLandscapeGiftBtn = (ImageView) findViewById(R.id.landscape_gift_btn);
        getmLandscapeGiftBtn.setOnClickListener(this);

        if (mPresenter != null) {
            mPresenter.getAnchorInfo();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (mIsLandscape) {
            // 横屏下的点击事件
            if (id == R.id.landscape_back_btn) {
                if (mPresenter != null) {
                    mPresenter.exitRoom();
                }
            }
        } else {
            // 竖屏下的点击事件
            if (id == R.id.portrait_back_btn) {
                if (mPresenter != null) {
                    mPresenter.exitRoom();
                }
            }
        }
    }

    /**
     * PortraitLineUpButtons的各个Button的点击回调
     * @param v
     */
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
            if (mPresenter != null) {
                mPresenter.forceRotate();
            }
        }
    }

    /**
     * 接收横竖屏切换通知
     * @param isLandscape
     */
    private void onReOrient(boolean isLandscape) {
        MyLog.d(TAG, "change to" + (isLandscape ? "landscape" : "portrait"));
        if (mIsLandscape != isLandscape) {
            // 横竖屏相互切换　重新加载布局
            mIsLandscape = isLandscape;
            setUpLayout(getContext(), !mIsLandscape);
        } else {
            // 横屏切换到反向横屏　或者竖屏切换到反向竖屏
        }
    }

    private void updateAnchorInfo(long uid, long avatarTs, String nickName, boolean isFollowed) {
        if (mIsLandscape) {
            AvatarUtils.loadAvatarByUidTs(mLandscapeAnchorAvatar, uid, avatarTs, true);
            if (!TextUtils.isEmpty(nickName)) {
                mLandscapeAnchorNameTv.setText(nickName);
            } else if (uid > 0) {
                mLandscapeAnchorNameTv.setText(String.valueOf(uid));
            } else {
                mLandscapeAnchorNameTv.setText(R.string.watch_owner_name_default);
            }

            if (isFollowed) {
                mLandscapeFollowBtn.setVisibility(GONE);
            } else {
                mLandscapeFollowBtn.setVisibility(VISIBLE);
            }
        }
    }

    @Override
    public IView getViewProxy() {
        class ComponentView implements IView {

            @Override
            public void reOrient(boolean isLandscape) {
                // 接收横竖屏切换通知
                WatchGameZTopView.this.onReOrient(isLandscape);
            }

            @Override
            public void updateAnchorInfo(long uid, long avatarTs, String nickName, boolean isFollowed) {
                // 接收主播信息
                WatchGameZTopView.this.updateAnchorInfo(uid, avatarTs, nickName, isFollowed);
            }

            @Override
            public void onFollowResult(int resultCode) {

            }

            @Override
            public void showFollowBtn(boolean needShow, boolean needAnim) {

            }

            @Override
            public <T extends View> T getRealView() {
                return (T) WatchGameZTopView.this;
            }
        }
        return new ComponentView();
    }

    @Override
    public void setPresenter(IPresenter iPresenter) {
        this.mPresenter = iPresenter;
    }


    public interface  IPresenter {
        /**
         * 强制全屏
         */
        void forceRotate();

        /**
         * 退出房间
         */
        void exitRoom();

        /**
         * 获取主播信息
         */
        void getAnchorInfo();

        /**
         * 关注主播
         */
        void followAnchor();
    }

    public interface IView extends IViewProxy {

        void reOrient(boolean isLandscape);
        /**
         * 更新主播信息
         */
        void updateAnchorInfo(long uid, long avatarTs, String nickName, boolean isFollowed);

        /**
         * 关注主播结果
         */
        void onFollowResult(int resultCode);

        /**
         * 关注按钮状态变更
         */
        void showFollowBtn(boolean needShow, boolean needAnim);
    }
}
