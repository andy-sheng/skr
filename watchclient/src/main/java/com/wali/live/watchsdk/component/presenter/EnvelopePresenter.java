package com.wali.live.watchsdk.component.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.mi.live.data.event.GiftEventClass;
import com.mi.live.data.gift.redenvelope.RedEnvelopeModel;
import com.wali.live.component.ComponentController;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.component.view.EnvelopeView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.LinkedList;

/**
 * Created by yangli on 2017/07/12.
 *
 * @module 抢红包表现
 */
public class EnvelopePresenter extends ComponentPresenter<RelativeLayout> {
    private static final String TAG = "EnvelopePresenter";

    private static final int MAX_ENVELOPE_CACHE_CNT = 5;

    protected boolean mIsLandscape = false;

    private final LinkedList<EnvelopeView> mEnvelopeViewSet = new LinkedList<>();

    public EnvelopePresenter(@NonNull IComponentController componentController) {
        super(componentController);
        startPresenter();
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(ComponentController.MSG_ON_ORIENT_PORTRAIT);
        registerAction(ComponentController.MSG_ON_ORIENT_LANDSCAPE);
        registerAction(ComponentController.MSG_ON_BACK_PRESSED);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = 1)
    public void onEventMainThread(GiftEventClass.GiftAttrMessage.RedEnvelope event) {
        if (event != null && event.red != null) {
            onNewRedEnvelope(event.red);
        }
    }

    private void onNewRedEnvelope(@NonNull RedEnvelopeModel redEnvelopeModel) {
        MyLog.w(TAG, "onNewRedEnvelope envelopeId=" + redEnvelopeModel.getRedEnvelopeId());
        EnvelopeView envelopeView;
        if (mEnvelopeViewSet.isEmpty() || mEnvelopeViewSet.getLast().isShow()) {
            envelopeView = new EnvelopeView(mView);
        } else {
            envelopeView = mEnvelopeViewSet.removeLast();
        }
        envelopeView.setEnvelopeModel(redEnvelopeModel);
        envelopeView.showSelf(true, mIsLandscape);
    }

    public void onOrientation(boolean isLandscape) {
        if (mIsLandscape == isLandscape) {
            return;
        }
        MyLog.w(TAG, "onOrientation isLandscape=" + isLandscape);
        mIsLandscape = isLandscape;
        for (EnvelopeView envelopeView : mEnvelopeViewSet) {
            if (envelopeView.isShow()) {
                envelopeView.onOrientation(mIsLandscape);
            }
        }
    }

    private boolean onBackPressed() {
        if (mEnvelopeViewSet.isEmpty() || !mEnvelopeViewSet.getFirst().isShow()) {
            return false;
        }
        EnvelopeView envelopeView = mEnvelopeViewSet.removeFirst();
        envelopeView.hideSelf(true);
        mEnvelopeViewSet.addLast(envelopeView);
        if (mEnvelopeViewSet.size() < MAX_ENVELOPE_CACHE_CNT) { // 缓存
            mEnvelopeViewSet.addLast(envelopeView);
        }
        return true;
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
                case ComponentController.MSG_ON_ORIENT_PORTRAIT:
                    onOrientation(false);
                    return true;
                case ComponentController.MSG_ON_ORIENT_LANDSCAPE:
                    onOrientation(true);
                    return true;
                case ComponentController.MSG_ON_BACK_PRESSED:
                    return onBackPressed();
                default:
                    break;
            }
            return false;
        }
    }
}
