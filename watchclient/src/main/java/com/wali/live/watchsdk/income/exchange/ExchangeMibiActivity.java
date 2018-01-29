package com.wali.live.watchsdk.income.exchange;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.event.EventClass;
import com.wali.live.income.Exchange;
import com.wali.live.income.model.ExceptionWithCode;
import com.wali.live.proto.MibiTicketProto;
import com.wali.live.proto.MibiTicketProto.ExchangeMibiResponse;
import com.wali.live.proto.MibiTicketProto.GetMibiExchangeListResponse;
import com.wali.live.proto.MibiTicketProto.MibiExchange;
import com.wali.live.task.ITaskCallBack;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.income.auth.MiAccountToken;
import com.wali.live.watchsdk.income.auth.MiAccountTokenManager;
import com.wali.live.watchsdk.income.net.ExchangeMibiRequest;
import com.wali.live.watchsdk.income.net.GetMibiExchangeListRequest;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 兑换米币
 *
 * @module 收益
 * Created by rongzhisheng on 16-12-15.
 */

public class ExchangeMibiActivity extends com.wali.live.income.exchange.BaseExchangeActivity {
    /**
     * 可用米币星票，主线程更新
     */
    private int mMibiTicketNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //checkMiAccessToken();
    }

    @MainThread
    private void checkMiAccessToken() {
        if (TextUtils.isEmpty(MiAccountToken.getTokens().first)) {
            // 获取授权
            requestForAccessToken(true);
        } else {
            // TODO 验证授权是否还有效
            //showProcessDialog(5000, R.string.loading);
        }
    }

    @Override
    protected int getTitleBarTitle() {
        return R.string.income_ticket_tomibi;
    }

    @Override
    protected int getTicketCountBalance() {
        return mMibiTicketNum;
    }

    @Override
    protected int getGameTicketCountBalance() {
        return 0;
    }

    @Override
    protected void getExchangeListFromServer(final boolean callByEvent) {
        if (!callByEvent) {
            showProcessDialog(PROGRESS_SHOW_TIME_MOST, R.string.loading);
        }
        Observable.create(new Observable.OnSubscribe<GetMibiExchangeListResponse>() {
            @Override
            public void call(Subscriber<? super GetMibiExchangeListResponse> subscriber) {
                GetMibiExchangeListRequest request = new GetMibiExchangeListRequest();
                subscriber.onNext((GetMibiExchangeListResponse) request.syncRsp());
                subscriber.onCompleted();
            }
        })
                .flatMap(new Func1<GetMibiExchangeListResponse, Observable<GetMibiExchangeListResponse>>() {
                    @Override
                    public Observable<GetMibiExchangeListResponse> call(GetMibiExchangeListResponse rsp) {
                        if (rsp == null) {
                            return Observable.error(new Throwable("GetMibiExchangeListResponse is null"));
                        }
                        if (rsp.getRetCode() != ErrorCode.CODE_SUCCESS) {
                            return Observable.error(new Throwable("rsp.code:" + rsp.getRetCode()));
                        }
                        return Observable.just(rsp);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<GetMibiExchangeListResponse>bindToLifecycle())
                .subscribe(new Subscriber<GetMibiExchangeListResponse>() {
                    @Override
                    public void onCompleted() {
                        if (!callByEvent) {
                            hideProcessDialog(PROGRESS_SHOW_TIME_LEAST);
                        }
                        MyLog.w(getTAG(), "get mibi exchange list ok");
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!callByEvent) {
                            hideProcessDialog(PROGRESS_SHOW_TIME_LEAST);
                        }
                        MyLog.e(getTAG(), "get mibi exchange list fail", e);
                    }

                    @Override
                    public void onNext(GetMibiExchangeListResponse rsp) {
                        mMibiTicketNum = rsp.getUsableMibiTicketCnt();
                        updateBalanceView();
                        parseMibiExchangeList(rsp.getMibiExchangeListList());
                    }
                });
    }

    @MainThread
    private void parseMibiExchangeList(List<MibiExchange> mibiExchangeListList) {
        if (mibiExchangeListList == null || mibiExchangeListList.isEmpty()) {
            return;
        }
        List<Exchange> exchangeList = new ArrayList<>();
        for (MibiExchange mibiExchange : mibiExchangeListList) {
            exchangeList.add(new com.wali.live.watchsdk.income.MibiExchange(mibiExchange));
        }
        updateExchangeList(exchangeList);
    }

    @Override
    protected int getItemLayout() {
        return R.layout.exchange_mibi_item;
    }

    @Override
    public int getExchangeTargetName() {
        return R.plurals.mibi;
    }

    @Override
    public int getTicketViewText() {
        return R.string.count_mibi_ticket;
    }

    @Override
    public int getErrorTipText() {
        return R.string.income_mibi_not_enough;
    }

    @Override
    public int getExchangeTipId() {
        return R.string.income_isok_to_mibi;
    }

    @Override
    public void exchange(final SoftReference<ITaskCallBack> callBack, final @NonNull Exchange data) {
        //兑换米币星票
        if (TextUtils.isEmpty(MiAccountToken.getTokens().first)) {
            // 获取授权
            requestForAccessToken(false);
            // 恢复Item的enable状态
            callBack.get().processWithFailure(ErrorCode.CODE_SUCCESS);
            return;
        }
        showProcessDialog(PROGRESS_SHOW_TIME_MOST, R.string.exchange_in_progress_tip);
        Observable.create(new Observable.OnSubscribe<ExchangeMibiResponse>() {
            @Override
            public void call(Subscriber<? super ExchangeMibiResponse> subscriber) {
                ExchangeMibiRequest request = new ExchangeMibiRequest(data.getId(),
                        data.getDiamondNum(), data.getTicketNum(), data.getExtraDiamondNum(),
                        MiAccountToken.getTokens().first);
                subscriber.onNext((ExchangeMibiResponse) request.syncRsp());
                subscriber.onCompleted();
            }
        })
                .flatMap(new Func1<ExchangeMibiResponse, Observable<ExchangeMibiResponse>>() {
                    @Override
                    public Observable<ExchangeMibiResponse> call(ExchangeMibiResponse rsp) {
                        if (rsp == null) {
                            return Observable.error(new ExceptionWithCode("ExchangeMibiResponse is null", ErrorCode.CODE_ERROR_NORMAL));
                        }
                        if (rsp.getRetCode() != ErrorCode.CODE_SUCCESS) {
                            return Observable.error(new ExceptionWithCode(rsp.getRetCode()));
                        }
                        return Observable.just(rsp);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<ExchangeMibiResponse>bindToLifecycle())
                .subscribe(new Subscriber<ExchangeMibiResponse>() {
                    @Override
                    public void onCompleted() {
                        MyLog.w(getTAG(), "exchange mibi ok");
                        EventBus.getDefault().post(new EventClass.WithdrawEvent(EventClass.WithdrawEvent.EVENT_TYPE_ACCOUNT_TICKET_CHANGE));
                        hideProcessDialog(PROGRESS_SHOW_TIME_LEAST);
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(getTAG(), "exchange mibi fail", e);
                        if (e instanceof ExceptionWithCode) {
                            ITaskCallBack iTaskCallBack;
                            if (callBack != null && (iTaskCallBack = callBack.get()) != null) {
                                iTaskCallBack.processWithFailure(((ExceptionWithCode) e).getCode());
                            }
                            handleExchangeMibiFail(((ExceptionWithCode) e).getCode());
                        } else {
                            hideProcessDialog(PROGRESS_SHOW_TIME_LEAST);
                        }
                    }

                    @Override
                    public void onNext(ExchangeMibiResponse rsp) {
                        mMibiTicketNum = rsp.getUsableMibiTicketCnt();
                        updateBalanceView();
                        ITaskCallBack iTaskCallBack;
                        if (callBack != null && (iTaskCallBack = callBack.get()) != null) {
                            iTaskCallBack.process(null);
                        }
                    }
                });
    }

    @MainThread
    private void handleExchangeMibiFail(int code) {
        switch (code) {
            case ErrorCode.MiAccount.MI_ACCOUNT_NEED_RELOGIN:
            case ErrorCode.MiAccount.THRIFT_VERIFY_AT_ERROR:
            case ErrorCode.MiAccount.MI_REFRESH_TOKEN_EXPIRED:
            case ErrorCode.MiAccount.UUID_NOT_MATCHED:
                hideProcessDialog(PROGRESS_SHOW_TIME_LEAST);
                requestForAccessToken(false);
                break;
            case ErrorCode.MiAccount.MI_ACCESS_TOKEN_EXPIRED:
                refreshAccessToken();
                break;
            default:
                hideProcessDialog(PROGRESS_SHOW_TIME_LEAST);
                break;
        }
    }

    private void refreshAccessToken() {
        MiAccountTokenManager.refreshAccessToken()
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<MibiTicketProto.OpenAccountInfo>bindToLifecycle())
                .subscribe(new Subscriber<MibiTicketProto.OpenAccountInfo>() {
                    @Override
                    public void onCompleted() {
                        hideProcessDialog(PROGRESS_SHOW_TIME_LEAST);
                        // 其实是懒得自动重试，网络又一次背锅
                        ToastUtils.showToast(R.string.net_is_busy_tip);
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProcessDialog(PROGRESS_SHOW_TIME_LEAST);
                        // 用refreshToken换取新accessToken失败
                        MiAccountToken.clear();
                        ToastUtils.showToast(R.string.mi_account_auth_invalid_tip);
                        requestForAccessToken(false);
                    }

                    @Override
                    public void onNext(MibiTicketProto.OpenAccountInfo openAccountInfo) {
                        if (!TextUtils.isEmpty(openAccountInfo.getAccessToken()) && !TextUtils.isEmpty(openAccountInfo.getRefreshToken())) {
                            MiAccountToken.setTokens(openAccountInfo.getAccessToken(), openAccountInfo.getRefreshToken());
                        } else {
                            onError(new Exception("exist empty token"));
                        }
                    }
                });
    }

    @MainThread
    private void requestForAccessToken(final boolean finishOnFailed) {
        MiAccountTokenManager.jumpToAuthorizedPage(this)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<MibiTicketProto.OpenAccountInfo>bindToLifecycle())
                .subscribe(new Subscriber<MibiTicketProto.OpenAccountInfo>() {
                    @Override
                    public void onCompleted() {
                        MyLog.w(getTAG(), "get accessToken ok");
                        ToastUtils.showToast(R.string.get_mi_account_auth_ok_tip);
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(getTAG(), "get accessToken fail", e);
                        ToastUtils.showToast(R.string.get_mi_account_auth_fail_tip);
                        if (finishOnFailed) {
                            finish();
                        }
                    }

                    @Override
                    public void onNext(MibiTicketProto.OpenAccountInfo openAccountInfo) {
                        if (!TextUtils.isEmpty(openAccountInfo.getAccessToken()) && !TextUtils.isEmpty(openAccountInfo.getRefreshToken())) {
                            MiAccountToken.setTokens(openAccountInfo.getAccessToken(), openAccountInfo.getRefreshToken());
                        } else {
                            onError(new Exception("exist empty token"));
                        }
                    }
                });
    }


    public static void openActivity(@NonNull Activity activity) {
        Intent intent = new Intent(activity, ExchangeMibiActivity.class);
        activity.startActivity(intent);
    }
}
