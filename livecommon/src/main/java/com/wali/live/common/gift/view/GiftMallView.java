package com.wali.live.common.gift.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.base.activity.BaseSdkActivity;
import com.base.activity.RxActivity;
import com.base.activity.assist.IBindActivityLIfeCycle;
import com.base.dialog.MyAlertDialog;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.mvp.specific.RxRelativeLayout;
import com.base.preference.PreferenceUtils;
import com.base.utils.display.DisplayUtils;
import com.base.utils.toast.ToastUtils;
import com.jakewharton.rxbinding.view.RxView;
import com.live.module.common.R;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.event.GiftEventClass;
import com.mi.live.data.gift.model.GiftType;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.common.gift.adapter.GiftDisplayRecycleViewAdapter;
import com.wali.live.common.gift.adapter.GiftDisplayViewPagerAdapter;
import com.wali.live.common.gift.presenter.GiftMallPresenter;
import com.wali.live.common.gift.utils.MyAnimationUtils;
import com.wali.live.common.view.ErrorView;
import com.wali.live.common.view.ViewPagerWithCircleIndicator;
import com.wali.live.dao.Gift;
import com.wali.live.pay.fragment.BalanceFragment;
import com.wali.live.pay.manager.PayManager;
import com.wali.live.pay.model.BalanceDetail;
import com.wali.live.proto.PayProto;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by zjn on 16-11-30.
 */
public class GiftMallView extends RxRelativeLayout implements IBindActivityLIfeCycle {
    public static final String SP_FILENAME_GIFTMALL_CONFIG = "giftmall.config";

    public static final String FIRST_SHOW_GIFT_MALL_VIEW = "isFirstShowGiftMall";

    private RoomBaseDataModel mMyRoomData; // 主播id

    private ViewPagerWithCircleIndicator mGiftDisplayViewPager; // 礼物页面

    private GiftDisplayViewPagerAdapter mGiftDisplayViewPagerAdapter; // 适配器

    private TextView mBalanceTv; // 余额

    private ErrorView mGiftListErrorView;// 错误页面

    private GiftDisPlayItemView mSelectedView; // 选中的gift view

    private ObjectAnimator mSelectedShakeAnimation; // 选中时的抖动动画

    private GiftMallPresenter.GiftWithCard mSelectedGift;// 选中的gift

    private ContinueSendBtn mContinueSendBtn; // 连送按钮

    private boolean mIsLandscape = false;// 是否是横屏

    private boolean mHasLoadView = false;// 是否加载视图

    private RecyclerView mGiftDisplayRecycleView;

    private GiftDisplayRecycleViewAdapter mGiftDisplayRecycleViewAdapter;

    private TextView mSiliverDiamond;

    private Subscription mDiamondTipsSubscription;//第一次打开礼物橱窗提示优先使用银钻订阅

    private RelativeLayout mGiftBottomPanel;

    private Subscription mTipsHideSubscription;//银钻展示时间的订阅

    private Subscription mGiftMallGuidePageSubscription;

    private ViewStub mGiftMallGuidePageViewStub;
    private RelativeLayout mGiftMallGuidePage;

    //以下为直接点击礼物橱窗赠送礼物状态位
    private boolean mIsBuyGiftBySendBtn;//true表示使用赠送按钮显示礼物,false:直接点击礼物橱窗赠送礼物

    private boolean mIsBigGiftBtnShowFlag;//大礼物被选中标志位-mSelectGift.gift.getCanContinuous()

    private boolean mSelectViewClickedFlag = false;//礼物橱窗item被选中按钮标志

    private boolean mIsContinueSendFlag = false;//彩蛋礼物引入判断是否处于连送状态

    private static final int TIPS_TYPE_BALANCE = 1;
    private static final int TIPS_TYPE_SILIVER_DIAMOND = 2;
    private static final int TIPS_TYPE_MI_COIN = 3;

    private Activity mActivity;

    //TODO 开始修改
    private TextView mMallGiftTv;
    private TextView mPktGiftTv;
    private View mSlideGift;
    private View mSlidePkt;

    private TextView mRechargeTv;
    //TODO 准备考虑去掉
    private TextView mSendGiftTv; // 发送按钮

    private TextView mPktDetailTv;

    //标记当前的选择的礼物状态
    private boolean mIsMallGift = true;

    public GiftMallView(Context context) {
        super(context);
        init(context);
    }

    public GiftMallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GiftMallView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public String getTAG() {
        return TAG;
    }

    public void onActivityCreate() {
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        FirstShowGiftMallTips();
    }

    private void init(Context context) {
        if (!mIsLandscape) {
            inflate(context, R.layout.gift_mall_view, this);
        } else {
            inflate(context, R.layout.gift_mall_landscape_view, this);
        }
        bindView();
        mHasLoadView = true;
    }

    //转屏时重置状态
    public void resetStatus() {
        mHasLoadView = false;
    }

