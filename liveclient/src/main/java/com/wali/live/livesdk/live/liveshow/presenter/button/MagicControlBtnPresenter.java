package com.wali.live.livesdk.live.liveshow.presenter.button;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.livesdk.live.liveshow.view.button.MagicControlBtnView;

/**
 * Created by yangli on 2017/03/09.
 * <p>
 * Generated using create_component_view.py
 *
 * @module 美妆按钮表现
 */
public class MagicControlBtnPresenter extends ComponentPresenter<MagicControlBtnView.IView>
        implements MagicControlBtnView.IPresenter {
    private static final String TAG = "MagicControlBtnPresenter";

    public MagicControlBtnPresenter(
            @NonNull IComponentController componentController) {
        super(componentController);
//        EventBus.getDefault().register(this);
    }

    @Override
    public void destroy() {
        super.destroy();
//        if (EventBus.getDefault().isRegistered(this)) {
//            EventBus.getDefault().unregister(this);
//        }
    }

//    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
//    public void onEventMainThread(EventClass.ExpressionSupportEvent event) {
//        if (event != null && event.isSupport()) {
//            syncExpressionStatus();
//        } else {
//            mView.showRedDot(false);
//        }
//    }

//    private void syncExpressionStatus() {
//        Observable.just(0)
//                .map(new Func1<Integer, Boolean>() {
//                    @Override
//                    public Boolean call(Integer integer) {
//                        return PreferenceUtils.getSettingBoolean(
//                                GlobalData.app(), PreferenceKeys.PRE_KEY_EXPRESSION_USED, false);
//                    }
//                }).subscribeOn(Schedulers.io())
//                .compose(bindUntilEvent(PresenterEvent.DESTROY))
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Action1<Boolean>() {
//                    @Override
//                    public void call(Boolean used) {
//                        mView.showRedDot(!used);
//                    }
//                }, new Action1<Throwable>() {
//                    @Override
//                    public void call(Throwable throwable) {
//                        MyLog.e(TAG, "syncExpressionStatus failed, exception=" + throwable);
//                    }
//                });
//    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEventMainThread(EventClass.HideRedIconEvent event) {
//        mView.showRedDot(false);
//    }

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
