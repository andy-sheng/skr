package com.wali.live.modulewatch.barrage.presenter;

import android.support.annotation.NonNull;
import android.view.View;

import com.common.log.MyLog;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.modulewatch.barrage.event.CommentRefreshEvent;
import com.wali.live.modulewatch.barrage.view.LiveCommentView;
import com.wali.live.modulewatch.base.component.BaseSdkController;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by yangli on 2017/03/02.
 *
 * @module 弹幕区表现
 */
public class LiveCommentPresenter extends ComponentPresenter<LiveCommentView.IView>
        implements LiveCommentView.IPresenter {
    private static final String TAG = "LiveCommentPresenter";

    private boolean mIsGameMode;
    private boolean mIsLandscape = false;// 是否是横屏
    private boolean mIsInputAreaShow = false; //输入框是否弹出

    @Override
    protected final String getTAG() {
        return TAG;
    }

    public LiveCommentPresenter(@NonNull IEventController controller) {
        super(controller);
        // TEST
//        Observable.interval(3, 5, TimeUnit.SECONDS)
//                .observeOn(AndroidSchedulers.mainThread())
//                .compose(this.<Long>bindUntilEvent(PresenterEvent.DESTROY))
//                .subscribe(new Action1<Long>() {
//                    @Override
//                    public void call(Long aLong) {
//                        if (mView != null) {
//                            mView.setRightMargin(aLong % 2 == 0 ?
//                                    LARGE_MARGIN_PORTRAIT : NORMAL_MARGIN_PORTRAIT);
//                        }
//                    }
//                }, new Action1<Throwable>() {
//                    @Override
//                    public void call(Throwable throwable) {
//                    }
//                });
    }

    public void setGameMode(boolean mIsGameMode){
        this.mIsGameMode = mIsGameMode;
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(BaseSdkController.MSG_ON_ORIENT_PORTRAIT);
        registerAction(BaseSdkController.MSG_ON_ORIENT_LANDSCAPE);
        registerAction(BaseSdkController.MSG_BOTTOM_POPUP_SHOWED);
        registerAction(BaseSdkController.MSG_BOTTOM_POPUP_HIDDEN);
        registerAction(BaseSdkController.MSG_INPUT_VIEW_SHOWED);
        registerAction(BaseSdkController.MSG_INPUT_VIEW_HIDDEN);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        unregisterAllAction();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mView != null) {
            mView.destroy();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(CommentRefreshEvent event) {
        MyLog.d(TAG, "CommentRefreshEvent ");
        if (mView != null) {
            mView.onCommentRefreshEvent(event);
        }
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        if (mView == null) {
            MyLog.e(TAG, "onAction but mView is null, event=" + event);
            return false;
        }
        switch (event) {
            case BaseSdkController.MSG_INPUT_VIEW_SHOWED:
                mIsInputAreaShow = true;
                if(!mIsLandscape){
                    //高度调小
                    mView.dropHeight(true);
                }
                break;
            case BaseSdkController.MSG_INPUT_VIEW_HIDDEN:
                mIsInputAreaShow = false;
                if(!mIsLandscape){
                    //恢复正常
                    mView.dropHeight(false);
                }
                break;
            case BaseSdkController.MSG_ON_ORIENT_PORTRAIT:
                mView.onOrientation(false);
                mIsLandscape = false;
                return true;
            case BaseSdkController.MSG_ON_ORIENT_LANDSCAPE:
                mView.onOrientation(true);
                mIsLandscape = true;
                return true;
            case BaseSdkController.MSG_BOTTOM_POPUP_SHOWED:
                mView.getRealView().setVisibility(View.INVISIBLE);
                return true;
            case BaseSdkController.MSG_BOTTOM_POPUP_HIDDEN:
                if ( mIsGameMode && mIsLandscape){
                    mView.getRealView().setVisibility(View.GONE);
                }else{
                    mView.getRealView().setVisibility(View.VISIBLE);
                }

                return true;
            default:
                break;
        }
        return false;
    }
}
