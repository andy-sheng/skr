package com.wali.live.watchsdk.component.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.rx.RefuseRetryExeption;
import com.base.utils.rx.RxRetryAssist;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.event.GiftEventClass;
import com.mi.live.data.gift.redenvelope.RedEnvelopeModel;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.common.gift.exception.GiftErrorCode;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.proto.RedEnvelProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.adapter.WinnerItemAdapter;
import com.wali.live.watchsdk.component.utils.EnvelopeUtils;
import com.wali.live.watchsdk.component.view.EnvelopeResultView;
import com.wali.live.watchsdk.component.view.EnvelopeView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.wali.live.component.ComponentController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.ComponentController.MSG_ON_ORIENT_PORTRAIT;

/**
 * Created by yangli on 2017/07/12.
 *
 * @module 抢红包表现
 */
public class EnvelopePresenter extends ComponentPresenter<RelativeLayout>
        implements EnvelopeView.IPresenter, EnvelopeResultView.IPresenter {
    private static final String TAG = "EnvelopePresenter";

    private static final int MAX_ENVELOPE_CACHE_CNT = 2;

    private final LinkedList<EnvelopeInfo> mEnvelopeInfoList = new LinkedList<>(); // 当前收到的且未关闭的所有红包，最近收到的在最前面
    private final LinkedList<EnvelopeView> mEnvelopeViewList = new LinkedList<>(); // 当前展示的红包列表，最多为MAX_ENVELOPE_CACHE_CNT
    private EnvelopeResultView mEnvelopeResultView; // 展示红包抽取结果

    private RoomBaseDataModel mMyRoomData;
    protected boolean mIsLandscape = false;

    public EnvelopePresenter(
            @NonNull IComponentController componentController,
            @NonNull RoomBaseDataModel myRoomData) {
        super(componentController);
        mMyRoomData = myRoomData;
        startPresenter();
    }

    @Override
    public void setComponentView(@Nullable RelativeLayout relativeLayout) {
        super.setComponentView(relativeLayout);
        relativeLayout.setSoundEffectsEnabled(false);
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 吃点点击事件
            }
        });
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
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
            addNewEnvelope(event.red);
        }
    }

    // 收到新红包
    private void addNewEnvelope(@NonNull RedEnvelopeModel redEnvelopeModel) {
        String envelopeId = redEnvelopeModel.getRedEnvelopeId();
        MyLog.w(TAG, "addNewEnvelope envelopeId=" + envelopeId);
        if (TextUtils.isEmpty(envelopeId)) {
            return;
        }
        EnvelopeInfo envelopeInfo = new EnvelopeInfo(redEnvelopeModel);
        mEnvelopeInfoList.addFirst(envelopeInfo);
        updateEnvelopeView(true);
    }

    @Override
    public void removeEnvelope(EnvelopeInfo envelopeInfo) {
        if (envelopeInfo == null) {
            MyLog.w(TAG, "removeEnvelope, but envelopeInfo is null");
            return;
        }
        mEnvelopeInfoList.remove(envelopeInfo);
        updateEnvelopeView(false);
    }

    @Override
    public void grabEnvelope(EnvelopeInfo envelopeInfo) {
        if (envelopeInfo == null) {
            MyLog.w(TAG, "removeEnvelope, but grabEnvelope is null");
            return;
        }
        if (envelopeInfo.state == EnvelopeInfo.STATE_GRABBING) {
            return;
        } else if (envelopeInfo.state == EnvelopeInfo.STATE_GRAB_SUCCESS) {
            updateEnvelopeView(false);
            return;
        }
        envelopeInfo.state = EnvelopeInfo.STATE_GRABBING;
        doGrabEnvelope(envelopeInfo);
    }

    private void doGrabEnvelope(final EnvelopeInfo envelopeInfo) {
        final String netTips = GlobalData.app().getString(R.string.net_is_busy_tip);
        Observable.just(envelopeInfo.getEnvelopeId())
                .flatMap(new Func1<String, Observable<?>>() {
                    @Override
                    public Observable<?> call(String envelopeId) {
                        RedEnvelProto.GrabEnvelopRsp envelopRsp = EnvelopeUtils.grabRedEnvelope(envelopeId);
                        if (envelopRsp == null) {
                            MyLog.w(TAG, "grabEnvelope failed, rsp is null");
                            return Observable.error(new RefuseRetryExeption(netTips));
                        } else if (envelopRsp.getRetCode() == GiftErrorCode.REDENVELOP_GAME_BUSY) {
                            MyLog.w(TAG, "grabEnvelope failed, rsp is null");
                            return Observable.error(new Exception(netTips));
                        }
                        return Observable.just(envelopRsp);
                    }
                })
                .retryWhen(new RxRetryAssist(1, netTips))
                .compose(bindUntilEvent(PresenterEvent.DESTROY))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mView != null) {
                            String msg = e.getMessage();
                            if (!TextUtils.isEmpty(msg)) {
                                ToastUtils.showToast(msg);
                            }
                            MyLog.w(TAG, "grabEnvelope failed, error:" + msg);
                            processGrabDone(envelopeInfo, null);
                        }
                    }

                    @Override
                    public void onNext(Object rsp) {
                        if (mView != null) {
                            processGrabDone(envelopeInfo, (RedEnvelProto.GrabEnvelopRsp) rsp);
                        }
                    }
                });
    }

    private void processGrabDone(
            @NonNull EnvelopeInfo envelopeInfo,
            @Nullable RedEnvelProto.GrabEnvelopRsp grabEnvelopRsp) {
        if (grabEnvelopRsp == null || (grabEnvelopRsp.getRetCode() != ErrorCode.CODE_SUCCESS
                && grabEnvelopRsp.getRetCode() != GiftErrorCode.REDENVELOP_HAS_DONE)) { // 加入这个判断，防止SDK与直播同一账号抢红包
            envelopeInfo.state = EnvelopeInfo.STATE_GRAB_FAILED;
        } else {
            envelopeInfo.state = EnvelopeInfo.STATE_GRAB_SUCCESS;
            envelopeInfo.grabCnt = grabEnvelopRsp.getGain();
        }
        updateEnvelopeView(false);
    }

    @Override
    public void syncEnvelopeDetail(final EnvelopeInfo envelopeInfo) {
        if (envelopeInfo == null) {
            MyLog.w(TAG, "syncEnvelopeDetail, but grabEnvelope is null");
            return;
        }
        final long anchorId = mMyRoomData.getUid();
        Observable.just(0)
                .map(new Func1<Integer, Object[]>() {
                    @Override
                    public Object[] call(Integer integer) {
                        RedEnvelProto.GetEnvelopRsp rsp = EnvelopeUtils.getRedEnvelope(envelopeInfo.getEnvelopeId(),
                                envelopeInfo.getRoomId(), System.currentTimeMillis());
                        if (rsp == null || rsp.getRetCode() != ErrorCode.CODE_SUCCESS) {
                            return null;
                        }
                        WinnerItemAdapter.WinnerItem anchorItem = null;
                        List<WinnerItemAdapter.WinnerItem> otherWinners = new ArrayList<>();
                        List<RedEnvelProto.Winner> winners = rsp.getWinnersList();
                        long bestId = winners.get(0).getUserId();
                        for (RedEnvelProto.Winner elem : winners) {
                            WinnerItemAdapter.WinnerItem winnerItem = new WinnerItemAdapter.WinnerItem(
                                    elem.getUserId(), elem.getNickname(), elem.getGain());
                            if (anchorId != 0 && anchorId == elem.getUserId()) {
                                anchorItem = winnerItem;
                            } else {
                                otherWinners.add(winnerItem);
                            }
                        }
                        return new Object[]{anchorItem, bestId, otherWinners};
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Object[]>bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Action1<Object[]>() {
                    @Override
                    public void call(Object[] result) {
                        if (mView == null) {
                            return;
                        }
                        if (result == null) {
                            ToastUtils.showToast(R.string.net_is_busy_tip);
                            return;
                        }
                        if (mEnvelopeResultView != null) {
                            mEnvelopeResultView.onEnvelopeDetail(
                                    envelopeInfo,
                                    (WinnerItemAdapter.WinnerItem) result[0],
                                    (long) result[1],
                                    (List<WinnerItemAdapter.WinnerItem>) result[2]);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "syncEnvelopeDetail failed, exception=" + throwable);
                    }
                });
    }

    private void updateEnvelopeView(boolean isAddNew) {
        if (mEnvelopeInfoList.isEmpty()) { // 当前没有红包了
            for (EnvelopeView envelopeView : mEnvelopeViewList) {
                envelopeView.hideSelf(false);
            }
            if (mEnvelopeResultView != null) {
                mEnvelopeResultView.hideSelf(false);
            }
        } else { // 当前有红包
            EnvelopeInfo currTopItem = mEnvelopeInfoList.getFirst();
            if (currTopItem.state == EnvelopeInfo.STATE_GRAB_SUCCESS) { // 显示红包结果页
                if (!mEnvelopeViewList.isEmpty()) { // 停止当前顶层View的动画
                    EnvelopeView envelopeView = mEnvelopeViewList.getFirst();
                    envelopeView.stopRotation();
                    if (envelopeView.getEnvelopeInfo() == currTopItem) {
                        envelopeView.hideSelf(false);
                        mEnvelopeViewList.removeFirst();
                    }
                }
                if (mEnvelopeResultView == null) {
                    mEnvelopeResultView = new EnvelopeResultView(mView);
                    mEnvelopeResultView.setPresenter(this);
                }
                mEnvelopeResultView.setEnvelopeInfo(currTopItem);
                mEnvelopeResultView.showSelf(false, mIsLandscape);
            } else { // 显示抢红包页
                if (mEnvelopeResultView != null) {
                    mEnvelopeResultView.hideSelf(false);
                }
                EnvelopeView envelopeView;
                if (isAddNew) {
                    envelopeView = mEnvelopeViewList.size() < MAX_ENVELOPE_CACHE_CNT ?
                            new EnvelopeView(mView) : mEnvelopeViewList.removeLast();
                    envelopeView.hideSelf(false);
                } else {
                    envelopeView = mEnvelopeViewList.isEmpty() ?
                            new EnvelopeView(mView) : mEnvelopeViewList.removeFirst();
                }
                envelopeView.setPresenter(this);
                envelopeView.setEnvelopeInfo(currTopItem);
                envelopeView.showSelf(isAddNew, mIsLandscape);
                if (!mEnvelopeViewList.isEmpty()) { // 停止前一个顶层View的动画
                    mEnvelopeViewList.getFirst().stopRotation();
                }
                if (currTopItem.state == EnvelopeInfo.STATE_GRABBING) { // 恢复当前顶层View的动画
                    envelopeView.startRotation();
                } else {
                    envelopeView.stopRotation();
                }
                mEnvelopeViewList.addFirst(envelopeView);
            }
        }
    }

    public void onOrientation(boolean isLandscape) {
        if (mIsLandscape == isLandscape) {
            return;
        }
        MyLog.w(TAG, "onOrientation isLandscape=" + isLandscape);
        mIsLandscape = isLandscape;
        for (EnvelopeView envelopeView : mEnvelopeViewList) {
            envelopeView.onOrientation(mIsLandscape);
        }
        if (mEnvelopeResultView != null && mEnvelopeResultView.isShow()) {
            mEnvelopeResultView.onOrientation(mIsLandscape);
        }
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
                case MSG_ON_ORIENT_PORTRAIT:
                    onOrientation(false);
                    return true;
                case MSG_ON_ORIENT_LANDSCAPE:
                    onOrientation(true);
                    return true;
                default:
                    break;
            }
            return false;
        }
    }

    public static class EnvelopeInfo {
        public static final int STATE_NEW = 0; // 新红包
        public static final int STATE_GRABBING = 1;  // 正在抢红包
        public static final int STATE_GRAB_SUCCESS = 2; // 抢红包成功，可能抢到大于等于0个钻石
        public static final int STATE_GRAB_FAILED = 3; // 抢红包失败了

        public RedEnvelopeModel envelopeModel;
        public int state = STATE_NEW;
        public int grabCnt = 0;

        public String getEnvelopeId() {
            return envelopeModel != null ? envelopeModel.getRedEnvelopeId() : null;
        }

        public String getRoomId() {
            return envelopeModel != null ? envelopeModel.getRoomId() : null;
        }

        public EnvelopeInfo(RedEnvelopeModel envelopeModel) {
            this.envelopeModel = envelopeModel;
        }
    }
}
