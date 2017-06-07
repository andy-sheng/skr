package com.wali.live.watchsdk.videodetail.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.data.user.User;
import com.wali.live.component.ComponentController;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.feeds.FeedsLikeUtils;
import com.wali.live.watchsdk.feeds.model.IFeedsInfoable;
import com.wali.live.watchsdk.feeds.model.SimpleFeedsInfoable;
import com.wali.live.watchsdk.videodetail.view.DetailBottomView;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.wali.live.component.ComponentController.MSG_SHOW_COMMENT_INPUT;

/**
 * Created by yangli on 2017/05/31.
 * <p>
 * Generated using create_component_view.py
 *
 * @module 详情播放表现
 */
public class DetailBottomPresenter extends ComponentPresenter<DetailBottomView.IView>
        implements DetailBottomView.IPresenter {
    private static final String TAG = "DetailBottomPresenter";

    private RoomBaseDataModel mMyRoomData;

    private final IFeedsInfoable mFeedsInfo = new SimpleFeedsInfoable() {
        @Override
        public String getFeedsInfoId() {
            return mMyRoomData.getRoomId();
        }

        @Override
        public long getOwnerUserId() {
            return mMyRoomData.getUid();
        }
    };

    public DetailBottomPresenter(
            @NonNull IComponentController componentController,
            @NonNull RoomBaseDataModel roomData) {
        super(componentController);
        mMyRoomData = roomData;
    }

    @Override
    public void showInputView() {
        mComponentController.onEvent(MSG_SHOW_COMMENT_INPUT, new Params().putItem(mMyRoomData.getRoomId()));
    }

    @Override
    public void praiseVideo(final boolean isLike) {
        Observable.just(0)
                .map(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer integer) {
                        User user = MyUserInfoManager.getInstance().getUser();
                        if (isLike) { //点赞
                            return FeedsLikeUtils.likeFeeds(user, mFeedsInfo);
                        } else { //取消点赞
                            return FeedsLikeUtils.cancelLikeFeeds(user, mFeedsInfo);
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
        mComponentController.onEvent(ComponentController.MSG_SHOW_SHARE_PANEL);
    }

    @Nullable
    @Override
    protected IAction createAction() {
        return new Action();
    }

    public class Action implements IAction {
        @Override
        public boolean onAction(int source, @Nullable Params params) {
            if (mView == null) {
                MyLog.e(TAG, "onAction but mView is null, source=" + source);
                return false;
            }
            switch (source) {
                default:
                    break;
            }
            return false;
        }
    }
}
