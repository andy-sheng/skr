package com.wali.live.livesdk.live.liveshow.presenter.panel;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.thornbirds.component.IParams;
import com.wali.live.componentwrapper.presenter.BaseSdkRxPresenter;
import com.wali.live.livesdk.R;
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
 *
 * @module 秀场美妆面板表现
 */
public class LiveMagicPresenter extends BaseSdkRxPresenter<LiveMagicPanel.IView>
        implements LiveMagicPanel.IPresenter {
    private static final String TAG = "LiveMagicPresenter";

    private List<FilterItemAdapter.FilterItem> mFilterItems;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public LiveMagicPresenter() {
        super(null);
    }

    @Override
    public void syncPanelStatus() {
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
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        return false;
    }
}
