package com.wali.live.pay.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.base.activity.BaseActivity;
import com.base.dialog.MyProgressDialogEx;
import com.base.log.MyLog;
import com.live.module.common.R;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.pay.fragment.BalanceFragment;
import com.wali.live.pay.manager.PayManager;
import com.wali.live.pay.model.BalanceDetail;
import com.wali.live.proto.PayProto;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.mi.live.data.repository.GiftRepository.getGiftListCache;

/**
 * 专门用于schema跳转的余额界面<br/>
 *
 * @author caoxiangyu
 * @module 充值
 */
public class BalanceActivity extends BaseActivity {
    public static String TAG = BalanceActivity.class.getSimpleName();

    public static void openActivity(Activity from) {
        if (from != null) {
            from.startActivity(new Intent(from, BalanceActivity.class));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recharge_activity_layout);
        Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                getGiftListCache();
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).subscribe();
        getBalance();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }

    @Override
    public String getTAG() {
        return TAG;
    }

   @Override
    public void onBackPressed() {
        finish();
    }
    private Subscription mGetBalanceDetailSub;

    public void getBalance() {
        if (mGetBalanceDetailSub != null && !mGetBalanceDetailSub.isUnsubscribed()) {
            mGetBalanceDetailSub.unsubscribe();
        }
        mGetBalanceDetailSub = Observable.just(0)
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        showProcessDialog(5000);
                    }
                })
                .observeOn(Schedulers.io())
                .flatMap(new Func1<Integer, Observable<PayProto.QueryBalanceDetailResponse>>() {
                    @Override
                    public Observable<PayProto.QueryBalanceDetailResponse> call(Integer integer) {
                        return  PayManager.getBalanceDetailRsp();
                    }
                })
                .flatMap(new Func1<PayProto.QueryBalanceDetailResponse, Observable<?>>() {
                    @Override
                    public Observable<?> call(PayProto.QueryBalanceDetailResponse rsp) {
                        if (rsp == null) {
                            return Observable.error( new Exception("QueryBalanceDetailResponse is null"));
                        } else if (rsp.getRetCode() != 0) {
                            return Observable.error( new Exception("QueryBalanceDetailResponse.retCode:" + rsp.getRetCode()));
                        }
                        return Observable.just(BalanceDetail.parseFrom(rsp));
                    }
                })
                .compose(bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(BalanceFragment.BUNDLE_KEY_BALANCE_DETAIL, (BalanceDetail)o);
                        BalanceFragment.openFragment(BalanceActivity.this, bundle, null);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable.getMessage());
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        MyLog.d(TAG, "get QueryBalanceDetailResponse success");
                        hideProcessDialog(1000);
                    }
                });
    }

    private MyProgressDialogEx mProgressDialog;

    private void showProcessDialog(long most) {
        if (!isFinishing()) {
            if (mProgressDialog == null) {
                //创建ProgressDialog对象
                mProgressDialog = MyProgressDialogEx.createProgressDialog(this);
            }
            mProgressDialog.show(most);
        }

    }

    private void hideProcessDialog(long least) {
        if (mProgressDialog != null) {
            mProgressDialog.hide(least);
        }
    }

}
