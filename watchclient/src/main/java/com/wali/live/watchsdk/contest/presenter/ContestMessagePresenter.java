package com.wali.live.watchsdk.contest.presenter;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.base.log.MyLog;
import com.mi.live.data.push.IPushMsgProcessor;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.contest.BaseContestMsgExt;
import com.mi.live.data.push.model.contest.ContestAnswerMsgExt;
import com.mi.live.data.push.model.contest.ContestQuestionMsgExt;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.watchsdk.channel.view.presenter.HeaderVideoPresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;

import static com.mi.live.data.push.model.BarrageMsgType.B_MSG_TYPE_ANSWER;
import static com.mi.live.data.push.model.BarrageMsgType.B_MSG_TYPE_QUESTION;

/**
 * Created by zyh on 2018/1/12.
 *
 * @module 接收contest的presenter, 拿到消息通知view显示
 */
public class ContestMessagePresenter implements IPushMsgProcessor {
    private String TAG = "ContestMessagePresenter";
    private String SYN_DEBUG = "sync debug:";
    private static final int DEFAULT_TIME_OUT = 5_000; //默认5s超时时间，如果服务器不带超时时间的话

    private List<String> mQuestionMsgExts = new ArrayList<>();
    private List<String> mAnswerMsgExts = new ArrayList<>();

    private Queue<BaseContestMsgExt> mControlQueue = new LinkedBlockingQueue<>(12);
    private Subscription mQuestionTimer;     //每一個題的显示时间的timer
    private boolean mIsProcessing = false;

    private IContestCallBack mCallBack;
    private ContestVideoPlayerPresenter mPlayerPresenter;

