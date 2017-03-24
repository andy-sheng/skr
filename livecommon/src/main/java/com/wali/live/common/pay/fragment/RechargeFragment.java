package com.wali.live.common.pay.fragment;

import android.content.Context;
import android.content.ServiceConnection;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.vending.billing.IInAppBillingService;
import com.base.activity.BaseActivity;
import com.base.dialog.MyProgressDialogEx;
import com.base.fragment.MyRxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.utils.CommonUtils;
import com.base.utils.network.Network;
import com.base.utils.toast.ToastUtils;
import com.base.view.BackTitleBar;
import com.jakewharton.rxbinding.view.RxView;
import com.live.module.common.R;
import com.wali.live.common.pay.adapter.RechargeRecyclerViewAdapter;
import com.wali.live.common.pay.assist.IPayActivityFlag;
import com.wali.live.common.pay.constant.PayConstant;
import com.wali.live.common.pay.constant.PayWay;
import com.wali.live.common.pay.constant.RechargeConfig;
import com.wali.live.common.pay.model.Diamond;
import com.wali.live.common.pay.presenter.RechargePresenter;
import com.wali.live.common.pay.utils.PayStatisticUtils;
import com.wali.live.common.pay.view.IRechargeView;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

import static com.wali.live.statistics.StatisticsKey.AC_APP;
import static com.wali.live.statistics.StatisticsKey.KEY;
import static com.wali.live.statistics.StatisticsKey.Recharge.VISIT;
import static com.wali.live.statistics.StatisticsKey.TIMES;

/**
 * 充值界面<br/>
 *
 * @module 充值
 * Created by rongzhisheng
 */
public class RechargeFragment extends MyRxFragment implements IRechargeView {
    private static final String TAG = RechargeFragment.class.getSimpleName();
    public static final int REQUEST_CODE = 0;

    /**
     * 用户当前选择的支付方式
     */
    private static PayWay sPayWay = PayWay.WEIXIN;

    public static PayWay getCurrentPayWay() {
        return sPayWay;
    }

    public static void setCurrentPayWay(PayWay payWay) {
        sPayWay = payWay;
    }

    //////////////////////////////////////////////////////
    /////////////////////非静态成员变量/////////////////////
    //////////////////////////////////////////////////////

    private RechargeRecyclerViewAdapter mRechargeAdapter;
    //private View mRechargeSelectPayWayTipView;
    //private ExpandableListView mExpandableListView;
    //private PayWayExpandableListAdapter mExpandableListAdapter;
    // google play支付用的Service
    private IInAppBillingService mService;
    private ServiceConnection mServiceConn;

    /**
     * 是否为国际化支付模式（支付方式排列顺序和国内有差异），区别于Native模式
     */
    private boolean mIsInternationalPayMode = CommonUtils.isInternationalPayMode();

    /**
     * 是否为首次充值
     */
    private boolean mIsFirstRecharge = RechargeConfig.getIsFirstRecharge();

    /** 是否存在展开的支付方式,结合{@link #mIsFirstRecharge}使用 */
    //private boolean mExistExpandedPayWay = false;

    /**
     * 是否只是需要获取可兑换的钻石数,结合{@link #mIsFirstRecharge}使用
     */
    private boolean mOnlyGetExchangeableDiamond = true;

    /**
     * 根据语言和渠道设置为国内支付列表或国际支付列表
     */
    private List<PayWay> mPayWayList;////

    //Inject
    private RechargePresenter mRechargePresenter;

    //////////////////////////////////////////////////////
    /////////////////////////方法//////////////////////////
    //////////////////////////////////////////////////////

    public RechargePresenter getRechargePresenter() {
        return mRechargePresenter;
    }

