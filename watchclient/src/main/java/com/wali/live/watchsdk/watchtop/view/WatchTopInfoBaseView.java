package com.wali.live.watchsdk.watchtop.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.RxActivity;
import com.base.activity.assist.IBindActivityLIfeCycle;
import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.display.DisplayUtils;
import com.base.utils.span.SpanUtils;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.query.model.ViewerModel;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.data.room.model.RoomDataChangeEvent;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.base.BaseEvent;
import com.wali.live.common.listener.OnItemClickListener;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.watchtop.adapter.UserAvatarRecyclerAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


/**
 * Created by chengsimin on 16/3/31.
 */
public abstract class WatchTopInfoBaseView extends RelativeLayout implements IBindActivityLIfeCycle {
    public static final String TAG = WatchTopInfoBaseView.class.getSimpleName();
    private static final float GRADIENT_WIDTH = 278;
    public static final int EXACT_VIEWER_NUM = 10;

    protected View mOwnerContainer;
    protected BaseImageView mOwnerIv;     //直播者顶部的头像
    protected ImageView mOwnerBadgeIv;    //直播者等级图标

    protected TextView mTicketTvUp, mTicketTvDown;         // 星票
    protected WrapperView mTicketArea;
    private int TICKET_MAX_Y = DisplayUtils.dip2px(16);
    private final long TICKET_ANIME_DURATION = 500;
    private final long TICKET_DELAYED = 10 * 1000;
    private final long TICKET_DELAYED_TOTAL = 60 * 1000;
    private final int TICKET_PADDING = DisplayUtils.dip2px(30);
    //计算字符串宽度
    Paint pFont = new Paint();
    Rect rect = new Rect();
    AnimatorSet mTicketAnimatorSet;
    //是否正在播放星票切换动画
    boolean mIsTicketAnimating = false;
    //是否是本场模式
    boolean mIsTicketing = false;

    protected TextView mViewerCountTv;    // 人数

    protected RecyclerView mAvatarRv;// 观众头像
    protected LinearLayoutManager mAvatarLayoutManager;
    protected UserAvatarRecyclerAdapter mAvatarAdapter;

    protected TextView mShowerNameTv;//主播名字

    protected RoomBaseDataModel mMyRoomBaseDataModel;

    protected boolean mIsLoadViewer = false;
    protected boolean mIsAnchor = false;
    protected boolean mIsLandScape = false;

    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    public WatchTopInfoBaseView(Context context) {
        super(context);
    }

    public WatchTopInfoBaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WatchTopInfoBaseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void init(Context context, boolean mIsLandScape) {
        inflate(context, getLayout(mIsLandScape), this);
        initView();
        initParticular();
        initCommon();
    }

    abstract protected int getLayout(boolean mIsLandScape);

    abstract protected void initParticular();

    abstract protected void initView();

