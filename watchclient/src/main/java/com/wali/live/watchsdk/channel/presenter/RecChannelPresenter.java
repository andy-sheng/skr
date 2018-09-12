package com.wali.live.watchsdk.channel.presenter;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.base.activity.RxActivity;
import com.base.log.MyLog;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.proto.CommonChannelProto;
import com.wali.live.proto.HotChannelProto;
import com.wali.live.watchsdk.channel.data.ChannelDataStore;
import com.wali.live.watchsdk.channel.viewmodel.BaseViewModel;
import com.wali.live.watchsdk.channel.viewmodel.ChannelModelFactory;
import com.wali.live.watchsdk.channel.viewmodel.ChannelUiType;
import com.wali.live.watchsdk.channel.viewmodel.ChannelViewModel;

import java.util.ArrayList;
import java.util.List;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by liuting on 18-9-12.
 * 直播间内根据当前观看的直播，关注等信息，拉取推荐的直播列表
 */

public class RecChannelPresenter implements IChannelPresenter{
    private String TAG = getClass().getSimpleName();

    private ChannelDataStore mDataStore;

    private RxActivity mRxActivity;
    private IChannelView mView;

    // param
    long mViewerId;
    long mAnchorId;
    String mPackageName = "";
    long mGameId;
    int mRecType;
    int mReqFrom;

    private Subscription mSubscription;

    public RecChannelPresenter(RxActivity rxActivity, IChannelView view) {
        mDataStore = new ChannelDataStore();
        mRxActivity = rxActivity;
        mView = view;
    }

    public void setRequestParam(long viewerId, long anchorId, String packageName, long gameId, int recType, int reqFrom) {
        mViewerId = viewerId;
        mAnchorId = anchorId;
        if (!TextUtils.isEmpty(packageName)) {
            mPackageName = packageName;
        }
        mGameId = gameId;
        mRecType = recType;
        mReqFrom = reqFrom;
    }

    @Override
    public void start() {
        mSubscription = mDataStore.getRecListObservable(mViewerId, mAnchorId, mPackageName, mGameId, mRecType, mReqFrom)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mRxActivity.<HotChannelProto.GetRecListRsp>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new Observer<HotChannelProto.GetRecListRsp>() {
                    @Override
                    public void onCompleted() {
                        MyLog.d(TAG, "getRecListObservable onCompleted");
                        mView.finishRefresh();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, "getRecListObservable onError=" + e.getMessage());
                        mView.finishRefresh();
                    }

                    @Override
                    public void onNext(HotChannelProto.GetRecListRsp rsp) {
                        MyLog.d(TAG, "getRecListObservable onNext");
                        if (rsp != null) {
                            mView.updateView(processRsp(rsp), rsp.getChannelId());
                        }
                    }
                });
    }

    private List<? extends BaseViewModel> processRsp(@NonNull HotChannelProto.GetRecListRsp rsp) {
        long channelId = rsp.getChannelId();

        List<ChannelViewModel> models = new ArrayList();
        boolean splitFirst = true;
        boolean splitDuplicate = false;
        for (CommonChannelProto.ChannelItem protoItem : rsp.getItemsList()) {
            ChannelViewModel viewModel = ChannelModelFactory.getChannelViewModel(protoItem);
            if (viewModel == null || viewModel != null && viewModel.isNeedRemove()) {
                MyLog.i(TAG, "viewModel need remove ");
                continue;
            }

            viewModel.setChannelId(channelId);

            int uiType = viewModel.getUiType();
            if (ChannelUiType.ALL_CHANNEL_UI_TYPE.contains(uiType)) {
                if (viewModel.getUiType() == ChannelUiType.TYPE_SPLIT_LINE) {
                    if (!splitFirst && !splitDuplicate) {
                        models.add(viewModel);
                        splitDuplicate = true;
                    }
                } else {
                    models.add(viewModel);
                    splitFirst = false;
                    splitDuplicate = false;
                }
            }
        }
        return models;
    }

    @Override
    public void stop() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }
}
