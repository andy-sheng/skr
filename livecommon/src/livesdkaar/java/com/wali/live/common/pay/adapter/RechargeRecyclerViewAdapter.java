package com.wali.live.common.pay.adapter;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.base.activity.RxActivity;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ImageFactory;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.DeviceUtils;
import com.base.utils.display.DisplayUtils;
import com.base.utils.layout.LayoutUtils;
import com.jakewharton.rxbinding.view.RxView;
import com.live.module.common.R;
import com.mi.live.data.account.MyUserInfoManager;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.common.pay.constant.PayConstant;
import com.wali.live.common.pay.constant.PayWay;
import com.wali.live.common.pay.constant.RechargeConfig;
import com.wali.live.common.pay.fragment.RechargeFragment;
import com.wali.live.common.pay.handler.OneDayQuotaHandler;
import com.wali.live.common.pay.handler.RechargeActionHandler;
import com.wali.live.common.pay.handler.SingleDealQuotaHandler;
import com.wali.live.common.pay.model.Diamond;
import com.wali.live.common.pay.model.SkuDetail;
import com.wali.live.common.pay.presenter.RechargePresenter;
import com.wali.live.common.pay.utils.PayStatisticUtils;
import com.wali.live.common.pay.view.PayWaySwitchDialogHolder;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;
import com.wali.live.common.view.ErrorView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.wali.live.common.pay.fragment.RechargeFragment.getCurrentPayWay;
import static com.wali.live.common.pay.fragment.RechargeFragment.setCurrentPayWay;
import static com.wali.live.statistics.StatisticsKey.AC_APP;
import static com.wali.live.statistics.StatisticsKey.KEY;
import static com.wali.live.statistics.StatisticsKey.Recharge.APP_NOT_INSTALL;
import static com.wali.live.statistics.StatisticsKey.Recharge.CLICK_PAY_BTN;
import static com.wali.live.statistics.StatisticsKey.Recharge.EXCEED_SINGLE_DEAL_QUOTA_ADJUST;
import static com.wali.live.statistics.StatisticsKey.Recharge.EXCEED_SINGLE_DEAL_QUOTA_TO_ZFB;
import static com.wali.live.statistics.StatisticsKey.Recharge.PRICE_LIST;
import static com.wali.live.statistics.StatisticsKey.TIMES;

/**
 * Created by rongzhisheng on 16-11-13.
 */

public class RechargeRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements PayWaySwitchDialogHolder.IPayWaySwitchListener {
    private static final String TAG = RechargeRecyclerViewAdapter.class.getSimpleName();
    /**
     * 余额部分
     */
    private static final int ITEM_TYPE_BALANCE = 1;
    /**
     * 分隔线部分
     */
    private static final int ITEM_TYPE_DIVIDER = 2;
    /**
     * 支付方式选择部分
     */
    private static final int ITEM_TYPE_PAY_WAY = 3;
    /**
     * 价格和支付部分，与ITEM_TYPE_ERROR、ITEM_TYPE_LOADING互斥
     */
    private static final int ITEM_TYPE_PRICE = 4;
    /**
     * 错误部分，与ITEM_TYPE_PRICE、ITEM_TYPE_LOADING互斥
     */
    private static final int ITEM_TYPE_ERROR = 5;
    /**
     * 正在载入部分，ITEM_TYPE_PRICE、ITEM_TYPE_ERROR互斥
     */
    private static final int ITEM_TYPE_LOADING = 6;
    /**
     * 延迟选中第一个价格的时间
     */
    private static final int DEFAULT_SELECTED_DELAY_MILLIS = 200;

    /////////////////界面相关//////////////////////
    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private boolean mLoading = true;
    private boolean mLoadFailed = false;
    private BalanceViewHolder mBalanceViewHolder;
    private DividerViewHolder mDividerViewHolder;
    private PayWayViewHolder mPayWayViewHolder;
    private PriceGridViewHolder mPriceGridViewHolder;
    private ErrorViewHolder mErrorViewHolder;
    private ErrorViewHolder mLoadingViewHolder;
    private BaseAdapter mGridViewAdapter;
    private ImageView sharp;
    private TextView saleTip;
    /**
     * 弹出窗口
     */
    private PopupWindow mPopupWindow;
    /**
     * 弹出窗口附着的gridView itemView，用于应用从后台返回时恢复弹窗
     */
    private View mPopupWindowAttatchedView;
    /**
     * 弹出窗口所在的gridView位置[0,n)，用于应用从后台返回时恢复弹窗
     */
    private int mPopupWindowPosition;
    /**
     * 用户选择的金额(单位：分)，用于在不同支付方式间跳转后确定{@link #mPopupWindowPosition}
     */
    private int mUserSelectedPrice;
    /**
     * GridView的item的实际高度
     */
    private int itemHeightPx;
    /**
     * 用户主动切换支付方式时的对话框
     */
    private PayWaySwitchDialogHolder mPayWaySwitchDialogHolder;

    /////////////////逻辑控制状态相关//////////////////////
    /**
     * 最近一次选择的支付方式的充值列表类型
     */
    private int mLastRechargeListType;
    /**
     * 当{@link RechargeFragment#mIsFirstRecharge}为true时有意义，表示是首次充值的第几个界面，取值范围[1,2]<br/>非首次充值只有第2个界面
     */
    private int mStep;
    /**
     * 首次充值
     */
    private boolean mIsFirstRecharge;
    /**
     * 单笔限额处理器
     */
    private RechargeActionHandler mFirstRechargeActionHandler;
    private RechargePresenter mRechargePresenter;

    ////////////////数据相关///////////////////////
    private List<Diamond> mRechargeList = new ArrayList<>();
    private List<PayWay> mPayWayList;

