package com.wali.live.watchsdk.income.exchange;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.event.EventClass;
import com.wali.live.income.Exchange;
import com.wali.live.income.WithdrawManager;
import com.wali.live.income.WithdrawTask;
import com.wali.live.income.exchange.BaseExchangeActivity;
import com.wali.live.income.model.ExceptionWithCode;
import com.wali.live.proto.MibiTicketProto;
import com.wali.live.proto.PayProto.ExchangeGemResponse;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.task.ITaskCallBack;
import com.wali.live.task.TaskCallBackWrapper;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.webview.WebViewActivity;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.SoftReference;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 兑换钻石
 *
 * @module 收益
 * Created by rongzhisheng on 16-12-15.
 */

public class ExchangeGemActivity extends BaseExchangeActivity {
    private static final String EXTRA_STAT_FROM = "from";
    /**
     * 可用普通星票余额，主线程更新
     */
    private int mUsableTicketCount = 0;
    private int mGameTicketCount = 0;

    public static final String GEM_EXCHANGE_H5_URL = "http://weixin.zb.mi.com/wechat-cgi/exchange/h5page";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent;
        if ((intent = getIntent()) != null) {
            mStatFromWhere = intent.getIntExtra(EXTRA_STAT_FROM,
                    StatisticsKey.ExchangeGem.FROM_OTHER);
            if (mStatFromWhere < StatisticsKey.ExchangeGem.FROM_OTHER) {
                mStatFromWhere = StatisticsKey.ExchangeGem.FROM_OTHER;
            }
        }
        // TODO: 18-1-20 有注释
//        MiStatAdapter.recordCalculateEvent(null,
//                String.format(StatisticsKey.ExchangeGem.VISIT, mStatFromWhere), 1);
    }

    @Override
    protected int getTitleBarTitle() {
        return R.string.exchange;
    }

    @Override
    protected int getTicketCountBalance() {
        return mUsableTicketCount;
    }

    @Override
    protected int getGameTicketCountBalance() {
        return mGameTicketCount;
    }

    @Override
    protected void getExchangeListFromServer(final boolean callByEvent) {
        if (!callByEvent) {
            showProcessDialog(PROGRESS_SHOW_TIME_MOST, R.string.loading);
        }
        if (mCallBack == null) {
            mCallBack = new SoftReference<ITaskCallBack>(new TaskCallBackWrapper() {
                @Override
                public void processWithMore(Object... objects) {
                    if (!callByEvent) {
                        hideProcessDialog(PROGRESS_SHOW_TIME_LEAST);
                    }
                    if (!isFinishing()) {
                        List<Exchange> exchangeList = (List<Exchange>) objects[0];
                        mUsableTicketCount = (int) objects[1];
                        mGameTicketCount = (int) objects[2];
                        updateBalanceView();
                        int type = (int) objects[3];
                        switch (type) {
                            case TYPE_SHOW:
                                updateExchangeList(exchangeList);
                                break;
                            case TYPE_GAME:
                                updateGameExchangeList(exchangeList);
                                break;
                        }

                    }
                }

                @Override
                public void processWithFailure(int errCode) {
                    if (!callByEvent) {
                        hideProcessDialog(PROGRESS_SHOW_TIME_LEAST);
                    }
                    if (!isFinishing()) {
                        ToastUtils.showToast(R.string.no_net);
                        finish();
                    }
                }
            });
        }
        WithdrawTask.getExchangeList(mCallBack);
        WithdrawTask.getGameExchangeList(mCallBack);
    }

    SoftReference<ITaskCallBack> mCallBack;


    @Override
    protected int getItemLayout() {
        return R.layout.exchange_diamond_item;
    }

    @Override
    public int getExchangeTargetName() {
        return R.plurals.gold_diamond;
    }

    @Override
    public int getTicketViewText() {
        return R.string.exchange_ticket;
    }

    @Override
    public int getErrorTipText() {
        return R.string.exchange_failure_dialog_message;
    }

    @Override
    public int getExchangeTipId() {
        if (mType == TYPE_GAME && mShowTip == 1) {
            return R.string.exchange_diamond_silver_dialog;
        }
        return R.string.exchange_diamond_dialog_title;
    }

    @Override
    public void exchange(final SoftReference<ITaskCallBack> callBack, final @NonNull Exchange data) {
        //WithdrawTask.exchangeDiamond(callBack, data.getId(), data.getDiamondNum(), data
        // .getTicketNum(), data.getExtraDiamondNum());
        showProcessDialog(PROGRESS_SHOW_TIME_MOST, R.string.exchange_in_progress_tip);
        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                Object rsp = null;
                switch (mType) {
                    case TYPE_SHOW:
                        rsp = WithdrawManager.exchangeDiamondSync(data.getId(),
                                data.getDiamondNum(), data.getTicketNum(),
                                data.getExtraDiamondNum());
                        break;
                    case TYPE_GAME:
                        rsp = WithdrawManager.exchangeGameDiamondSync(data.getId(),
                                data.getDiamondNum(), data.getTicketNum(),
                                data.getExtraDiamondNum());
                        break;
                }
                subscriber.onNext(rsp);
                subscriber.onCompleted();
            }
        })
                .flatMap(new Func1<Object, Observable<Object>>() {
                    @Override
                    public Observable<Object> call(Object rsp) {
                        if (rsp == null) {
                            return Observable.error(
                                    new ExceptionWithCode("ExchangeGemResponse is null",
                                            ErrorCode.CODE_ERROR_NORMAL));
                        }
                        switch (mType) {
                            case TYPE_SHOW:
                                ExchangeGemResponse rsp0 = (ExchangeGemResponse) rsp;
                                if (rsp0.getRetCode() != ErrorCode.CODE_SUCCESS) {
                                    return Observable.error(
                                            new ExceptionWithCode(rsp0.getRetCode()));
                                }
                                break;
                            case TYPE_GAME:
                                MibiTicketProto.GameTicketExchangeGemResponse rsp1 =
                                        (MibiTicketProto.GameTicketExchangeGemResponse) rsp;
                                if (rsp1.getRetCode() != ErrorCode.CODE_SUCCESS) {
                                    return Observable.error(
                                            new ExceptionWithCode(rsp1.getRetCode()));
                                }
                                break;
                        }
                        return Observable.just(rsp);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindUntilEvent())
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {
                        EventBus.getDefault().post(new EventClass.WithdrawEvent(
                                EventClass.WithdrawEvent.EVENT_TYPE_ACCOUNT_TICKET_CHANGE));
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProcessDialog(PROGRESS_SHOW_TIME_LEAST);
                        MyLog.e(getTAG(), "exchange gem fail", e);
                        ITaskCallBack iTaskCallBack;
                        if (callBack != null && (iTaskCallBack = callBack.get()) != null
                                && e instanceof ExceptionWithCode) {
                            iTaskCallBack.processWithFailure(((ExceptionWithCode) e).getCode());
                        }
                    }

                    @Override
                    public void onNext(Object r) {
                        switch (mType) {
                            case TYPE_SHOW:
                                ExchangeGemResponse rsp0 = (ExchangeGemResponse) r;
                                MyUserInfoManager.getInstance().setDiamondNum(
                                        rsp0.getUsableGemCnt());
                                mUsableTicketCount = rsp0.getUsableTicketCnt();
                                break;
                            case TYPE_GAME:
                                MibiTicketProto.GameTicketExchangeGemResponse rsp1 =
                                        (MibiTicketProto.GameTicketExchangeGemResponse) r;
                                MyUserInfoManager.getInstance().setVirtualDiamondNum(
                                        rsp1.getUsableGemCnt());
                                mGameTicketCount = rsp1.getUsableTicketCnt();
                                break;
                        }
                        hideProcessDialog(PROGRESS_SHOW_TIME_LEAST);
                        updateBalanceView();
                        ITaskCallBack iTaskCallBack;
                        if (callBack != null && (iTaskCallBack = callBack.get()) != null) {
                            iTaskCallBack.process(null);
                        }
                    }
                });
    }

    public static void openActivity(@NonNull Activity activity, int from) {
        Intent intent = new Intent(activity, ExchangeGemActivity.class);
        intent.putExtra(EXTRA_STAT_FROM, from);
        activity.startActivity(intent);
    }

    /**
     * 跳转到钻石兑换页面 本地或h5
     */
    public static void handleJumpToExchangePage(Activity activity, int from) {
//        int flag = PreferenceUtils.getSettingInt(activity,
//                PreferenceKeys.PRE_KEY_EXCHANGE_USE_H5_FLAG, 0);
//        String url = PreferenceUtils.getSettingString(activity,
//                PreferenceKeys.PRE_KEY_EXCHANGE_H5_URL, "");
//        if (flag == 1 && !TextUtils.isEmpty(url)) {
//            SchemeActivity.openActivity(activity, Uri.parse(url));
        //产品要求直接跳转到 webview activity
        WebViewActivity.open(activity,GEM_EXCHANGE_H5_URL);
//            return;
//        }
//        openActivity(activity, from);
    }


}