package com.wali.live.watchsdk.envelope.presenter;

import android.text.TextUtils;

import com.base.activity.RxActivity;
import com.base.log.MyLog;
import com.base.presenter.RxLifeCyclePresenter;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.event.UserInfoEvent;
import com.mi.live.data.api.ErrorCode;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.proto.RedEnvelProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.utils.EnvelopeUtils;
import com.wali.live.watchsdk.envelope.SendEnvelopeFragment;

import org.greenrobot.eventbus.EventBus;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by wangmengjie on 17-7-20.
 *
 * @module 发红包表现
 */

public class SendEnvelopePresenter extends RxLifeCyclePresenter implements SendEnvelopeFragment.IPresenter {
    private static final String TAG = "SendEnvelopePresenter";

    private SendEnvelopeFragment.ISendEnvelopeView mView;

    @Override
    public void setSendEnvelopeView(SendEnvelopeFragment.ISendEnvelopeView view) {
        mView = view;
    }

    private void syncBalance() {
        MyUserInfoManager.getInstance().syncSelfDetailInfo();
    }

    @Override
    public void sendEnvelope(final long anchorId, final String roomId,
                             final int viewerCnt, final int gemCnt, final String msg) {
        if (TextUtils.isEmpty(roomId) || gemCnt <= 0) {
            MyLog.w(TAG, "sendEnvelope, but EnvelopeModel is null");
            return;
        }
        Observable.just(0)
                .map(new Func1<Integer, RedEnvelProto.CreateRedEnvelopRsp>() {
                    @Override
                    public RedEnvelProto.CreateRedEnvelopRsp call(Integer i) {
                        return EnvelopeUtils.createRedEnvelope(anchorId, roomId, viewerCnt, gemCnt, msg);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<RedEnvelProto.CreateRedEnvelopRsp>bindUntilEvent(RxLifeCyclePresenter.PresenterEvent.DESTROY))
                .subscribe(new Action1<RedEnvelProto.CreateRedEnvelopRsp>() {
                    @Override
                    public void call(RedEnvelProto.CreateRedEnvelopRsp rsp) {
                        if (mView == null){
                            return;
                        }
                        if (rsp != null && rsp.getRetCode() == ErrorCode.CODE_SUCCESS){
                            MyLog.w(TAG,"sendEnvelope done");
                            mView.onSendSuccess();
                            syncBalance();
                        } else {
                            MyLog.w(TAG,"sendEnvelope failed");
                            ToastUtils.showToast(R.string.create_red_envelop_failed_error);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.w(TAG, "sendEnvelope failed, exception=" + throwable);
                    }
                });
    }

}
