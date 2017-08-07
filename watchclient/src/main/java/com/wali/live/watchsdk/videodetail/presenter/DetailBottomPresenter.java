package com.wali.live.watchsdk.videodetail.presenter;

import android.support.annotation.NonNull;

import com.base.log.MyLog;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.Params;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
import com.wali.live.watchsdk.feeds.FeedsLikeUtils;
import com.wali.live.watchsdk.videodetail.view.DetailBottomView;
import com.wali.live.watchsdk.watch.presenter.SnsShareHelper;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.wali.live.component.BaseSdkController.MSG_SHOW_COMMENT_INPUT;
import static com.wali.live.component.BaseSdkController.MSG_UPDATE_LIKE_STATUS;
import static com.wali.live.watchsdk.feeds.FeedsInfoUtils.FEED_TYPE_DEFAULT;

/**
 * Created by yangli on 2017/05/31.
 *
 * @module 详情播放表现
 */
public class DetailBottomPresenter extends BaseSdkRxPresenter<DetailBottomView.IView>
        implements DetailBottomView.IPresenter {
    private static final String TAG = "DetailBottomPresenter";

    private RoomBaseDataModel mMyRoomData;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public DetailBottomPresenter(
            @NonNull IEventController controller,
            @NonNull RoomBaseDataModel roomData) {
        super(controller);
        mMyRoomData = roomData;
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_UPDATE_LIKE_STATUS);
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        unregisterAllAction();
    }

    @Override
    public void showInputView() {
        postEvent(MSG_SHOW_COMMENT_INPUT, new Params().putItem(mMyRoomData.getRoomId()));
    }

    @Override
    public void praiseVideo(final boolean isLike) {
        final String feedId = mMyRoomData.getRoomId();
        final long ownerId = mMyRoomData.getUid();
        Observable.just(0)
                .map(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer integer) {
                        if (isLike) { // 点赞
                            return FeedsLikeUtils.likeFeeds(feedId, ownerId, FEED_TYPE_DEFAULT);
                        } else { // 取消点赞
                            return FeedsLikeUtils.cancelLikeFeeds(feedId, ownerId, FEED_TYPE_DEFAULT);
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Boolean>bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        if (mView != null) {
                            if (result) {
                                mView.onPraiseDone(isLike);
                            } else {
                                mView.onPraiseFailed(isLike);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "praiseVideo failed, exception=" + throwable);
                        if (mView != null) {
                            mView.onPraiseFailed(isLike);
                        }
                    }
                });
    }

    @Override
    public void showSharePanel() {
        SnsShareHelper.getInstance().shareToSns(-1, mMyRoomData);
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        if (mView == null) {
            MyLog.e(TAG, "onAction but mView is null, event=" + event);
            return false;
        }
        switch (event) {
            case MSG_UPDATE_LIKE_STATUS:
                mView.onPraiseDone((boolean) params.getItem(0));
                break;
            default:
                break;
        }
        return false;
    }
}
