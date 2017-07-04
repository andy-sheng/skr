package com.wali.live.watchsdk.videodetail.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.proto.Live2Proto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.feeds.FeedsCommentUtils;
import com.wali.live.watchsdk.videodetail.VideoDetailController;
import com.wali.live.watchsdk.videodetail.adapter.DetailReplayAdapter;
import com.wali.live.watchsdk.videodetail.view.DetailReplayView;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.wali.live.component.ComponentController.MSG_COMPLETE_USER_INFO;

/**
 * Created by zyh on 2017/06/06.
 *
 * @module 詳情頁底下的回放
 */
public class DetailReplayPresenter extends ComponentPresenter<DetailReplayView.IView>
        implements DetailReplayView.IPresenter {
    private static final String TAG = "DetailReplayPresenter";
    private Subscription mSubscription;
    private RoomBaseDataModel mMyRoomData;

    public DetailReplayPresenter(@NonNull IComponentController componentController
            , RoomBaseDataModel myRoomData) {
        super(componentController);
        this.mMyRoomData = myRoomData;
    }

    @Override
    public void startPresenter() {
        registerAction(MSG_COMPLETE_USER_INFO);
    }

    @Override
    public void pullReplayList() {
        if (mSubscription != null && mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
        MyLog.w(TAG, "pullReplayList");
        mSubscription = Observable.just(0)
                .map(new Func1<Integer,
                        List<DetailReplayAdapter.ReplayInfoItem>>() {
                    @Override
                    public List<DetailReplayAdapter.ReplayInfoItem> call(Integer integer) {
                        Live2Proto.HistoryLiveRsp rsp = FeedsCommentUtils.getHistoryShowList(
                                UserAccountManager.getInstance().getUuidAsLong(),
                                mMyRoomData.getUid());
                        if (rsp == null || rsp.getRetCode() != ErrorCode.CODE_SUCCESS
                                || rsp.getHisLiveList() == null) {
                            return null;
                        }
                        List<DetailReplayAdapter.ReplayInfoItem> list =
                                new ArrayList<DetailReplayAdapter.ReplayInfoItem>();
                        for (Live2Proto.HisLive hisLive : rsp.getHisLiveList()) {
                            String coverUrl = (hisLive.getLiveCover() == null) ? "" :
                                    hisLive.getLiveCover().getCoverUrl();
                            list.add(new DetailReplayAdapter.ReplayInfoItem(
                                    hisLive.getLiveId(),
                                    hisLive.getViewerCnt(),
                                    hisLive.getUrl(),
                                    hisLive.getLiveTitle(),
                                    coverUrl,
                                    hisLive.getShareUrl(),
                                    hisLive.getStartTime()));
                        }
                        return list;
                    }
                }).subscribeOn(Schedulers.io())
                .compose(this.<List<DetailReplayAdapter.ReplayInfoItem>>bindUntilEvent(PresenterEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<DetailReplayAdapter.ReplayInfoItem>>() {
                    @Override
                    public void call(List<DetailReplayAdapter.ReplayInfoItem> itemList) {
                        if (itemList == null) {
                            mView.onPullReplayFailed();
                        } else {
                            mComponentController.onEvent(VideoDetailController.MSG_REPLAY_TOTAL_CNT,
                                    new Params().putItem(itemList.size()));
                            mView.onPullReplayDone(itemList);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "pullReplayList failed=" + throwable);
                        mView.onPullReplayFailed();
                    }
                });
    }

    @Override
    public void onClickReplayItem(DetailReplayAdapter.ReplayInfoItem replayInfoItem) {
        if (replayInfoItem == null || TextUtils.isEmpty(mMyRoomData.getVideoUrl())) {
            return;
        }
        if (mMyRoomData.getVideoUrl().equals(replayInfoItem.mUrl)) {
            ToastUtils.showToast(GlobalData.app(), R.string.open_same_video_hint);
            return;
        }
        mMyRoomData.setVideoUrl(replayInfoItem.mUrl);
        mMyRoomData.setRoomId(replayInfoItem.mLiveId);
        mComponentController.onEvent(VideoDetailController.MSG_NEW_DETAIL_REPLAY);
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
                case MSG_COMPLETE_USER_INFO:
                    mView.updateAllReplayView();
                    break;
                default:
                    break;
            }
            return false;
        }
    }
}
