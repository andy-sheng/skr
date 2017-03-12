package com.wali.live.livesdk.live.liveshow.presenter.panel;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.component.utils.MagicParamUtils;
import com.wali.live.livesdk.live.liveshow.data.MagicParamPresenter;
import com.wali.live.livesdk.live.liveshow.presenter.adapter.FilterItemAdapter;
import com.wali.live.livesdk.live.liveshow.view.panel.LiveMagicPanel;

import java.util.ArrayList;
import java.util.Collections;
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

    private int mBeautyPosition = -1;

    private List<FilterItemAdapter.FilterItem> mFilterItems;
    private String mFilter;
    private int mFilterIntensity = -1;

    private MagicParamPresenter.MagicParams mMagicParams;

    public LiveMagicPresenter(
            @NonNull IComponentController componentController) {
        super(componentController);
    }

    @Override
    public void syncPanelStatus() {
        if (mMagicParams != null) {
            mView.onPanelStatus(mMagicParams);
            return;
        }
        Observable.just(0)
                .map(new Func1<Integer, MagicParamPresenter.MagicParams>() {
                    @Override
                    public MagicParamPresenter.MagicParams call(Integer integer) {
                        MagicParamPresenter.MagicParams magicParams =
                                new MagicParamPresenter.MagicParams();
                        magicParams.loadParams(GlobalData.app());
                        return magicParams;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<MagicParamPresenter.MagicParams>bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Action1<MagicParamPresenter.MagicParams>() {
                    @Override
                    public void call(MagicParamPresenter.MagicParams magicParams) {
                        mMagicParams = magicParams;
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
    public void syncBeautyData() {
        if (mBeautyPosition != -1) {
            mView.onBeautyData(mBeautyPosition);
            return;
        }
        Observable.just(0)
                .map(new Func1<Integer, Integer>() {
                    @Override
                    public Integer call(Integer integer) {
                        return mMagicParams.findBeautyPos(MagicParamUtils.getBeautyLevel());
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Integer>bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer position) {
                        mBeautyPosition = position;
                        if (mView != null) {
                            mView.onBeautyData(mBeautyPosition);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "syncBeautyData failed, exception=" + throwable);
                    }
                });
    }

    @Override
    public void syncFilterData() {
        if (mFilterItems == null) {
            mFilterItems = new ArrayList<FilterItemAdapter.FilterItem>();
            Collections.addAll(mFilterItems,
                    new FilterItemAdapter.FilterItem(R.string.filter_normal, R.drawable.original,
                            "com.wali.live.videofilter.basic"),
                    new FilterItemAdapter.FilterItem(R.string.filter_sweet, R.drawable.sweet,
                            "com.wali.live.videofilter.sweet"),
                    new FilterItemAdapter.FilterItem(R.string.filter_crema, R.drawable.crema,
                            "com.wali.live.videofilter.crema"),
                    new FilterItemAdapter.FilterItem(R.string.filter_nashville, R.drawable.nashville,
                            "com.wali.live.videofilter.nashville"),
                    new FilterItemAdapter.FilterItem(R.string.filter_aden, R.drawable.aden,
                            "com.wali.live.videofilter.aden"),
                    new FilterItemAdapter.FilterItem(R.string.filter_gingham, R.drawable.gingham,
                            "com.wali.live.videofilter.gingham"),
                    new FilterItemAdapter.FilterItem(R.string.filter_stinson, R.drawable.stinson,
                            "com.wali.live.videofilter.stinson"),
                    new FilterItemAdapter.FilterItem(R.string.filter_clarendon, R.drawable.clarendon,
                            "com.wali.live.videofilter.clarendon"),
                    new FilterItemAdapter.FilterItem(R.string.filter_juno, R.drawable.juno,
                            "com.wali.live.videofilter.juno"),
                    new FilterItemAdapter.FilterItem(R.string.filter_dogpatch, R.drawable.dogpatch,
                            "com.wali.live.videofilter.dogpatch"),
                    new FilterItemAdapter.FilterItem(R.string.filter_gray, R.drawable.gray,
                            "com.wali.live.videofilter.gray")
            );
        }
        mView.onFilterData(mFilterItems);
        if (mFilterIntensity != -1) {
            mView.onFilterData(mFilter, mFilterIntensity);
            return;
        }
        Observable.just(0)
                .map(new Func1<Integer, Pair<String, Integer>>() {
                    @Override
                    public Pair<String, Integer> call(Integer integer) {
                        String filter = MagicParamUtils.getFilterCategory();
                        int intensity = MagicParamUtils.getFilterIntensity();
                        return Pair.create(filter, intensity);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Pair<String, Integer>>bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Action1<Pair<String, Integer>>() {
                    @Override
                    public void call(Pair<String, Integer> param) {
                        mFilter = param.first;
                        mFilterIntensity = param.second;
                        if (mView != null) {
                            mView.onFilterData(mFilter, mFilterIntensity);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "syncFilterData failed, exception=" + throwable);
                    }
                });
    }

    @Override
    public void syncExpressionData() {
    }

    @Override
    public void setBeauty(int index) {
    }

    @Override
    public void setFilter(String filter) {
//        notifyOnFilter(FILTER_SAMPLE_PARAMS[position]);
//        StatisticsAlmightyWorker.getsInstance().recordDelay(StatisticsKey.AC_APP,
//                StatisticsKey.KEY, String.format(StatisticsKey.KEY_FILTER_CLICK, FILTER_STATISTICS_KEY[position]),
//                StatisticsKey.TIMES, "1");
//        PreferenceUtils.setSettingInt(GlobalData.app(), PreferenceUtils.PREF_KEY_FILTER_CATEGORY_POSITION, position);
    }

    @Override
    public void setFilterIntensity(int intensity) {
//        notifyOnFilterIntensity(((float) volume) / 100.f);
    }

    @Override
    public void setExpression(int index) {
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
