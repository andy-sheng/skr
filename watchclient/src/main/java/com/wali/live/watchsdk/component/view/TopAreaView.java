package com.wali.live.watchsdk.component.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.display.DisplayUtils;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.query.model.ViewerModel;
import com.mi.live.data.user.User;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.common.listener.OnItemClickListener;
import com.wali.live.event.UserActionEvent;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.watchtop.adapter.UserAvatarRecyclerAdapter;

import java.util.List;

/**
 * Created by wangmengjie on 2017/08/03.
 *
 * @module 顶部view
 */
public class TopAreaView extends RelativeLayout implements View.OnClickListener,
        IComponentView<TopAreaView.IPresenter, TopAreaView.IView> {
    private static final String TAG = "TopAreaView";
    private static final int ANCHOR_BADGE_CERT = DisplayUtils.dip2px(16f);
    private static final int ANCHOR_BADGE_UN_CERT = DisplayUtils.dip2px(11f);

    @Nullable
    protected IPresenter mPresenter;
    private final AnimationHelper mAnimationHelper = new AnimationHelper();
    private boolean mIsFollowGone = false;
    private boolean mIsLinking = false;
    private User mLinkUser;
    //主播信息
    private BaseImageView mAnchorIv;
    private ImageView mUserBadgeIv;
    private TextView mNameTv;
    private TextView mViewersNumTv;
    private TextView mFollowTv;
    //连麦
    private ImageView mLinkAnchorIv;
    private BaseImageView mGuestIv;
    private View mLinkArea;
    //观众
    private RecyclerView mAvatarRv;
    //星票
    private TextView mTicketNumTv;
    //管理
    private View mManagerArea;

    private UserAvatarRecyclerAdapter mAvatarRvAdapter;
    private final LinearLayoutManager mAvatarLayoutManager = new LinearLayoutManager(getContext());

    protected final <T extends View> T $(@IdRes int resId) {
        return (T) findViewById(resId);
    }

    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    public TopAreaView(Context context) {
        this(context, null);
    }

    public TopAreaView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopAreaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
        if (mPresenter == null) {
            return;
        }
        mPresenter.syncData();
    }


    private void init(Context context) {
        inflate(context, R.layout.top_area_view, this);
        mAnchorIv = $(R.id.anchor_iv);
        mUserBadgeIv = $(R.id.user_badge_iv);
        mNameTv = $(R.id.name_tv);
        mViewersNumTv = $(R.id.viewers_num_tv);
        mFollowTv = $(R.id.follow_tv);
        mAvatarRv = $(R.id.avatar_rv);
        mTicketNumTv = $(R.id.ticket_num_tv);
        mGuestIv = $(R.id.guest_iv);
        mLinkAnchorIv = $(R.id.link_anchor_iv);
        mLinkArea = $(R.id.link_guest_area);
        mManagerArea = $(R.id.manager_area);
        mLinkAnchorIv.setVisibility(View.GONE);
        mFollowTv.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mAnimationHelper.setFollowWidth(mFollowTv.getMeasuredWidth());
        mLinkArea.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        mAnimationHelper.setLinkWidth(mLinkArea.getMeasuredWidth());

        $click(mFollowTv, this);
        $click(mManagerArea, this);
        $click($(R.id.ticket_area), this);
        $click($(R.id.anchor_info_container), this);

        mAvatarRvAdapter = new UserAvatarRecyclerAdapter();
        mAvatarRvAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ViewerModel viewer = mAvatarRvAdapter.getViewer(position);
                if (viewer != null) {
                    UserActionEvent.post(UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_INFO, viewer.getUid(), null);
                }
            }
        });
        mAvatarRv.setAdapter(mAvatarRvAdapter);
        mAvatarRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int lastCompleteVisibleItem = mAvatarLayoutManager.findLastCompletelyVisibleItemPosition();
                    if (lastCompleteVisibleItem == mAvatarRvAdapter.getItemCount() - 1) {
                        mPresenter.postAvatarEvent(UserActionEvent.EVENT_TYPE_REQUEST_LOOK_MORE_VIEWER, mAvatarRvAdapter.getItemCount());
                    }
                }
            }
        });
        mAvatarRv.setItemAnimator(new DefaultItemAnimator());
        mAvatarLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mAvatarRv.setLayoutManager(mAvatarLayoutManager);
        mAvatarRv.setHasFixedSize(true);
    }

    @Override
    public void onClick(View v) {
        if (mPresenter == null) {
            MyLog.w(TAG, "onClick, mPresenter is null！");
            return;
        }
        int id = v.getId();
        if (id == R.id.anchor_info_container) {
            mPresenter.getAnchorInfo();
        } else if (id == R.id.follow_tv) {
            mPresenter.followAnchor();
        } else if (id == R.id.ticket_area) {
            mPresenter.getTicketDetail();
        } else if (id == R.id.manager_area) {
            UserActionEvent.post(UserActionEvent.EVENT_TYPE_REQUEST_SET_MANAGER, null, null);
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
                return (T) TopAreaView.this;
            }

            @Override
            public void onFollowResult(int resultCode) {
                if (resultCode == ErrorCode.CODE_RELATION_BLACK) {
                    ToastUtils.showToast(getResources().getString(R.string.setting_black_follow_hint));
                } else if (resultCode == 0) {
                    ToastUtils.showToast(getResources().getString(R.string.follow_success));
                    mAnimationHelper.startFollow(false);
                } else if (resultCode == -1) {
                    ToastUtils.showToast(getResources().getString(R.string.follow_failed));
                }
            }

            @Override
            public void showFollowBtn(boolean needShow, boolean isSwitchScreen) {
                if (isSwitchScreen) {
                    if (needShow && !mIsLinking) {
                        mFollowTv.setVisibility(View.VISIBLE);
                    } else if (mIsLinking) {
                        mLinkArea.setVisibility(View.VISIBLE);
                        mIsFollowGone = needShow;
                    }
                } else {
                    if (mIsFollowGone && !needShow) {
                        mIsFollowGone = false;
                    } else if (needShow && mFollowTv.getVisibility() != View.VISIBLE) {
                        mAnimationHelper.startFollow(true);
                    }
                }
            }


            @Override
            public void updateTicketAndViewerCount(int ticketCount, int viewerCount) {
                mTicketNumTv.setText(String.valueOf(ticketCount));
                mViewersNumTv.setText(String.valueOf(viewerCount));
            }

            @Override
            public void updateAnchorInfo(long uid, long avatarTs, int certificationType, int level, String nickName) {
                AvatarUtils.loadAvatarByUidTs(mAnchorIv, uid, avatarTs, true);
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
                    CommonUtils.setMaxEclipse(mNameTv, DisplayUtils.dip2px(75), nickName);
                } else if (uid > 0) {
                    CommonUtils.setMaxEclipse(mNameTv, DisplayUtils.dip2px(75), String.valueOf(uid));
                } else {
                    mNameTv.setText(R.string.watch_owner_name_default);
                }
            }

            @Override
            public void updateViewers(List<ViewerModel> viewersList) {
                mAvatarRvAdapter.setViewerList(viewersList);
            }

            @Override
            public void linkToAnchor(final User user) {
                if (user == null || mIsLinking) {
                    return;
                }
                mIsLinking = true;
                mLinkUser = user;
                AvatarUtils.loadAvatarByUidTs(mGuestIv, mLinkUser.getUid(), mLinkUser.getAvatar(), true);
                $click(mGuestIv, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UserActionEvent.post(UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_INFO, user.getUid(), null);
                    }
                });
                if (mFollowTv.getVisibility() == View.VISIBLE) {
                    mAnimationHelper.startFollowToLink(true);
                    mIsFollowGone = true;
                } else {
                    mAnimationHelper.startLink(true);
                }
            }

            @Override
            public void linkStop(boolean isNeedAnim) {
                if (!mIsLinking) {
                    return;
                }
                mIsLinking = false;
                mLinkUser = null;
                if (isNeedAnim) {
                    if (mIsFollowGone) {
                        MyLog.d(TAG, "link to follow");
                        mAnimationHelper.startFollowToLink(false);
                    } else {
                        MyLog.d(TAG, "link to null");
                        mAnimationHelper.startLink(false);
                    }
                } else {
                    mLinkArea.setVisibility(View.GONE);
                }

            }

            @Override
            public void cancelAnimator() {
                mAnimationHelper.clearAnimator();
            }

            @Override
            public void showManager(boolean mIsLive) {
                if (mIsLive) {
                    mManagerArea.setVisibility(View.VISIBLE);
                }
            }

        }
        return new ComponentView();
    }

    public void reset() {
        clearData();
        mPresenter.reset();
    }

    private void clearData() {
        AvatarUtils.loadAvatarByUidTs(mAnchorIv, 0, 0, true);
        mNameTv.setText("");
        mUserBadgeIv.setVisibility(View.GONE);
        mViewersNumTv.setText("0");
        mTicketNumTv.setText("0");
        mAvatarRvAdapter.setViewerList(null);
        mFollowTv.setVisibility(View.GONE);
        mLinkArea.setVisibility(View.GONE);
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
         * 打开星票详情
         */
        void getTicketDetail();

        /**
         * 更新数据
         */
        void syncData();

        /**
         * 发送查看更多观众的event
         */
        void postAvatarEvent(int eventTypeRequestLookMoreViewer, int itemCount);

        /**
         * 上下切换时刷新
         */
        void reset();

    }

    public interface IView extends IViewProxy {

        /**
         * 关注主播结果
         */
        void onFollowResult(int resultCode);

        /**
         * 初始化关注
         */
        void showFollowBtn(boolean needShow, boolean isSwitchScreen);

        /**
         * 更新观看者头像
         */
        void updateViewers(List<ViewerModel> viewersList);

        /**
         * 连麦
         */
        void linkToAnchor(User user);

        /**
         * 连麦结束
         */
        void linkStop(boolean needAnim);

        /**
         * 取消动画
         */
        void cancelAnimator();

        /**
         * 显示管理
         */
        void showManager(boolean mIsLive);

        /**
         * 更新星票和观众数量
         */
        void updateTicketAndViewerCount(int ticket, int viewerCnt);

        /**
         * 更新主播信息
         */
        void updateAnchorInfo(long uid, long avatarTs, int certificationType, int level, String nickName);

    }

    private class AnimationHelper {
        private ValueAnimator mLinkAnimator;
        private int mLinkAreaWidth;
        private boolean mLinkShow = false;

        private ValueAnimator mFollowAnimator;
        private int mFollowWidth;
        private boolean mFollowShow = false;

        private AnimatorSet mFollowToLinkAnim;
        private AnimatorSet mLinkToFollowAnim;

        private void setupLinkAnimator() {
            if (mLinkAnimator == null) {
                mLinkAnimator = new ValueAnimator();
                mLinkAnimator.setDuration(400);
                mLinkAnimator.setStartDelay(300);
                mLinkAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int value = (int) animation.getAnimatedValue();
                        ViewGroup.LayoutParams params = mLinkArea.getLayoutParams();
                        params.width = value;
                        mLinkArea.setLayoutParams(params);
                        mLinkArea.setAlpha((float) value / mLinkAreaWidth);
                    }
                });
                mLinkAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        mLinkArea.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mLinkArea.setVisibility(mLinkShow ? View.VISIBLE : View.GONE);
                        ViewGroup.LayoutParams layoutParams = mLinkArea.getLayoutParams();
                        layoutParams.width = mLinkAreaWidth;
                        mLinkArea.setLayoutParams(layoutParams);
                        mLinkArea.setAlpha(1f);
                    }
                });
            }
        }

        private void setupFollowAnimator() {
            if (mFollowAnimator == null) {
                mFollowAnimator = new ValueAnimator();
                mFollowAnimator.setDuration(400);
                mFollowAnimator.setStartDelay(300);
                mFollowAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int value = (int) animation.getAnimatedValue();
                        ViewGroup.LayoutParams layoutParams = mFollowTv.getLayoutParams();
                        layoutParams.width = value;
                        mFollowTv.setLayoutParams(layoutParams);
                        mFollowTv.setAlpha((float) value / mFollowWidth);
                    }
                });
                mFollowAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mFollowTv.setVisibility(mFollowShow ? View.VISIBLE : View.GONE);
                        ViewGroup.LayoutParams layoutParams = mFollowTv.getLayoutParams();
                        layoutParams.width = mFollowWidth;
                        mFollowTv.setLayoutParams(layoutParams);
                        mFollowTv.setAlpha(1f);
                    }

                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        mFollowTv.setVisibility(View.VISIBLE);
                    }
                });
            }
        }

        private void initFollowToLinkAnim(boolean isShow) {
            if (isShow) {
                mFollowToLinkAnim = new AnimatorSet();
                mFollowAnimator.setIntValues(mFollowWidth, 0);
                mLinkAnimator.setIntValues(0, mLinkAreaWidth);
                mFollowShow = false;
                mLinkShow = true;
                mFollowToLinkAnim.playSequentially(mFollowAnimator, mLinkAnimator);
            } else {
                mLinkToFollowAnim = new AnimatorSet();
                mFollowAnimator.setIntValues(0, mFollowWidth);
                mLinkAnimator.setIntValues(mLinkAreaWidth, 0);
                mFollowShow = true;
                mLinkShow = false;
                mLinkToFollowAnim.playSequentially(mLinkAnimator, mFollowAnimator);
            }
        }

        public void startFollow(boolean isShow) {
            if (mFollowShow == isShow) {
                return;
            }
            mFollowShow = isShow;
            setupFollowAnimator();
            if (checkAnimIsStart(mFollowAnimator)) {
                return;
            }
            if (isShow) {
                mFollowAnimator.setIntValues(0, mFollowWidth);
            } else {
                mFollowAnimator.setIntValues(mFollowWidth, 0);
            }
            mFollowAnimator.start();
        }

        public void startLink(boolean isShow) {
            if (mLinkShow == isShow) {
                return;
            }
            mLinkShow = isShow;
            setupLinkAnimator();
            if (checkAnimIsStart(mLinkAnimator)) {
                return;
            }
            if (isShow) {
                mLinkAnimator.setIntValues(0, mLinkAreaWidth);
            } else {
                mLinkAnimator.setIntValues(mLinkAreaWidth, 0);
            }
            mLinkAnimator.start();
        }

        public void startFollowToLink(boolean isShow) {
            setupFollowAnimator();
            setupLinkAnimator();
            if (checkAnimIsStart(mFollowAnimator, mLinkAnimator)) {
                return;
            }
            initFollowToLinkAnim(isShow);
            if (isShow) {
                mFollowToLinkAnim.start();
            } else {
                mLinkToFollowAnim.start();
            }
        }

        public void setLinkWidth(int linkWidth) {
            mLinkAreaWidth = linkWidth;
        }

        public void setFollowWidth(int followWidth) {
            mFollowWidth = followWidth;
        }

        private boolean checkAnimIsStart(Animator... anims) {
            for (Animator anim : anims) {
                if (anim.isStarted() || anim.isRunning()) {
                    return true;
                }
            }
            return false;
        }

        public void clearAnimator() {
            if (mFollowAnimator != null && (mFollowAnimator.isRunning() || mFollowAnimator.isStarted())) {
                mFollowAnimator.cancel();
            }
            if (mLinkAnimator != null && (mLinkAnimator.isRunning() || mLinkAnimator.isStarted())) {
                mLinkAnimator.cancel();
            }
            mIsFollowGone = false;
            mFollowShow = false;
            mLinkShow = false;
        }
    }

}
