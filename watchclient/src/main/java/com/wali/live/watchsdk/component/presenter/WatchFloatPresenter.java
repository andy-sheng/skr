package com.wali.live.watchsdk.component.presenter;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.mi.live.data.push.IPushMsgProcessor;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgExt;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.Params;
import com.thornbirds.component.presenter.IEventPresenter;
import com.thornbirds.component.view.IEventView;
import com.thornbirds.component.view.IOrientationListener;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
import com.wali.live.watchsdk.component.presenter.panel.LinkInfoPresenter;
import com.wali.live.watchsdk.component.presenter.panel.PkInfoPresenter;
import com.wali.live.watchsdk.component.view.panel.LinkInfoPanel;
import com.wali.live.watchsdk.component.view.panel.PkInfoPanel;

import java.lang.ref.WeakReference;

import static com.wali.live.component.BaseSdkController.MSG_ON_LINK_MIC_START;
import static com.wali.live.component.BaseSdkController.MSG_ON_LINK_MIC_STOP;
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
    private WeakReference<LinkInfoPresenter> mLinkInfoPresenterRef;

    private Handler mUiHandler = new Handler();

    private RoomBaseDataModel mMyRoomData;

    private boolean mIsLandscape = false;

    private Runnable mEndPkDelayTask = new Runnable() {
        @Override
        public void run() {
            PkInfoPresenter presenter = deRef(mPkInfoPresenterRef);
            if (presenter != null && presenter.isShow() && !presenter.isResulting()) {
                mController.postEvent(MSG_ON_PK_STOP);
                presenter.stopPresenter();
            }
        }
    };

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
        registerAction(MSG_ON_LINK_MIC_START);
        registerAction(MSG_ON_LINK_MIC_STOP);
        registerAction(MSG_ON_PK_START);
        registerAction(MSG_ON_PK_STOP);
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        unregisterAllAction();
        mUiHandler.removeCallbacksAndMessages(null);
    }

    private void showLinkInfoPanel(long userId, String userRoomId) {
        LinkInfoPresenter presenter = deRef(mLinkInfoPresenterRef);
        if (presenter == null) {
            presenter = new LinkInfoPresenter(mController, mMyRoomData);
            LinkInfoPanel panel = new LinkInfoPanel(mView);
            panel.setLayoutRatio(LinkInfoPanel.DEFAULT_RATIO);
            setupComponent(panel, presenter);
            mLinkInfoPresenterRef = new WeakReference<>(presenter);
        }
        presenter.startPresenter();
        presenter.onLinkStart(userId, userRoomId, mIsLandscape);
    }

    private void showPkInfoPanel(BarrageMsgExt.PkStartInfo pkStartInfo) {
        PkInfoPresenter presenter = deRef(mPkInfoPresenterRef);
        if (presenter == null) {
            presenter = new PkInfoPresenter(mController, mMyRoomData);
            PkInfoPanel panel = new PkInfoPanel(mView);
            setupComponent(panel, presenter);
            presenter.startPresenter();
            mPkInfoPresenterRef = new WeakReference<>(presenter);
        }
        presenter.startPresenter();
        presenter.onPkStart(pkStartInfo, mIsLandscape);
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
            case MSG_ON_LINK_MIC_START: {
                BarrageMsgExt.MicBeginInfo micBeginInfo = params.getItem(0);
                if (micBeginInfo != null && micBeginInfo.isMicAnchor()) {
                    showLinkInfoPanel(micBeginInfo.micuid, micBeginInfo.micLiveId);
                }
                break;
            }
            case MSG_ON_LINK_MIC_STOP: {
                BarrageMsgExt.MicEndInfo micEndInfo = params.getItem(0);
                LinkInfoPresenter presenter = deRef(mLinkInfoPresenterRef);
                if (presenter != null && micEndInfo != null && micEndInfo.isMicAnchor() && presenter.isShow()) {
                    presenter.onLinkStop();
                    presenter.stopPresenter();
                    // 连麦结束时，确保PK也被关闭
                    PkInfoPresenter presenter1 = deRef(mPkInfoPresenterRef);
                    if (presenter1 != null && presenter1.isShow() && !presenter1.isResulting()) {
                        mUiHandler.removeCallbacks(mEndPkDelayTask);
                        mUiHandler.postDelayed(mEndPkDelayTask, 5000);
                    }
                }
                break;
            }
            case MSG_ON_PK_START: {
                mUiHandler.removeCallbacks(mEndPkDelayTask);
                showPkInfoPanel((BarrageMsgExt.PkStartInfo) params.getItem(0));
                return true;
            }
            case MSG_ON_PK_STOP: {
                if (params == null) {
                    break;
                }
                BarrageMsgExt.PkEndInfo pkEndInfo = params.getItem(0);
                PkInfoPresenter presenter = deRef(mPkInfoPresenterRef);
                if (pkEndInfo != null && presenter != null) {
                    LinkInfoPresenter linkInfoPresenter = deRef(mLinkInfoPresenterRef);
                    if (linkInfoPresenter != null && linkInfoPresenter.getLinkUser() != null) {
                        pkEndInfo.setNickName(linkInfoPresenter.getLinkUser());
                    }
                    pkEndInfo.setNickName(mMyRoomData.getUser());
                    presenter.onPkEnd(pkEndInfo);
                }
                break;
            }
            default:
                break;
        }
        return false;
    }

    @Override
    public void process(final BarrageMsg msg, RoomBaseDataModel roomBaseDataModel) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                switch (msg.getMsgType()) {
                    case BarrageMsgType.B_MSG_TYPE_LINE_MIC_BEGIN: {
                        BarrageMsgExt.MicBeginInfo micBeginInfo = (BarrageMsgExt.MicBeginInfo) msg.getMsgExt();
                        if (micBeginInfo != null) {
                            mController.postEvent(MSG_ON_LINK_MIC_START, new Params().putItem(micBeginInfo));
                        }
                        break;
                    }
                    case BarrageMsgType.B_MSG_TYPE_LINE_MIC_END: {
                        BarrageMsgExt.MicEndInfo micEndInfo = (BarrageMsgExt.MicEndInfo) msg.getMsgExt();
                        if (micEndInfo != null) {
                            mController.postEvent(MSG_ON_LINK_MIC_STOP, new Params().putItem(micEndInfo));
                        }
                        break;
                    }
                    case BarrageMsgType.B_MSG_TYPE_NEW_PK_SCORE: {
                        MyLog.d(TAG, "B_MSG_TYPE_NEW_PK_SCORE");
                        BarrageMsgExt.PkScoreInfo pkScoreInfo = (BarrageMsgExt.PkScoreInfo) msg.getMsgExt();
                        if (pkScoreInfo != null) {
                            PkInfoPresenter presenter = deRef(mPkInfoPresenterRef);
                            if (presenter != null && presenter.isShow()) {
                                presenter.onPkScore(pkScoreInfo);
                            } else {
                                showPkInfoPanel(new BarrageMsgExt.PkStartInfo().parseFromScoreInfo(
                                        pkScoreInfo, msg.getSentTime()));
                            }
                        }
                        break;
                    }
                    case BarrageMsgType.B_MSG_TYPE_NEW_PK_START: {
                        MyLog.w(TAG, "B_MSG_TYPE_NEW_PK_START");
                        BarrageMsgExt.PkStartInfo pkStartInfo = (BarrageMsgExt.PkStartInfo) msg.getMsgExt();
                        if (pkStartInfo != null) {
                            showPkInfoPanel(pkStartInfo);
                        }
                        break;
                    }
                    case BarrageMsgType.B_MSG_TYPE_NEW_PK_END: {
                        MyLog.w(TAG, "B_MSG_TYPE_NEW_PK_END");
                        BarrageMsgExt.PkEndInfo pkEndInfo = (BarrageMsgExt.PkEndInfo) msg.getMsgExt();
                        if (pkEndInfo != null) {
                            mController.postEvent(MSG_ON_PK_STOP, new Params().putItem(pkEndInfo));
                        }
                        break;
                    }
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public int[] getAcceptMsgType() {
        return new int[]{
                BarrageMsgType.B_MSG_TYPE_LINE_MIC_BEGIN,
                BarrageMsgType.B_MSG_TYPE_LINE_MIC_END,
                BarrageMsgType.B_MSG_TYPE_NEW_PK_START,
                BarrageMsgType.B_MSG_TYPE_NEW_PK_END,
                BarrageMsgType.B_MSG_TYPE_NEW_PK_SCORE
        };
    }
}
