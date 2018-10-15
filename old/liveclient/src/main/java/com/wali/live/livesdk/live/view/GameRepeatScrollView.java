package com.wali.live.livesdk.live.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.mi.live.data.gift.model.GiftType;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.repository.GiftRepository;
import com.mi.live.data.room.model.RoomDataChangeEvent;
import com.wali.live.common.model.CommentModel;
import com.wali.live.dao.Gift;
import com.wali.live.event.EventClass;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.window.event.FloatGiftEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by chenyong on 2016/12/15.
 */

// TODO chenyong1 后面增加丢弹幕的逻辑，第一个版本先不做弹幕丢失处理
public class GameRepeatScrollView extends RelativeLayout {
    private static final String TAG = GameRepeatScrollView.class.getSimpleName();

    private String mToken;
    private LinkedList<CommentModel> mBarrageMsgQueue = new LinkedList<>();

    private Subscription mClearSubscription;

    private boolean mIsChatRoomMode = true;
    private boolean mIsHideTitleView = false;
    private boolean mForbidReceiveComment = false;

    TextView mTitleTv;
    TextView mTitleInfo;
    RelativeLayout mCommentContent;

    private GameRepeatScrollItemView mItemView;
    private GameRepeatScrollItemView mItemView2;

    private ValueAnimator mAnimator;
    private int mTranslationY = DisplayUtils.dip2px(40f);

    private int mIndex = 0;
    private volatile boolean mIsGift;

    public GameRepeatScrollView(Context context) {
        super(context);
        init(context);
    }

    public GameRepeatScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GameRepeatScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private <T> T $(int id) {
        return (T) findViewById(id);
    }

    private void init(Context context) {
        inflate(context, R.layout.game_chat_room_scroll_view, this);
        ButterKnife.bind(this);

        mTitleTv = $(R.id.title_tv);
        mTitleInfo = $(R.id.title_info);
        mCommentContent = $(R.id.comment_content);

        mTitleInfo.setText(getResources().getQuantityString(R.plurals.game_comment_number, 0, 0));

        mItemView = addItemView();
        mItemView2 = addItemView();

        enterScrollMode();
    }

    private GameRepeatScrollItemView addItemView() {
        GameRepeatScrollItemView itemView = new GameRepeatScrollItemView(GlobalData.app());
        mCommentContent.addView(itemView);
        return itemView;
    }

    private void initAnimator() {
        if (mAnimator == null) {
            mAnimator = ValueAnimator.ofFloat(0, 1);
            mAnimator.setInterpolator(new LinearInterpolator());
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (mItemView != null && mItemView2 != null) {
                        mItemView.setTranslationY(-mTranslationY * (float) animation.getAnimatedValue());
                        mItemView2.setTranslationY(mTranslationY * (1 - (float) animation.getAnimatedValue()));
                    }
                }
            });
            mAnimator.setDuration(400);
            mAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mItemView != null && mItemView2 != null) {
                        mItemView.setTranslationY(mTranslationY);
                        GameRepeatScrollItemView itemView = mItemView;
                        mItemView = mItemView2;
                        mItemView2 = itemView;
                    }
                    clear();
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            processBarrageMsg();
                        }
                    }, 100);
                }
            });
        }
    }

    private void stopAnimator() {
        if (mAnimator != null) {
            mAnimator.end();
            mAnimator = null;
        }
    }

    public void enterScrollMode() {
        mItemView.setVisibility(View.VISIBLE);
        mItemView2.setVisibility(View.VISIBLE);
        mItemView.setTranslationY(0);
        mItemView2.setTranslationY(mTranslationY);

        initAnimator();
    }

    public void setToken(String token) {
        mToken = token;
    }

    public void setViewerCnt(int viewerCnt) {
        mTitleInfo.setText(getResources().getQuantityString(R.plurals.game_comment_number, viewerCnt, viewerCnt));
    }

    public void register() {
        EventBus.getDefault().register(this);
    }

    public void unregister() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        stopAnimator();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RoomDataChangeEvent event) {
        switch (event.type) {
            case RoomDataChangeEvent.TYPE_CHANGE_VIEWER_COUNT:
                mTitleInfo.setText(getResources().getQuantityString(R.plurals.game_comment_number, event.source.getViewerCnt(), event.source.getViewerCnt()));
                break;
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.RefreshGameLiveCommentEvent event) {
        if (mForbidReceiveComment) {
            return;
        }
        if (event != null && event.token.equals(mToken) && mIsChatRoomMode) {
            boolean isAdd = false;
            if (event.barrageMsg != null) {
                isAdd = addBarrageMsg(event.barrageMsg);
            } else if (event.barrageMsgs != null) {
                for (CommentModel msg : event.barrageMsgs) {
                    isAdd |= addBarrageMsg(msg);
                }
            }
            if (isAdd) {
                processBarrageMsg();
            }
        }
    }

    private boolean addBarrageMsg(CommentModel msg) {
        return mBarrageMsgQueue.add(msg);
    }

    private void processBarrageMsg() {
        if (mAnimator == null || mAnimator.isRunning() || mIsGift) {
            return;
        }
        CommentModel commentModel = mBarrageMsgQueue.poll();
        if (commentModel == null) {
            return;
        }

        // 高价值礼物 停留时间长
        Gift gift = GiftRepository.findGiftById((int) commentModel.getGiftId());
        if (gift != null) {
            EventBus.getDefault().post(new FloatGiftEvent(gift));
        }
        if ((commentModel.getMsgType() == BarrageMsgType.B_MSG_TYPE_GIFT
                && gift != null && gift.getCatagory() == GiftType.HIGH_VALUE_GIFT)
                || commentModel.getMsgType() == BarrageMsgType.B_MSG_TYPE_GLABAL_MSG) {
            mIsGift = true;
        }

        // 隐藏标题
        hideTitleView(true);

        // 进入动画
        if (mIndex == 0) {
            clear();
            mIndex = 1;
            mItemView.setCommentContent(commentModel);
        } else {
            if (mClearSubscription != null && !mClearSubscription.isUnsubscribed()) {
                mClearSubscription.unsubscribe();
            }
            mItemView2.setCommentContent(commentModel);
            mAnimator.start();
        }
    }

    private void clear() {
        if (mClearSubscription != null && !mClearSubscription.isUnsubscribed()) {
            mClearSubscription.unsubscribe();
        }
        mClearSubscription = Observable.timer(5000, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        if (mIsGift) {
                            mIsGift = false;
                            processBarrageMsg();
                        } else {
                            hideTitleView(false);
                            mIndex = 0;
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "mClearSubscription throwable=" + throwable);
                    }
                });
    }

    public void setChatRoomMode(boolean isChatRoomMode) {
        if (mIsChatRoomMode != isChatRoomMode) {
            mIsChatRoomMode = isChatRoomMode;
            if (!mIsChatRoomMode) {
                hideTitleView(false);
                mBarrageMsgQueue.clear();
            }
        }
    }

    private void hideTitleView(boolean isHide) {
        if (mIsHideTitleView != isHide) {
            mIsHideTitleView = isHide;
            if (mIsHideTitleView) {
                mTitleTv.setVisibility(GONE);
                mTitleInfo.setVisibility(GONE);
                mCommentContent.setVisibility(VISIBLE);
            } else {
                mTitleTv.setVisibility(VISIBLE);
                mTitleInfo.setVisibility(VISIBLE);
                mCommentContent.setVisibility(GONE);
            }
        }
    }

    public void forbidReceiveComment(boolean isForbid) {
        mForbidReceiveComment = isForbid;
    }
}