    /**
     * 需要在随后调用
     * <ul>
     * <li>{@link #setIsFirstRecharge(boolean)}(required)</li>
     * <li>{@link #setPayWayList(List)}(required)</li>
     * <li>{@link #setRechargePresenter(RechargePresenter)}(required)</li>
     * <li> {@link #setLastRechargeListType(int)}(optional)</li>
     * </ul>
     *
     * @param activity
     */
    public RechargeRecyclerViewAdapter(@NonNull Context activity) {
        mContext = activity;
        mLayoutInflater = LayoutInflater.from(mContext);
        // 无论横屏竖屏，我们都要拿到通常意义上较短的那个边的长度
        int screenWith = Math.min(DisplayUtils.getScreenWidth(), DisplayUtils.getScreenHeight());
        //if (getActivity() instanceof BaseRotateActivity) {
        //    BaseRotateActivity activity = (BaseRotateActivity) getActivity();
        //    if (activity.isDisplayPortrait()) {
        //        screenWith = DisplayUtils.getScreenWidth();
        //    } else {
        //        screenWith = DisplayUtils.getScreenHeight();
        //    }
        //} else {
        //    screenWith = DisplayUtils.getScreenWidth();
        //}
        int itemWidthPx = (screenWith - DisplayUtils.dip2px(mContext, 40)) / 3;
        itemHeightPx = itemWidthPx * 15 / 32;
        initRechargeActionHandler();
    }

