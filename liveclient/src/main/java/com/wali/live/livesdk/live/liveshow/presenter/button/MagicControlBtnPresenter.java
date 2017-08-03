package com.wali.live.livesdk.live.liveshow.presenter.button;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.componentwrapper.BaseSdkController;
import com.wali.live.livesdk.live.liveshow.view.button.MagicControlBtnView;

/**
 * Created by yangli on 2017/03/09.
 * <p>
 * Generated using create_component_view.py
 *
 * @module 美妆按钮表现
 */
public class MagicControlBtnPresenter extends ComponentPresenter<MagicControlBtnView.IView, BaseSdkController>
        implements MagicControlBtnView.IPresenter {
    private static final String TAG = "MagicControlBtnPresenter";

    @Override
    protected String getTAG() {
        return TAG;
    }

    public MagicControlBtnPresenter(
            @NonNull BaseSdkController controller) {
        super(controller);
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

    @Override
    public boolean onEvent(int event, IParams params) {
        return false;
    }
}