    private Handler mMainHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case B_MSG_TYPE_QUESTION:
                    if (mCallBack != null) {
                        MyLog.w(TAG, "handleMessage call onQuestion callback");
                        mCallBack.onQuestion((ContestQuestionMsgExt) msg.obj);
                    }
                    break;
                case B_MSG_TYPE_ANSWER:
                    if (mCallBack != null) {
                        MyLog.w(TAG, "handleMessage call onAnswer callback");
                        mCallBack.onAnswer((ContestAnswerMsgExt) msg.obj);
                    }
                    break;
            }
            mIsProcessing = false;
            next();
            return false;
        }
    });

    /**
     * 这里传进来的presenter是为了获取时间戳的
     */
    public ContestMessagePresenter(ContestVideoPlayerPresenter presenter) {
        this.mPlayerPresenter = presenter;
    }

    public void setCallBack(IContestCallBack callBack) {
        mCallBack = callBack;
    }

    private void processDataInUIThread(final BarrageMsg msg) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "processDataInUIThread  type=" + msg.getMsgType() + " threadId =" + Thread.currentThread().getId());
                switch (msg.getMsgType()) {
                    case B_MSG_TYPE_QUESTION: {
                        ContestQuestionMsgExt msgExt = (ContestQuestionMsgExt) msg.getMsgExt();
                        MyLog.w(TAG, "QUESTION step0");
                        if (msgExt != null && msgExt.getQuestionInfoModel() != null) {
                            MyLog.w(TAG, "QUESTION step1=" + msgExt.toString());
                            String questionId = msgExt.getQuestionInfoModel().getSeq();
                            if (!TextUtils.isEmpty(questionId) && !mQuestionMsgExts.contains(questionId)) {
                                mQuestionMsgExts.add(questionId);
                                mControlQueue.offer(msgExt);
                                MyLog.w(TAG, "QUESTION process type=" + msg.getMsgType() + "msgExt=" + msgExt.toString() + " questionId=" + questionId);
                            } else {
                                MyLog.w(TAG, "QUESTION step 3 questionId=" + questionId);
                            }
                        }
                    }
                    break;
                    case B_MSG_TYPE_ANSWER: {
                        ContestAnswerMsgExt msgExt = (ContestAnswerMsgExt) msg.getMsgExt();
                        MyLog.w(TAG, "ANSWER step0");
                        if (msgExt != null && msgExt.getQuestionInfoModel() != null) {
                            MyLog.w(TAG, "ANSWER step1 msgExt=" + msgExt.toString());
                            String questionId = msgExt.getQuestionInfoModel().getSeq();
                            if (!TextUtils.isEmpty(questionId) && !mAnswerMsgExts.contains(questionId)) {
                                mAnswerMsgExts.add(questionId);
                                mControlQueue.offer(msgExt);
                                MyLog.w(TAG, "ANSWER process type=" + msg.getMsgType() + "msgExt=" + msgExt.toString() + " questionId=" + questionId);
                            } else {
                                MyLog.w(TAG, "ANSWER step 3 questionId=" + questionId);
                            }
                        }
                    }
                    break;
                }
                next();
            }
        });
    }

    @Override
    public void process(BarrageMsg msg, RoomBaseDataModel roomBaseDataModel) {
        if (msg == null) {
            return;
        }
        MyLog.w(TAG, "process  type=" + msg.getMsgType() + " threadId =" + Thread.currentThread().getId());
        processDataInUIThread(msg);
    }

    //处理队列消息
    private void next() {
        MyLog.w(TAG, "next");
        if (!mControlQueue.isEmpty() && !mIsProcessing) {
            calculateShowTime(mControlQueue.remove());
        }
    }

    private void stopQuestionTimer() {
        MyLog.w(TAG, "stopQuestionTimer");
        if (mQuestionTimer != null && !mQuestionTimer.isUnsubscribed()) {
            mQuestionTimer.unsubscribe();
        }
    }

    private void calculateShowTime(final BaseContestMsgExt msgExt) {
        MyLog.w(TAG, "calculateShowTime");
        if (msgExt == null || msgExt.getQuestionInfoModel() == null) {
            return;
        }
        mIsProcessing = true;
        long delayTime = msgExt.getQuestionInfoModel().getDelayTime();
        long maxTs = delayTime > 0 ? delayTime : DEFAULT_TIME_OUT;
        MyLog.w(TAG, SYN_DEBUG + "playerTimestamp=" + mPlayerPresenter.getCurrentAudioTimestamp() + " msgTimestamp=" + msgExt.getStreamTs());
        mQuestionTimer = Observable.interval(0, 200, TimeUnit.MILLISECONDS)
                .take((int) (maxTs / 200) + 1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        long streamerAudioTs = mPlayerPresenter.getCurrentAudioTimestamp();
                        MyLog.v(TAG, "calculateQuestion streamerAudioTs=" + streamerAudioTs +
                                " msgExt.getStreamTs()=" + msgExt.getStreamTs());
                        if (streamerAudioTs > 0 && msgExt.getStreamTs() > 0
                                && streamerAudioTs >= msgExt.getStreamTs()) {
                            MyLog.w(TAG, SYN_DEBUG + "show message because of timestamp; " +
                                    "streamTimestamp=" + streamerAudioTs + " msgTimestamp=" + msgExt.getStreamTs());
                            sendQuestionMessage(msgExt, "onNext");
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, SYN_DEBUG + "calculateQuestion failed=" + throwable);
                        mIsProcessing = false;
                        next();
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        MyLog.d(TAG, "onComplete");
                        MyLog.w(TAG, SYN_DEBUG + "sync debug:show message because of timeout msgTimestamp=" + msgExt.getStreamTs());
                        sendQuestionMessage(msgExt, "onComplete");
                    }
                });
    }

    private void sendQuestionMessage(BaseContestMsgExt msgExt, String from) {
        MyLog.w(TAG, "sendQuestionMessage from=" + from);
        Message message = Message.obtain();
        message.what = msgExt.getMsgType();
        message.obj = msgExt;
        mMainHandler.sendMessage(message);
        stopQuestionTimer();
    }

    @Override
    public int[] getAcceptMsgType() {
        return new int[]{
                B_MSG_TYPE_QUESTION,
                B_MSG_TYPE_ANSWER
        };
    }

    @Override
    public void start() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {
        MyLog.w(TAG, "destroy");
        mCallBack = null;
        stopQuestionTimer();
    }

    public interface IContestCallBack {
        void onQuestion(ContestQuestionMsgExt msgExt);

        void onAnswer(ContestAnswerMsgExt msgExt);
    }
}
