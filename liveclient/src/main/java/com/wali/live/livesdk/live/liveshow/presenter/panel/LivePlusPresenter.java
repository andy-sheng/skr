package com.wali.live.livesdk.live.liveshow.presenter.panel;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.component.utils.PlusParamUtils;
import com.wali.live.livesdk.live.livegame.LiveComponentController;
import com.wali.live.livesdk.live.liveshow.presenter.adapter.PlusItemAdapter;
import com.wali.live.livesdk.live.liveshow.view.panel.LivePlusPanel;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by yangli on 2017/03/09.
 * <p>
 * Generated using create_bottom_panel.py
 *
 * @module 秀场直播加面板表现
 */
public class LivePlusPresenter extends ComponentPresenter<LivePlusPanel.IView>
        implements LivePlusPanel.IPresenter {
    private static final String TAG = "LivePlusPresenter";

    public LivePlusPresenter(
            @NonNull IComponentController componentController) {
        super(componentController);
    }

    @Override
    public void showAtmosphereView() {
        mComponentController.onEvent(LiveComponentController.MSG_HIDE_BOTTOM_PANEL);
        mComponentController.onEvent(LiveComponentController.MSG_SHOW_ATMOSPHERE_VIEW);
    }

    @Override
    public void showInputView() {
        mComponentController.onEvent(LiveComponentController.MSG_HIDE_BOTTOM_PANEL);
        mComponentController.onEvent(LiveComponentController.MSG_SHOW_INPUT_VIEW);
    }

    @Override
    public void syncPlusBtnConfig() {
        Observable.just(0)
                .map(new Func1<Integer, List<PlusItemAdapter.PlusItem>>() {
                    @Override
                    public List<PlusItemAdapter.PlusItem> call(Integer integer) {
                        List<PlusItemAdapter.PlusItem> plusItems = new ArrayList<>();
                        if (!PlusParamUtils.isHideAtmosphere()) {
                            plusItems.add(new PlusItemAdapter.PlusItem(R.id.atmosphere_btn,
                                    R.string.live_plus_atmosphere, R.drawable.live_plus_atmosphere));
                        }
                        plusItems.add(new PlusItemAdapter.PlusItem(R.id.comment_btn,
                                R.string.live_plus_comment, R.drawable.live_plus_comment));
                        return plusItems;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<List<PlusItemAdapter.PlusItem>>bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Action1<List<PlusItemAdapter.PlusItem>>() {
                    @Override
                    public void call(List<PlusItemAdapter.PlusItem> plusItems) {
                        if (mView != null) {
                            mView.onPlusBtnConfig(plusItems);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "syncPlusBtnConfig failed, exception=" + throwable);
                    }
                });
    }

    @Nullable
    @Override
    protected IAction createAction() {
        return null;
    }
}