    public void hidePopupWindow() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }

    private void setPopupWindowPosition(int position) {
        mPopupWindowPosition = position;
    }

    private void setUserSelectedPrice(int price) {
        mUserSelectedPrice = price;
    }

    public void setLoadingStatus() {
        mLoading = true;
        mLoadFailed = false;
        notifyDataSetChanged();
    }

    public void setLastRechargeListType(int lastRechargeListType) {
        mLastRechargeListType = lastRechargeListType;
    }

    private boolean isFirstStep() {
        return mStep == PayConstant.RECHARGE_STEP_FIRST;
    }

    public int getStep() {
        return mStep;
    }

    public void setStep(int step) {
        mStep = step;
    }

    public void setIsFirstRecharge(boolean isFirstRecharge) {
        mIsFirstRecharge = isFirstRecharge;
        mStep = mIsFirstRecharge ? PayConstant.RECHARGE_STEP_FIRST : PayConstant.RECHARGE_STEP_SECOND;
    }

    private void initRechargeActionHandler() {
        RechargeActionHandler.IDialogActionListener singleDealQuotaDialogActionListener = new RechargeActionHandler.IDialogActionListener() {
            @Override
            public void positiveHandle(Diamond item) {
                //微信、小米钱包不支持3000的支付页面点击去支付宝
                StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                        PayStatisticUtils.getRechargeTemplate(EXCEED_SINGLE_DEAL_QUOTA_TO_ZFB, getCurrentPayWay()), TIMES, "1");
                switchPayWayAndRecharge(item, PayWay.ZHIFUBAO);
            }

            @Override
            public void negativeHandle(Diamond item) {
                //用户选择的金额超出了微信和小米钱包3000的限制，则会弹出一个提示框，提示框展示一次打一次，携带用户选择钻石数
                StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                        PayStatisticUtils.getRechargeTemplate(EXCEED_SINGLE_DEAL_QUOTA_ADJUST, getCurrentPayWay(),
                                item.getPrice() / 100), TIMES, "1");
                setUserSelectedPrice(0);
            }
        };
        /**单笔限额处理器*/
        mFirstRechargeActionHandler = new SingleDealQuotaHandler(mContext);
        mFirstRechargeActionHandler.setDialogActionListener(singleDealQuotaDialogActionListener);

        RechargeActionHandler.IDialogActionListener oneDayQuotaDialogActionListener = new RechargeActionHandler.IDialogActionListener() {
            @Override
            public void positiveHandle(Diamond item) {
                switchPayWayAndRecharge(item, PayWay.ZHIFUBAO);
            }

            @Override
            public void negativeHandle(Diamond item) {
                setUserSelectedPrice(0);
                mRechargePresenter.recharge(item, getCurrentPayWay());
            }
        };

        /*单日限额处理器*/
        RechargeActionHandler mOneDayQuotaHandler = new OneDayQuotaHandler(mContext);
        mOneDayQuotaHandler.setDialogActionListener(oneDayQuotaDialogActionListener);

        mFirstRechargeActionHandler.setNext(mOneDayQuotaHandler);
    }

    public void setPayWayList(@NonNull List<PayWay> payWayList) {
        mPayWayList = payWayList;
    }

    public void setRechargePresenter(@NonNull RechargePresenter presenter) {
        mRechargePresenter = presenter;
    }

    ////////////////////////
    ////////////////////////
    ////////////////////////

    private BalanceViewHolder getBalanceViewHolder(ViewGroup parent) {
        if (mBalanceViewHolder == null) {
            mBalanceViewHolder = new BalanceViewHolder(
                    mLayoutInflater.inflate(R.layout.recharge_balance_and_exchangeable_section, parent, false));
            // 初始化余额
            setBalance(MyUserInfoManager.getInstance().getUser().getDiamondNum(), MyUserInfoManager.getInstance().getUser().getVirtualDiamondNum(), false);
            updateExchangeableAndWillExpireDiamond(mRechargePresenter.getExchangeableDiamondCnt(),
                    mRechargePresenter.getWillExpireDiamondCnt(), mRechargePresenter.getWillExpireGiftCardCnt(), false);
            RxView.clicks(mBalanceViewHolder.mExchangeBtn)
                    .throttleFirst(PayConstant.RECHARGE_CLICK_INTERVAL, TimeUnit.SECONDS)
                    .subscribe(new Action1<Void>() {
                        @Override
                        public void call(Void aVoid) {
                            //TODO 记得改
//                            ExchangeDiamondActivity.openActivity((Activity) mContext)
                        }
                    });
            RxView.clicks(mBalanceViewHolder.mBalanceGoldContainer)
                    .throttleFirst(PayConstant.RECHARGE_CLICK_INTERVAL, TimeUnit.SECONDS)
                    .subscribe(new Action1<Void>() {
                        @Override
                        public void call(Void aVoid) {
                            mRechargePresenter.getBalance();
                        }
                    });
            RxView.clicks(mBalanceViewHolder.mBalanceSilverContainer)
                    .throttleFirst(PayConstant.RECHARGE_CLICK_INTERVAL, TimeUnit.SECONDS)
                    .subscribe(new Action1<Void>() {
                        @Override
                        public void call(Void aVoid) {
                            mRechargePresenter.getBalance();
                        }
                    });
        }
        return mBalanceViewHolder;
    }

    public void setBalance(int balance, int virtualBalance, boolean isNeedNotify) {
        if (mBalanceViewHolder == null || balance < 0 || virtualBalance < 0) {
            MyLog.e(TAG, "mBalanceViewHolder maybe null, balance:" + balance + "virtualBalance: " + virtualBalance);
            return;
        }
        mBalanceViewHolder.mTotalDiamondCount.setText(String.valueOf(balance + virtualBalance));
        mBalanceViewHolder.mBalanceGold.setText(String.valueOf(balance));
        mBalanceViewHolder.mBalanceSilver.setText(String.valueOf(virtualBalance));
        if (isNeedNotify) {
            notifyDataSetChanged();
        }
    }

    /**
     * add by lyy显示兑换view
     *
     * @param exchangeableDiamondCnt 当前可兑换的钻石数
     * @param willExpireDiamondCnt   将要过期的钻石数
     * @param isNeedNotify
     */
    public void updateExchangeableAndWillExpireDiamond(int exchangeableDiamondCnt
            , int willExpireDiamondCnt, int willExpireGiftCardCnt, boolean isNeedNotify) {
        MyLog.d(TAG, "set exchangeableDiamondCnt:" + exchangeableDiamondCnt
                + ", willExpireDiamondCnt:" + willExpireDiamondCnt
                + ", willExpireGiftCardCnt:" + willExpireGiftCardCnt
                + ", isNeedNotify:" + isNeedNotify);
        if (mBalanceViewHolder == null) {
            MyLog.e(TAG, "mBalanceViewHolder maybe null, exchangeableDiamondCnt:" + exchangeableDiamondCnt
                    + ", willExpireDiamondCnt:" + willExpireDiamondCnt
                    + ", willExpireGiftCardCnt:" + willExpireGiftCardCnt
                    + ", isNeedNotify:" + isNeedNotify);
            return;
        }

//        mBalanceViewHolder.mExchangeContainer.setVisibility(View.VISIBLE);
        if (exchangeableDiamondCnt <= 0) {
            mBalanceViewHolder.mExchangeableDiamondTv.setText(R.string.insufficient_ticket_tip);
            mBalanceViewHolder.mExchangeBtn.setText(R.string.recharge_exchange_gem_btn_tip_detail);
        } else if (!CommonUtils.isChinese() && exchangeableDiamondCnt > 9999) {
            mBalanceViewHolder.mExchangeableDiamondTv.setText(mContext.getResources().getQuantityString(R.plurals.recharge_exchange_tip, exchangeableDiamondCnt, "\n" + exchangeableDiamondCnt));
            mBalanceViewHolder.mExchangeBtn.setText(R.string.recharge_exchange_gem_btn);
        } else {
            mBalanceViewHolder.mExchangeableDiamondTv.setText(mContext.getResources().getQuantityString(R.plurals.recharge_exchange_tip, exchangeableDiamondCnt, String.valueOf(exchangeableDiamondCnt)));
            mBalanceViewHolder.mExchangeBtn.setText(R.string.recharge_exchange_gem_btn);
        }
        mBalanceViewHolder.mExchangeBtn.setVisibility(View.GONE);
        //TODO 即将过期的提醒部分
        //if (willExpireDiamondCnt > 0 || willExpireGiftCardCnt > 0) {
        //    mBalanceViewHolder.mWillExpireContainer.setVisibility(View.VISIBLE);
        //    mBalanceViewHolder.mNoWillExpire.setVisibility(View.GONE);
        //    if (willExpireDiamondCnt > 0 && willExpireGiftCardCnt > 0) {
        //        String diamondCnt = mContext.getResources().getQuantityString(R.plurals.recharge_silver_diamond, willExpireDiamondCnt, willExpireDiamondCnt);
        //        String giftCnt = mContext.getResources().getQuantityString(R.plurals.gift, willExpireGiftCardCnt, willExpireGiftCardCnt);
        //        mBalanceViewHolder.mWillExpireText.setText(getString(R.string.both_will_expire, diamondCnt, giftCnt));
        //    } else if (willExpireDiamondCnt > 0) {
        //        String diamondCnt = mContext.getResources().getQuantityString(R.plurals.recharge_silver_diamond, willExpireDiamondCnt, willExpireDiamondCnt);
        //        mBalanceViewHolder.mWillExpireText.setText(getString(R.string.will_expire, diamondCnt));
        //    } else {
        //        String giftCnt = mContext.getResources().getQuantityString(R.plurals.gift, willExpireGiftCardCnt, willExpireGiftCardCnt);
        //        mBalanceViewHolder.mWillExpireText.setText(getString(R.string.will_expire, giftCnt));
        //    }
        //} else {
        //    mBalanceViewHolder.mWillExpireContainer.setVisibility(View.GONE);
        //    mBalanceViewHolder.mNoWillExpire.setVisibility(View.VISIBLE);
        //}
        if (isNeedNotify) {
            notifyDataSetChanged();
        }
    }

    private RecyclerView.ViewHolder getDividerViewHolder(ViewGroup parent) {
        if (mDividerViewHolder == null) {
            mDividerViewHolder = new DividerViewHolder(mLayoutInflater.inflate(R.layout.recharge_divider, parent, false));
        }
        //optionalShowDivider(false);
        return mDividerViewHolder;
    }

    private void optionalShowDivider(boolean isNeedNotify) {
        if (mDividerViewHolder == null) {
            return;
        }
        if (isFirstStep()) {
            mDividerViewHolder.mChoosePayWayTip.setVisibility(View.VISIBLE);
            mDividerViewHolder.mBlank.setVisibility(View.GONE);
        } else {
            mDividerViewHolder.mChoosePayWayTip.setVisibility(View.GONE);
            mDividerViewHolder.mBlank.setVisibility(View.VISIBLE);
        }
        if (isNeedNotify) {
            notifyDataSetChanged();
        }
    }

    private PayWayViewHolder getPayWayViewHolder(ViewGroup parent) {
        return new PayWayViewHolder(mLayoutInflater.inflate(R.layout.recharge_pay_way_item_with_arrow, parent, false));
    }

    private void bindPayWayViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        if (!(viewHolder instanceof PayWayViewHolder)) {
            MyLog.e(TAG, "unexpected type, position:" + position);
            return;
        }
        PayWayViewHolder payWayViewHolder = (PayWayViewHolder) viewHolder;
        if (!isFirstStep() && mPayWayViewHolder == null) {
            mPayWayViewHolder = payWayViewHolder;
        }
        payWayViewHolder.mOtherPayWayTip.setVisibility(isFirstStep() ? View.GONE : View.VISIBLE);
        PayWay payWay = isFirstStep() ? mPayWayList.get(position - 2) : getCurrentPayWay();
        applySelectedPayWay(false, payWay, payWayViewHolder);
        RxView.clicks(payWayViewHolder.itemView)
                .throttleFirst(PayConstant.RECHARGE_CLICK_INTERVAL, TimeUnit.SECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        if (isFirstStep()) {
                            mStep++;
                            setCurrentPayWay(mPayWayList.get(position - 2));
                            mPopupWindowPosition = 0;
                            mLastRechargeListType = RechargeConfig.getRechargeListType(getCurrentPayWay());
                            MyLog.d(TAG, "current pay way:" + getCurrentPayWay() + ", position:" + position);
                            mRechargePresenter.pullPriceListAsync();
                            //各支付列表，有钻石选择的页面，展示一次打一次
                            StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY, PayStatisticUtils.getRechargeTemplate(PRICE_LIST, getCurrentPayWay()), TIMES, "1");
                        } else {
                            //显示选择支付方式的对话框
                            if (mPayWaySwitchDialogHolder == null) {
                                mPayWaySwitchDialogHolder = new PayWaySwitchDialogHolder(mContext, mPayWayList, RechargeRecyclerViewAdapter.this);
                            }
                            mPayWaySwitchDialogHolder.getSelectPayWayDialog(getCurrentPayWay()).show();
                        }
                    }
                });
        // 非首次充值，进来就显示上次选择的支付方式
        if (!mIsFirstRecharge) {
            //各支付列表，有钻石选择的页面，展示一次打一次
            StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY, PayStatisticUtils.getRechargeTemplate(PRICE_LIST, getCurrentPayWay()), TIMES, "1");
        }
    }

    /**
     * 根据选择的支付方式显示对应的图标、文字， 更新充值列表
     */
    private void applySelectedPayWay(boolean isNeedNotify, PayWay payWay, PayWayViewHolder payWayViewHolder) {
        if (payWayViewHolder == null) {
            payWayViewHolder = mPayWayViewHolder;
        }
        if (payWayViewHolder == null) {
            MyLog.e(TAG, "payWayViewHolder == null, can not apply selected pay way, isNeedNotify:" + isNeedNotify);
            return;
        }
        Drawable icon = mContext.getResources().getDrawable(RechargeConfig.getPayWayInfoMap().get(payWay).mIconId);
        if (isFirstStep()) {
            payWayViewHolder.mBottomLine.setVisibility(View.VISIBLE);
            icon.setBounds(0, 0, icon.getMinimumWidth(), icon.getMinimumHeight());
            payWayViewHolder.mPayWayTv.setCompoundDrawables(icon, null, null, null);
            payWayViewHolder.mPayWayTv.setText(RechargeConfig.getPayWayInfoMap().get(payWay).mNameId);
            payWayViewHolder.mPayWayTv.setCompoundDrawablePadding(DisplayUtils.dip2px(mContext, 20f));
        } else {
            payWayViewHolder.mBottomLine.setVisibility(View.GONE);
            // 这里的图要比原图小
            icon.setBounds(0, 0, 60, 52);
            payWayViewHolder.mPayWayTv.setCompoundDrawables(icon, null, null, null);
            payWayViewHolder.mPayWayTv.setText(RechargeConfig.getPayWayInfoMap().get(payWay).mNameId);
            payWayViewHolder.mPayWayTv.setCompoundDrawablePadding(DisplayUtils.dip2px(mContext, 10f));
            if (isNeedNotify) {
                if (mLastRechargeListType != RechargeConfig.getRechargeListType(payWay)) {
                    mLastRechargeListType = RechargeConfig.getRechargeListType(payWay);
                    // 异步更新充值列表
                    mRechargePresenter.pullPriceListAsync();
                } else {
                    MyLog.d(TAG, "need not pull price list, because recharge list type not change:" + mLastRechargeListType);
                    Observable.timer(DEFAULT_SELECTED_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .compose(((RxActivity) mContext).bindUntilEvent(ActivityEvent.DESTROY))
                            .subscribe(new Action1<Object>() {
                                @Override
                                public void call(Object o) {
                                    if (mPopupWindowAttatchedView != null) {
                                        onGridViewItemClicked(mPopupWindowAttatchedView, mPopupWindowPosition);
                                    }
                                }
                            });
                }
                notifyDataSetChanged();
            }
        }
    }

    private RecyclerView.ViewHolder getPriceGridViewHolder(ViewGroup parent) {
        if (mPriceGridViewHolder != null) {
            return mPriceGridViewHolder;
        }
        View view = mLayoutInflater.inflate(R.layout.recharge_show_and_pay, parent, false);
        mPriceGridViewHolder = new PriceGridViewHolder(view);

        return mPriceGridViewHolder;
    }

    private void bindGirdViewHolder() {
        if (mPriceGridViewHolder == null) {
            return;
        }
        int bigAmountTipHeightAndMargin = 0;
        // TODO 大额充值引导客服提示
        //if (getCurrentPayWay() == PayWay.WEIXIN || getCurrentPayWay() == PayWay.ZHIFUBAO) {
        //    mPriceGridViewHolder.mBigAmountTip.setVisibility(View.VISIBLE);
        //    mPriceGridViewHolder.mBigAmountTip.setOnClickListener(v -> {
        //        //提转到999
        //        User user999 = new User();
        //        user999.setUid(TARGET_999);
        //        user999.setNickname(getString(R.string.username_999));
        //        user999.setIsBothwayFollowing(true);
        //        user999.setCertificationType(2);//官方账号，参考User.proto PersonalInfo.certificationType
        //        ComposeMessageActivity.openActivity(mContext, user999);
        //    });
        //    // TextView的高度 + marginBottom
        //    bigAmountTipHeightAndMargin = DisplayUtils.dip2px(mContext, 20 + 34.67f);
        //} else {
        //    mPriceGridViewHolder.mBigAmountTip.setVisibility(View.GONE);
        //}

        // 计算高度
        int lineNumber = mRechargeList.size() % 3 == 0 ? mRechargeList.size() / 3 : mRechargeList.size() / 3 + 1;
        ViewGroup.LayoutParams layoutParams = mPriceGridViewHolder.itemView.getLayoutParams();
        // 行数 × (item上方的间距+item的高度) + 支付按钮高度(包括上下边距) + 大额提现提示高度
        layoutParams.height = lineNumber * (DisplayUtils.dip2px(mContext, 13.33f) + itemHeightPx)
                + DisplayUtils.dip2px(mContext, (float) (40 + 43.33 + 18.67))
                + bigAmountTipHeightAndMargin;

        mPriceGridViewHolder.mGridView.setAdapter(getGridViewAdapter());
        mPriceGridViewHolder.mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onGridViewItemClicked(view, position);
            }
        });

        // 点击充值按钮
        RxView.clicks(mPriceGridViewHolder.mPayBtn)
                .throttleFirst(PayConstant.RECHARGE_CLICK_INTERVAL, TimeUnit.SECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        // 支付方式使用了deviceId，在使用前申请一下修复权限问题
                        DeviceUtils.getDeviceId();
                        //选择钻石数后，点击立即付款一次，上报一次
                        StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY, PayStatisticUtils.getRechargeTemplate(CLICK_PAY_BTN, getCurrentPayWay()), TIMES, "1");
                        switch (getCurrentPayWay()) {
                            // 微信支付SDK有未安装微信的返回码
                            //case WEIXIN:
                            //    scribeAppNotInstall("com.tencent.mm", PayWay.WEIXIN);
                            //    break;
                            case ZHIFUBAO:
                                scribeAppNotInstall("com.eg.android.AlipayGphone", PayWay.ZHIFUBAO);
                                break;
                        }

                        Diamond data = mRechargeList.get(mPopupWindowPosition);
                        if (data == null) {
                            MyLog.e(TAG, "Diamond is null, position:" + mPopupWindowPosition);
                            return;
                        }
                        if (!mFirstRechargeActionHandler.intercept(data)) {
                            mRechargePresenter.recharge(data, getCurrentPayWay());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "unexpected error, position:" + mPopupWindowPosition, throwable);
                    }
                });
    }

    private void scribeAppNotInstall(@NonNull final String packageName, @NonNull final PayWay payWay) {
        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                if (!CommonUtils.isAppInstalled(mContext, packageName)) {
                    StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY, PayStatisticUtils.getRechargeTemplate(APP_NOT_INSTALL, payWay), TIMES, "1");
                }
                subscriber.onNext(0);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    private ListAdapter getGridViewAdapter() {
        if (mGridViewAdapter != null) {
            return mGridViewAdapter;
        }
        mGridViewAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return mRechargeList == null ? 0 : mRechargeList.size();
            }

            @Override
            public Object getItem(int position) {
                return mRechargeList == null ? null : mRechargeList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                GridViewItemViewHolder gridViewItemViewHolder = null;
                if (convertView != null && convertView.getTag() instanceof GridViewItemViewHolder) {
                    gridViewItemViewHolder = (GridViewItemViewHolder) convertView.getTag();
                } else {
                    convertView = mLayoutInflater.inflate(R.layout.recharge_diamond_price_item, parent, false);
                    ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
                    layoutParams.height = itemHeightPx;
                    convertView.setLayoutParams(layoutParams);
                    gridViewItemViewHolder = new GridViewItemViewHolder(convertView);
                }
                convertView.setTag(gridViewItemViewHolder);

                Diamond data = (Diamond) getItem(position);
                if (data == null) {
                    MyLog.e(TAG, "price info is null, position:" + position);
                    return convertView;
                }
                // 国际化的支付方式的价格单位不是服务器下发的，需要自己组装在SkuDetail里
                if (!RechargeConfig.isServerDiamondInfoCanDirectlyUse(RechargeConfig.getRechargeListType(getCurrentPayWay()))) {
                    SkuDetail skuDetail = data.getSkuDetail();
                    if (skuDetail != null && !TextUtils.isEmpty(skuDetail.getPrice())) {
                        gridViewItemViewHolder.price.setText(skuDetail.getPrice());
                    } else {
                        MyLog.e(TAG, "skuDetail:" + skuDetail + ", payWay:" + getCurrentPayWay());
                    }
                } else {
                    gridViewItemViewHolder.price.setText(mContext.getResources().getQuantityString(
                            R.plurals.recharge_money_amount, data.getPrice() / 100,
                            String.format("%.0f", data.getPrice() / 100.0)));
                }
                String diamondNumber = mContext.getResources().getQuantityString(R.plurals.gold_diamond, data.getCount(), data.getCount());
                // 不是中文的情况下钻石数字号改为40px，价格字号改为34px
                if (!CommonUtils.isChinese()) {
                    gridViewItemViewHolder.diamondNumber.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13.33f);// 40px
                    gridViewItemViewHolder.price.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11.33f);// 34px
                }
                gridViewItemViewHolder.diamondNumber.setText(diamondNumber);
                if (TextUtils.isEmpty(data.getIconUrl())) {
                    gridViewItemViewHolder.cornerIcon.setVisibility(View.GONE);
                } else {
                    gridViewItemViewHolder.cornerIcon.setVisibility(View.VISIBLE);
                    FrescoWorker.loadImage(gridViewItemViewHolder.cornerIcon, ImageFactory.newHttpImage(data.getIconUrl()).build());
                }
                if (position == mPopupWindowPosition) {
                    mPopupWindowAttatchedView = convertView;
                }
                return convertView;
            }
        };
        return mGridViewAdapter;
    }

    public void clickGridViewItem() {
        onGridViewItemClicked(mPopupWindowAttatchedView, mPopupWindowPosition);
    }

    private void onGridViewItemClicked(@Nullable View itemView, int position) {
        if (!isRechargeFragmentOnTop()) {
            MyLog.w(TAG, "recharge fragment not on top, do not show popup window");
            return;
        }
        if (itemView == null) {
            MyLog.w(TAG, "itemView is null");
            return;
        }
        MyLog.d(TAG, "item clicked, position:" + position);
        if (position < 0 || position >= mRechargeList.size()) {
            return;
        }
        mPopupWindowPosition = position;
        if (mPopupWindowAttatchedView != null && mPopupWindowAttatchedView != itemView) {
            mPopupWindowAttatchedView.setSelected(false);
        }
        mPopupWindowAttatchedView = itemView;
        mPopupWindowAttatchedView.setSelected(true);
        CharSequence price = ((TextView) itemView.findViewById(R.id.price)).getText();
        mPriceGridViewHolder.mPayBtn.setText(mContext.getString(R.string.recharge_immediately, price));
        Diamond data = mRechargeList.get(position);
        if (data == null) {
            return;
        }
        StringBuilder tipBuilder = new StringBuilder();
        if (data.getExtraGive() > 0) {
            tipBuilder.append(mContext.getResources().getQuantityString(R.plurals.given_diamond, data.getExtraGive(), data.getExtraGive()));
        }
        // TODO 服务器下发的这个字段需要实现国际化
        if (!TextUtils.isEmpty(data.getSubTitle())) {
            if (tipBuilder.length() != 0) {
                tipBuilder.append(GlobalData.app().getResources().getString(R.string.comma));
            }
            tipBuilder.append(data.getSubTitle());
        }
        showPopupWindow(itemView, position, tipBuilder.toString());
    }

    private void showPopupWindow(@NonNull View target, int position, @NonNull String tip) {
        if (mPopupWindow == null) {
            View contentView = mLayoutInflater.inflate(R.layout.recharge_diamond_sale_info_popup, null);
            sharp = (ImageView) contentView.findViewById(R.id.sharp);
            saleTip = (TextView) contentView.findViewById(R.id.sale_tip);
            mPopupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mPopupWindow.setOutsideTouchable(true);
            mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
            mPopupWindow.update();
        }
        if (mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
        // just fot test
        // tip = "送3000钻；有效期30天；1234；123；12；1；";
        if (TextUtils.isEmpty(tip)) {
            return;
        }
        String[] tipArray = tip.split("；");
        if (tipArray.length == 0) {
            MyLog.w(TAG, "split result exception 1, tip:" + tip);
            return;
        }
        List<String> tipList = new ArrayList<>();
        for (String t : tipArray) {
            if (TextUtils.isEmpty(t) || TextUtils.isEmpty(t.trim())) {
                continue;
            }
            tipList.add(t.trim());
        }
        if (tipList.isEmpty()) {
            MyLog.w(TAG, "split result exception 2, tip:" + tip);
            return;
        }
        tip = "";
        for (String t : tipList) {
            tip += t + "\n";
        }
        tip = tip.substring(0, tip.length() - 1);
        MyLog.d(TAG, "will show tip(\\n replaced by <br>):" + tip.replace("\n", "<br>"));
        saleTip.setText(tip);
        LayoutUtils.setMargins(sharp, getSharpX(target, position), 0, 0, 0);
        mPopupWindow.showAsDropDown(target, getPopupWindowX(target, position), gePopupWindowY(target));
    }

    private int getSharpX(@NonNull View target, int position) {
        int sharpX = 0;
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        if (mPopupWindow.getContentView() == null) {
            return sharpX;
        }
        saleTip.measure(w, h);
        int popupWindowWidth = saleTip.getMeasuredWidth();
        int targetWidth = target.getWidth();
        if (targetWidth >= popupWindowWidth) {
            sharpX = popupWindowWidth / 2;
        } else {
            switch (position % 3) {
                case 0:// 左边
                    sharpX = targetWidth / 2;
                    break;
                case 1:// 中间
                    sharpX = popupWindowWidth / 2;
                    break;
                case 2:// 右边
                    sharpX = popupWindowWidth - targetWidth / 2;
                    break;
            }
        }
        sharpX -= 14;// 14是小三角宽度的一半
        return sharpX;
    }

    private int gePopupWindowY(@NonNull View target) {
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        saleTip.measure(w, h);
        // 上移 价格列表（gridView的item）的高度 + popupWindow的高度（圆角矩形的高度 + 箭头高度） + 间距
        return -1 * (target.getHeight() + saleTip.getMeasuredHeight() + 13 + DisplayUtils.dip2px(4));
    }

    private int getPopupWindowX(@NonNull View target, int position) {
        int x = 0;
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        mPopupWindow.getContentView().measure(w, h);
        int popupWindowWidth = mPopupWindow.getContentView().getMeasuredWidth();
        int targetWidth = target.getWidth();
        if (targetWidth >= popupWindowWidth) {
            x = (targetWidth - popupWindowWidth) / 2;
        } else {
            switch (position % 3) {
                case 0:// 左边
                    x = 0;
                    break;
                case 1:// 中间，一个负数
                    x = (targetWidth - popupWindowWidth) / 2;
                    break;
                case 2:// 右边，一个负数
                    x = targetWidth - popupWindowWidth;
                    break;
            }
        }
        return x;
    }

    private ErrorViewHolder getErrorViewHolder(ViewGroup parent) {
        if (mErrorViewHolder == null) {
            mErrorViewHolder = new ErrorViewHolder(mLayoutInflater.inflate(R.layout.recharge_error_view_section, parent, false));
            int padding = DisplayUtils.dip2px(mContext, 10);
            mErrorViewHolder.mErrorView.getRetryTv().setPadding(padding, padding, padding, DisplayUtils.dip2px(mContext, 150));

            // 设置重试动作
            RxView.clicks(mErrorViewHolder.mErrorView.getRetryTv())
                    .throttleFirst(PayConstant.RECHARGE_CLICK_INTERVAL, TimeUnit.SECONDS)
                    .subscribe(new Action1<Void>() {
                        @Override
                        public void call(Void aVoid) {
                            mRechargePresenter.pullPriceListAsync();
                        }
                    });
        }
        return mErrorViewHolder;
    }

    private ErrorViewHolder getLoadingViewHolder(ViewGroup parent) {
        if (mLoadingViewHolder == null) {
            mLoadingViewHolder = new ErrorViewHolder(mLayoutInflater.inflate(R.layout.recharge_error_view_section, parent, false));
            int padding = DisplayUtils.dip2px(mContext, 10);
            mLoadingViewHolder.mErrorView.getRetryTv().setPadding(padding, padding, padding, DisplayUtils.dip2px(mContext, 150));
            mLoadingViewHolder.mErrorView.getRetryTv().setText(R.string.default_loading_hint);
        }
        return mLoadingViewHolder;
    }


    @Override
    public int getItemViewType(int position) {
        //MyLog.d(TAG, String.format("get item view type, loadFailed:%s, loading:%s", mLoadFailed, mLoading));
        switch (position) {
            case 0:
                return ITEM_TYPE_BALANCE;
            case 1:
                return ITEM_TYPE_DIVIDER;
            default: {
                if (isFirstStep() || position == 2) {
                    return ITEM_TYPE_PAY_WAY;
                }
                if (mLoading) {
                    return ITEM_TYPE_LOADING;
                }
                if (mLoadFailed) {
                    return ITEM_TYPE_ERROR;
                }
                return ITEM_TYPE_PRICE;
            }
        }
    }

    @Override
    public int getItemCount() {
        if (isFirstStep()) {
            return 2 + mPayWayList.size();// 如果是第一次充值、第1步，余额部分 + 分隔线部分 + 支付方式部分
        }
        // 如果充值列表正在加载或加载失败，则显示余额、可兑换钻石和错误提示部分；否则显示余额、可兑换钻石和充值列表部分
        // 第2步，余额部分 + 分隔线部分 + 支付方式部分 + {价格列表支付部分，加载中，加载失败}
        return 4;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //MyLog.w(TAG, "viewType:" + viewType);
        switch (viewType) {
            case ITEM_TYPE_BALANCE:
                return getBalanceViewHolder(parent);
            case ITEM_TYPE_DIVIDER:
                return getDividerViewHolder(parent);
            case ITEM_TYPE_PAY_WAY:
                return getPayWayViewHolder(parent);
            case ITEM_TYPE_PRICE:
                return getPriceGridViewHolder(parent);
            case ITEM_TYPE_ERROR:
                return getErrorViewHolder(parent);
            case ITEM_TYPE_LOADING:
                return getLoadingViewHolder(parent);
            default:
                MyLog.e(TAG, "unexpected viewType:" + viewType);
                break;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int itemType = getItemViewType(position);
        switch (itemType) {
            case ITEM_TYPE_DIVIDER:
                optionalShowDivider(false);
                break;
            case ITEM_TYPE_PAY_WAY:
                bindPayWayViewHolder(holder, position);
                break;
            case ITEM_TYPE_PRICE:
                bindGirdViewHolder();
                break;
        }
        //if (itemType == ITEM_TYPE_PRICE) {
        //    // 减去余额部分和钻石兑换钱部分
        //    bindRechargeListItem(holder, mRechargeList.get(position - 2), getCurrentPayWay());
        //}
    }


    /**
     * 别忘了通知adapter更新
     */
    @MainThread
    public void setRechargeList(@NonNull List<Diamond> rechargeList) {
        mRechargeList.clear();
        mRechargeList.addAll(rechargeList);
        mLoadFailed = mRechargeList.isEmpty();
        mLoading = false;
        MyLog.d(TAG, String.format("set recharge list, loadFailed:%s, loading:%s", mLoadFailed, mLoading));
        if (mGridViewAdapter != null) {
            mGridViewAdapter.notifyDataSetChanged();
        }
        // 先隐藏起来，防止数据刷新时跑到左上角
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
        if (mUserSelectedPrice > 0) {
            determinePopupWindowPosition(mRechargeList);
        }
        notifyDataSetChanged();
        Observable.timer(DEFAULT_SELECTED_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(((RxActivity) mContext).bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        if (!isFirstStep() && !mLoadFailed && mPopupWindowAttatchedView != null) {
                            onGridViewItemClicked(mPopupWindowAttatchedView, mPopupWindowPosition);
                        }
                    }
                });
    }

    /**
     * 用于大额充值引导时，从一种支付方式切换到其他支付方式（支付宝）时重新确定需要弹窗的位置<br/>
     * 这里会修改{@link #mPopupWindowPosition}，然后RechargePriceAdapter#notifyDataSetChanged()后
     * 会重新调用{@link #mGridViewAdapter}的getView()，在那里
     * 根据{@link #mPopupWindowPosition}设置{@link #mPopupWindowAttatchedView}
     *
     * @param rechargeList
     */
    @MainThread
    private void determinePopupWindowPosition(@NonNull List<Diamond> rechargeList) {
        for (int i = 0; i < rechargeList.size(); i++) {
            Diamond diamond = rechargeList.get(i);
            if (diamond.getSkuDetail() != null) {
                MyLog.e(TAG, "unsupported big amount recharge pay way");
                break;
            }
            if (diamond.getPrice() == mUserSelectedPrice) {
                mPopupWindowPosition = i;
                return;
            }
        }
        MyLog.w(TAG, String.format("not found price:%d in %s," +
                        " always means that bug or recharge list config error",
                mUserSelectedPrice, getCurrentPayWay().name()));
        mPopupWindowPosition = 0;
    }

    @Override
    public void onPayWaySwitched(PayWay payWay) {
        setCurrentPayWay(payWay);
        //各支付列表，有钻石选择的页面，展示一次打一次
        StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY, PayStatisticUtils.getRechargeTemplate(PRICE_LIST, getCurrentPayWay()), TIMES, "1");
        if (!mIsFirstRecharge) {
            mRechargePresenter.saveUserSelectedPayWay();
        }
        setPopupWindowPosition(0);
        // 更换payway_section的图标、文本，更新充值列表
        applySelectedPayWay(true, getCurrentPayWay(), null);
    }

    /**
     * 大额充值引导时，自动切换支付方式
     *
     * @param item
     * @param newPayWay
     */
    private void switchPayWayAndRecharge(@NonNull Diamond item, @NonNull PayWay newPayWay) {
        setUserSelectedPrice(item.getPrice());
        setCurrentPayWay(newPayWay);
        // 更换payway_section的图标、文本，更新充值列表
        applySelectedPayWay(true, getCurrentPayWay(), null);
        mRechargePresenter.recharge(item, getCurrentPayWay());
    }

    private boolean isRechargeFragmentOnTop() {
        return FragmentNaviUtils.getTopFragment((FragmentActivity) mContext) instanceof RechargeFragment;
    }

}

