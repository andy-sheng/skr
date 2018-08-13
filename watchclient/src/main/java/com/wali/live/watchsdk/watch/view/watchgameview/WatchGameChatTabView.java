package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.display.DisplayUtils;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.api.ErrorCode;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.component.presenter.LiveCommentPresenter;
import com.wali.live.watchsdk.watch.presenter.watchgamepresenter.WatchGameChatTabPresenter;
import com.wali.live.watchsdk.watch.presenter.watchgamepresenter.WatchGameLiveCommentPresenter;

import static com.wali.live.component.view.Utils.$click;

public class WatchGameChatTabView extends RelativeLayout implements
        IComponentView<WatchGameChatTabView.IPresenter, WatchGameChatTabView.IView>, View.OnClickListener {

    private static final int ANCHOR_BADGE_CERT = DisplayUtils.dip2px(16f);
    private static final int ANCHOR_BADGE_UN_CERT = DisplayUtils.dip2px(11f);

    private static final String TAG = "WatchGameChatTabView";

    WatchGameChatTabPresenter mWatchGameChatTabPresenter;

    WatchGameLiveCommentView mWatchGameLiveCommentView;
    WatchGameLiveCommentPresenter mWatchGameLiveCommentPresenter;


    RelativeLayout mAnchorInfoContainer;
    BaseImageView mAnchorAvatarIv;
    TextView mAnchorNameTv;
    ImageView mUserBadgeIv;
    TextView mAnchorRoomTv;
    TextView mFocusBtn;
    TextView mViewerNum;
    RelativeLayout mCommentContainer;

    private boolean mEnableFollow = true;

    public WatchGameChatTabView(Context context, WatchComponentController componentController) {
        super(context);
        init(context, componentController);
    }

    private void init(Context context, WatchComponentController componentController) {
        inflate(context, R.layout.watch_game_tab_chat_layout, this);
        mAnchorInfoContainer = (RelativeLayout) this.findViewById(R.id.anchor_info_container);
        mAnchorAvatarIv = (BaseImageView) this.findViewById(R.id.anchor_avatar_iv);
        mUserBadgeIv = (ImageView) this.findViewById(R.id.anchor_badge_iv);
        mAnchorNameTv = (TextView) this.findViewById(R.id.anchor_name_tv);
        mAnchorRoomTv = (TextView) this.findViewById(R.id.anchor_room_tv);
        mFocusBtn = (TextView) this.findViewById(R.id.focus_btn);
        mViewerNum = (TextView) this.findViewById(R.id.viewer_num);

        mCommentContainer = (RelativeLayout) this.findViewById(R.id.comment_container);
        mWatchGameLiveCommentView = (WatchGameLiveCommentView) this.findViewById(R.id.live_comment_view);
        mWatchGameLiveCommentView.setIsGameLive(true);
        mWatchGameLiveCommentView.setToken(componentController.mRoomChatMsgManager.toString());
        mWatchGameLiveCommentPresenter = new WatchGameLiveCommentPresenter(componentController);
        mWatchGameLiveCommentPresenter.setView(mWatchGameLiveCommentView.getViewProxy());
        mWatchGameLiveCommentView.setPresenter(mWatchGameLiveCommentPresenter);

        mWatchGameChatTabPresenter = new WatchGameChatTabPresenter(componentController, componentController.getRoomBaseDataModel());
        mWatchGameChatTabPresenter.setView(this.getViewProxy());
        this.setPresenter(mWatchGameChatTabPresenter);

        $click(mAnchorAvatarIv, this);
        $click(mFocusBtn, this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mWatchGameLiveCommentPresenter != null) {
            mWatchGameLiveCommentPresenter.startPresenter();
        }
        if (mWatchGameChatTabPresenter != null) {
            mWatchGameChatTabPresenter.startPresenter();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mWatchGameLiveCommentPresenter != null) {
            mWatchGameLiveCommentPresenter.stopPresenter();
        }
        if (mWatchGameChatTabPresenter != null) {
            mWatchGameChatTabPresenter.stopPresenter();
        }
    }

    @Override
    public void onClick(View v) {
        if (mWatchGameChatTabPresenter == null) {
            MyLog.w(TAG, "onClick, mPresenter is null！");
            return;
        }
        int id = v.getId();
        if (id == R.id.anchor_avatar_iv) {
            mWatchGameChatTabPresenter.getAnchorInfo();
        } else if (id == R.id.focus_btn) {
            mWatchGameChatTabPresenter.followAnchor();
        }
    }

    @Override
    public IView getViewProxy() {
        return new IView() {

            @Override
            public void showFollowBtn(boolean needShow, boolean isFollow) {
                mEnableFollow = needShow;
                if (needShow) {
                    mFocusBtn.setVisibility(VISIBLE);
                    if (isFollow) {
                        mFocusBtn.setText(R.string.followed);
                        mFocusBtn.setEnabled(false);
                    } else {
                        mFocusBtn.setText(R.string.follow);
                        mFocusBtn.setEnabled(true);
                    }
                } else {
                    mFocusBtn.setVisibility(INVISIBLE);
                }

            }

            @Override
            public void onFollowResult(int resultCode) {
                if (mEnableFollow) {
                    if (resultCode == ErrorCode.CODE_RELATION_BLACK) {
                        ToastUtils.showToast(getResources().getString(R.string.setting_black_follow_hint));
                    } else if (resultCode == 0) {
                        ToastUtils.showToast(getResources().getString(R.string.follow_success));
                        mFocusBtn.setText(R.string.followed);
                        mFocusBtn.setEnabled(false);
                    } else if (resultCode == -1) {
                        ToastUtils.showToast(getResources().getString(R.string.follow_failed));
                    } else {
                        ToastUtils.showToast("关注失败 code:" + resultCode);
                    }
                }
            }


            @Override
            public void updateViewerNum(int viewerCnt) {
                String info;
                if (viewerCnt > 9999) {
                    info = String.format("%.1f万人", viewerCnt / 10000.0);
                } else {
                    info = String.format("%d人", viewerCnt);
                }
                mViewerNum.setText(info);
            }

            @Override
            public void updateAnchorInfo(long uid, long avatarTs, int certificationType, int level, String nickName) {
                AvatarUtils.loadAvatarByUidTs(mAnchorAvatarIv, uid, avatarTs, true);
                mUserBadgeIv.setVisibility(View.VISIBLE);
                LayoutParams badgeLp = (LayoutParams) mUserBadgeIv.getLayoutParams();
                if (certificationType > 0) {
                    badgeLp.width = ANCHOR_BADGE_CERT;
                    badgeLp.height = ANCHOR_BADGE_CERT;
                    mUserBadgeIv.setImageDrawable(ItemDataFormatUtils.getCertificationImgSource(certificationType));
                } else {
                    badgeLp.width = ANCHOR_BADGE_UN_CERT;
                    badgeLp.height = ANCHOR_BADGE_UN_CERT;
                    mUserBadgeIv.setImageDrawable(ItemDataFormatUtils.getLevelSmallImgSource(level));
                }
                mUserBadgeIv.setLayoutParams(badgeLp);
                if (!TextUtils.isEmpty(nickName)) {
                    CommonUtils.setMaxEclipse(mAnchorNameTv, DisplayUtils.dip2px(75), nickName);
                } else if (uid > 0) {
                    CommonUtils.setMaxEclipse(mAnchorNameTv, DisplayUtils.dip2px(75), String.valueOf(uid));
                } else {
                    mAnchorNameTv.setText(R.string.watch_owner_name_default);
                }

                mAnchorRoomTv.setText(String.format(getResources().getString(R.string.anchor_room_id), String.valueOf(uid)));

            }


            @Override
            public <T extends View> T getRealView() {
                return (T) WatchGameChatTabView.this;
            }
        };
    }

    @Override
    public void setPresenter(IPresenter iPresenter) {
        iPresenter.syncData();
    }

    public interface IView extends IViewProxy {
        /**
         * 初始化关注
         */
        void showFollowBtn(boolean needShow, boolean isFollow);

        /**
         * 关注主播结果
         */
        void onFollowResult(int resultCode);

        /**
         * 更新观众人数
         */
        void updateViewerNum(int viewerCnt);

        /**
         * 更新主播信息
         */
        void updateAnchorInfo(long uid, long avatarTs, int certificationType, int level, String nickName);
    }

    public interface IPresenter {

        /**
         * 打开主播信息框
         */
        void getAnchorInfo();

        /**
         * 关注主播
         */
        void followAnchor();

        /**
         * 更新数据
         */
        void syncData();

    }
}
