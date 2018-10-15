package com.wali.live.watchsdk.channel.sublist.presenter;

import com.base.log.MyLog;
import com.wali.live.proto.CommonChannelProto;
import com.wali.live.proto.HotChannelProto.GetRecommendSublistRsp;
import com.wali.live.watchsdk.channel.sublist.data.SubChannelDataStore;
import com.wali.live.watchsdk.channel.viewmodel.BaseViewModel;
import com.wali.live.watchsdk.channel.viewmodel.ChannelModelFactory;
import com.wali.live.watchsdk.channel.viewmodel.ChannelUiType;
import com.wali.live.watchsdk.channel.viewmodel.ChannelViewModel;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 推荐频道二级页面的Presenter，提供数据的加载
 */
public class SubChannelPresenter implements ISubChannelPresenter {
    public static final String TAG = SubChannelPresenter.class.getSimpleName();

    private SubChannelDataStore mDataStore;
    private ISubChannelView mView;

    private Subscription mSubscription;
    private SubChannelParam mParam;
    private int mGender; //  //性别刷选 , 默认0 全部,1 男,2 女

    public void setGender(int gender) {
        mGender = gender;
    }

    public SubChannelPresenter(ISubChannelView view) {
        mDataStore = new SubChannelDataStore();
        mView = view;
    }

    public void setParam(SubChannelParam param) {
        this.mParam = param;
    }

    @Override
    public void start() {
        mSubscription = mDataStore.getHotSubListObservable(mParam, mGender)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<GetRecommendSublistRsp>() {
                    @Override
                    public void onCompleted() {
                        MyLog.d(TAG, "onCompleted");
                        mView.finishRefresh();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, "onError : " + e.getMessage());
                        mView.finishRefresh();
                    }

                    @Override
                    public void onNext(GetRecommendSublistRsp rsp) {
                        List<BaseViewModel> models = new ArrayList();

                        boolean splitFirst = true;
                        boolean splitDuplicate = false;
                        for (CommonChannelProto.ChannelItem protoItem : rsp.getItemsList()) {
                            ChannelViewModel viewModel = ChannelModelFactory.getChannelViewModel(protoItem);
                            if (viewModel != null && viewModel.isNeedRemove()) {
                                viewModel = null;
                            }
                            if (viewModel != null) {
                                MyLog.e(TAG, "sub uiType=" + viewModel.getUiType());
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
                        if (models.size() > 0) {
                            ChannelViewModel viewModel = models.get(models.size() - 1).get();
                            if (viewModel.getUiType() == ChannelUiType.TYPE_SPLIT_LINE) {
                                models.remove(models.size() - 1);
                            }
                        }

                        mView.updateView(models);
                    }
                });
    }

    @Override
    public void stop() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }
}