    @Override
    public int getRequestCode() {
        return REQUEST_CODE;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_recharge, container, false);
    }

    @Override
    protected void bindView() {
//        mPayWayList = mIsInternationalPayMode ? RechargeConfig.getInternationalPayWayList() : RechargeConfig.getNativePayWayList();
        mRechargePresenter = new RechargePresenter(this,(BaseActivity) getActivity());
        mPayWayList = RechargeConfig.getNativePayWayList();
        sPayWay = getInitialPayWay();// 设置支付手段的首选项
        // 清空缓存的价格列表，消除缓存在切换支付方式时可能出现问题的隐患
        mRechargePresenter.clearPriceListCache();


        BackTitleBar titleBar = (BackTitleBar) mRootView.findViewById(R.id.title_bar);
        titleBar.setTitle(R.string.diamond_outcome_hint);
        titleBar.getBackBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        titleBar.getTitleTv().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        titleBar.getRightTextBtn().setText(R.string.record_recharge_title);
        RxView.clicks(titleBar.getRightTextBtn()).throttleFirst(3, TimeUnit.SECONDS).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                {
                    if (!Network.hasNetwork(GlobalData.app())) {
                        ToastUtils.showToast(GlobalData.app(), R.string.network_unavailable);
                        return;
                    } else {
                        FragmentNaviUtils.addFragment(getActivity(), R.id.main_act_container, RechargeRecordFragment.class, null, true, false, true);
                    }
                }
            }
        });

        mRechargeAdapter = new RechargeRecyclerViewAdapter(getActivity());
        if (!mIsFirstRecharge) {
            mRechargeAdapter.setLastRechargeListType(RechargeConfig.getRechargeListType(sPayWay));
        }
        mRechargeAdapter.setIsFirstRecharge(mIsFirstRecharge);
        mRechargeAdapter.setPayWayList(mPayWayList);
        mRechargeAdapter.setRechargePresenter(mRechargePresenter);

        RecyclerView recyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mRechargeAdapter);
        recyclerView.setHasFixedSize(true);

        //mRechargeSelectPayWayTipView = mRootView.findViewById(R.id.recharge_select_pay_way_tip);
        //mRechargeSelectPayWayTipView.setVisibility(mIsFirstRecharge ? View.VISIBLE : View.GONE);
        //
        //mExpandableListView = (ExpandableListView) mRootView.findViewById(R.id.expandable_list);
        //mExpandableListView.setVisibility(mRechargeSelectPayWayTipView.getVisibility());
        //
        //mRootView.findViewById(R.id.line).setVisibility(mRechargeSelectPayWayTipView.getVisibility());
        //if (mIsFirstRecharge) {
        //    mExpandableListAdapter = new PayWayExpandableListAdapter(getActivity());
        //    mExpandableListView.setAdapter(mExpandableListAdapter);
        //    // 默认收起所有
        //    for (int i = 0; i < mExpandableListAdapter.getPayWayList().size(); i++) {
        //        mExpandableListView.collapseGroup(i);
        //    }
        //    mExpandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
        //        @Override
        //        public void onGroupExpand(int groupPosition) {
        //            MyLog.d(TAG, "on group expand");
        //            // 收起其他支付方式
        //            for (int i = 0; i < mExpandableListAdapter.getPayWayList().size(); i++) {
        //                if (i != groupPosition) {
        //                    mExpandableListView.collapseGroup(i);// 会触发OnGroupCollapseListener.onGroupCollapse
        //                }
        //            }
        //            mExistExpandedPayWay = true;
        //            // 异步拉取数据
        //            sPayWay = mExpandableListAdapter.getPayWayList().get(groupPosition);
        //            if (mLastRechargeListType != getRechargeListType(sPayWay)) {
        //                mLastRechargeListType = getRechargeListType(sPayWay);
        //                mRechargePresenter.pullPriceListAsync();
        //            } else {
        //                MyLog.d(TAG, "need not pull price list, because list type same, payWay:" + sPayWay);
        //            }
        //        }
        //    });
        //    mExpandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
        //        @Override
        //        public void onGroupCollapse(int groupPosition) {
        //            mExistExpandedPayWay = false;
        //        }
        //    });
        //}

        initPullPriceList();
        // 进入钻石充值页面打一次
        StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY, PayStatisticUtils.getRechargeTemplate(VISIT, getCurrentPayWay()), TIMES, "1");
    }

    /**
     * 在{@link #mPayWayList}初始化后调用
     */
    private PayWay getInitialPayWay() {
        PayWay defaultPayWay = mPayWayList.get(0);
        PayWay payWay = defaultPayWay;
        String lastPayWayName = RechargeConfig.getLastPaywayName();
        if (!TextUtils.isEmpty(lastPayWayName)) {
            try {
                payWay = PayWay.valueOf(lastPayWayName.toUpperCase());
            } catch (Exception e) {
                MyLog.e(TAG, "unexpected saved pay way:" + lastPayWayName);
            }
        }
        // 版本替换可能造成程序保存的用户上次使用的支付方式不适合当前版本
        // 想象这样的情况：小米钱包不能用于国际支付，GoogleWallet和PayPal不能用于国内支付
        if (!mPayWayList.contains(payWay)) {
            payWay = defaultPayWay;
        }
        return payWay;
    }

    private void initPullPriceList() {
//        if (mPayWayList.contains(PayWay.GOOGLEWALLET)) {
//            // 绑定Google Play的service
//            Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
//            serviceIntent.setPackage("com.android.vending");
//            mServiceConn = new ServiceConnection() {
//                @Override
//                public void onServiceConnected(ComponentName name, IBinder service) {
//                    mService = IInAppBillingService.Stub.asInterface(service);
//                    MyLog.i(TAG, "in app billing service connected");
//                    mRechargePresenter.setInAppBillingService(mService);
//                    // querySkuInfo();
//                    mRechargePresenter.consumeGooglePlayProduct();
//                    if (sPayWay == PayWay.GOOGLEWALLET) {
//                        // GooglePlay连接成功时拉一次
//                        mRechargePresenter.pullPriceListAsync();
//                    }
//                }
//
//                @Override
//                public void onServiceDisconnected(ComponentName name) {
//                    MyLog.i(TAG, "in app billing service disconnected");
//                    mService = null;
//                }
//            };
//            boolean bindResult = getActivity().bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
//            if (!bindResult) {
//                MyLog.e(TAG, "bind in app billing service fail");
//                if (sPayWay == PayWay.GOOGLEWALLET) {
//                    // GooglePlay连接失败时拉一次
//                    mRechargePresenter.pullPriceListAsync();
//                }
//            } else {
//                // 不知是否存在绑定成功但是没有回调ServiceConnection.onServiceConnected()的情况
//                MyLog.i(TAG, "bind in app billing service success");
//            }
//            // 拉取一次充值列表，决定要不要显示可兑换钻石的view
//            // 防止GoogleWallet在界面载入的时候拉两次列表
//            if (sPayWay != PayWay.GOOGLEWALLET) {
//                mRechargePresenter.pullPriceListAsync();
//            }
//        } else {
        mRechargePresenter.pullPriceListAsync();
//        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // 先隐藏弹窗，避免弹窗移到屏幕左上角被用户看到
        mRechargeAdapter.hidePopupWindow();
    }

    /**
     * 是否可能去微信充值了
     */
    private boolean mMayRechargeFromOutSide = false;

    @Override
    public void onResume() {
        MyLog.d(TAG, "onResume");
        super.onResume();
        KeyboardUtils.hideKeyboard(getActivity());
        if (mMayRechargeFromOutSide) {
            mRechargePresenter.syncBalance();
        }
    }

    @Override
    public void onPause() {
        MyLog.d(TAG, "onPause");
        mMayRechargeFromOutSide = true;
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        if (mPayWayList.contains(PayWay.GOOGLEWALLET)) {
//            getActivity().unbindService(mServiceConn);
//        }
        if (mRechargePresenter != null) {
            mRechargePresenter.destroy();
            mRechargePresenter = null;
        }
        mRechargeAdapter = null;
    }


    /**
     * 是否重载状态栏，如果不重载，则用该Fragment所附着的Activity的设置
     *
     * @return
     */
    @Override
    public boolean isOverrideStatusBar() {
        return true;
    }

    @Override
    public boolean onBackPressed() {
        if (mRechargeAdapter != null) {
            mRechargeAdapter.hidePopupWindow();
        }
        if (mIsFirstRecharge && mRechargeAdapter != null && mRechargeAdapter.getStep() > PayConstant.RECHARGE_STEP_FIRST) {
            mRechargeAdapter.setStep(mRechargeAdapter.getStep() - 1);
            mRechargeAdapter.notifyDataSetChanged();
            return true;
        }
        if (getActivity() != null && !isDetached()) {
            if (getActivity() instanceof IPayActivityFlag) {
                getActivity().finish();
            } else {
                FragmentNaviUtils.popFragment(getActivity());
            }
        }
        return true;
    }

    private MyProgressDialogEx mProgressDialog;

    @Override
    public void showProcessDialog(long most) {
        if (getActivity() != null && !isDetached()) {
            if (mProgressDialog == null) {
                //创建ProgressDialog对象
                mProgressDialog = MyProgressDialogEx.createProgressDialog(getActivity());
            }
            mProgressDialog.show(most);
        }

    }

    @Override
    public void hideProcessDialog(long least) {
        if (mProgressDialog != null) {
            mProgressDialog.hide(least);
        }
    }

    @Override
    public void showToast(@NonNull String msg) {
        ToastUtils.showToast(getActivity(), msg);
    }

    @Override
    public String getPackageName0() {
        return getActivity().getPackageName();
    }

    @Override
    public void setBalanceText(int balance, int vBalance) {
        mRechargeAdapter.setBalance(balance, vBalance, true);
    }

    @Override
    public void setRecyclerViewAdapterDataSourceAndNotify(List<Diamond> diamonds) {
        mRechargeAdapter.setRechargeList(diamonds);
    }

    @Override
    public void setRecyclerViewLoadingStatusAndNotify() {
        mRechargeAdapter.setLoadingStatus();
    }

    @Override
    public void updateExchangeableAndWillExpireDiamond(int exchangeableDiamondCnt
            , int willExpireDiamondCnt, int willExpireGiftCardCnt) {
        mRechargeAdapter.updateExchangeableAndWillExpireDiamond(exchangeableDiamondCnt,
                willExpireDiamondCnt, willExpireGiftCardCnt, true);
    }

    @Override
    public boolean isFirstRecharge() {
        return mIsFirstRecharge;
    }

    @Override
    public void showPopupWindow() {
        if (mRechargeAdapter.getStep() == PayConstant.RECHARGE_STEP_SECOND) {
            mRechargeAdapter.clickGridViewItem();
        }
    }

    @Override
    public boolean isNotPullRechargeList() {
        if (mIsFirstRecharge) {
            if (mOnlyGetExchangeableDiamond) {
                mOnlyGetExchangeableDiamond = false;
                return true;
            }
        }
        return false;
    }

    //@Override
    //public boolean existExpandedPayWay() {
    //    return mExistExpandedPayWay;
    //}
    //
    //@Override
    //public void setExpandableListAdapterDataSourceAndNotify(List<Diamond> diamonds) {
    //    mExpandableListAdapter.setPriceList(diamonds);
    //    mExpandableListAdapter.notifyDataSetChanged();
    //}
    //
    //@Override
    //public void setExpandableListLoadingStatusAndNotify() {
    //    mExpandableListAdapter.setLoadingStatus();
    //    mExpandableListAdapter.notifyDataSetChanged();
    //}

//
//    /**
//     * 装饰RecyclerView的Item
//     */
//    @Deprecated
//    static class DividerItemDecoration extends RecyclerView.ItemDecoration {
//        /*
//    * RecyclerView的布局方向，默认先赋值
//    * 为纵向布局
//    * RecyclerView 布局可横向，也可纵向
//    * 横向和纵向对应的分割线画法不一样
//    * */
//        private int mOrientation = LinearLayoutManager.VERTICAL;
//
//        /**
//         * item之间分割线的size，默认为1
//         */
//        private int mItemSize = 1;
//
//        /**
//         * 绘制item分割线的画笔，和设置其属性
//         * 来绘制个性分割线
//         */
//        private Paint mPaint;
//
//        /**
//         * 构造方法传入布局方向，不可不传
//         *
//         * @param context
//         * @param orientation
//         */
//        public DividerItemDecoration(Context context, int orientation) {
//            this.mOrientation = orientation;
//            if (orientation != LinearLayoutManager.VERTICAL && orientation != LinearLayoutManager.HORIZONTAL) {
//                throw new IllegalArgumentException(context.getString(R.string.invalid_param_hint));
//            }
//            // 转成dp
////            mItemSize = (int) TypedValue.applyDimension(mItemSize, TypedValue.COMPLEX_UNIT_DIP, context.getResources().getDisplayMetrics());
//            mPaint = new Paint();
//            mPaint.setColor(context.getResources().getColor(R.color.color_e5e5e5));
//            /*设置填充*/
//        }
//
//        @Override
//        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
//            if (mOrientation == LinearLayoutManager.VERTICAL) {
//                drawVertical(c, parent);
//            } else {
//                drawHorizontal(c, parent);
//            }
//        }
//
//        /**
//         * 绘制纵向 item 分割线
//         *
//         * @param canvas
//         * @param parent
//         */
//        private void drawVertical(Canvas canvas, RecyclerView parent) {
//            final int left = parent.getPaddingLeft();
//            final int right = parent.getMeasuredWidth() - parent.getPaddingRight();
//            final int childSize = parent.getChildCount();
//            // 跳过余额和支付方式部分
//            for (int i = 2; i < childSize; i++) {
//                final View child = parent.getChildAt(i);
//                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
//                final int top = child.getBottom() + layoutParams.bottomMargin;
//                final int bottom = top + mItemSize;
//                canvas.drawRect(left, top, right, bottom, mPaint);
//            }
//        }
//
//        /**
//         * 绘制横向 item 分割线
//         *
//         * @param canvas
//         * @param parent
//         */
//        private void drawHorizontal(Canvas canvas, RecyclerView parent) {
//            final int top = parent.getPaddingTop();
//            final int bottom = parent.getMeasuredHeight() - parent.getPaddingBottom();
//            final int childSize = parent.getChildCount();
//            // 跳过余额和支付方式部分
//            for (int i = 2; i < childSize; i++) {
//                final View child = parent.getChildAt(i);
//                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
//                final int left = child.getRight() + layoutParams.rightMargin;
//                final int right = left + mItemSize;
//                canvas.drawRect(left, top, right, bottom, mPaint);
//            }
//        }
//
//        /**
//         * 设置item分割线的size
//         *
//         * @param outRect
//         * @param view
//         * @param parent
//         * @param state
//         */
//        @Override
//        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
//            if (mOrientation == LinearLayoutManager.VERTICAL) {
//                outRect.set(0, 0, 0, mItemSize);
//            } else {
//                outRect.set(0, 0, mItemSize, 0);
//            }
//        }
//    }

    ///**
    // * 当Group收起时,显示divider;<br/>
    // * 当Group展开时,不显示divider,而是在child上显示childDivider<br/>
    // * 最下面的Group在收起时,不会显示divider,这里的解决办法是在ExpandableListView下增加一条线<br/>
    // * isChildSelectable返回true时,childrenDivider才会生效,否则显示默认颜色#fafafa
    // */
    //@Deprecated
    //class PayWayExpandableListAdapter extends BaseExpandableListAdapter {
    //    private LayoutInflater mLayoutInflater;
    //    private List<Diamond> mPriceList = new ArrayList<>();
    //    private List<PayWay> mPayWayList = RechargeFragment.this.mPayWayList;
    //    private boolean mIsError = false;
    //    private boolean mIsLoading = true;
    //
    //    public void setPriceList(@NonNull List<Diamond> priceList) {
    //        mPriceList.clear();
    //        mPriceList.addAll(priceList);
    //        mIsError = priceList.isEmpty();
    //        mIsLoading = false;
    //    }
    //
    //    public void setLoadingStatus() {
    //        MyLog.d(TAG, "set loading status");
    //        mIsLoading = true;
    //        mIsError = false;
    //    }
    //
    //    public List<PayWay> getPayWayList() {
    //        return mPayWayList;
    //    }
    //
    //    public PayWayExpandableListAdapter(@NonNull Context context) {
    //        mLayoutInflater = LayoutInflater.from(context);
    //    }
    //
    //    @Override
    //    public int getGroupCount() {
    //        return mPayWayList.size();
    //    }
    //
    //    @Override
    //    public int getChildrenCount(int groupPosition) {
    //        int count = 1;// 显示一个正在加载的view
    //        if (!mIsLoading) {
    //            count = mIsError ? 1 : mPriceList.size();
    //        }
    //        MyLog.d(TAG, String.format("get children count:%s, mIsLoading:%s, mIsError:%s", count, mIsLoading, mIsError));
    //        return count;// 如果出错，展示错误页
    //    }
    //
    //    @Override
    //    public Object getGroup(int groupPosition) {
    //        return sPayWayInfoMap.get(mPayWayList.get(groupPosition));
    //    }
    //
    //    @Override
    //    public Object getChild(int groupPosition, int childPosition) {
    //        return mPriceList.get(childPosition);
    //    }
    //
    //    @Override
    //    public long getGroupId(int groupPosition) {
    //        return groupPosition;
    //    }
    //
    //    @Override
    //    public long getChildId(int groupPosition, int childPosition) {
    //        return childPosition;
    //    }
    //
    //    @Override
    //    public boolean hasStableIds() {
    //        return false;
    //    }
    //
    //    @Override
    //    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
    //        ViewGroup payWayItem = (ViewGroup) mLayoutInflater.inflate(R.layout.recharge_pay_way_item_with_arrow, parent, false);
    //        TextView payWayTv = (TextView) payWayItem.findViewById(R.id.pay_way_tv);
    //        PayWayInfo payWayInfo = (PayWayInfo) getGroup(groupPosition);
    //        //MyLog.d(TAG, String.format("groupPossiton:%d, payWayIconId:%d", groupPosition, payWayInfo.mIconId));
    //        Drawable payWayIcon = getResources().getDrawable(payWayInfo.mIconId);
    //        payWayIcon.setBounds(0, 0, payWayIcon.getMinimumWidth(), payWayIcon.getMinimumHeight());
    //        payWayTv.setCompoundDrawables(payWayIcon, null, null, null);// 设置支付方式图标
    //        payWayTv.setText(payWayInfo.mNameId);// 设置支付方式名称
    //        payWayItem.findViewById(R.id.other_pay_way_tip_tv).setVisibility(View.GONE);// 隐藏“更多支付方式”
    //        ImageView arrowIcon = (ImageView) payWayItem.findViewById(R.id.arrow);
    //        // 更换箭头图标
    //        if (isExpanded) {
    //            arrowIcon.setImageDrawable(getResources().getDrawable(R.drawable.recharge_expanded_icon));
    //        } else {
    //            arrowIcon.setImageDrawable(getResources().getDrawable(R.drawable.recharge_collapsed_icon));
    //        }
    //        // TODO 显示分隔线
    //        //View line = payWayItem.findViewById(R.id.line);
    //        //if ((isExpanded && (mIsLoading || mIsError)) || (!isExpanded && groupPosition + 1 == mPayWayList.size())) {
    //        //    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) line.getLayoutParams();
    //        //    lp.height = 1;
    //        //    line.setLayoutParams(lp);
    //        //}
    //        //line.setVisibility(View.VISIBLE);
    //
    //        return payWayItem;
    //    }
    //
    //    @Override
    //    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
    //        //MyLog.d(TAG, "get children view, mIsLoading:" + mIsLoading + ", mIsError:" + mIsError + ", mPriceList.size()=" + mPriceList.size());
    //        if (mIsLoading) {
    //            // 载入中的图片， children count设为1
    //            //View loadingView = mLayoutInflater.inflate(R.layout.empty_view, parent, false);
    //            //((TextView) loadingView.findViewById(R.id.empty_tv)).setText(R.string.default_loading_hint);
    //            //return loadingView;
    //
    //            View view = getTipView(parent, true);
    //            return view;
    //        } else if (mIsError) {
    //            View view = getTipView(parent, false);
    //            ErrorViewHolder errorViewHolder = new ErrorViewHolder(view);
    //            RxView.clicks(errorViewHolder.mErrorView.getRetryTv())
    //                    .throttleFirst(RECHARGE_INTERVAL, TimeUnit.SECONDS)
    //                    .subscribe(aVoid -> mRechargePresenter.pullPriceListAsync());
    //            return view;
    //        } else {
    //            ViewGroup childItem = (ViewGroup) mLayoutInflater.inflate(R.layout.diamond_price_item, parent, false);
    //            // 改变背景颜色
    //            childItem.setBackground(getResources().getDrawable(R.drawable.recharge_first_recharge_diamond_list_item_bg));
    //            // TODO 显示分隔线
    //            //childItem.findViewById(R.id.line).setVisibility(View.VISIBLE);
    //
    //            bindRechargeListItem(new DiamondViewHolder(childItem), (Diamond) getChild(groupPosition, childPosition), mPayWayList.get(groupPosition));
    //            return childItem;
    //        }
    //    }
    //
    //    @NonNull
    //    private View getTipView(ViewGroup parent, boolean isLoading) {
    //        View tipView = mLayoutInflater.inflate(R.layout.recharge_error_view_section, parent, false);
    //        ViewGroup errorView = (ViewGroup) tipView.findViewById(R.id.price_list_error_view);
    //        errorView.findViewById(R.id.error_tips_tv).setVisibility(View.GONE);
    //
    //        View retryView = errorView.findViewById(R.id.error_retry);
    //        int padding = DisplayUtils.dip2px(getActivity(), 10);
    //        retryView.setPadding(padding, padding, padding, DisplayUtils.dip2px(getActivity(), 25.33f));
    //        if (isLoading) {
    //            ((TextView) retryView).setText(R.string.default_loading_hint);
    //        }
    //        return tipView;
    //    }
    //
    //    @Override
    //    public boolean isChildSelectable(int groupPosition, int childPosition) {
    //        return true;// 设为true时childDivider才会起作用
    //    }
    //}


    //@Deprecated
    //private void bindRechargeListItem(RecyclerView.ViewHolder holder, @NonNull Diamond data, @NonNull PayWay payWay) {
    //    if (holder instanceof DiamondViewHolder) {
    //        DiamondViewHolder diamondViewHolder = (DiamondViewHolder) holder;
    //
    //        if (!isServerDiamondInfoCanDirectlyUse(getRechargeListType(payWay))) {// 国际化的支付方式的价格单位不是服务器下发的，需要自己组装在SkuDetail里
    //            SkuDetail skuDetail = data.getSkuDetail();
    //            if (skuDetail != null) {
    //                diamondViewHolder.mPriceTv.setText(skuDetail.getPrice()); // 这里暂时先显示为$6的形式
    //            } else {
    //                MyLog.e(TAG, "skuDetail is null, payWay:" + payWay);
    //            }
    //        } else {
    //            diamondViewHolder.mPriceTv.setText(data.getPrice() / 100.0 + getString(R.string.account_rmb));
    //        }
    //        diamondViewHolder.mDiamondNumTv.setText(String.valueOf(data.getCount()));
    //        //额外赠送
    //        int extra = data.getExtraGive();
    //        if (extra > 0) {
    //            diamondViewHolder.mExtraDiamondNumTv.setText(getString(R.string.with_extra_diamond, extra));
    //        } else {
    //            diamondViewHolder.mExtraDiamondNumTv.setText("");
    //        }
    //        // 子标题信息
    //        String subtitle = data.getSubTitle();
    //        if (!TextUtils.isEmpty(subtitle)) {
    //            diamondViewHolder.mDiamondSubTitle.setText(subtitle);
    //        } else {
    //            diamondViewHolder.mDiamondSubTitle.setText("");
    //        }
    //        // 是否有角标
    //        String iconUrl = data.getIconUrl();
    //        if (!TextUtils.isEmpty(iconUrl)) {
    //            diamondViewHolder.mIconIv.setVisibility(View.VISIBLE);
    //            FrescoWorker.loadImage(diamondViewHolder.mIconIv, new HttpImage(iconUrl));
    //        } else {
    //            diamondViewHolder.mIconIv.setVisibility(View.GONE);
    //        }
    //        RxView.clicks(diamondViewHolder.itemView)
    //                .throttleFirst(RECHARGE_INTERVAL, TimeUnit.SECONDS)
    //                .subscribe(aVoid -> mRechargePresenter.recharge(data, sPayWay));
    //    }
    //}

}