    private GiftDisplayRecycleViewAdapter.GiftItemListener mGiftItemListener = new GiftDisplayRecycleViewAdapter.GiftItemListener() {

        @Override
        public void clickGiftItem(View v, GiftMallPresenter.GiftWithCard info, int position) {
            MyLog.d(TAG, "clickGiftItem v:" + v + ",info:" + info);

            if (mNormalBuyAnimationSet != null) {
                mNormalBuyAnimationSet.cancel();
            }

            if (info.gift.getCatagory() == GiftType.RED_ENVELOPE_GIFT) {
                getRxActivity().onBackPressed();
                mGiftMallPresenter.showSendEnvelopeView();
            } else {
                if (v != mSelectedView) {
                    if (mSelectedView != null) {
                        clearAllGiftItemStatus();
                        mSelectedView.hideContinueSendBtn();
                        mSelectedView.changeCornerStatus(mSelectedGift.gift.getIcon(), false);
                        mSendGiftTv.setVisibility(View.VISIBLE);
                        mContinueSendBtn.setVisibility(View.GONE);
                    }

                    mGiftMallPresenter.resetRandomGift();
                    mGiftMallPresenter.resetContinueSend();
                }
                mGiftMallPresenter.unsubscribeSountDownSubscription();

                if (v != null) {
                    MyLog.d(TAG, "clickGiftItem");
                    cancelSelectView(mSelectedView);
                    mSelectedGift = info;
                    selectView(v, true);
                    // 设置按钮可点击
                    mSendGiftTv.setEnabled(true);
                }

                if (mGiftDisplayViewPagerAdapter != null && !mIsLandscape) {
                    //礼物橱窗item信息入口
                    mGiftDisplayViewPagerAdapter.setSelectedGiftInfo(v, info, position, mGiftDisplayViewPager.getCurrentItem());
                }

                if (!mSelectViewClickedFlag) {
                    mSelectViewClickedFlag = true;
                    return;
                }

                MyLog.d(TAG, "mSelectedGift.gift" + mSelectedGift.gift.toString());
                MyLog.d(TAG, "mSelectedGift.gift.getCanContinuous():" + mSelectedGift.gift.getCanContinuous() + " mIsBigGiftBtnShowFlag" + mIsBigGiftBtnShowFlag);
                if (mSelectedGift.gift.getCanContinuous() || mIsBigGiftBtnShowFlag) {
                    //引入

                    if (!judgeBuyGiftCondition()) {
                        mSelectedView.hideContinueSendBtn();
                        return;
                    }
                    if (mSelectedGift.gift.getCanContinuous()) {
                        mSelectedView.showContinueSendBtn(true);
                    }

                    //是否显示角标
                    mSelectedView.changeCornerStatus(mSelectedGift.gift.getIcon(), true);

                    mIsBuyGiftBySendBtn = false;
                    mGiftMallPresenter.buyGift();
                } else {
                    mSelectedView.changeContinueSendBtnBackGroup(true);

                    mSelectedView.showContinueSendBtn(false);
                    mIsBigGiftBtnShowFlag = true;
                }
            }
        }

        /**
         * 判断当前是否符合购买礼物
         * @return false 条件不符合, true 可以发起购买
         */
        private boolean judgeBuyGiftCondition() {
            if (mSelectedGift.gift.getCatagory() == GiftType.MAGIC_GIFT && !mMyRoomData.isSupportMagicFace()) {
                ToastUtils.showToast(mActivity, getResources().getString(R.string.no_support_magic_gift_tips));
                return false;
            }

            if (mSelectedGift.card == null || mSelectedGift.card.getGiftCardCount() <= 0) {
                int limitLevel = mSelectedGift.gift.getLowerLimitLevel();
                if (mSelectedGift.gift.getCatagory() == GiftType.PRIVILEGE_GIFT && limitLevel > MyUserInfoManager.getInstance().getUser().getLevel()) {
                    //特权礼物
                    Toast.makeText(getContext(), getResources().getQuantityString(R.plurals.verify_user_level_toast, limitLevel, limitLevel), Toast.LENGTH_SHORT).show();
                    return false;
                } else if ((mSelectedGift.gift.getCatagory() == GiftType.Mi_COIN_GIFT && (mSelectedGift.gift.getPrice() / 10) > mGiftMallPresenter.getCurrentTotalBalance())
                        || (mSelectedGift.gift.getCatagory() != GiftType.Mi_COIN_GIFT && mSelectedGift.gift.getPrice() > mGiftMallPresenter.getCurrentTotalBalance())) {
//                    showInsufficientBalanceTips();
                    EventBus.getDefault().post(new GiftEventClass.GiftMallEvent(GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_GO_RECHARGE));
                    return false;
                }
            }

            return true;
        }

        @Override
        public Gift getSelectedGift() {
            if (mSelectedGift != null) {
                return mSelectedGift.gift;
            }
            return null;
        }

        @Override
        public void updateSelectedGiftView(View v, GiftMallPresenter.GiftWithCard info) {
            // 选中的v因为刷新改变了
            if (v != mSelectedView) {
                cancelSelectView(mSelectedView);
                selectView(v, false);
                mSelectedGift = info;
                // 设置按钮可点击
                mSendGiftTv.setEnabled(true);
            }
        }

        @Override
        public void updateContinueSend() {
            MyLog.d(TAG, "updateContinueSend");
            mGiftMallPresenter.resetContinueSend();
        }
    };

