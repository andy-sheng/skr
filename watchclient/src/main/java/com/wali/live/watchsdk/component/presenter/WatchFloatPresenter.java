package com.wali.live.watchsdk.component.presenter;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.mi.live.data.push.IPushMsgProcessor;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.IEventPresenter;
import com.thornbirds.component.view.IEventView;
import com.thornbirds.component.view.IOrientationListener;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
import com.wali.live.watchsdk.component.presenter.panel.PkInfoPresenter;
import com.wali.live.watchsdk.component.view.panel.PkInfoPanel;

import java.lang.ref.WeakReference;

import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_ON_PK_START;
import static com.wali.live.component.BaseSdkController.MSG_ON_PK_STOP;

/**
 * Created by yangli on 2017/9/11.
 *
 * @module 观看页面浮层容器
 */
public class WatchFloatPresenter extends BaseSdkRxPresenter<RelativeLayout>
        implements IOrientationListener, IPushMsgProcessor {
    private static final String TAG = "WatchFloatPresenter";

    private WeakReference<PkInfoPresenter> mPkInfoPresenterRef;

    private RoomBaseDataModel mMyRoomData;

    private boolean mIsLandscape = false;

    @Override
    protected String getTAG() {
        return TAG;
    }

    protected static <T> T deRef(WeakReference<T> reference) {
        return reference != null ? reference.get() : null;
    }

    protected final void setupComponent(IEventView view, IEventPresenter presenter) {
        presenter.setView(view.getViewProxy());
        view.setPresenter(presenter);
    }

    protected final void setupHybridComponent(IEventPresenter presenter, View view) {
        presenter.setView(view);
    }

    public WatchFloatPresenter(
            @NonNull IEventController controller,
            @NonNull RoomBaseDataModel myRoomData) {
        super(controller);
        mMyRoomData = myRoomData;
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
        registerAction(MSG_ON_PK_START);
        registerAction(MSG_ON_PK_STOP);
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        unregisterAllAction();
    }

    private void showPkInfoPanel() {
        PkInfoPresenter presenter = deRef(mPkInfoPresenterRef);
        if (presenter == null) {
            presenter = new PkInfoPresenter(mController, mMyRoomData);
            PkInfoPanel panel = new PkInfoPanel(mView);
            setupComponent(panel, presenter);
            presenter.startPresenter();
            mPkInfoPresenterRef = new WeakReference<>(presenter);
        }
        presenter.startPresenter(mIsLandscape);
    }

    private void hidePkInfoPanel() {
        PkInfoPresenter presenter = deRef(mPkInfoPresenterRef);
        if (presenter != null) {
            presenter.stopPresenter();
        }
    }

    @Override
    public final void onOrientation(boolean isLandscape) {
        if (mIsLandscape == isLandscape) {
            return;
        }
        MyLog.w(TAG, "onOrientation isLandscape=" + isLandscape);
        mIsLandscape = isLandscape;
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        if (mView == null) {
            MyLog.e(TAG, "onAction but mView is null, event=" + event);
            return false;
        }
        switch (event) {
            case MSG_ON_ORIENT_PORTRAIT:
                onOrientation(false);
                return true;
            case MSG_ON_ORIENT_LANDSCAPE:
                onOrientation(true);
                return true;
            case MSG_ON_PK_START:
                showPkInfoPanel();
                return true;
            case MSG_ON_PK_STOP:
                hidePkInfoPanel();
                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public void process(BarrageMsg msg, RoomBaseDataModel roomBaseDataModel) {
        PkInfoPresenter presenter = deRef(mPkInfoPresenterRef);
        if (presenter == null) {
            return;
        }
        switch (msg.getMsgType()) {
            case BarrageMsgType.B_MSG_TYPE_NEW_PK_START: {
                BarrageMsg.PKInfoMessageExt pkInfoMessageExt = (BarrageMsg.PKInfoMessageExt) msg.getMsgExt();
                presenter.onPkStart(pkInfoMessageExt, msg.getSentTime(), mIsLandscape);
                break;
            }
            case BarrageMsgType.B_MSG_TYPE_NEW_PK_END: {
                BarrageMsg.PKEndInfoMessageExt pkEndInfoMessageExt = (BarrageMsg.PKEndInfoMessageExt) msg.getMsgExt();
                presenter.onPkEnd(pkEndInfoMessageExt);
                break;
            }
            case BarrageMsgType.B_MSG_TYPE_NEW_PK_SCORE: {
                BarrageMsg.PKInfoMessageExt pkScoreInfo = (BarrageMsg.PKInfoMessageExt) msg.getMsgExt();
                presenter.onPkScore(pkScoreInfo);
                break;
            }
            default:
                break;
        }
    }

    @Override
    public int[] getAcceptMsgType() {
        return new int[]{
                BarrageMsgType.B_MSG_TYPE_NEW_PK_START,
                BarrageMsgType.B_MSG_TYPE_NEW_PK_END,
                BarrageMsgType.B_MSG_TYPE_NEW_PK_SCORE
        };
    }
}