//////////////////////////////
///////View Holder////////////
//////////////////////////////

class BalanceViewHolder extends RecyclerView.ViewHolder {
    TextView mTotalDiamondCount;
    View mBalanceGoldContainer;
    TextView mBalanceGold;
    View mBalanceSilverContainer;
    TextView mBalanceSilver;
    TextView mExchangeableDiamondTv;
    TextView mExchangeBtn;
    LinearLayout mExchangeContainer;

    public BalanceViewHolder(View itemView) {
        super(itemView);
        mTotalDiamondCount = (TextView) itemView.findViewById(R.id.total_diamond_count_tv);
        mBalanceGoldContainer = itemView.findViewById(R.id.gold_balance_container);
        mBalanceGold = (TextView) itemView.findViewById(R.id.gold_balance);
        mBalanceSilverContainer = itemView.findViewById(R.id.silver_balance_container);
        mBalanceSilver = (TextView) itemView.findViewById(R.id.silver_balance);
        mExchangeableDiamondTv = (TextView) itemView.findViewById(R.id.exchangeable_diamond_tip);
        mExchangeBtn = (TextView) itemView.findViewById(R.id.exchange_btn);
        mExchangeContainer = (LinearLayout) itemView.findViewById(R.id.exchange_container);
    }

}

/**
 * 代表余额部分与支付方式部分之间的空间，不是RecyclerView的item之间的divider
 */
