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
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
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
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.event.GiftEventClass;
import com.mi.live.data.gift.model.BuyGiftType;
import com.mi.live.data.gift.model.GiftType;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.repository.GiftRepository;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.common.barrage.manager.BarrageMessageManager;
import com.wali.live.common.gift.adapter.GiftDisplayRecycleViewAdapter;
import com.wali.live.common.gift.adapter.GiftDisplayViewPagerAdapter;
import com.wali.live.common.gift.presenter.GiftMallPresenter;
import com.wali.live.common.view.ErrorView;
import com.wali.live.common.view.ViewPagerWithCircleIndicator;
import com.wali.live.dao.Gift;
import com.wali.live.pay.fragment.BalanceFragment;
import com.wali.live.pay.manager.PayManager;
import com.wali.live.pay.model.BalanceDetail;
import com.wali.live.proto.PayProto;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
    private TextView mBalanceTvDisplay;

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
    private TextView mSiliverDiamondDisplay;

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

    private TextView mMallGiftTv;
    private TextView mPktGiftTv;
    private View mSlideGift;
    private View mSlidePkt;

    private TextView mRechargeTv;
    private TextView mRechargeDisplay;

    private TextView mPktDetailTv;

    private ImageView mEmptyIv;

    //标记当前的选择的礼物状态
    private boolean mIsMallGift = true;

    IGiftSendController currentGiftSendController;

    private int mSelectedPos = -1; //选中的 giftview 的位置

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
            currentGiftSendController = mPortraitStateController;
        } else {
            inflate(context, R.layout.gift_mall_landscape_view, this);
            currentGiftSendController = mLandScapeStateController;
        }
        bindView();
        mHasLoadView = true;
    }

    //转屏时重置状态
    public void resetStatus() {
        mHasLoadView = false;
    }

    private Handler mHandler = new Handler();

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
                        mContinueSendBtn.setVisibility(View.GONE);
                    }

                    mGiftMallPresenter.resetRandomGift();
                    mGiftMallPresenter.resetContinueSend();
                }
                mGiftMallPresenter.unsubscribeSountDownSubscription();

                if (v != null) {
                    MyLog.d(TAG, "clickGiftItem");
                    mSelectedGift = info;
                    if (mIsLandscape) {
                        //mSelectedPos是0  position也是0的时候不走里面的逻辑  加一个判断
                        if ((mSelectedPos == 0 && position == 0) || mSelectedPos != position) {
                            cancelSelectView(mSelectedView);
                            mSelectedPos = position;
                            selectView(v, true);
                        }
                    } else {
                        cancelSelectView(mSelectedView);
                        selectView(v, true);
                    }
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
                if (mSelectedGift != null && mSelectedGift.gift != null && mSelectedGift.gift.getCanContinuous() || mIsBigGiftBtnShowFlag) {
                    //引入

                    if (!judgeBuyGiftCondition()) {
                        return;
                    }

                    //是否显示角标
                    mSelectedView.changeCornerStatus(mSelectedGift.gift.getIcon(), true);

                    mIsBuyGiftBySendBtn = false;
//                    mGiftMallPresenter.buyGift(1);
                } else {
                    mSelectedView.changeContinueSendBtnBackGroup(true);
                    mIsBigGiftBtnShowFlag = true;
                }
            }
        }

        @Override
        public Gift getSelectedGift() {
            if (mSelectedGift != null) {
                return mSelectedGift.gift;
            }
            return null;
        }

        @Override
        public void updateSelectedGiftView(final View v, final GiftMallPresenter.GiftWithCard info) {
            // 选中的v因为刷新改变了
            if (v != mSelectedView) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!mIsLandscape) {
//                            cancelSelectView(mSelectedView);
//                            selectView(v, false);
                            //模拟再次被点击事件，变成
                            v.callOnClick();
                            MyLog.d(TAG, "updateSelectedGiftView");
                        }
                        mSelectedView = (GiftDisPlayItemView) v;
                        mSelectedGift = info;
                    }
                });
            }
        }

        @Override
        public void updateContinueSend() {
            MyLog.d(TAG, "updateContinueSend");
            mGiftMallPresenter.resetContinueSend();
        }

        @Override
        public boolean getMallType() {
            return mIsMallGift;
        }

        @Override
        public Set getAllowedPktGiftId() {
            return mGiftMallPresenter.getPktGiftId();
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
            mGiftDisplayViewPager.setLimitHeight(60);
            mGiftListErrorView = (ErrorView) findViewById(R.id.gift_list_error_view);
            mGiftListErrorView.setRetryOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mGiftMallPresenter.loadDataFromCache("retryOnClickListener");
                }
            });
        } else {
            mGiftDisplayRecycleView = (RecyclerView) findViewById(R.id.gift_display_recycleview);
            mGiftDisplayRecycleViewAdapter = new GiftDisplayRecycleViewAdapter(true, mGiftItemListener);
            mGiftDisplayRecycleView.setAdapter(mGiftDisplayRecycleViewAdapter);
            mGiftDisplayRecycleView.setLayoutManager(new LinearLayoutManager(getRxActivity(), LinearLayoutManager.HORIZONTAL, false));
//            mGiftDisplayRecycleView.addItemDecoration(new GiftDisplayDividerItemDecoration(GiftDisplayDividerItemDecoration.HORIZONTAL_LIST));
            mGiftDisplayRecycleView.setHasFixedSize(false);
        }
        mGiftBottomPanel = (RelativeLayout) findViewById(R.id.gift_bottom_panel);
        mGiftMallGuidePageViewStub = (ViewStub) findViewById(R.id.gift_mall_guide_page_viewstub);
        mBalanceTv = (TextView) findViewById(R.id.diamond_max_tv);
        mBalanceTvDisplay = (TextView) findViewById(R.id.diamond_max_tv_real);

        mSiliverDiamond = (TextView) findViewById(R.id.diamond_siliver_tv);
        mSiliverDiamondDisplay = (TextView) findViewById(R.id.diamond_siliver_tv_real);

        mRechargeDisplay = (TextView) findViewById(R.id.recharge_tv);

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

        // 连送按钮的点击
        RxView.clicks(mContinueSendBtn)
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        mIsBuyGiftBySendBtn = true;
//                        mGiftMallPresenter.buyGift();
                    }
                });

        // 充值按钮的点击
        mRechargeTv = $rxClick(R.id.recharge_tv_real, 500, new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                //TODO 一定记得加上
                EventBus.getDefault().post(new GiftEventClass.GiftMallEvent(GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_GO_RECHARGE));
            }
        });

        //金钻银钻点击事件处理一样
        RxView.clicks(findViewById(R.id.diamond_max_tv_real))
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        showDiamondTips(TIPS_TYPE_BALANCE);
                    }
                });

        RxView.clicks(mBalanceTv)
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        showDiamondTips(TIPS_TYPE_BALANCE);
                    }
                });

        RxView.clicks(findViewById(R.id.diamond_siliver_tv_real))
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        showDiamondTips(TIPS_TYPE_SILIVER_DIAMOND);
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

        mPktDetailTv = $rxClick(R.id.tv_pkt_detail_real, 300, new Action1<Void>() {
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

        mEmptyIv = $(R.id.pkt_empty_iv);
        setGiftTabBackground();
    }

    public boolean isMallGift() {
        return mIsMallGift;
    }

    /**
     * 选择花钱礼物，默认选中花钱礼物
     */
    private void clickMallGift() {
        currentGiftSendController.giftSwichPacket();
        mIsMallGift = true;
        setGiftTabBackground();
        switchMallType();

        if (!mGiftMallPresenter.loadExistedDataFromBean()) {
            mGiftMallPresenter.loadDataFromCache("clickMallGift");
        }
    }

    /**
     * 选择背包礼物
     */
    private void clickPktGift() {
        currentGiftSendController.giftSwichPacket();
        mIsMallGift = false;
        setGiftTabBackground();
        switchMallType();

        if (!mGiftMallPresenter.loadExistedDataFromBean()) {
            mGiftMallPresenter.loadDataFromCache("clickPktGift");
        }
    }

    void setGiftTabBackground() {
        if (mIsMallGift) {
            mMallGiftTv.setSelected(true);
            mPktGiftTv.setSelected(false);
            mMallGiftTv.setBackgroundColor(Color.TRANSPARENT);
            mRechargeDisplay.setVisibility(VISIBLE);
//            mSlideGift.setVisibility(View.VISIBLE);
//            mSlidePkt.setVisibility(View.GONE);

            mBalanceTv.setVisibility(View.VISIBLE);
            mBalanceTvDisplay.setVisibility(VISIBLE);
            mSiliverDiamond.setVisibility(View.VISIBLE);
            mSiliverDiamondDisplay.setVisibility(VISIBLE);
            mRechargeTv.setVisibility(View.VISIBLE);

            mPktDetailTv.setVisibility(View.GONE);
        } else {
            mMallGiftTv.setSelected(false);
            mPktGiftTv.setSelected(true);
//            mPktGiftTv.setBackgroundColor(Color.TRANSPARENT);

//            mSlideGift.setVisibility(View.GONE);
//            mSlidePkt.setVisibility(View.VISIBLE);

            mRechargeDisplay.setVisibility(GONE);
            mBalanceTv.setVisibility(View.GONE);
            mBalanceTvDisplay.setVisibility(GONE);
            mSiliverDiamond.setVisibility(View.GONE);
            mSiliverDiamondDisplay.setVisibility(GONE);
            mRechargeTv.setVisibility(View.GONE);

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
            mDiamondTips = new GiftDiamondTips(getContext(), tipsType, mIsLandscape);
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

            if (mIsLandscape) {
                layoutParams.setMargins(left, 0, 0, bottom);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            } else {
                layoutParams.setMargins(left, bottom, 0,0 );
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            }

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
                            mContinueSendBtn.setVisibility(View.VISIBLE);
                            mIsBigGiftBtnShowFlag = false;
                        } else {
                            mContinueSendBtn.setVisibility(View.GONE);
                        }
                    }
                })
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        if (mIsBuyGiftBySendBtn) {
                            mContinueSendBtn.setVisibility(View.GONE);
                        }
                    }
                })
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                        if (mIsBuyGiftBySendBtn) {
                            mContinueSendBtn.setVisibility(View.GONE);
                        } else {
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
     * 判断当前是否符合购买礼物
     *
     * @return false 条件不符合, true 可以发起购买
     */
    private boolean judgeBuyGiftCondition() {
        if (mSelectedGift.gift.getCatagory() == GiftType.MAGIC_GIFT && !mMyRoomData.isSupportMagicFace()) {
            ToastUtils.showToast(getResources().getString(R.string.no_support_magic_gift_tips));
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
        MyLog.d(TAG, "setGiftDisplayViewPagerAdapterDataSource=" + dataSourceList.size());
        mGiftDisplayViewPagerAdapter.setDataSource(dataSourceList);
        mEmptyIv.setVisibility(dataSourceList.isEmpty() ? VISIBLE : GONE);
    }

    /**
     * 横屏加载数据源
     */
    public boolean setGiftDisplayRecycleViewAdapterDataSource(List<GiftMallPresenter.GiftWithCard> dataList) {
        if (mGiftDisplayRecycleViewAdapter != null) {
            mGiftDisplayRecycleViewAdapter.setData(dataList);
            mEmptyIv.setVisibility(dataList.isEmpty() ? VISIBLE : GONE);
            return true;
        }
        return false;
    }

    public void setGiftListErrorViewGone(boolean isGone) {
        mGiftListErrorView.setVisibility(isGone ? GONE : VISIBLE);
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
            mGiftMallPresenter.resetContinueSend();
//            mContinueSend.reset();
            mIsBigGiftBtnShowFlag = false;
            mIsContinueSendFlag = false;
            mIsBuyGiftBySendBtn = false;
        }

        if (mNormalBuyAnimationSet != null) {
            mNormalBuyAnimationSet.cancel();
        }

        mContinueSendBtn.setVisibility(View.GONE);
    }

    /**
     * 目前和{@link #switchMallType}一致，但是单独出来，先看看有没有问题
     */
    public void cancelPktGiftSendStatus() {
        currentGiftSendController.packetGiftUsedUp();
        cancelSelectView(mSelectedView);
        mSelectedGift = null;

        clearAllGiftItemStatus();
        resetGiftItemBtnInfo();
    }

    /**
     * 第一次inflate时候加载数据
     */
    private GiftMallPresenter mGiftMallPresenter;

    //TODO 一定记得改
    public void firstInflateGiftMallView(GiftMallPresenter giftMallPresenter, RoomBaseDataModel myRoomData, boolean isLandscape) {
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
        currentGiftSendController.giftSelectedCancel();
        mSelectedPos = -1;
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

            Gift gift = mSelectedGift.gift;
            MyLog.d(TAG, "gift.toString:" + gift.toString());
            String gifUrl = gift.getGifUrl();

            MyLog.d(TAG, "gift.getGiftId():" + gift.getGiftId() + " gift.getComment():" + gift.getComment() + "gift.getGifUrl():" + gift.getGifUrl());
            MyLog.d(TAG, "gifUrl:" + gifUrl);

            currentGiftSendController.giftSelected(gift, shake, mSelectedView);
        }
    }

    public void onActivityDestroy() {
        MyLog.d(TAG, "onDestroy : unregister eventbus");
        unSubscribeDiamondTipSubscription();

        if (mNormalBuyAnimationSet != null) {
            mNormalBuyAnimationSet.cancel();
        }
    }

