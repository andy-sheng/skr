package com.wali.live.watchsdk.contest.presenter;

import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.LiveSummitProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.contest.cache.ContestCurrentCache;
import com.wali.live.watchsdk.contest.cache.ContestGlobalCache;
import com.wali.live.watchsdk.contest.request.CommitContestAnswerRequest;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by liuyanyan on 2018/1/12.
 */
public class CommitContestAnswerPresenter extends BaseRxPresenter<IContestCommitAnswerView> {
    private Subscription mCommitAnswerSubscription;

    public CommitContestAnswerPresenter(IContestCommitAnswerView view) {
        super(view);
    }

    public void commitContestAnswer(final String seq,final String id,final long zuId,final String liveId) {
        if (TextUtils.isEmpty(seq)) {
            MyLog.w(TAG, "param seq isEmpty");
            return;
        }
        if (TextUtils.isEmpty(id)) {
            MyLog.w(TAG, "param id isEmpty");
        }
        if (mCommitAnswerSubscription != null && !mCommitAnswerSubscription.isUnsubscribed()) {
            return;
        }
        mCommitAnswerSubscription = Observable
                .create(new Observable.OnSubscribe<LiveSummitProto.CommitContestAnswerRsp>() {
                    @Override
                    public void call(Subscriber<? super LiveSummitProto.CommitContestAnswerRsp> subscriber) {
                        LiveSummitProto.CommitContestAnswerRsp rsp = new CommitContestAnswerRequest(seq, id, zuId, liveId).syncRsp();
                        subscriber.onNext(rsp);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(mView.<LiveSummitProto.CommitContestAnswerRsp>bindUntilEvent())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<LiveSummitProto.CommitContestAnswerRsp>() {
                    @Override
                    public void call(LiveSummitProto.CommitContestAnswerRsp rsp) {
                        MyLog.w(TAG, "commitContestAnswer onNext");
                        if (rsp == null) {
                            MyLog.w(TAG,"commitContestAnswer rsp is null");
                            setAbleContestData();
                        } else if(rsp.getRetCode() == ErrorCode.CODE_SUCCESS){
                            MyLog.w(TAG,"commitContestAnswer rsp = " + rsp.toString());
                            if (rsp.getExtraInfo() != null) {
                                ContestCurrentCache.getInstance().setSeq(seq);

                                ContestCurrentCache.getInstance().setId(rsp.getExtraInfo().getId());
                                ContestCurrentCache.getInstance().setCorrect(rsp.getExtraInfo().getIsCorrect());
                                ContestCurrentCache.getInstance().setUseRevival(rsp.getExtraInfo().getUseRevival());
                                ContestCurrentCache.getInstance().setContinue(rsp.getExtraInfo().getIsContinue());

                                if(rsp.getExtraInfo().getUseRevival()){
                                    ContestGlobalCache.setRevivalNum(rsp.getExtraInfo().getRevivalNum());
                                }
                            } else {
                                MyLog.w(TAG,"commitContestAnswer  extraInfo= null");
                                setAbleContestData();
                            }
                        } else if (rsp.getRetCode() == ErrorCode.CODE_CONTEST_UNABLE) {
                            ToastUtils.showToast(GlobalData.app().getResources().getString(R.string.commit_answer_error_code,rsp.getRetCode()));
                            MyLog.w(TAG,"commitContestAnswer retCode = " + rsp.getRetCode());
                            setUnAbleContestData();
                        } else {
                            ToastUtils.showToast(GlobalData.app().getResources().getString(R.string.commit_answer_error_code,rsp.getRetCode()));
                            MyLog.w(TAG,"commitContestAnswer retCode = " + rsp.getRetCode());
                            setAbleContestData();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.w(TAG, "commitContestAnswer onError=" + throwable.getMessage());
                        setAbleContestData();
                    }
                });
    }

    //对于异常处理设置为可以继续答题
    private void setAbleContestData(){
        //清空cache消息 下次显示答案信息时顶部状态为空白（表示答题出现异常） 但是下次可以继续答题
        ContestCurrentCache.getInstance().clearCache();
        ContestCurrentCache.getInstance().setContinue(true);
    }

    //对于异常处理设置为淘汰
    private void setUnAbleContestData(){
        //清空cache消息，下次显示答案信息时顶部状态为已淘汰
        ContestCurrentCache.getInstance().clearCache();
        ContestCurrentCache.getInstance().setContinue(false);
    }
}