    protected void bindView() {
        if (!mIsLandscape) {
//            mRefreshRechargeIv = (RefreshIconImageView) findViewById(R.id.refresh_iv);
            mGiftDisplayViewPager = (ViewPagerWithCircleIndicator) findViewById(R.id.gift_display_viewpager);
            mGiftDisplayViewPagerAdapter = new GiftDisplayViewPagerAdapter((Activity) getContext(), mGiftItemListener);
            mGiftDisplayViewPager.setAdapter(mGiftDisplayViewPagerAdapter);
            mGiftDisplayViewPager.setItemWidth(12);
            mGiftDisplayViewPager.setItemHeight(3);
            mGiftListErrorView = (ErrorView) findViewById(R.id.gift_list_error_view);
            mGiftListErrorView.setRetryOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mGiftMallPresenter.loadDataFromCache("retryOnClickListener");
                }
            });
        } else {
            mGiftDisplayRecycleView = (RecyclerView) findViewById(R.id.gift_display_recycleview);
            mGiftDisplayRecycleViewAdapter = new GiftDisplayRecycleViewAdapter(getContext(), true, mGiftItemListener);
            mGiftDisplayRecycleView.setAdapter(mGiftDisplayRecycleViewAdapter);
            mGiftDisplayRecycleView.setLayoutManager(new LinearLayoutManager(getRxActivity(), LinearLayoutManager.HORIZONTAL, false));
//            mGiftDisplayRecycleView.addItemDecoration(new GiftDisplayDividerItemDecoration(GiftDisplayDividerItemDecoration.HORIZONTAL_LIST));
            mGiftDisplayRecycleView.setHasFixedSize(true);
        }
        mGiftBottomPanel = (RelativeLayout) findViewById(R.id.gift_bottom_panel);
        mGiftMallGuidePageViewStub = (ViewStub) findViewById(R.id.gift_mall_guide_page_viewstub);
        mBalanceTv = (TextView) findViewById(R.id.diamond_max_tv);
        mSiliverDiamond = (TextView) findViewById(R.id.diamond_siliver_tv);

        setBalanceInfo();
        mContinueSendBtn = (ContinueSendBtn) findViewById(R.id.continue_send_btn);
        // 顶部透明view点击
        RxView.clicks(findViewById(R.id.top_transparent_view))
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        // 点击顶部透明区域
                        //TODO 一定记得加上
                        EventBus.getDefault().post(new GiftEventClass.GiftMallEvent(GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_HIDE_MALL_LIST));
                    }
                });

        mSendGiftTv = $rxClick(R.id.send_gift, 500, new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                if (mNormalBuyAnimationSet == null) {
                    ObjectAnimator scaleX = ObjectAnimator.ofFloat(mSendGiftTv, "scaleX", 1.5f, 1f);
                    ObjectAnimator scaleY = ObjectAnimator.ofFloat(mSendGiftTv, "scaleY", 1.5f, 1f);
                    scaleX.setDuration(50);
                    scaleY.setDuration(50);
                    mNormalBuyAnimationSet = new AnimatorSet();
                    mNormalBuyAnimationSet.play(scaleX).with(scaleY);
                    mNormalBuyAnimationSet.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationCancel(Animator animation) {
                            if (mCancelBuyAnimationSet == null) {
                                ObjectAnimator scaleX = ObjectAnimator.ofFloat(mSendGiftTv, "scaleX", 1f);
                                ObjectAnimator scaleY = ObjectAnimator.ofFloat(mSendGiftTv, "scaleY", 1f);
                                scaleX.setDuration(1);
                                scaleY.setDuration(1);
                                mCancelBuyAnimationSet = new AnimatorSet();
                                mCancelBuyAnimationSet.play(scaleX).with(scaleY);
                            }
                            mCancelBuyAnimationSet.start();
                        }
                    });
                } else {
                    if (mNormalBuyAnimationSet.isRunning()) {
                        mNormalBuyAnimationSet.cancel();
                    }
                }
                if (mSelectedGift != null) {
                    if (mSelectedGift.gift.getCatagory() == GiftType.MAGIC_GIFT && !mMyRoomData.isSupportMagicFace()) {
                        ToastUtils.showToast(R.string.no_support_magic_gift_tips);
                        return;
                    }
                    // 符合购买条件才播放动画
                    if ((mSelectedGift.card != null && mSelectedGift.card.getGiftCardCount() <= 0) || (mSelectedGift.gift.getPrice() <= mGiftMallPresenter.getCurrentTotalBalance())) {
                        mNormalBuyAnimationSet.start();
                    }
                }
                mIsBuyGiftBySendBtn = true;
                mGiftMallPresenter.buyGift();
            }
        });
        mSendGiftTv.setEnabled(mSelectedGift != null);

        // 连送按钮的点击
        RxView.clicks(mContinueSendBtn)
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        mIsBuyGiftBySendBtn = true;
                        mGiftMallPresenter.buyGift();
                    }
                });

        // 充值按钮的点击
        mRechargeTv = $rxClick(R.id.recharge_tv, 500, new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                //TODO 一定记得加上
                EventBus.getDefault().post(new GiftEventClass.GiftMallEvent(GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_GO_RECHARGE));
            }
        });

        //金钻银钻点击事件处理一样
        RxView.clicks(mBalanceTv)
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        showDiamondTips(TIPS_TYPE_BALANCE);
                    }
                });

        RxView.clicks(mSiliverDiamond)
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        showDiamondTips(TIPS_TYPE_SILIVER_DIAMOND);
                    }
                });

        mPktDetailTv = $rxClick(R.id.pkt_detail_tv, 300, new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                if (!mIsMallGift) {
                    showPktGiftDetail();
                }
            }
        });

        mMallGiftTv = $rxClick(R.id.tv_gift, 300, new Action1() {
            @Override
            public void call(Object o) {
                if (!mIsMallGift) {
                    clickMallGift();
                }
            }
        });

        mPktGiftTv = $rxClick(R.id.tv_pkt_gift, 300, new Action1() {
            @Override
            public void call(Object o) {
                if (mIsMallGift) {
                    clickPktGift();
                }
            }
        });

        mSlideGift = $(R.id.slide_gift);
        mSlidePkt = $(R.id.slide_pkt);
    }

    public boolean isMallGift() {
        return mIsMallGift;
    }

    /**
     * 选择花钱礼物，默认选中花钱礼物
     */
    private void clickMallGift() {
        mIsMallGift = true;
        setGiftTabBackground();
        switchMallType();

        // 先直接加载，之后加缓存策略
        mGiftMallPresenter.loadDataFromCache("clickMallGift");
    }

    /**
     * 选择背包礼物
     */
    private void clickPktGift() {
        mIsMallGift = false;
        setGiftTabBackground();
        switchMallType();

        // 先直接加载，之后加缓存策略
        mGiftMallPresenter.loadDataFromCache("clickPktGift");
    }

    void setGiftTabBackground() {
        if (mIsMallGift) {
            mMallGiftTv.setSelected(true);
            mPktGiftTv.setSelected(false);
            mMallGiftTv.setBackgroundColor(Color.TRANSPARENT);

            mSlideGift.setVisibility(View.VISIBLE);
            mSlidePkt.setVisibility(View.GONE);

            mBalanceTv.setVisibility(View.VISIBLE);
            mSiliverDiamond.setVisibility(View.VISIBLE);
            mRechargeTv.setVisibility(View.VISIBLE);
            mSendGiftTv.setVisibility(View.VISIBLE);

            mPktDetailTv.setVisibility(View.GONE);
        } else {
            mMallGiftTv.setSelected(false);
            mPktGiftTv.setSelected(true);
            mPktGiftTv.setBackgroundColor(Color.TRANSPARENT);

            mSlideGift.setVisibility(View.GONE);
            mSlidePkt.setVisibility(View.VISIBLE);

            mBalanceTv.setVisibility(View.GONE);
            mSiliverDiamond.setVisibility(View.GONE);
            mRechargeTv.setVisibility(View.GONE);
            mSendGiftTv.setVisibility(View.GONE);

            mPktDetailTv.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 切换礼物状态，花钱礼物和 包裹礼物（礼物卡，免费礼物）
     */
    public void switchMallType() {
        cancelSelectView(mSelectedView);
        mSelectedGift = null;

        clearAllGiftItemStatus();
        resetGiftItemBtnInfo();
    }

    private Subscription mGetBalanceDetailSub;

    /**
     * 点击包裹礼物详情
     */
    private void showPktGiftDetail() {
        if (mGetBalanceDetailSub != null && !mGetBalanceDetailSub.isUnsubscribed()) {
            mGetBalanceDetailSub.unsubscribe();
        }
        mGetBalanceDetailSub = PayManager.getBalanceDetailRsp()
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<PayProto.QueryBalanceDetailResponse, Observable<BalanceDetail>>() {
                    @Override
                    public Observable<BalanceDetail> call(PayProto.QueryBalanceDetailResponse rsp) {
                        if (rsp == null) {
                            return Observable.error(new Exception("QueryBalanceDetailResponse is null"));
                        } else if (rsp.getRetCode() != 0) {
                            return Observable.error(new Exception("QueryBalanceDetailResponse.retCode:" + rsp.getRetCode()));
                        }
                        return Observable.just(BalanceDetail.parseOnlyPktFrom(rsp));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(getRxActivity().<BalanceDetail>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new Action1<BalanceDetail>() {
                    @Override
                    public void call(BalanceDetail balanceDetail) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(BalanceFragment.BUNDLE_KEY_BALANCE_DETAIL, balanceDetail);
                        bundle.putSerializable(BalanceFragment.BUNDLE_KEY_FROM, BalanceFragment.BUNDLE_VALUE_FROM_GIFT);
                        BalanceFragment.openFragment((BaseSdkActivity) getContext(), bundle, null);

                        //隐藏包裹界面,防止消耗onBackPressed()事件
                        EventBus.getDefault().post(new GiftEventClass.GiftMallEvent(GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_HIDE_MALL_LIST));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable.getMessage());
                    }
                });
    }

    private void unSubscribeDiamondTipSubscription() {
        if (mTipsHideSubscription != null && !mTipsHideSubscription.isUnsubscribed()) {
            mTipsHideSubscription.unsubscribe();
        }

        if (mDiamondTipsSubscription != null && !mDiamondTipsSubscription.isUnsubscribed()) {
            mDiamondTipsSubscription.unsubscribe();
        }

        if (mGiftMallGuidePageSubscription != null && !mGiftMallGuidePageSubscription.isUnsubscribed()) {
            mGiftMallGuidePageSubscription.isUnsubscribed();
        }
    }

    private GiftDiamondTips mDiamondTips;

    private void unsubscribeTipsHideSubscription() {
        if (mTipsHideSubscription != null && !mTipsHideSubscription.isUnsubscribed()) {
            if (mDiamondTips != null) {
                mDiamondTips.setVisibility(GONE);
                mDiamondTips = null;
            }
            mTipsHideSubscription.unsubscribe();
        }
    }

    /**
     * 根据横竖屏调整toast位置并展示
     *
     * @param tipsType
     */
    private void showDiamondTips(int tipsType) {
        unsubscribeTipsHideSubscription();

        if (mDiamondTips == null) {
            mDiamondTips = new GiftDiamondTips(getContext(), tipsType);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int left;
            int bottom;
//            if (!mIsLandscape) {
//                mDiamondTips.changeTipsBackGroup(false);//改变tips的背景
//
//                bottom = DisplayUtils.dip2px(getContext(), 40f);
//                if (tipsType == TIPS_TYPE_BALANCE) {
//                    left = mBalanceTv.getLeft() + mBalanceTv.getMeasuredWidth() / 2 - DisplayUtils.dip2px(getContext(), 10f);
//                } else {
//                    left = mSiliverDiamond.getLeft() + mSiliverDiamond.getMeasuredWidth() / 2 - DisplayUtils.dip2px(getContext(), 10f);
//                }
//
//                if (left < DisplayUtils.dip2px(getContext(), 13.33f)) {
//                    left = DisplayUtils.dip2px(getContext(), 13.33f);
//                }
//                layoutParams.setMargins(left, 0, 0, bottom);
//            } else {
//                mDiamondTips.changeTipsBackGroup(true);
//
//                left = mGiftDisplayRecycleView.getMeasuredWidth() - getMeasureViewParams(mDiamondTips)[0] + DisplayUtils.dip2px(getContext(), 30f);
//                if (tipsType == TIPS_TYPE_MI_COIN) {
//                    bottom = DisplayUtils.dip2px(getContext(), 50f);
//                } else if (tipsType == TIPS_TYPE_BALANCE) {
//                    bottom = DisplayUtils.dip2px(getContext(), 50f);
//                } else {
//                    bottom = DisplayUtils.dip2px(getContext(), 25f);
//                }
//                layoutParams.setMargins(left, 0, 0, bottom);
//            }
            mDiamondTips.changeTipsBackGroup(false);//改变tips的背景

            if (!mIsLandscape) {
                bottom = DisplayUtils.dip2px(getContext(), 40f);
            } else {
                bottom = DisplayUtils.dip2px(getContext(), 30f);
            }
//            if (tipsType == TIPS_TYPE_MI_COIN) {
//                left = mMiCoinBalance.getLeft() + mMiCoinBalance.getMeasuredWidth() - DisplayUtils.dip2px(getContext(), 22f);
//            } else
            if (tipsType == TIPS_TYPE_BALANCE) {
                left = mBalanceTv.getLeft() + mBalanceTv.getMeasuredWidth() - DisplayUtils.dip2px(getContext(), 22f);
            } else {
                left = mSiliverDiamond.getLeft() + mSiliverDiamond.getMeasuredWidth() - DisplayUtils.dip2px(getContext(), 22f);
            }

            if (left < DisplayUtils.dip2px(getContext(), 13.33f)) {
                left = DisplayUtils.dip2px(getContext(), 13.33f);
            }

            layoutParams.setMargins(left, 0, 0, bottom);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            mGiftBottomPanel.addView(mDiamondTips, layoutParams);
        }

        mDiamondTips.setOnShowDiamondTipListener(new GiftDiamondTips.onShowDiamondTipListener() {
            @Override
            public void onCloseTips() {
                mDiamondTips = null;
            }
        });

        prepareHideDiamondTips();
    }

    /**
     * 五秒后然将优先使用银钻提示消失
     */
    private void prepareHideDiamondTips() {
        mTipsHideSubscription = Observable
                .timer(5, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                        if (mTipsHideSubscription != null && !mTipsHideSubscription.isUnsubscribed()) {
                            mTipsHideSubscription.unsubscribe();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Long aLong) {
                        mDiamondTips.setVisibility(GONE);
                        mDiamondTips = null;
                    }
                });
    }

    private int[] getMeasureViewParams(View v) {
        int[] location = new int[2];
        int width;
        int height;
        width = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        height = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);

        v.measure(width, height);
        location[0] = v.getMeasuredWidth();
        location[1] = v.getMeasuredHeight();
        return location;
    }

    AnimatorSet mNormalBuyAnimationSet;
    AnimatorSet mCancelBuyAnimationSet;

    private RxActivity getRxActivity() {
        return (RxActivity) getContext();
    }

    public Subscription countDown() {
        // 先完成complete后取消订阅unsubcribe，注意保证ui在主线程中。

        final int[] repeatTimes = {1};
        return Observable
                .interval(100, TimeUnit.MILLISECONDS)
                .take(50)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(getRxActivity().<Long>bindUntilEvent(ActivityEvent.DESTROY))
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {

                        if (mIsBuyGiftBySendBtn) {
                            mSendGiftTv.setVisibility(View.INVISIBLE);
                            mContinueSendBtn.setVisibility(View.VISIBLE);

                            mSelectedView.hideContinueSendBtn();
                            mSelectedView.changeCornerStatus(mSelectedGift.gift.getIcon(), false);
                            mIsBigGiftBtnShowFlag = false;
                        } else {
                            if (mSelectedGift.gift.getCanContinuous()) {
                                mSelectedView.showContinueSendBtn(true);
                            } else {
                                mSelectedView.showContinueSendBtn(false);
                            }

                            mSelectedView.changeCornerStatus(mSelectedGift.gift.getIcon(), true);

                            mSendGiftTv.setVisibility(View.VISIBLE);
                            mContinueSendBtn.setVisibility(View.GONE);
                        }
                    }
                })
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        if (mIsBuyGiftBySendBtn) {
                            mSendGiftTv.setVisibility(View.VISIBLE);
                            mContinueSendBtn.setVisibility(View.GONE);
                        }
                    }
                })
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                        if (mIsBuyGiftBySendBtn) {
                            mSendGiftTv.setVisibility(View.VISIBLE);
                            mContinueSendBtn.setVisibility(View.GONE);
                        } else {
                            mSelectedView.hideContinueSendBtn();
                            mSelectedView.changeCornerStatus(mSelectedGift.gift.getIcon(), false);
                            mIsBigGiftBtnShowFlag = false;
                        }

                        mGiftMallPresenter.resetContinueSend();
                        mGiftMallPresenter.resetRandomGift();
                        mIsContinueSendFlag = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Long aLong) {
                        if (mIsBuyGiftBySendBtn) {
                            mContinueSendBtn.setCountDown(50 - aLong.intValue());
                        } else {
                            repeatTimes[0]++;
                            mSelectedView.changeContinueSendBtnProgressBarProgress(100 - repeatTimes[0] * 2);
                        }
                    }
                });
    }

    //有时需要先去下载数据再显示
    public interface SelectGiftView {
        void select(int id);

        void select();
    }

    SelectGiftView selectGiftView = null;

    public void selectGiftViewById(int id) {
        selectGiftView = new SelectGiftView() {
            private int mGiftId = -1;

            @Override
            public void select(int id) {
                mGiftId = id;
                selectGiftById(mGiftId);
            }

            @Override
            public void select() {
                if (mGiftId != -1) {
                    selectGiftById(mGiftId);
                }
            }
        };

        selectGiftView.select(id);
    }

    /**
     * 根据礼物id选中一个礼物
     */
    public void selectGiftById(final int id) {
        //如果这个商品是已经选中的商品就走正常选中逻辑就行
        if (null != mSelectedGift && null != mSelectedGift.gift && mSelectedGift.gift.getGiftId() == id)
            return;

        if (!mIsLandscape) {
            Observable.create(new Observable.OnSubscribe<int[]>() {
                @Override
                public void call(Subscriber<? super int[]> subscriber) {

                    //一个礼物的位置 page(页面), index(位置)
                    int[] position = {-1, -1};
                    try {
                        List<List<GiftMallPresenter.GiftWithCard>> source = mGiftDisplayViewPagerAdapter.getDataSource();

                        if (source == null || source.size() == 0
                                || null == mGiftDisplayViewPagerAdapter
                                || null == mGiftDisplayViewPagerAdapter.getCacheListView()
                                || mGiftDisplayViewPagerAdapter.getCacheListView().size() == 0) {
                            return;
                        }

                        outer:
                        for (int page = 0; page < source.size(); page++) {
                            for (int index = 0; index < source.get(page).size(); index++) {
                                if (source.get(page).get(index).gift.getGiftId() == id) {
                                    position[0] = page;
                                    position[1] = index;
                                    break outer;
                                }
                            }
                        }

                    } catch (Exception e) {

                    }
                    subscriber.onNext(position);
                    subscriber.onCompleted();
                }
            }).compose(getRxActivity().<int[]>bindUntilEvent(ActivityEvent.DESTROY))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<int[]>() {
                        @Override
                        public void call(int[] position) {

                            List<List<GiftMallPresenter.GiftWithCard>> source = mGiftDisplayViewPagerAdapter.getDataSource();
                            if (-1 == position[0]) return;

                            if (null == source || source.size() == 0 || mGiftDisplayViewPagerAdapter.getCacheListView().size() == 0)
                                return;

                            mGiftDisplayViewPager.setCurrentItem(position[0]);

                            GiftDisplayView view = (GiftDisplayView) mGiftDisplayViewPagerAdapter.getCacheListView().get(position[0]);
                            View giftItemView = ((RecyclerView) view.findViewById(R.id.gift_display_recycleview)).getChildAt(position[1]);

                            mGiftItemListener.clickGiftItem(giftItemView, source.get(position[0]).get(position[1]), position[1]);
                            selectGiftView = null;
                        }
                    });
        } else {
            selectGiftByIdLandScape(id);
        }
    }

    /**
     * 横屏时候选中礼物
     */
    private void selectGiftByIdLandScape(final int id) {
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                //一个礼物的位置 page(页面), index(位置)
                List<GiftMallPresenter.GiftWithCard> source;

                int position = -1;
                try {
                    if (null == mGiftDisplayRecycleViewAdapter || mGiftDisplayRecycleViewAdapter.getData().size() == 0) {
                        return;
                    }

                    source = mGiftDisplayRecycleViewAdapter.getData();

                    for (int index = 0; index < source.size(); index++) {
                        if (source.get(index).gift.getGiftId() == id) {
                            position = index;
                            break;
                        }
                    }
                } catch (Exception e) {

                }

                subscriber.onNext(position);
                subscriber.onCompleted();
            }
        }).compose(getRxActivity().<Integer>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer position) {
                        if (-1 == position) return;
                        List<GiftMallPresenter.GiftWithCard> source = mGiftDisplayRecycleViewAdapter.getData();

                        if (null == source || source.size() == 0 || mGiftDisplayRecycleViewAdapter.getItemCount() == 0)
                            return;

                        mGiftDisplayRecycleView.smoothScrollToPosition(position);
                        showSelectView(source, position);
                    }
                });
    }

    //优化部分，在子线程得到
    public void showSelectView(final List<GiftMallPresenter.GiftWithCard> source, final int position) {
        Observable.timer(100, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        LinearLayoutManager manager = (LinearLayoutManager) mGiftDisplayRecycleView.getLayoutManager();

                        int firstItemPosition = manager.findFirstVisibleItemPosition();
                        View view = mGiftDisplayRecycleView.getChildAt(position - firstItemPosition);
                        if (view == null) showSelectView(source, position);
                        mGiftItemListener.clickGiftItem(view, source.get(position), position);
                        selectGiftView = null;
                    }
                });
    }


    MyAlertDialog mMyAlertDialog;

    public void showInsufficientBalanceTips() {
        if (mMyAlertDialog == null) {
            mMyAlertDialog = new MyAlertDialog.Builder(getRxActivity()).create();
            mMyAlertDialog.setTitle(R.string.account_withdraw_pay_user_account_not_enough);
            mMyAlertDialog.setMessage(getContext().getString(R.string.account_withdraw_pay_user_account_not_enough_tip));
            mMyAlertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getContext().getString(R.string.recharge), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //TODO 一定记得加上
                    EventBus.getDefault().post(new GiftEventClass.GiftMallEvent(GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_GO_RECHARGE));
                    dialog.dismiss();
                }
            });
            mMyAlertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }

        mMyAlertDialog.setCancelable(false);
        mMyAlertDialog.show();
    }

    /**
     * 竖屏加载数据源
     */
    public void setGiftDisplayViewPagerAdapterDataSource(List<List<GiftMallPresenter.GiftWithCard>> dataSourceList) {
        mGiftDisplayViewPagerAdapter.setDataSource(dataSourceList);
    }

    /**
     * 横屏加载数据源
     *
     * @param dataList
     * @return
     */
    public boolean setGiftDisplayRecycleViewAdapterDataSource(List<GiftMallPresenter.GiftWithCard> dataList) {
        if (mGiftDisplayRecycleViewAdapter != null) {
            mGiftDisplayRecycleViewAdapter.setData(dataList);
            return true;
        }

        return false;
    }

    public void setGiftListErrorViewVisibility(boolean isGone) {
        if (isGone) {
            mGiftListErrorView.setVisibility(View.GONE);
        } else {
            mGiftListErrorView.setVisibility(View.VISIBLE);
        }
    }

    public boolean getHasLoadViewFlag() {
        return mHasLoadView;
    }

    public void setHasLoadViewFlag(boolean hasLoadView) {
        mHasLoadView = hasLoadView;
    }

    public void removeGiftMallView() {
        removeAllViews();
    }

    public void initGiftMallView(RxActivity rxActivity) {
        init(rxActivity);
    }

    public void hideInsufficientBalanceTips() {
        if (mMyAlertDialog != null) {
            mMyAlertDialog.dismiss();
        }
    }

    public GiftMallPresenter.GiftWithCard getSelectedGift() {
        return mSelectedGift;
    }

    public GiftDisPlayItemView getSelectedView() {
        return mSelectedView;
    }

    public boolean getIsContinueSendFlag() {
        return mIsContinueSendFlag;
    }

    public void setIsContinueSendFlag(boolean isContinueSendFlag) {
        mIsContinueSendFlag = isContinueSendFlag;
    }

    public boolean getIsBigGiftBtnShowFlag() {
        return mIsBigGiftBtnShowFlag;
    }

    public void setIsBigGiftBtnShowFlag(boolean isBigGiftBtnShowFlag) {
        mIsBigGiftBtnShowFlag = isBigGiftBtnShowFlag;
    }

    public boolean getIsBuyGiftBySendBtn() {
        return mIsBuyGiftBySendBtn;
    }

    public void setIsBuyGiftBySendBtn(boolean isBuyGiftBySendBtn) {
        mIsBuyGiftBySendBtn = isBuyGiftBySendBtn;
    }

    public void setContinueSendBtnNum(int num) {
        mContinueSendBtn.setNumber(num);
    }

    public void setOrientEventInfo(boolean isLandscape) {
        mIsLandscape = isLandscape;
    }

    public SelectGiftView getSelectGiftViewByGiftId() {
        return selectGiftView;
    }

    private void inflateGiftMallGuidePage() {
        if (mGiftMallGuidePage == null) {
            View root = mGiftMallGuidePageViewStub.inflate();
            mGiftMallGuidePage = (RelativeLayout) root.findViewById(R.id.gift_mall_guide_page);
            TextView knowBtn = (TextView) root.findViewById(R.id.i_know_tv);
            if (mIsLandscape) {
                mGiftMallGuidePage.setBackgroundResource(R.drawable.gift_mall_guide_land_bg);
            } else {
                mGiftMallGuidePage.setBackgroundResource(R.drawable.gift_mall_guide_view_bg);
            }
            knowBtn.setBackgroundResource(R.drawable.gift_mall_btn);

            RxView.clicks(findViewById(R.id.i_know_tv))
                    .throttleFirst(200, TimeUnit.MILLISECONDS)
                    .subscribe(new Action1<Void>() {
                        @Override
                        public void call(Void aVoid) {
                            MyLog.d(TAG, "GiftMallGuidePageView");
                            if (mGiftMallGuidePage != null) {
                                mGiftMallGuidePage.setVisibility(GONE);
                            }
//                    prepareShowDiamondTips();
                        }
                    });
            mGiftMallGuidePageViewStub = null;
        }

        mGiftMallGuidePage.setVisibility(VISIBLE);
    }

    /**
     * 第一次进入3秒后礼物橱窗弹优先使用银钻提示
     */
    private void FirstShowGiftMallTips() {
        if (PreferenceUtils.getSettingBoolean(GlobalData.app().getSharedPreferences(SP_FILENAME_GIFTMALL_CONFIG, Context.MODE_PRIVATE),
                FIRST_SHOW_GIFT_MALL_VIEW,
                false)) {
            return;
        }

        PreferenceUtils.setSettingBoolean(
                GlobalData.app().getSharedPreferences(SP_FILENAME_GIFTMALL_CONFIG, Context.MODE_PRIVATE),
                FIRST_SHOW_GIFT_MALL_VIEW,
                true);

        if (mGiftMallGuidePageSubscription != null && !mGiftMallGuidePageSubscription.isUnsubscribed()) {
            return;
        }

        inflateGiftMallGuidePage();

        mGiftMallGuidePageSubscription = Observable
                .timer(5, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .compose(getRxActivity().<Long>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                        if (mGiftMallGuidePageSubscription != null && !mGiftMallGuidePageSubscription.isUnsubscribed()) {
                            mGiftMallGuidePageSubscription.unsubscribe();
                        }
                        mGiftMallGuidePage.setVisibility(GONE);
//                        prepareShowDiamondTips();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Long aLong) {
                    }
                });
    }

    private void prepareShowDiamondTips() {
        if (mDiamondTipsSubscription != null && !mDiamondTipsSubscription.isUnsubscribed()) {
            return;
        }

        //3s后展示银钻提示
        mDiamondTipsSubscription = Observable
                .timer(3, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .compose(getRxActivity().<Long>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                        if (mDiamondTipsSubscription != null && !mDiamondTipsSubscription.isUnsubscribed()) {
                            mDiamondTipsSubscription.unsubscribe();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Long aLong) {
                        showDiamondTips(TIPS_TYPE_MI_COIN);
                    }
                });
    }

    /**
     * 横竖屏切换时候清楚所有标志位状态
     */
    private void clearAllGiftItemStatus() {
        mIsBigGiftBtnShowFlag = false;
        mSelectViewClickedFlag = false;
        mIsBuyGiftBySendBtn = false;
        mIsContinueSendFlag = false;
    }

    public void resetGiftItemBtnInfo() {
        if (mSelectedView != null) {
            mSelectedView.hideContinueSendBtn();
            mSelectedView.changeCornerStatus(mSelectedGift.gift.getInternationalIcon(), false);
            mGiftMallPresenter.resetContinueSend();
//            mContinueSend.reset();
            mIsBigGiftBtnShowFlag = false;
            mIsContinueSendFlag = false;
            mIsBuyGiftBySendBtn = false;
        }

        if (mNormalBuyAnimationSet != null) {
            mNormalBuyAnimationSet.cancel();
        }

//        mSendGiftTv.setVisibility(View.VISIBLE);
        mContinueSendBtn.setVisibility(View.GONE);
    }

    /**
     * 第一次inflate时候加载数据
     */

    private GiftMallPresenter mGiftMallPresenter;

    //TODO 一定记得改
    public void firstInflateGiftMallView(GiftMallPresenter giftMallPresenter, Activity activity, RoomBaseDataModel myRoomData, boolean isLandscape) {
        this.mActivity = activity;
        this.mMyRoomData = myRoomData;
        this.mIsLandscape = isLandscape;
        this.mGiftMallPresenter = giftMallPresenter;
        removeAllViews();
        init(getRxActivity());
    }

    public void processSwitchAnchorEvent() {
        cancelSelectView(mSelectedView);
        mSelectedGift = null;
        clearAllGiftItemStatus();
        resetStatus();
    }

    public void processOrientEvent(boolean isLandscape) {
        MyLog.d(TAG, "processOrientEvent" + isLandscape);
        mIsLandscape = isLandscape;
        if (this.getVisibility() == VISIBLE) {
            cancelSelectView(mSelectedView);
            mSelectedGift = null;
            clearAllGiftItemStatus();
            removeAllViews();
            init(getRxActivity());
            mGiftMallPresenter.loadDataFromCache("processOrientEvent");
        }
    }

    public void setBalanceInfo() {
        mBalanceTv.setText(String.valueOf(MyUserInfoManager.getInstance().getUser().getDiamondNum()));
        mSiliverDiamond.setText(String.valueOf(MyUserInfoManager.getInstance().getUser().getVirtualDiamondNum()));
    }

    private void cancelSelectView(View v) {
        if (mNormalBuyAnimationSet != null) {
            mNormalBuyAnimationSet.cancel();
        }
        if (v != null) {
            v.setBackgroundResource(0);
            if (v == mSelectedView) {

                //取消礼物支持动画入口
                mSelectedView.cancelSelectedGiftItemAnimator();

                mSelectedView = null;
                if (mSelectedShakeAnimation != null) {
                    mSelectedShakeAnimation.cancel();
                    mSelectedShakeAnimation = null;
                }
            } else {
                MyLog.d(TAG, "new v=" + v + ",old view=" + mSelectedGift + " not eq");
            }
        }
    }

    private void selectView(View v, boolean shake) {
        MyLog.d(TAG, "selectView");
        if (v != null) {
            MyLog.d(TAG, "v != null" + shake);
            mSelectedView = (GiftDisPlayItemView) v;
            v.setBackgroundResource(R.drawable.live_choice_selected);

            Gift gift = mSelectedGift.gift;
            MyLog.d(TAG, "gift.toString:" + gift.toString());
            String gifUrl = gift.getGifUrl();

            MyLog.d(TAG, "gift.getGiftId():" + gift.getGiftId() + " gift.getComment():" + gift.getComment() + "gift.getGifUrl():" + gift.getGifUrl());
            MyLog.d(TAG, "gifUrl:" + gifUrl);

            View giftIv = v.findViewById(R.id.gift_iv);
            if (shake) {

                if (TextUtils.isEmpty(gifUrl)) {
                    if (mSelectedShakeAnimation != null) {
                        mSelectedShakeAnimation.cancel();
                        mSelectedShakeAnimation = null;
                    }
                    mSelectedShakeAnimation = MyAnimationUtils.shake(giftIv, 1);
                    mSelectedShakeAnimation.start();
                } else {
                    //礼物item动画入口
                    mSelectedView.playSelectedGiftItemAnimator(gifUrl, true);
                }
                if (mSelectViewClickedFlag && !mSelectedView.isContinueSendBtnShow()) {
                    if (mSelectedGift.gift.getPrice() > mGiftMallPresenter.getCurrentTotalBalance() || mSelectedGift.gift.getCatagory() == GiftType.RED_ENVELOPE_GIFT) {
//                        showInsufficientBalanceTips();
                        return;
                    }
                    mSelectedView.showContinueSendBtn(true);
                }
            }
        }
    }

    public void onActivityDestroy() {
        MyLog.d(TAG, "onDestroy : unregister eventbus");
        unSubscribeDiamondTipSubscription();

        if (mNormalBuyAnimationSet != null) {
            mNormalBuyAnimationSet.cancel();
        }
    }

    @Override
    public void setVisibility(int visibility) {
        if (this.getVisibility() != GONE && visibility == GONE) {
            GiftMallView.super.setVisibility(GONE);
            Animation animation = onCreateAnimation(false);
            animation.setDuration(500);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    MyLog.d(TAG, "animation hide start");
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    MyLog.d(TAG, "animation hide end");
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            this.startAnimation(animation);
            return;
        }
        if (this.getVisibility() != VISIBLE && visibility == VISIBLE) {
            GiftMallView.super.setVisibility(VISIBLE);
            Animation animation = onCreateAnimation(true);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    MyLog.d(TAG, "animation show start");
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    MyLog.d(TAG, "animation show end");
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            this.startAnimation(animation);
            return;
        }
    }

    public Animation onCreateAnimation(boolean enter) {
        if (enter) {
            return AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_from_bottom);
        } else {
            return AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_to_bottom);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
}
