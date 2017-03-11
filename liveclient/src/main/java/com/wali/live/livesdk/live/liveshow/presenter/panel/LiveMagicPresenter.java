package com.wali.live.livesdk.live.liveshow.presenter.panel;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.base.log.MyLog;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.component.utils.MagicParamUtils;
import com.wali.live.livesdk.live.liveshow.data.MagicParamPresenter;
import com.wali.live.livesdk.live.liveshow.presenter.adapter.FilterItemAdapter;
import com.wali.live.livesdk.live.liveshow.view.panel.LiveMagicPanel;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by yangli on 2017/03/07.
 * <p>
 * Generated using create_bottom_panel.py
 *
 * @module 秀场美妆面板表现
 */
public class LiveMagicPresenter extends ComponentPresenter<LiveMagicPanel.IView>
        implements LiveMagicPanel.IPresenter {
    private static final String TAG = "LiveMagicPresenter";

    public LiveMagicPresenter(
            @NonNull IComponentController componentController) {
        super(componentController);
    }

    @Override
    public void syncPanelStatus() {
        Observable.just(0)
                .map(new Func1<Integer, MagicParamPresenter.MagicParams>() {
                    @Override
                    public MagicParamPresenter.MagicParams call(Integer integer) {
                        MagicParamPresenter.MagicParams magicParams =
                                new MagicParamPresenter.MagicParams();
//                        magicParams.loadParams(GlobalData.app());
                        return magicParams;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<MagicParamPresenter.MagicParams>bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Action1<MagicParamPresenter.MagicParams>() {
                    @Override
                    public void call(MagicParamPresenter.MagicParams magicParams) {
                        if (mView != null) {
                            mView.onPanelStatus(magicParams);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "syncPanelStatus failed, exception=" + throwable);
                    }
                });
    }

    @Override
    public void syncFilterData() {
        Observable.just(0)
                .map(new Func1<Integer, Pair<List<FilterItemAdapter.FilterItem>, String>>() {
                    @Override
                    public Pair<List<FilterItemAdapter.FilterItem>, String> call(Integer integer) {
                        List<FilterItemAdapter.FilterItem> filterItems = new ArrayList<>();
                        filterItems.add(new FilterItemAdapter.FilterItem(
                                R.string.filter_normal,
                                R.drawable.original,
                                "com.wali.live.videofilter.basic"));
                        filterItems.add(new FilterItemAdapter.FilterItem(
                                R.string.filter_sweet,
                                R.drawable.sweet,
                                "com.wali.live.videofilter.sweet"));
                        filterItems.add(new FilterItemAdapter.FilterItem(
                                R.string.filter_crema,
                                R.drawable.crema,
                                "com.wali.live.videofilter.crema"));
                        filterItems.add(new FilterItemAdapter.FilterItem(
                                R.string.filter_nashville,
                                R.drawable.nashville,
                                "com.wali.live.videofilter.nashville"));
                        filterItems.add(new FilterItemAdapter.FilterItem(
                                R.string.filter_aden,
                                R.drawable.aden,
                                "com.wali.live.videofilter.aden"));
                        filterItems.add(new FilterItemAdapter.FilterItem(
                                R.string.filter_gingham,
                                R.drawable.gingham,
                                "com.wali.live.videofilter.gingham"));
                        filterItems.add(new FilterItemAdapter.FilterItem(
                                R.string.filter_stinson,
                                R.drawable.stinson,
                                "com.wali.live.videofilter.stinson"));
                        filterItems.add(new FilterItemAdapter.FilterItem(
                                R.string.filter_clarendon,
                                R.drawable.clarendon,
                                "com.wali.live.videofilter.clarendon"));
                        filterItems.add(new FilterItemAdapter.FilterItem(
                                R.string.filter_juno,
                                R.drawable.juno,
                                "com.wali.live.videofilter.juno"));
                        filterItems.add(new FilterItemAdapter.FilterItem(
                                R.string.filter_dogpatch,
                                R.drawable.dogpatch,
                                "com.wali.live.videofilter.dogpatch"));
                        filterItems.add(new FilterItemAdapter.FilterItem(
                                R.string.filter_gray,
                                R.drawable.gray,
                                "com.wali.live.videofilter.gray"));
                        String currFilter = MagicParamUtils.getFilterCategory();
                        if (TextUtils.isEmpty(currFilter)) {
                            currFilter = "com.wali.live.videofilter.basic";
                        }
                        return Pair.create(filterItems, currFilter);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Pair<List<FilterItemAdapter.FilterItem>, String>>bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Action1<Pair<List<FilterItemAdapter.FilterItem>, String>>() {
                    @Override
                    public void call(Pair<List<FilterItemAdapter.FilterItem>, String> filterData) {
                        if (mView != null) {
                            mView.onFilterData(filterData.first, filterData.second);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "syncFilterData failed, exception=" + throwable);
                    }
                });
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