//    @Override
//    public void setVisibility(int visibility) {
//        if (this.getVisibility() != GONE && visibility == GONE) {
//            GiftMallView.super.setVisibility(GONE);
//            Animation animation = onCreateAnimation(false);
//            animation.setDuration(500);
//            animation.setAnimationListener(new Animation.AnimationListener() {
//                @Override
//                public void onAnimationStart(Animation animation) {
//                    MyLog.d(TAG, "animation hide start");
//                }
//
//                @Override
//                public void onAnimationEnd(Animation animation) {
//                    MyLog.d(TAG, "animation hide end");
//                }
//
//                @Override
//                public void onAnimationRepeat(Animation animation) {
//
//                }
//            });
//            this.startAnimation(animation);
//            return;
//        }
//        if (this.getVisibility() != VISIBLE && visibility == VISIBLE) {
//            GiftMallView.super.setVisibility(VISIBLE);
//            Animation animation = onCreateAnimation(true);
//            animation.setAnimationListener(new Animation.AnimationListener() {
//                @Override
//                public void onAnimationStart(Animation animation) {
//                    MyLog.d(TAG, "animation show start");
//                }
//
//                @Override
//                public void onAnimationEnd(Animation animation) {
//                    MyLog.d(TAG, "animation show end");
//                }
//
//                @Override
//                public void onAnimationRepeat(Animation animation) {
//
//                }
//            });
//            this.startAnimation(animation);
//            return;
//        }
//    }

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

    //礼物被选中的DisplayView
    GiftDisplayView currentView = null;
    //被选中时添加进去的view
    private GiftSelectedView mClickedView;

    private void addSelectedView(Gift gift) {
        if (mClickedView != null && mClickedView.getParent() != null && currentView != null) {
            currentView.removeView(mClickedView);
        }

        //数组为2, 第一个是第几页，第二个是第几个
        int pp[] = mGiftMallPresenter.getGiftIndex(gift, false, !mIsMallGift);
        int index = pp[1];

        currentView = (GiftDisplayView) mGiftDisplayViewPager.findViewWithTag(pp[0]);
        MyLog.d(TAG, "current view item" + mGiftDisplayViewPager.getCurrentItem());
        mClickedView = new GiftSelectedView(getContext(), sendGiftCallBack);
        mClickedView.setGiftInfo(gift, true);

        int itemWidth = DisplayUtils.getScreenWidth() / 4;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        if (index % 4 == 0) {
            layoutParams.setMargins((itemWidth * (index % 4)) + DisplayUtils.dip2px(3.33f), index > 3 ? DisplayUtils.dip2px(91) : DisplayUtils.dip2px(1), 0, 0);
        } else if (index % 4 == 3) {
            layoutParams.setMargins((itemWidth * (index % 4)) - (DisplayUtils.dip2px(100) - itemWidth), index > 3 ? DisplayUtils.dip2px(91) : DisplayUtils.dip2px(1), DisplayUtils.dip2px(3.33f), 0);
        } else {
            layoutParams.setMargins((itemWidth * (index % 4)) - (DisplayUtils.dip2px(100) - itemWidth) / 2, index > 3 ? DisplayUtils.dip2px(91) : DisplayUtils.dip2px(1), 0, 0);
        }

        mClickedView.setLayoutParams(layoutParams);
        currentView.addView(mClickedView, layoutParams);

        MyLog.d(TAG, "addSelectedView addView");
    }

    private void addSendCircleGift(Gift gift, SelectedViewInfo info) {
        if (isNeedSendcircle(gift) && mSelectedView != null) {

            mGiftSendView = new GiftSendView(getContext(), gift, info, new GiftSendView.BuyGiftCallBack() {
                @Override
                public void buy(Gift gift, int buyCount) {
                    if (mSelectedGift != null && mSelectedGift.gift != null) {
                        if (!judgeBuyGiftCondition()) {
                            return;
                        }
                        if (mIsMallGift) {
                            mGiftMallPresenter.buyGift(buyCount);
                            return;
                        }

                        if ((mSelectedGift != null && mSelectedGift.card != null
                                && mSelectedGift.card.getGiftCardCount() >= buyCount)) {
                            mGiftMallPresenter.buyGift(buyCount);
                        } else {
                            ToastUtils.showToast(GlobalData.app().getResources().getString(R.string.packet_count_not_enough));
                        }
                    }
                }

                @Override
                public void resetContinueSend(Gift gift) {
                    mGiftMallPresenter.resetContinueSend();
                }

                @Override
                public void isRemoved(Gift selecedGift) {
                    currentGiftSendController.isSendViewRemoved(selecedGift);
                }

                @Override
                public int getFace(Gift gift) {
                    return currentGiftSendController.getCircleFace(gift);
                }
            });

            RelativeLayout.LayoutParams layoutParams =
                    new RelativeLayout.LayoutParams(DisplayUtils.dip2px(2000), ViewGroup.LayoutParams.MATCH_PARENT);
            mGiftSendView.setLayoutParams(layoutParams);
            addView(mGiftSendView);
            currentGiftSendController.giftSendType();
        }
    }

    private boolean isNeedSendcircle(Gift gift) {
        int giftPrice = 0;
        if (gift.getCatagory() == GiftType.Mi_COIN_GIFT || gift.getBuyType() == BuyGiftType.BUY_GIFT_BY_MI_COIN) {
            giftPrice = gift.getPrice() / 10;
        } else {
            giftPrice = gift.getPrice();
        }
        return giftPrice <= GiftSendView.BIGGEST_SMALLGIFT_PIRCE;
    }

    public static class SelectedViewInfo {
        int width = 0;
        int height = 0;
        int[] position = {0, 0};

        public SelectedViewInfo(int width, int height, int[] position) {
            this.height = height;
            this.width = width;
            this.position = position;
        }
    }

    //赠送状态的View
    GiftSendView mGiftSendView;

    private GiftSelectedView.SendGiftCallBack sendGiftCallBack = new GiftSelectedView.SendGiftCallBack() {
        @Override
        public void onClickSend(Gift gift, View giftSelectedView) {
            MyLog.d(TAG, "sendGiftCallBack onClickSend gift:" + gift);

            if (!judgeBuyGiftCondition()) {
                return;
            }

            if (!mIsMallGift && mSelectedGift != null
                    && mSelectedGift.card != null
                    && mSelectedGift.card.getGiftCardCount() == 1) {
                mGiftMallPresenter.buyGift(1);
                return;
            }

            int giftPrice = 0;

            if (gift.getCatagory() == GiftType.Mi_COIN_GIFT || gift.getBuyType() == BuyGiftType.BUY_GIFT_BY_MI_COIN) {
                giftPrice = gift.getPrice() / 10;
            } else {
                giftPrice = gift.getPrice();
            }

            if (giftPrice <= GiftSendView.BIGGEST_SMALLGIFT_PIRCE) {
                currentGiftSendController.addSendCircleView(gift, giftSelectedView);
            }

            mGiftMallPresenter.buyGift(1);
        }
    };

    /*
    竖屏时候新的礼物选中发送操作
    */
    private IGiftSendController mPortraitStateController = new IGiftSendController() {
        @Override
        public void giftSelected(Gift gift, boolean shake, View selected) {
            MyLog.d(TAG, "mPortraitStateController giftSelected " + gift);
            if (shake) {
                selected.setVisibility(GONE);
                addSelectedView(gift);
            }

        }

        @Override
        public void giftSendType() {
            MyLog.d(TAG, "mPortraitStateController giftSendType ");
            if (mSelectedView != null) {
                mSelectedView.setVisibility(VISIBLE);
            }

            if (mClickedView != null) {
                mClickedView.setVisibility(INVISIBLE);
            }

        }

        @Override
        public void giftSwichPacket() {
            MyLog.d(TAG, "mPortraitStateController giftSwichPacket ");
            if (mSelectedView != null) {
                mSelectedView.setVisibility(VISIBLE);
            }

            if (mClickedView != null && mClickedView.getParent() != null && currentView != null) {
                currentView.removeView(mClickedView);
            }

            if (mSelectedGift != null) {
                mSelectedGift.selectStatus = GiftMallPresenter.GiftWithCard.TYPE_NORMAL;
            }
            currentView = null;

        }

        @Override
        public int getCircleFace(Gift gift) {
            MyLog.d(TAG, "mPortraitStateController getCircleFace ");
            int index = mGiftMallPresenter.getGiftIndex(gift, false, !mIsMallGift)[1];
            if (index % 4 >= 2) {
                return SmallSendGiftBtn.leftFace;
            } else {
                return SmallSendGiftBtn.rightFace;
            }
        }

        @Override
        public void giftSelectedCancel() {
            MyLog.d(TAG, "mPortraitStateController giftSelectedCancel ");
            if (mSelectedView != null) {
                mSelectedView.setVisibility(VISIBLE);
            }

            if (mClickedView != null && mClickedView.getParent() != null && currentView != null) {
                currentView.removeView(mClickedView);
            }

            if (mGiftSendView != null && mGiftSendView.getParent() != null) {
                removeView(mGiftSendView);
            }
        }

        @Override
        public void isSendViewRemoved(Gift gift) {
            MyLog.d(TAG, "mPortraitStateController isSendViewRemoved ");
            if (mClickedView != null) {
                mClickedView.setVisibility(VISIBLE);
                if (mSelectedView != null) {
                    mSelectedView.setVisibility(GONE);
                }
            }

        }

        @Override
        public void addSendCircleView(Gift gift, View selectedView) {
            MyLog.d(TAG, "mPortraitStateController addSendCircleView " + gift);
            int[] pocation = new int[2];
            selectedView.getLocationOnScreen(pocation);
            pocation[1] += DisplayUtils.dip2px(52.67f);

            int width = selectedView.getWidth();
            int height = DisplayUtils.dip2px(56.67f);
            MyLog.d(TAG, pocation[0] + "," + pocation[1] + " width:" + width + " height:" + height);

            addSendCircleGift(gift, new SelectedViewInfo(width, height, pocation));
        }

        @Override
        public void packetGiftUsedUp() {
            MyLog.d(TAG, "mPortraitStateController packetGiftUsedUp ");
            if (mGiftSendView != null) {
                mGiftSendView.setRemovedByPacket();
            }
            giftSelectedCancel();
            mSelectedGift = null;
        }
    };


    /*
    横屏时候新的礼物选中发送操作
     */
    private IGiftSendController mLandScapeStateController = new IGiftSendController() {
        @Override
        public void giftSelected(Gift gift, boolean shake, View selected) {
            MyLog.d(TAG, "mLandScapeStateController giftSelected" + gift);

            mGiftDisplayRecycleViewAdapter.changeSelectState(mSelectedPos, GiftMallPresenter.GiftWithCard.TYPE_SELECTED, sendGiftCallBack);
            LinearLayoutManager manager = (LinearLayoutManager) mGiftDisplayRecycleView.getLayoutManager();
            final int first = manager.findFirstVisibleItemPosition();
            final int last = manager.findLastVisibleItemPosition();
            final int index = mGiftDisplayRecycleView.getChildAdapterPosition(selected);
            if (index == first || index == last) {
                mGiftDisplayRecycleView.smoothScrollToPosition(index);
                if (index == last) {
                    mGiftDisplayRecycleView.post(new Runnable() {
                        @Override
                        public void run() {
                            mGiftDisplayRecycleView.scrollBy(DisplayUtils.dip2px(3.33f), 0);
                        }
                    });
                }
            }

            Observable.just(0)
                    .compose(getRxActivity().bindToLifecycle())
                    .delay(100, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object object) {
                            List<Animator> animators = new ArrayList<>();
                            for (int i = first; i <= last; i++) {
                                View view = mGiftDisplayRecycleView.findViewHolderForAdapterPosition(i).itemView;
                                if (view != null && i != index) {
                                    ObjectAnimator animatorX = ObjectAnimator.ofFloat(view, "translationX", 0, i < index ? -30 : 30, 0);
                                    animators.add(animatorX);
                                }
                            }
                            AnimatorSet smallBtnAddAnimation = new AnimatorSet();//组合动画
                            smallBtnAddAnimation.setDuration(1000);
                            smallBtnAddAnimation.setInterpolator(new OvershootInterpolator());
                            smallBtnAddAnimation.playTogether(animators);
                            smallBtnAddAnimation.start();
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            MyLog.w(TAG, "error in giftSelected: " + throwable.getMessage());
                        }
                    });
        }

        @Override
        public void giftSendType() {

        }

        @Override
        public void giftSwichPacket() {
            MyLog.d(TAG, "mLandScapeStateController giftSwichPacket");
            if (mSelectedGift != null && mSelectedGift.gift != null) {
                mGiftDisplayRecycleViewAdapter.changeSelectState(mSelectedPos, GiftMallPresenter.GiftWithCard.TYPE_NORMAL, null);
            }
        }

        @Override
        public int getCircleFace(Gift gift) {
            int index = mGiftMallPresenter.getGiftIndex(gift, true, !mIsMallGift)[1];
            //防止游戏直播间横屏时第一个被遮挡住
            if (index == 0 && mMyRoomData.getLiveType() == LiveManager.TYPE_LIVE_GAME) {
                return SmallSendGiftBtn.rightFace;
            }
            //横屏默认向右
            return SmallSendGiftBtn.leftFace;
        }

        @Override
        public void giftSelectedCancel() {
            MyLog.d(TAG, "mLandScapeStateController giftSelectedCancel");
            if (mGiftDisplayRecycleViewAdapter != null) {
                mGiftDisplayRecycleViewAdapter.changeSelectState(mSelectedPos, GiftMallPresenter.GiftWithCard.TYPE_NORMAL, null);
            }
        }

        @Override
        public void isSendViewRemoved(Gift gift) {
            MyLog.d(TAG, "mLandScapeStateController isSendViewRemoved");
            mGiftDisplayRecycleViewAdapter.changeSelectState(mSelectedPos, GiftMallPresenter.GiftWithCard.TYPE_SELECTED, sendGiftCallBack);
        }

        @Override
        public void addSendCircleView(Gift gift, View selectedView) {
            MyLog.d(TAG, "mLandScapeStateController addSendCircleView");
            int[] pocation = new int[2];
            selectedView.getLocationOnScreen(pocation);
            pocation[1] += DisplayUtils.dip2px(52.67f);

            int width = selectedView.getWidth();
            int height = DisplayUtils.dip2px(56.67f);
            MyLog.d(TAG, pocation[0] + "," + pocation[1] + " width:" + width + " height:" + height);

            mGiftDisplayRecycleViewAdapter.changeSelectState(mSelectedPos, GiftMallPresenter.GiftWithCard.TYPE_SEND, sendGiftCallBack);
            addSendCircleGift(gift, new SelectedViewInfo(width, height, pocation));
        }

        @Override
        public void packetGiftUsedUp() {
            MyLog.d(TAG, "mLandScapeStateController packetGiftUsedUp");
            MyLog.d(TAG, "mLandScapeStateController packetGiftUsedUp");
            if (mGiftSendView != null && mGiftSendView.getParent() != null) {
                mSelectedPos = -1;
                if (mGiftSendView != null) {
                    mGiftSendView.setRemovedByPacket();
                }
                removeView(mGiftSendView);
            }
        }
    };

    interface IGiftSendController {
        void giftSelected(Gift gift, boolean shake, View selected);

        void giftSendType();

        void giftSwichPacket();

        int getCircleFace(Gift gift);

        void giftSelectedCancel();

        void isSendViewRemoved(Gift gift);

        void addSendCircleView(Gift gift, View selectedView);

        //一个背包礼物用完之后调用
        void packetGiftUsedUp();
    }
}