class DividerViewHolder extends RecyclerView.ViewHolder {
    View mChoosePayWayTip;
    View mBlank;

    public DividerViewHolder(View itemView) {
        super(itemView);
        mChoosePayWayTip = itemView.findViewById(R.id.choose_pay_way_tip);
        mBlank = itemView.findViewById(R.id.blank);
        mChoosePayWayTip.setVisibility(View.GONE);
        mChoosePayWayTip.setVisibility(View.GONE);
    }
}

class PayWayViewHolder extends RecyclerView.ViewHolder {
    TextView mPayWayTv;
    TextView mOtherPayWayTip;
    View mBottomLine;

    public PayWayViewHolder(View itemView) {
        super(itemView);
        mPayWayTv = (TextView) itemView.findViewById(R.id.pay_way_tv);
        mOtherPayWayTip = (TextView) itemView.findViewById(R.id.other_pay_way_tip_tv);
        mBottomLine = itemView.findViewById(R.id.bottom_line);
    }
}

class PriceGridViewHolder extends RecyclerView.ViewHolder {
    GridView mGridView;
    TextView mPayBtn;
    TextView mBigAmountTip;

    public PriceGridViewHolder(View itemView) {
        super(itemView);
        mGridView = (GridView) itemView.findViewById(R.id.grid_view);
        mPayBtn = (TextView) itemView.findViewById(R.id.pay_btn);
        mBigAmountTip = (TextView) itemView.findViewById(R.id.big_amount_tip);
    }
}

