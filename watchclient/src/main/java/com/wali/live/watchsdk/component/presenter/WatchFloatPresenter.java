package com.wali.live.watchsdk.component.presenter;

import android.os.Handler;
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
import com.wali.live.watchsdk.component.presenter.panel.LinkInfoPresenter;
import com.wali.live.watchsdk.component.presenter.panel.PkInfoPresenter;
import com.wali.live.watchsdk.component.view.panel.LinkInfoPanel;
import com.wali.live.watchsdk.component.view.panel.PkInfoPanel;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;

import static com.wali.live.component.BaseSdkController.MSG_ON_LINK_MIC_START;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_ON_PK_START;

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
        registerAction(MSG_ON_PK_START);

        // TEST PK/主播-主播连麦 观众端逻辑暴力测试
        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                startPkTest();
//                startLinkTest();
            }
        }, 1000);
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        unregisterAllAction();
        mUiHandler.removeCallbacksAndMessages(null);
    }

    // TEST PK观众端逻辑暴力测试
//    private void startPkTest() {
//        final Random random = new Random();
//        Observable.interval(1, 1, TimeUnit.SECONDS)
//                .onBackpressureBuffer()
//                .take(960)
//                .compose(this.<Long>bindUntilEvent(PresenterEvent.STOP))
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<Long>() {
//                    long uuid1, uuid2;
//                    long score1, score2;
//                    int remainTime;
//                    String pkType;
//
//                    @Override
//                    public void onCompleted() {
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        MyLog.e(TAG, "pkTest failed, exception=" + e);
//                    }
//
//                    @Override
//                    public void onNext(Long cnt) {
//                        MyLog.d(TAG, "pkTest cnt=" + cnt);
//                        if (cnt % 10 == 0) { // 每10秒随机发送一次开始/结束
//                            // 若当前没有PK，则以50%的概率，否则以20%的概率，决定是否发送开始事件
//                            if (uuid1 == 0 && random.nextBoolean() || uuid1 != 0 && random.nextInt(100) < 20) { // 开始PK
//                                pkType = new String[]{"唱歌", "舞蹈", "吹牛逼", "讲段子"}[Math.abs(random.nextInt()) % 4];
//                                remainTime = new int[]{180, 300, 900}[Math.abs(random.nextInt()) % 3];
//                                uuid1 = mMyRoomData.getUid();
//                                uuid2 = (uuid1 != 100067) ? 100067 : 100068;
//                                if (random.nextBoolean()) { // 进房间之后PK才开始
//                                    score1 = score2 = 0;
//                                } else { // // 进房间之前PK已经开始
//                                    score1 = Math.abs(random.nextInt()) % 1000;
//                                    score2 = Math.abs(random.nextInt()) % 1000;
//                                    remainTime = (int) (remainTime * random.nextFloat());
//                                }
//                                showPkPanel(new PkInfoPresenter.PkStartInfo(uuid1, uuid2, score1, score2, pkType, remainTime));
//                                return;
//                            }
//                            // 若当前正在PK，则以50%的概率，否则以20%的概率，决定是否发送结束事件
//                            if (uuid1 != 0 && random.nextBoolean() || uuid1 == 0 && random.nextInt(100) < 20) { // 结束PK
//                                PkInfoPresenter presenter = deRef(mPkInfoPresenterRef);
//                                if (presenter != null && presenter.isShow()) {
//                                    if (random.nextBoolean()) { // 提前结束
//                                        long quitUuid = random.nextBoolean() ? uuid1 : uuid2;
//                                        presenter.onPkEnd(new PkInfoPresenter.PkEndInfo(uuid1, uuid2, score1, score2, quitUuid));
//                                    } else {
//                                        if (random.nextBoolean()) { // 平局
//                                            score1 = score2 = Math.max(score1, score2);
//                                        }
//                                        presenter.onPkEnd(new PkInfoPresenter.PkEndInfo(uuid1, uuid2, score1, score2, 0));
//                                    }
//                                }
//                                uuid1 = uuid2 = 0;
//                                score1 = score2 = 0;
//                                remainTime = 0;
//                                pkType = null;
//                            }
//                            return;
//                        }
//                        // 每1秒，若当前正在Pk，则以60%的概率，否则以20%的概率，决定是否发送一次比分更新
//                        if (uuid1 != 0 && random.nextInt(100) <= 60 || uuid1 == 0 && random.nextInt(100) <= 20) {
//                            if (uuid1 == 0) {
//                                pkType = new String[]{"唱歌", "舞蹈", "吹牛逼"}[Math.abs(random.nextInt()) % 3];
//                                remainTime = new int[]{180, 300, 900}[Math.abs(random.nextInt()) % 3];
//                                uuid1 = mMyRoomData.getUid();
//                                uuid2 = (uuid1 != 100067) ? 100067 : 100068;
//                                score1 = Math.abs(random.nextInt()) % 1000;
//                                score2 = Math.abs(random.nextInt()) % 1000;
//                                remainTime = (int) (remainTime * random.nextFloat());
//                            } else {
//                                score1 += Math.abs(random.nextInt()) % 100;
//                                score2 += Math.abs(random.nextInt()) % 100;
//                            }
//                            PkInfoPresenter presenter = deRef(mPkInfoPresenterRef);
//                            if (presenter != null && presenter.isShow()) {
//                                presenter.onPkScore(new PkInfoPresenter.PkScoreInfo(uuid1, uuid2, score1, score2));
//                            } else {
//                                showPkPanel(new PkInfoPresenter.PkStartInfo(uuid1, uuid2, score1, score2, pkType, remainTime));
//                            }
//                        }
//                    }
//                });
//    }

    // TEST 主播-主播连麦观众端逻辑暴力测试
    private void startLinkTest() {
        Observable.interval(1, 1, TimeUnit.SECONDS)
                .onBackpressureBuffer()
                .take(600)
                .compose(this.<Long>bindUntilEvent(PresenterEvent.STOP))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    long userId;
                    String nickName;

                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, "linkTest failed, exception=" + e);
                    }

                    @Override
                    public void onNext(Long cnt) {
                        MyLog.d(TAG, "linkTest cnt=" + cnt);
                        if (cnt % 10 == 0) { // 每10秒发送一次开始/结束
                            if (userId == 0) {
                                userId = 100067;
                                nickName = "游不动的鱼";
                                showLinkInfoPanel(userId, nickName);
                            } else {
                                LinkInfoPresenter presenter = deRef(mLinkInfoPresenterRef);
                                if (presenter != null && presenter.isShow()) {
                                    presenter.onLinkStop();
                                }
                                userId = 0;
                                nickName = "";
                            }
                        }
                    }
                });
    }

    private void showPkInfoPanel(PkInfoPresenter.PkStartInfo pkStartInfo) {
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

    private void showLinkInfoPanel(long userId, String userName) {
        LinkInfoPresenter presenter = deRef(mLinkInfoPresenterRef);
        if (presenter == null) {
            presenter = new LinkInfoPresenter(mController);
            LinkInfoPanel panel = new LinkInfoPanel(mView);
            panel.setLayoutRatio(LinkInfoPanel.DEFAULT_RATIO);
            setupComponent(panel, presenter);
            mLinkInfoPresenterRef = new WeakReference<>(presenter);
        }
        presenter.startPresenter();
        presenter.onLinkStart(userId, userName, mIsLandscape);
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
            case MSG_ON_LINK_MIC_START:
                showLinkInfoPanel(100067, "游不动的鱼");
                break;
            case MSG_ON_PK_START:
                showPkInfoPanel((PkInfoPresenter.PkStartInfo) params.getItem(0));
                return true;
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
                    case BarrageMsgType.B_MSG_TYPE_LINE_MIC_BEGIN:
                        showLinkInfoPanel(100067, "xfdfsdfs");
                        break;
                    case BarrageMsgType.B_MSG_TYPE_LINE_MIC_END: {
                        LinkInfoPresenter presenter = deRef(mLinkInfoPresenterRef);
                        if (presenter != null) {
                            presenter.onLinkStop();
                        }
                        break;
                    }
                    case BarrageMsgType.B_MSG_TYPE_NEW_PK_START: {
                        MyLog.w(TAG, "B_MSG_TYPE_NEW_PK_START");
                        BarrageMsg.PKInfoMsgExt msgExt = (BarrageMsg.PKInfoMsgExt) msg.getMsgExt();
                        if (msgExt.info != null) {
                            showPkInfoPanel(new PkInfoPresenter.PkStartInfo(msgExt.info, msg.getSentTime()));
                        }
                        break;
                    }
                    case BarrageMsgType.B_MSG_TYPE_NEW_PK_SCORE: {
                        MyLog.d(TAG, "B_MSG_TYPE_NEW_PK_SCORE");
                        BarrageMsg.PKInfoMsgExt msgExt = (BarrageMsg.PKInfoMsgExt) msg.getMsgExt();
                        if (msgExt.info != null) {
                            PkInfoPresenter presenter = deRef(mPkInfoPresenterRef);
                            if (presenter != null && presenter.isShow()) {
                                presenter.onPkScore(new PkInfoPresenter.PkScoreInfo(msgExt.info));
                            } else {
                                showPkInfoPanel(new PkInfoPresenter.PkStartInfo(msgExt.info, msg.getSentTime()));
                            }
                        }
                        break;
                    }
                    case BarrageMsgType.B_MSG_TYPE_NEW_PK_END: {
                        MyLog.w(TAG, "B_MSG_TYPE_NEW_PK_END");
                        BarrageMsg.PKEndMsgExt msgExt = (BarrageMsg.PKEndMsgExt) msg.getMsgExt();
                        PkInfoPresenter presenter = deRef(mPkInfoPresenterRef);
                        if (msgExt.info != null && presenter != null) { // msgExt.endType为1时表示PK提前结束
                            presenter.onPkEnd(new PkInfoPresenter.PkEndInfo(msgExt.info, msgExt.endType == 1 ? msgExt.uuid : 0));
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