    protected void initCommon() {

        mOwnerContainer = findViewById(R.id.owner_container);
        RxView.clicks(mOwnerContainer)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        EventBus.getDefault().post(new BaseEvent.UserActionEvent(BaseEvent.UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_INFO, mMyRoomBaseDataModel.getUid(), null));

                    }
                });

        mShowerNameTv = (TextView) findViewById(R.id.name_tv);

        mOwnerIv = (BaseImageView) mOwnerContainer.findViewById(R.id.owner_iv);
        mOwnerBadgeIv = (ImageView) findViewById(R.id.user_badge_iv);

        mAvatarRv = (RecyclerView) findViewById(R.id.avatar_rv);
        mAvatarAdapter = new UserAvatarRecyclerAdapter();
        mAvatarAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                if (CommonUtils.isFastDoubleClick(1000)) {
                    return;
                }
                ViewerModel viewer = mAvatarAdapter.getViewer(position);
                MyLog.d(TAG, "viewer:" + viewer);
                if (viewer != null) {
                    EventBus.getDefault().post(new BaseEvent.UserActionEvent(BaseEvent.UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_INFO, viewer.getUid(), null));
                }
            }
        });

        mAvatarRv.setAdapter(mAvatarAdapter);
        mAvatarRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                // 拉取更多
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (mIsLoadViewer) {
                        return;
                    }
                    int lastCompleteVisibleItem = mAvatarLayoutManager.findLastCompletelyVisibleItemPosition();
                    if (lastCompleteVisibleItem == mAvatarAdapter.getItemCount() - 1 && mMyRoomBaseDataModel.getViewerCnt() > mAvatarAdapter.getItemCount()) {
                        mIsLoadViewer = true;
                        EventBus.getDefault().post(new BaseEvent.UserActionEvent(BaseEvent.UserActionEvent.EVENT_TYPE_REQUEST_LOOK_MORE_VIEWER, mMyRoomBaseDataModel, null));
                    }
                    //gradientItems();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // gradientItems();
            }
        });
        mAvatarRv.setItemAnimator(new DefaultItemAnimator());
        mAvatarRv.setLayoutManager(mAvatarLayoutManager);
        mAvatarRv.setHasFixedSize(true);

        mTicketTvUp = (TextView) findViewById(R.id.ticket_tv_up);
        mTicketTvDown = (TextView) findViewById(R.id.ticket_tv_down);
        pFont.setTextSize(mTicketTvUp.getTextSize());
        View ticketArea = findViewById(R.id.ticket_area);
        mTicketArea = new WrapperView(ticketArea);
        RxView.clicks(ticketArea)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {

                        EventBus.getDefault().post(new BaseEvent.UserActionEvent(BaseEvent.UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_TICKET
                                , mMyRoomBaseDataModel.getUid(), mMyRoomBaseDataModel.getTicket(), mMyRoomBaseDataModel.getRoomId()));
                    }
                });

        mViewerCountTv = (TextView) findViewById(R.id.view_tv);
    }

    public void setMyRoomDataSet(RoomBaseDataModel model) {
        this.mMyRoomBaseDataModel = model;
    }

    public void onActivityCreate() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void onActivityDestroy() {
        if (mUiHandler != null) {
            mUiHandler.removeCallbacksAndMessages(null);
        }
        if (mRefreshSubscription != null) {
            mRefreshSubscription.unsubscribe();
        }
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
            mMainHandler = null;
        }
        EventBus.getDefault().unregister(this);
    }

    public void initViewUseData() {
        updateAnchorNickName();
        updateOwnerView();
        updateTicketView();
        updateViewerCountView();
        updateViewers();
    }

    public void updateAnchorNickName() {
        String nickName = mMyRoomBaseDataModel.getNickName();
        if (!TextUtils.isEmpty(nickName)) {
//            mShowerNameTv.setText(nickName);
            CommonUtils.setMaxEcplise(mShowerNameTv,DisplayUtils.dip2px(75),nickName);
        } else if (mMyRoomBaseDataModel.getUid() > 0) {
//            mShowerNameTv.setText());
            CommonUtils.setMaxEcplise(mShowerNameTv,DisplayUtils.dip2px(75),String.valueOf(mMyRoomBaseDataModel.getUid()));
        } else {
            mShowerNameTv.setText(R.string.watch_owner_name_default);
        }
    }

    protected void updateOwnerView() {
        AvatarUtils.loadAvatarByUidTs(mOwnerIv, mMyRoomBaseDataModel.getUid(), mMyRoomBaseDataModel.getAvatarTs(), true);
        if (mMyRoomBaseDataModel.getCertificationType() > 0) {
            mOwnerBadgeIv.getLayoutParams().width = DisplayUtils.dip2px(16f);
            mOwnerBadgeIv.getLayoutParams().height = DisplayUtils.dip2px(16f);
            mOwnerBadgeIv.setImageDrawable(ItemDataFormatUtils.getCertificationImgSource(mMyRoomBaseDataModel.getCertificationType()));
        } else {
            mOwnerBadgeIv.getLayoutParams().width = DisplayUtils.dip2px(11f);
            mOwnerBadgeIv.getLayoutParams().height = DisplayUtils.dip2px(11f);
            mOwnerBadgeIv.setImageDrawable(ItemDataFormatUtils.getLevelSmallImgSource(mMyRoomBaseDataModel.getLevel()));
        }
    }


    private Runnable mDelayUpdateTicketRunnable = new Runnable() {
        @Override
        public void run() {
            MyLog.v(TAG + " mDelayUpdateTicketRunnable liveTicketNum=" + mMyRoomBaseDataModel.getTicket() + " mInitTicket=" + mMyRoomBaseDataModel.getInitTicket() + " mIsTicketing=" + mIsTicketing + " mIsTicketAnimating=" + mIsTicketAnimating);
//            mIsTicketing = false;
//            mMyRoomBaseDataModel.setTicketing(mIsTicketing);
//            int ticket = mMyRoomBaseDataModel.getTicket();
//            mTicketTvDown.setText(SpanUtils.addColorSpan(String.valueOf(ticket), getResources().getString(R.string.live_ticket_count, ticket),
//                    R.color.color_white, R.color.color_e5aa1e));
//            resetTicketView();
//            switchTicketTextModeAnime(mTicketTvUp.getText().toString(), mTicketTvDown.getText().toString());
            int totalTicket = mMyRoomBaseDataModel.getTicket();
            int ticket = mMyRoomBaseDataModel.getTicket() - mMyRoomBaseDataModel.getInitTicket();
            mMainHandler.removeCallbacks(mDelayUpdateTicketRunnable);
            if (!mIsTicketing && ticket <= 0) {
                mTicketTvUp.setText(SpanUtils.addColorSpan(String.valueOf(totalTicket), getResources().getString(R.string.live_ticket_count, totalTicket),
                        R.color.color_white, R.color.color_fed533));
                resetTicketView();
                mMainHandler.postDelayed(mDelayUpdateTicketRunnable, TICKET_DELAYED_TOTAL);
                return;
            }
            if (mIsTicketing) {
                //切换到星票
                mTicketTvUp.setText(SpanUtils.addColorSpan(String.valueOf(ticket), getResources().getString(R.string.live_ticket_count_this_time, ticket),
                        R.color.color_white, R.color.color_fed533));
                mTicketTvDown.setText(SpanUtils.addColorSpan(String.valueOf(totalTicket), getResources().getString(R.string.live_ticket_count, totalTicket),
                        R.color.color_white, R.color.color_fed533));
            } else {
                //切换到本场
                mTicketTvUp.setText(SpanUtils.addColorSpan(String.valueOf(totalTicket), getResources().getString(R.string.live_ticket_count, totalTicket),
                        R.color.color_white, R.color.color_fed533));
                mTicketTvDown.setText(SpanUtils.addColorSpan(String.valueOf(ticket), getResources().getString(R.string.live_ticket_count_this_time, ticket),
                        R.color.color_white, R.color.color_fed533));
            }
            mIsTicketing = !mIsTicketing;
            mMyRoomBaseDataModel.setTicketing(mIsTicketing);
            switchTicketTextModeAnime(mTicketTvUp.getText().toString(), mTicketTvDown.getText().toString());
            if (mIsTicketing) {
                mMainHandler.postDelayed(mDelayUpdateTicketRunnable, TICKET_DELAYED);
            } else {
                mMainHandler.postDelayed(mDelayUpdateTicketRunnable, TICKET_DELAYED_TOTAL);
            }
        }
    };

    private void switchTicketTextModeAnime(String orign, String replace) {
        if (!mIsTicketAnimating) {
            pFont.getTextBounds(orign, 0, orign.length(), rect);
            int ticketUpWidth = rect.width() + TICKET_PADDING;
            pFont.getTextBounds(replace, 0, replace.length(), rect);
            int ticketDownWidth = rect.width() + TICKET_PADDING;
            MyLog.v(TAG + " mTicketUpWidth=" + ticketUpWidth + " mTicketDownWidth=" + ticketDownWidth);

            List<Animator> animSeq = new ArrayList<>();
            AnimatorSet composition = new AnimatorSet();
            ObjectAnimator upTransY, downTransY, upAlpha, downAlpha, areaWidth;
            upTransY = ObjectAnimator.ofFloat(mTicketTvUp, "translationY", 0f, -TICKET_MAX_Y);
            upAlpha = ObjectAnimator.ofFloat(mTicketTvUp, "alpha", 1f, 0f);
            downTransY = ObjectAnimator.ofFloat(mTicketTvDown, "translationY", TICKET_MAX_Y, 0f);
            downAlpha = ObjectAnimator.ofFloat(mTicketTvDown, "alpha", 0f, 1f);
            areaWidth = ObjectAnimator.ofInt(mTicketArea, "width", ticketUpWidth, ticketDownWidth);
            composition.play(upTransY).with(upAlpha).with(downTransY).with(downAlpha).with(areaWidth);
            composition.setDuration(TICKET_ANIME_DURATION);
            animSeq.add(composition);
            if (mTicketAnimatorSet != null) {
                mTicketAnimatorSet.end();
                mTicketAnimatorSet = null;
            }
            mTicketAnimatorSet = new AnimatorSet();
            mTicketAnimatorSet.playSequentially(animSeq);
            mTicketAnimatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    mIsTicketAnimating = true;
                    resetTicketView();
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    if (mTicketAnimatorSet != null) {
                        mTicketAnimatorSet.removeAllListeners();
                        mTicketAnimatorSet = null;
                    }
                    resetTicketView();
                    mTicketTvUp.setText(mTicketTvDown.getText());
                    pFont.getTextBounds(mTicketTvUp.getText().toString(), 0, mTicketTvUp.getText().toString().length(), rect);
                    mTicketArea.setWidth(rect.width() + TICKET_PADDING);
                    mIsTicketAnimating = false;
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            mTicketAnimatorSet.start();
        }

    }

    public void resetData() {
        //外边切换主播会调用
        mMyRoomBaseDataModel.setInitTicket(-1);
        mMyRoomBaseDataModel.setTicketFirstIn(true);
        if (mTicketAnimatorSet != null) {

            mTicketAnimatorSet.removeAllListeners();
            mTicketAnimatorSet.end();
            mTicketAnimatorSet = null;
        }
        mIsTicketing = false;
        mMyRoomBaseDataModel.setTicketing(mIsTicketing);
        mMainHandler.removeCallbacks(mDelayUpdateTicketRunnable);
        resetTicketView();
    }

    private void resetTicketView() {
        mTicketTvUp.setTranslationY(0);
        mTicketTvUp.setAlpha(1);
        mTicketTvDown.setTranslationY(TICKET_MAX_Y);
        mTicketTvDown.setAlpha(1);
    }

    protected void updateTicketView() {
        int totalTicket = mMyRoomBaseDataModel.getTicket();
        int ticket = mMyRoomBaseDataModel.getTicket() - mMyRoomBaseDataModel.getInitTicket();
        MyLog.v(TAG + " updateTicketView liveTicketNum=" + mMyRoomBaseDataModel.getTicket() + " mInitTicket=" + mMyRoomBaseDataModel.getInitTicket() + " mIsTicketing=" + mIsTicketing + " mIsTicketAnimating=" + mIsTicketAnimating + " firstinf=" + mMyRoomBaseDataModel.isTicketFirstIn());


        if (mMyRoomBaseDataModel.isTicketFirstIn()) {
            mIsTicketing = false;
            mMyRoomBaseDataModel.setTicketing(mIsTicketing);
            mTicketTvUp.setText(SpanUtils.addColorSpan(String.valueOf(totalTicket), getResources().getString(R.string.live_ticket_count, totalTicket),
                    R.color.color_white, R.color.color_fed533));
            pFont.getTextBounds(mTicketTvUp.getText().toString(), 0, mTicketTvUp.getText().toString().length(), rect);
            int ticketUpWidth = rect.width() + TICKET_PADDING;
            mTicketArea.setWidth(ticketUpWidth);
            if (mMyRoomBaseDataModel.getInitTicket() >= 0) {
                //初始值-1 大于0 说明数据已经获取
                mMyRoomBaseDataModel.setTicketFirstIn(false);
                mMainHandler.removeCallbacks(mDelayUpdateTicketRunnable);
                mMainHandler.postDelayed(mDelayUpdateTicketRunnable, TICKET_DELAYED_TOTAL);
            }
        } else {
            if (mIsTicketAnimating) {
                if (mIsTicketing) {
                    //本场 正在替换 星票
                    mTicketTvUp.setText(SpanUtils.addColorSpan(String.valueOf(totalTicket), getResources().getString(R.string.live_ticket_count, totalTicket),
                            R.color.color_white, R.color.color_fed533));
                    mTicketTvDown.setText(SpanUtils.addColorSpan(String.valueOf(ticket), getResources().getString(R.string.live_ticket_count_this_time, ticket),
                            R.color.color_white, R.color.color_fed533));
                } else {
                    //星票 正在替换 本场
                    mTicketTvUp.setText(SpanUtils.addColorSpan(String.valueOf(ticket), getResources().getString(R.string.live_ticket_count_this_time, ticket),
                            R.color.color_white, R.color.color_fed533));
                    mTicketTvDown.setText(SpanUtils.addColorSpan(String.valueOf(totalTicket), getResources().getString(R.string.live_ticket_count, totalTicket),
                            R.color.color_white, R.color.color_fed533));
                }
            } else {
                if (mIsTicketing) {
                    mTicketTvUp.setText(SpanUtils.addColorSpan(String.valueOf(ticket), getResources().getString(R.string.live_ticket_count_this_time, ticket),
                            R.color.color_white, R.color.color_fed533));
                } else {
                    mTicketTvUp.setText(SpanUtils.addColorSpan(String.valueOf(totalTicket), getResources().getString(R.string.live_ticket_count, totalTicket),
                            R.color.color_white, R.color.color_fed533));
                }
                pFont.getTextBounds(mTicketTvUp.getText().toString(), 0, mTicketTvUp.getText().toString().length(), rect);
                int ticketUpWidth = rect.width() + TICKET_PADDING;
                mTicketArea.setWidth(ticketUpWidth);
            }
        }


//        //星票变化显示本场 10s后还原  产品以前定义 现在改掉了 防止加回来 先注释掉
//        if (mMyRoomBaseDataModel.getInitTicket() < 0 || mMyRoomBaseDataModel.isTicketFirstIn()) {
//            if (mMyRoomBaseDataModel.getInitTicket() >= 0) {
//                //初始值-1 大于0 说明数据已经获取
//                mMyRoomBaseDataModel.setTicketFirstIn(false);
//            }
//            //第一次进入 本场星票为0 只显示全部星票
//            mTicketTvUp.setText(SpanUtils.addColorSpan(String.valueOf(totalTicket), getResources().getString(R.string.live_ticket_count, totalTicket),
//                    R.color.color_white, R.color.color_e5aa1e));
//            if (mTicketAnimatorSet != null) {
//                mTicketAnimatorSet.removeAllListeners();
//                mTicketAnimatorSet.end();
//                mTicketAnimatorSet = null;
//            }
//            resetTicketView();
//            pFont.getTextBounds(mTicketTvUp.getText().toString(), 0, mTicketTvUp.getText().toString().length(), rect);
//            int ticketUpWidth = rect.width() + TICKET_PADDING;
//            mTicketArea.setWidth(ticketUpWidth);
//        } else {
//            //显示本场星票
//            if (mIsTicketing) {
//                //处于本场状态无需播放动画
//                if (mIsTicketAnimating) {
//                    //正在动画 上面是总共 下面是本场
//                    mTicketTvUp.setText(SpanUtils.addColorSpan(String.valueOf(totalTicket), getResources().getString(R.string.live_ticket_count, totalTicket),
//                            R.color.color_white, R.color.color_e5aa1e));
//                    mTicketTvDown.setText(SpanUtils.addColorSpan(String.valueOf(ticket), getResources().getString(R.string.live_ticket_count_this_time, ticket),
//                            R.color.color_white, R.color.color_e5aa1e));
//                } else {
//                    //动画结束 直接显示本场就可以
//                    mTicketTvUp.setText(SpanUtils.addColorSpan(String.valueOf(ticket), getResources().getString(R.string.live_ticket_count_this_time, ticket),
//                            R.color.color_white, R.color.color_e5aa1e));
//                    resetTicketView();
//                }
//                mMainHandler.postDelayed(mDelayUpdateTicketRunnable, TICKET_DELAYED);
//            } else {
//                mIsTicketing = true;
//                mMyRoomBaseDataModel.setTicketing(mIsTicketing);
//                mTicketTvUp.setText(SpanUtils.addColorSpan(String.valueOf(totalTicket), getResources().getString(R.string.live_ticket_count, totalTicket),
//                        R.color.color_white, R.color.color_e5aa1e));
//                mTicketTvDown.setText(SpanUtils.addColorSpan(String.valueOf(ticket), getResources().getString(com.mi.live.data.R.string.live_ticket_count_this_time, ticket),
//                        R.color.color_white, R.color.color_e5aa1e));
//                switchTicketTextModeAnime(mTicketTvUp.getText().toString(), mTicketTvDown.getText().toString());
//                mMainHandler.postDelayed(mDelayUpdateTicketRunnable, TICKET_DELAYED);
//            }
//        }
    }

    protected void updateViewerCountView() {
        int viewerCnt = mMyRoomBaseDataModel.getViewerCnt();
        mViewerCountTv.setText(viewerCnt > 0 ? String.valueOf(viewerCnt) : "");
    }

    protected long mLastUpdateTime = 0;
    protected Subscription mRefreshSubscription;
    protected Runnable mUpdateViewersRunnable = new Runnable() {
        @Override
        public void run() {
            MyLog.d(TAG, "updateViewers run");
            List<ViewerModel> viewerList = mMyRoomBaseDataModel.getViewersList();
            final int viewerCount = mMyRoomBaseDataModel.getViewerCnt();
            if (!viewerList.isEmpty()) {
                mLastUpdateTime = System.currentTimeMillis();
                List<ViewerModel> temp = new ArrayList<>(viewerList.size());
                temp.addAll(viewerList);
                if (mRefreshSubscription != null && !mRefreshSubscription.isUnsubscribed()) {
                    mRefreshSubscription.unsubscribe();
                }
                //  刷新头像
                mRefreshSubscription = Observable.just(temp)
                        .observeOn(Schedulers.computation())
                        .map(new Func1<List<ViewerModel>, List<ViewerModel>>() {
                            @Override
                            public List<ViewerModel> call(List<ViewerModel> temp1) {
                                List<ViewerModel> top10List = new ArrayList<>();
                                List<ViewerModel> otherList = new ArrayList<>();
                                int lastLevel = 0;
                                for (int i = 0; i < temp1.size(); i++) {
                                    ViewerModel v = temp1.get(i);
                                    if (i < WatchTopInfoBaseView.EXACT_VIEWER_NUM) {
                                        top10List.add(v);
                                        if (i == WatchTopInfoBaseView.EXACT_VIEWER_NUM - 1) {
                                            lastLevel = v.getLevel();
                                        }
                                    } else {
                                        // 保证等级总体排序的正确性
                                        if (v.getLevel() <= lastLevel) {
                                            otherList.add(v);
                                        }
                                    }
                                }
                                // 对other排序，更新头像
                                Collections.sort(otherList, new Comparator<ViewerModel>() {
                                    @Override
                                    public int compare(ViewerModel lhs, ViewerModel rhs) {
                                        return rhs.getLevel() - lhs.getLevel();
                                    }
                                });
                                temp1.clear();
                                temp1.addAll(top10List);
                                temp1.addAll(otherList);
                                // 保证头像个数不可能大于观众人数
                                if (temp1.size() > viewerCount && viewerCount > 0) {
                                    return temp1.subList(0, viewerCount);
                                }
                                return temp1;
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .compose(((RxActivity) getContext()).<List<ViewerModel>>bindUntilEvent(ActivityEvent.DESTROY))
                        .subscribe(new Observer<List<ViewerModel>>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                MyLog.e(TAG,e);
                            }

                            @Override
                            public void onNext(List<ViewerModel> temp) {
//                                updatePositionAndAlpha();
                                mAvatarAdapter.setViewerList(temp);
                                mIsLoadViewer = false;
                            }
                        });
            } else {
                // 置为空
                List<ViewerModel> temp = new ArrayList<>();
                mAvatarAdapter.setViewerList(temp);
                mIsLoadViewer = false;
            }
        }
    };

    private Handler mUiHandler = new Handler();

    protected void updateViewers() {
        MyLog.d(TAG, "updateViewers size:" + mMyRoomBaseDataModel.getViewersList().size());
        long now = System.currentTimeMillis();
        if (now - mLastUpdateTime < 3000) {
            mUiHandler.removeCallbacks(mUpdateViewersRunnable);
            mUiHandler.postDelayed(mUpdateViewersRunnable, 3000);
        } else {
            mUiHandler.removeCallbacks(mUpdateViewersRunnable);
            mUpdateViewersRunnable.run();
        }
    }

    protected void updateManagers() {
    }


    abstract public void onUserInfoComplete();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RoomDataChangeEvent event) {
        if (event.source != mMyRoomBaseDataModel || this.getVisibility() == GONE) {
            return;
        }
        switch (event.type) {
            case RoomDataChangeEvent.TYPE_CHANGE_USER_INFO_COMPLETE: {
                onUserInfoComplete();
            }
            break;
            case RoomDataChangeEvent.TYPE_CHANGE_AVATAR: {
                updateOwnerView();
            }
            break;
            case RoomDataChangeEvent.TYPE_CHANGE_TICKET: {
                updateTicketView();
            }
            break;
            case RoomDataChangeEvent.TYPE_CHANGE_VIEWER_COUNT: {
                updateViewerCountView();
            }
            break;
            case RoomDataChangeEvent.TYPE_CHANGE_VIEWERS: {
                updateViewers();
            }
            break;
            case RoomDataChangeEvent.TYPE_CHANGE_USER_MANAGER: {
                updateManagers();
            }
            break;
        }
    }

    private void resetAlpha() {
        int first = mAvatarLayoutManager.findFirstVisibleItemPosition();
        int last = mAvatarLayoutManager.findLastVisibleItemPosition();
        for (int i = first; i <= last; i++) {
            View item = mAvatarLayoutManager.findViewByPosition(i);
            if (item != null) {
                item.setAlpha(1.0f);
            }
        }
    }

    private float getItemAlpha(View item) {
        float alpha = 1.0f;
        if (item != null) {
            alpha = (mAvatarRv.getWidth() - item.getLeft()) / GRADIENT_WIDTH;
            alpha = Math.min(Math.max(0, alpha), 1);
        }
        return alpha;
    }

    private void updatePositionAndAlpha() {     // 每次更新 重新计算adapter中最右item 对应的position 及透明度 在onBinder() 中设置alpha
        int lastVisiblePosition = mAvatarLayoutManager.findLastVisibleItemPosition();
        View lastOne = mAvatarLayoutManager.findViewByPosition(lastVisiblePosition);
        View lastTwo = mAvatarLayoutManager.findViewByPosition(lastVisiblePosition - 1);
        float lastOneAlpha = getItemAlpha(lastOne);
        float lastTwoAlpha = getItemAlpha(lastTwo);
        Pair<Integer, Float> positionAndAlpha1, positionAndAlpha2;
        positionAndAlpha1 = new Pair<>(lastVisiblePosition, lastOneAlpha);
        positionAndAlpha2 = new Pair<>(lastVisiblePosition - 1, lastTwoAlpha);
        mAvatarAdapter.setLastItemPositionAndAlpha(positionAndAlpha1);
        mAvatarAdapter.setLastSecondPositionAndAlpha(positionAndAlpha2);
    }

    private void gradientItems() {   // 滑动时 直接在此设置item 透明度
        resetAlpha();
        int lastVisiblePosition = mAvatarLayoutManager.findLastVisibleItemPosition();
        View lastOne = mAvatarLayoutManager.findViewByPosition(lastVisiblePosition);
        View lastTwo = mAvatarLayoutManager.findViewByPosition(lastVisiblePosition - 1);
        if (lastOne != null) {
            lastOne.setAlpha(getItemAlpha(lastOne));
        }
        if (lastTwo != null) {
            lastTwo.setAlpha(getItemAlpha(lastTwo));
        }
    }

    public void onScreenOrientationChanged(boolean isLandScape) {
        if (!isLandScape) {
            resetAlpha();
        }
        mIsLandScape = isLandScape;
    }

    public static class WrapperView {
        private View mTarget;

        public WrapperView(View target) {
            mTarget = target;
        }

        public int getWidth() {
            return mTarget.getLayoutParams().width;
        }

        public void setWidth(int width) {
            mTarget.getLayoutParams().width = width;
            mTarget.requestLayout();
        }

        public View getmTarget() {
            return mTarget;
        }
    }

    // 不想使用ButterKnife，可以使用下面方法简化代码
    public <V extends View> V $(int id) {
        return (V) findViewById(id);
    }
}