class ErrorViewHolder extends RecyclerView.ViewHolder {
    ErrorView mErrorView;

    public ErrorViewHolder(View itemView) {
        super(itemView);
        mErrorView = (ErrorView) itemView.findViewById(R.id.price_list_error_view);
    }
}

/**
 * GridView用的ViewHolder
 */
class GridViewItemViewHolder {
    View itemView;
    BaseImageView cornerIcon;
    TextView diamondNumber;
    TextView price;
    //int extraGive;
    //String subtitle;

    public GridViewItemViewHolder(View v) {
        itemView = v;
        cornerIcon = (BaseImageView) v.findViewById(R.id.corner_icon_iv);
        diamondNumber = (TextView) v.findViewById(R.id.diamond_number);
        price = (TextView) v.findViewById(R.id.price);
    }
}

//@Deprecated
//static class DiamondViewHolder extends RecyclerView.ViewHolder {
//    @Bind(R.id.price_tv)
//    TextView mPriceTv;
//    @Bind(R.id.diamond_num_tv)
//    TextView mDiamondNumTv;
//    @Bind(R.id.diamond_extra_tv)
//    TextView mExtraDiamondNumTv;
//    @Bind(R.id.diamond_sub_title)
//    TextView mDiamondSubTitle;
//    @Bind(R.id.icon_iv)
//    BaseImageView mIconIv;
//
//    public DiamondViewHolder(View view) {
//        super(view);
//        ButterKnife.bind(this, view);
//    }
//}
