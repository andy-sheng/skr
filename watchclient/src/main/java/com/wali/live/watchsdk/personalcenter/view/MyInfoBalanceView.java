package com.wali.live.watchsdk.personalcenter.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.activity.RxActivity;
import com.base.log.MyLog;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.event.UserInfoEvent;
import com.mi.live.data.event.GiftEventClass;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.event.EventClass;
import com.wali.live.pay.fragment.BalanceFragment;
import com.wali.live.pay.manager.PayManager;
import com.wali.live.pay.model.BalanceDetail;
import com.wali.live.proto.PayProto;
import com.wali.live.recharge.net.GetGemPriceRequest;
import com.wali.live.watchsdk.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MyInfoBalanceView extends RelativeLayout {
    public final static String TAG = "MyInfoBalanceView";

    TextView mTotalBalanceTv;
    TextView mDescTv;
    View mSpiltLine1;
    LinearLayout mMiddleContainer;
    RelativeLayout mGoldBalanceContainer;
    ImageView mGoldBalanceIv;
    TextView mGoldBalanceTv;
    TextView mGoldBalanceDescTv;
    RelativeLayout mSilverBalanceContainer;
    ImageView mSilverBalanceIv;
    TextView mSilverBalanceTv;
    TextView mSilverBalanceDescTv;
    View mSplitLine2;
    RelativeLayout mExchangeContainer;
    TextView mExchangeTipsTv;
    TextView mExchangeBtn;
    TextView mGoRechangeBtn;

    public MyInfoBalanceView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.my_info_half_balance_layout, this);
        mTotalBalanceTv = (TextView) this.findViewById(R.id.total_balance_tv);
        mDescTv = (TextView) this.findViewById(R.id.desc_tv);
        mSpiltLine1 = (View) this.findViewById(R.id.spilt_line_1);
        mMiddleContainer = (LinearLayout) this.findViewById(R.id.middle_container);
        mGoldBalanceContainer = (RelativeLayout) this.findViewById(R.id.gold_balance_container);
        mGoldBalanceIv = (ImageView) this.findViewById(R.id.gold_balance_iv);
        mGoldBalanceTv = (TextView) this.findViewById(R.id.gold_balance_tv);
        mGoldBalanceDescTv = (TextView) this.findViewById(R.id.gold_balance_desc_tv);
        mSilverBalanceContainer = (RelativeLayout) this.findViewById(R.id.silver_balance_container);
        mSilverBalanceIv = (ImageView) this.findViewById(R.id.silver_balance_iv);
        mSilverBalanceTv = (TextView) this.findViewById(R.id.silver_balance_tv);
        mSilverBalanceDescTv = (TextView) this.findViewById(R.id.silver_balance_desc_tv);
        mSplitLine2 = (View) this.findViewById(R.id.split_line_2);
        mExchangeContainer = (RelativeLayout) this.findViewById(R.id.exchange_container);
        mExchangeTipsTv = (TextView) this.findViewById(R.id.exchange_tips_tv);
        mExchangeBtn = (TextView) this.findViewById(R.id.exchange_btn);
        mGoRechangeBtn = (TextView) this.findViewById(R.id.go_rechange_btn);

        mGoRechangeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new GiftEventClass.GiftMallEvent(GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_GO_RECHARGE));
            }
        });

        mGoldBalanceContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                goBalanceFragment();
            }
        });

        mSilverBalanceContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                goBalanceFragment();
            }
        });

        mExchangeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new EventClass.H5ExchangeEvent());
            }
        });
        bindData();
    }

    private void bindData() {
        int t = MyUserInfoManager.getInstance().getDiamondNum() + MyUserInfoManager.getInstance().getVirtualDiamondNum();
        mTotalBalanceTv.setText("" + t);
        mGoldBalanceTv.setText("" + MyUserInfoManager.getInstance().getDiamondNum());
        mSilverBalanceTv.setText("" + MyUserInfoManager.getInstance().getVirtualDiamondNum());

        Observable.create(new Observable.OnSubscribe<PayProto.GetGemPriceResponse>() {
            @Override
            public void call(Subscriber<? super PayProto.GetGemPriceResponse> subscriber) {
                PayProto.GetGemPriceResponse rsp = new GetGemPriceRequest(PayProto.RChannel.AND_CH).syncRsp();
                subscriber.onNext(rsp);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .compose(getRxActivity().<PayProto.GetGemPriceResponse>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<PayProto.GetGemPriceResponse>() {
                    @Override
                    public void call(PayProto.GetGemPriceResponse getGemPriceResponse) {
                        if (getGemPriceResponse.getExchangeableGemCnt() > 0) {
                            mExchangeContainer.setVisibility(VISIBLE);
                            mExchangeTipsTv.setText(String.format("你当前可以兑换%s钻石", getGemPriceResponse.getExchangeableGemCnt()));
                        } else {
                            mExchangeContainer.setVisibility(GONE);
                        }
                    }
                });
    }

    private void goBalanceFragment() {

        PayManager.getBalanceDetailRsp()
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<PayProto.QueryBalanceDetailResponse, Observable<BalanceDetail>>() {
                    @Override
                    public Observable<BalanceDetail> call(PayProto.QueryBalanceDetailResponse rsp) {
                        if (rsp == null) {
                            return Observable.error(new Exception("QueryBalanceDetailResponse is null"));
                        } else if (rsp.getRetCode() != 0) {
                            return Observable.error(new Exception("QueryBalanceDetailResponse.retCode:" + rsp.getRetCode()));
                        }
                        return Observable.just(BalanceDetail.parseFrom(rsp));
                    }
                })
                .compose(getRxActivity().<BalanceDetail>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        BalanceDetail balanceDetail = (BalanceDetail) o;
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(BalanceFragment.BUNDLE_KEY_BALANCE_DETAIL, balanceDetail);
                        BalanceFragment.openFragment((BaseActivity) getRxActivity(), bundle, null);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable.getMessage());
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        MyLog.w(TAG, "get QueryBalanceDetailResponse success");
                    }
                });

    }

    private RxActivity getRxActivity() {
        return (RxActivity) getContext();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UserInfoEvent userInfoEvent) {
        bindData();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }
}