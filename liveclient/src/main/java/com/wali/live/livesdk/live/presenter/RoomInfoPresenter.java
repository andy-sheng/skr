package com.wali.live.livesdk.live.presenter;

import com.base.log.MyLog;
import com.base.presenter.RxLifeCyclePresenter;
import com.base.utils.FileIOUtils;
import com.base.utils.callback.ICommonCallBack;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.assist.Attachment;
import com.wali.live.common.MessageType;
import com.wali.live.livesdk.live.api.RoomInfoChangeRequest;
import com.wali.live.livesdk.live.task.TaskCallBackWrapper;
import com.wali.live.proto.Live2Proto.ChangeRoomInfoRsp;
import com.wali.live.upload.UploadTask;
import com.wali.live.utils.AttachmentUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 16/12/16.
 *
 * @description 更新房间信息，目前只是使用在游戏直播的更新封面上
 */
public class RoomInfoPresenter extends RxLifeCyclePresenter {
    private static final String TAG = RoomInfoPresenter.class.getSimpleName();

    private Subscription mTimerSubscription;
    private Subscription mSubscription;

    private GameLivePresenter mGameLivePresenter;

    private long mPlayerId;
    private String mLiveId;
    private String mUrl;

    private boolean mIsAlive = false;

    public RoomInfoPresenter(GameLivePresenter presenter) {
        mGameLivePresenter = presenter;
    }

    /**
     * 开始上传封面
     */
    public void startLiveCover(long zuid, String liveId) {
        MyLog.d(TAG, "start zuid" + zuid + ";liveId" + liveId);
        mPlayerId = zuid;
        mLiveId = liveId;

        mIsAlive = true;
        startTimer();
    }

    private void startTimer() {
        stopTimer();
        MyLog.d(TAG, "restartTimer");
        mTimerSubscription = Observable.interval(1, 3, TimeUnit.MINUTES)
                .compose(this.<Long>bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        MyLog.w(TAG, "mTimerSubscription start");
                        GameLivePresenter gameLivePresenter = mGameLivePresenter;
                        if (gameLivePresenter == null) {
                            MyLog.e(TAG, "mGameLivePresenter is null");
                            return;
                        }
                        gameLivePresenter.screenshot(new ICommonCallBack() {
                            @Override
                            public void process(Object object) {
                                uploadFile((String) object);
                            }
                        });
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "mTimerSubscription error=" + throwable);
                    }
                });
    }

    private void stopTimer() {
        MyLog.d(TAG, "stopTimer");
        if (mTimerSubscription != null && !mTimerSubscription.isUnsubscribed()) {
            mTimerSubscription.unsubscribe();
        }
    }

    private void uploadFile(String path) {
        MyLog.d(TAG, "uploadFile path=" + path);
        final Attachment att = new Attachment();
        att.setType(Attachment.TYPE_IMAGE);
        att.setLocalPath(path);
        att.setMimeType(AttachmentUtils.getMimeType(MessageType.IMAGE, att.getLocalPath()));
        UploadTask.uploadPhoto(att, Attachment.AUTH_TYPE_USER_PIC, new TaskCallBackWrapper() {
            public void process(Object object) {
                boolean isSuccess = (Boolean) object;
                MyLog.d(TAG, "uploadFile isSuccess=" + isSuccess);
                FileIOUtils.deletePath(att.getLocalPath());
            }

            public void processWithMore(Object... objects) {
                if (objects == null || objects.length < 2) {
                    MyLog.d(TAG, "uploadFile processWithMore param is too short");
                    return;
                }
                boolean isSuccess = (Boolean) objects[0];
                MyLog.d(TAG, "uploadFile isSuccess=" + isSuccess);
                FileIOUtils.deletePath(att.getLocalPath());
                if (isSuccess) {
                    mUrl = (String) objects[1];
                    MyLog.d(TAG, "uploadFile processWithMore url=" + mUrl);
                    roomInfoToServer();
                }
            }
        });
    }

    private void roomInfoToServer() {
        if (!mIsAlive) {
            MyLog.d(TAG, "roomInfoToServer isAlive false");
            return;
        }
        startRoomInfo();
    }

    private void startRoomInfo() {
        stopRoomInfo();
        mSubscription = Observable
                .create(new Observable.OnSubscribe<ChangeRoomInfoRsp>() {
                    @Override
                    public void call(Subscriber<? super ChangeRoomInfoRsp> subscriber) {
                        ChangeRoomInfoRsp rsp = new RoomInfoChangeRequest(mPlayerId, mLiveId, mUrl).syncRsp();
                        if (rsp == null) {
                            subscriber.onError(new Exception("ChangeRoomInfoRsp is null"));
                        } else if (rsp.getRetCode() != ErrorCode.CODE_SUCCESS) {
                            subscriber.onError(new Exception(String.format("ChangeRoomInfoRsp retCode = %d", rsp.getRetCode())));
                        } else {
                            subscriber.onNext(rsp);
                            subscriber.onCompleted();
                        }
                    }
                })
                .map(new Func1<ChangeRoomInfoRsp, Boolean>() {
                    @Override
                    public Boolean call(ChangeRoomInfoRsp changeRoomInfoRsp) {
                        if (changeRoomInfoRsp != null) {
                            int retCode = changeRoomInfoRsp.getRetCode();
                            MyLog.d(TAG, "ChangeRoomInfoRsp errCode=" + retCode);
                            return retCode == ErrorCode.CODE_SUCCESS;
                        }
                        return false;
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(this.<Boolean>bindUntilEvent(PresenterEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        MyLog.d(TAG, "ChangeRoomInfoRsp onNext=" + aBoolean);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.d(TAG, "ChangeRoomInfoRsp onError=" + throwable.getMessage());
                    }
                });
    }

    private void stopRoomInfo() {
        MyLog.d(TAG, "stopRoomInfo");
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    @Override
    public void destroy() {
        MyLog.d(TAG, "destroy");
        super.destroy();
        mIsAlive = false;
        mGameLivePresenter = null;
    }

    public void pauseTimer() {
        MyLog.d(TAG, "pauseTimer");
        mIsAlive = false;
        stopRoomInfo();
        stopTimer();
    }

    public void resumeTimer() {
        MyLog.d(TAG, "resumeTimer");
        mIsAlive = true;
        startTimer();
    }
}
