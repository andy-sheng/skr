package com.wali.live.watchsdk.component.presenter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.widget.EditText;

import com.base.dialog.MyAlertDialog;
import com.base.event.KeyboardEvent;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.data.LastBarrage;
import com.mi.live.data.push.event.BarrageMsgEvent;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.query.model.MessageRule;
import com.mi.live.data.repository.GiftRepository;
import com.mi.live.data.room.model.FansPrivilegeModel;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.common.barrage.manager.BarrageMessageManager;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.common.gift.exception.GiftErrorCode;
import com.wali.live.event.EventClass;
import com.wali.live.proto.GiftProto;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.proto.VFansProto;
import com.wali.live.recharge.view.RechargeFragment;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;
import com.wali.live.watchsdk.fans.request.GetGroupDetailRequest;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.wali.live.watchsdk.component.viewmodel.BarrageState.BARRAGE_MANAGE;
import static com.wali.live.watchsdk.component.viewmodel.BarrageState.BARRAGE_NORMAL;
import static com.wali.live.watchsdk.component.viewmodel.BarrageState.BARRAGE_NOTIFY;

/**
 * Created by zyh on 2017/7/28.
 *
 * @module 游戏和秀场输入框的基类, 主要放房间管理，禁言频率限制等操作
 */
public abstract class InputPresenter<VIEW extends InputPresenter.IView>
        extends ComponentPresenter<VIEW> {

    protected static final long CLEAR_BARRAGE_CACHE_INTERVAL = 12 * 60 * 60 * 1000;// 清理弹幕缓存的时间间隔
    protected static final int MSG_SEND_BARRAGE_COUNT_DOWN = 301;

    protected static final Map<String, LastBarrage> mLastBarrageMap = new HashMap<>();

    protected RoomBaseDataModel mMyRoomData;
    protected FansPrivilegeModel mFansPrivilegeModel;
    protected MyUIHandler mUIHandler;
    protected String mInputContent;
    protected boolean mCanInput;
    protected boolean mViewIsShow;
    protected CompositeSubscription mSubscriptions;
    protected LiveRoomChatMsgManager mLiveRoomChatMsgManager;
    protected int mBarrageState = BARRAGE_NORMAL;

    protected int mVipMaxCnt;
    protected int mVipCurCnt;
    protected int mManagerMaxCnt;
    protected int mManagerCurCnt;

    public InputPresenter(
            @NonNull IEventController controller,
            @NonNull RoomBaseDataModel myRoomData,
            LiveRoomChatMsgManager liveRoomChatMsgManager) {
        super(controller);
        mMyRoomData = myRoomData;
        mCanInput = true;
        mUIHandler = new MyUIHandler(this);
        mLiveRoomChatMsgManager = liveRoomChatMsgManager;
        mSubscriptions = new CompositeSubscription();
        clearBarrageCache();
    }

    @Override
    @CallSuper
    public void startPresenter() {
        super.startPresenter();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        getGroupDetailFromServer();
    }

    @Override
    @CallSuper
    public void stopPresenter() {
        super.stopPresenter();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        if (mSubscriptions != null) {
            mSubscriptions.clear();
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        mUIHandler.removeCallbacksAndMessages(null);
    }

    private void getGroupDetailFromServer() {
        Subscription subscription = Observable.just(0)
                .map(new Func1<Object, FansGroupDetailModel>() {
                    @Override
                    public FansGroupDetailModel call(Object object) {
                        if (mMyRoomData == null || mMyRoomData.getUid() <= 0) {
                            MyLog.e(TAG, "getGroupDetail null");
                            return null;
                        }
                        VFansProto.GroupDetailRsp rsp = new GetGroupDetailRequest(mMyRoomData.getUid()).syncRsp();
                        if (rsp != null && rsp.getErrCode() == ErrorCode.CODE_SUCCESS) {
                            return new FansGroupDetailModel(rsp);
                        }
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<FansGroupDetailModel>() {
                    @Override
                    public void call(FansGroupDetailModel groupDetailModel) {
                        if (groupDetailModel != null) {
                            if (mFansPrivilegeModel == null) {
                                mFansPrivilegeModel = new FansPrivilegeModel();
                            }
                            mFansPrivilegeModel.setMedal(groupDetailModel.getMedalValue());
                            mFansPrivilegeModel.setPetLevel(groupDetailModel.getMyPetLevel());
                            mFansPrivilegeModel.setExpireTime(groupDetailModel.getVipExpire());
                            mFansPrivilegeModel.setMemType(groupDetailModel.getMemType());
                            mFansPrivilegeModel.setVipLevel(groupDetailModel.getVipLevel());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "getGroupDetail failed=" + throwable);
                    }
                });
        mSubscriptions.add(subscription);
    }

    private void buyCostBarrage(final String msg) {
        Subscription subscription = Observable.just(msg)
                .map(new Func1<String, Object>() {
                    @Override
                    public Object call(String body) {
                        //TODO zyh 这里拿掉了@协议, 需要再加
                        return GiftRepository.bugGiftSync(GiftRepository.getBulletGift(),
                                mMyRoomData.getUid(), mMyRoomData.getRoomId(), 0,
                                System.currentTimeMillis(), System.currentTimeMillis(), body,
                                mMyRoomData.getLiveType(), false, false);
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        GiftProto.BuyGiftRsp rsp = (GiftProto.BuyGiftRsp) o;
                        if (rsp != null) {
                            switch (rsp.getRetCode()) {
                                case GiftErrorCode.SUCC:
                                    mLiveRoomChatMsgManager.sendFlyBarrageMessageAsync(msg, mMyRoomData.getRoomId(),
                                            mMyRoomData.getUid(), BarrageMsg.INNER_GLOBAL_PAY_HORN, null, mFansPrivilegeModel);
                                    MyUserInfoManager.getInstance().setDiamonds(rsp.getUsableGemCnt(), rsp.getUsableVirtualGemCnt());
                                    break;
                                case GiftErrorCode.GIFT_PAY_BARRAGE:
                                    showBalanceTipDialog();
                                    break;
                                default:
                                    ToastUtils.showToast(R.string.sns_unknown_error);
                                    break;
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "buyCostBarrage failed=" + throwable);
                    }
                });
        mSubscriptions.add(subscription);
    }

    private void showBalanceTipDialog() {
        if (mView == null) {
            return;
        }
        final Context context = mView.getRealView().getContext();
        MyAlertDialog dialog = new MyAlertDialog.Builder(context).create();
        dialog.setTitle(R.string.account_withdraw_pay_user_account_not_enough);
        dialog.setMessage(context.getString(R.string.account_withdraw_pay_barrage_user_account_not_enough_tip));
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.recharge),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RechargeFragment.openFragment((FragmentActivity) context, R.id.main_act_container, null, true);
                        dialog.dismiss();
                    }
                });
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * 是否是观众
     *
     * @return
     */
    private boolean isVisitor() {
        return mMyRoomData.getUid() != UserAccountManager.getInstance().getUuidAsLong();
    }

    public void sendBarrage(String msg, int barrageState) {
        if (TextUtils.isEmpty(msg) || mMyRoomData == null) {
            return;
        }
        if (isVisitor()) {
            if (!mMyRoomData.canSpeak()) {
                ToastUtils.showToast(GlobalData.app(), R.string.can_not_speak);
                return;
            }
            LastBarrage lastBarrage = mLastBarrageMap.get(getBarrageCacheKey(mMyRoomData.getRoomId()));
            long sendTime = System.currentTimeMillis();
            if (mMyRoomData.getMsgRule() != null && lastBarrage != null) {
                if (mMyRoomData.getMsgRule().isUnrepeatable() && lastBarrage.getLastSendContent() != null
                        && msg.trim().equals(lastBarrage.getLastSendContent())) {
                    MyLog.w(TAG, "sendMessage barrage repeated,last content:" + lastBarrage.getLastSendContent() + " body:" + msg.trim());
                    ToastUtils.showToast(GlobalData.app().getApplicationContext(), R.string.send_barrage_repeated);
                    return;
                }
                if (mMyRoomData.getMsgRule().getSpeakPeriod() != 0 && lastBarrage.getLastSendTime() > 0) {
                    if ((sendTime - lastBarrage.getLastSendTime()) < mMyRoomData.getMsgRule().getSpeakPeriod() * 1000) {
                        MyLog.w(TAG, "sendMessage barrage too frequent,interval:" + mMyRoomData.getMsgRule().getSpeakPeriod() +
                                "s senTime:" + sendTime + " last sendMessage time:" + lastBarrage.getLastSendTime());
                        return;
                    }
                }
            }
            //把此次发送的弹幕存入缓存
            if (mMyRoomData.getMsgRule() != null) {
                lastBarrage = lastBarrage == null ? new LastBarrage(new Date()) : lastBarrage;
                if (mMyRoomData.getMsgRule().isUnrepeatable())
                    lastBarrage.setLastSendContent(msg.trim());
                if (mMyRoomData.getMsgRule().getSpeakPeriod() > 0)
                    lastBarrage.setLastSendTime(sendTime);
                mLastBarrageMap.put(getBarrageCacheKey(mMyRoomData.getRoomId()), lastBarrage);
            }
            checkShowCountdownTimer();
        }
        mBarrageState = barrageState;
        switch (mBarrageState) {
            case BARRAGE_MANAGE:
                mLiveRoomChatMsgManager.sendFlyBarrageMessageAsync(msg, mMyRoomData.getRoomId(),
                        mMyRoomData.getUid(), BarrageMsg.INNER_GLOBAL_ADMIN_FLY, null, mFansPrivilegeModel);
                break;
            case BARRAGE_NOTIFY:
                if (mVipCurCnt < mVipMaxCnt && !MyUserInfoManager.getInstance().isVipFrozen()) {
                    mLiveRoomChatMsgManager.sendFlyBarrageMessageAsync(msg, mMyRoomData.getRoomId(),
                            mMyRoomData.getUid(), BarrageMsg.INNER_GLOBAL_PAY_HORN, null, mFansPrivilegeModel);
                } else {
                    buyCostBarrage(msg);
                }
                break;
            default:
                sendBarrageByType(msg, BarrageMsgType.B_MSG_TYPE_TEXT);
                break;
        }
    }

    private void sendBarrageByType(String msg, int type) {
        FansPrivilegeModel fansPrivilegeModel = mFansPrivilegeModel;
        BarrageMsg.GlobalRoomMessageExt globalRoomMsgExt = null;
        if (fansPrivilegeModel != null && fansPrivilegeModel.getMemType() != VFansCommonProto.GroupMemType.NONE.getNumber()) {
            globalRoomMsgExt = new BarrageMsg.GlobalRoomMessageExt();
            BarrageMsg.InnerGlobalRoomMessageExt ext = new BarrageMsg.InnerGlobalRoomMessageExt();
            ext.setType(BarrageMsg.INNER_GLOBAL_VFAN);
            BarrageMsg.VFansMemberBriefInfo vFansMemberBriefInfo = new BarrageMsg.VFansMemberBriefInfo();
            vFansMemberBriefInfo.setPetLevel(fansPrivilegeModel.getPetLevel());
            vFansMemberBriefInfo.setVipExpire(System.currentTimeMillis() > fansPrivilegeModel.getExpireTime() * 1000);
            vFansMemberBriefInfo.setMedalValue(fansPrivilegeModel.getMedal());
            ext.setvFansMemberBriefInfo(vFansMemberBriefInfo);
            globalRoomMsgExt.getInnerGlobalRoomMessageExtList().add(ext);
        }

        if(mMyRoomData.getLiveType() == LiveManager.TYPE_LIVE_HUYA){
            long huyaAnchorId = mMyRoomData.getHuyaInfo() == null ? 0 : mMyRoomData.getHuyaInfo().getUuid();
            mLiveRoomChatMsgManager.sendHuyaBarrageMessageAsync(msg, type,
                    mMyRoomData.getRoomId(), huyaAnchorId , mMyRoomData.getUid(), null, null, mMyRoomData.getLiveType(), globalRoomMsgExt, mMyRoomData.getHuyaInfo().getSource());
        }else{
            mLiveRoomChatMsgManager.sendBarrageMessageAsync(msg, type,
                    mMyRoomData.getRoomId(), mMyRoomData.getUid(), null, null, globalRoomMsgExt);
        }

    }

    /**
     * 获取发送弹幕的时间间隔
     *
     * @return
     */
    private int getSendBarrageInterval() {
        try {
            LastBarrage lastBarrage = mLastBarrageMap.get(getBarrageCacheKey(mMyRoomData.getRoomId()));
            long now = System.currentTimeMillis();
            if ((mMyRoomData.getMsgRule() != null && mMyRoomData.getMsgRule().getSpeakPeriod() != 0)
                    && (lastBarrage != null && lastBarrage.getLastSendTime() > 0)) {
                if ((now - lastBarrage.getLastSendTime()) < mMyRoomData.getMsgRule().getSpeakPeriod() * 1000) {
                    int interval = (int) (mMyRoomData.getMsgRule().getSpeakPeriod() - (now - lastBarrage.getLastSendTime()) / 1000);
                    return interval;
                }
            }
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
        return 0;
    }

    /**
     * 清理弹幕缓存
     */
    protected void clearBarrageCache() {
        try {
            Set<String> keySet = mLastBarrageMap.keySet();
            Date now = new Date();
            for (String key : keySet) {
                LastBarrage lastBarrage = mLastBarrageMap.get(key);
                if (lastBarrage != null && !key.equals(getBarrageCacheKey(mMyRoomData.getRoomId()))) {
                    if ((now.getTime() - lastBarrage.getCreateTime()) > CLEAR_BARRAGE_CACHE_INTERVAL) {
                        mLastBarrageMap.remove(key);
                        MyLog.w(TAG, "clear barrage cache,key:" + key);
                    }
                }
            }
        } catch (Exception e) {
            MyLog.e(TAG, "clear barrage cache error", e);
        }
    }

    private String getBarrageCacheKey(String roomId) {
        return roomId + UserAccountManager.getInstance().getUuidAsLong();
    }

    /**
     * 显示输入倒计时
     */
    protected void checkShowCountdownTimer() {
        try {
            if (isVisitor() && getSendBarrageInterval() > 0) {
                if (mCanInput == true) {
                    mCanInput = false;
                    mUIHandler.sendEmptyMessageDelayed(MSG_SEND_BARRAGE_COUNT_DOWN, 1000);
                }
            }
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
    }

    /**
     * 更新EditText的hint
     */
    public void updateInputHint(int barrageState) {
        MyLog.v(TAG, "updateInputHint");
        mBarrageState = barrageState;
        EditText editText = mView.getInputView();
        switch (mBarrageState) {
            case BARRAGE_NORMAL:
            case BARRAGE_MANAGE:
                editText.setHint(R.string.empty_edittext_hint);
                break;
            case BARRAGE_NOTIFY:
                updateVipHint();
                break;
        }
    }

    /**
     * vip和粉丝团vip的hint文案
     */
    private void updateVipHint() {
        EditText editText = mView.getInputView();
        if (mFansPrivilegeModel != null && mFansPrivilegeModel.canSendFlyBarrage() &&
                mFansPrivilegeModel.getMaxCanSendFlyBarrageTimes() - mFansPrivilegeModel.getHasSendFlyBarrageTimes() > 0) {
            if (mFansPrivilegeModel.getHasSendFlyBarrageTimes() == 0) {
                editText.setHint(GlobalData.app().getResources().getString(R.string.vfans_vip_horn_hint, mFansPrivilegeModel.getPetLevel(),
                        mFansPrivilegeModel.getMaxCanSendFlyBarrageTimes() - mFansPrivilegeModel.getHasSendFlyBarrageTimes()));
            } else {
                editText.setHint(GlobalData.app().getResources().getString(R.string.vfans_free_horn_hint,
                        mFansPrivilegeModel.getMaxCanSendFlyBarrageTimes() - mFansPrivilegeModel.getHasSendFlyBarrageTimes()));
            }
        } else if (!MyUserInfoManager.getInstance().isVipFrozen() && mVipMaxCnt > 0) {
            if (mVipCurCnt == 0) {
                editText.setHint(GlobalData.app().getString(R.string.vip_horn_hint,
                        MyUserInfoManager.getInstance().getVipLevel(), mVipMaxCnt).toString());
            } else {
                if (mVipCurCnt >= mVipMaxCnt) {
                    editText.setHint(GlobalData.app().getString(R.string.horn_barrage_hint, GiftRepository.getBulletGift().getPrice()));
                } else {
                    editText.setHint(GlobalData.app().getString(R.string.vip_free_horn_hint, mVipMaxCnt - mVipCurCnt));
                }
            }
        } else {
            editText.setHint(GlobalData.app().getString(R.string.horn_barrage_hint, GiftRepository.getBulletGift().getPrice()));
        }
    }

    /**
     * 处理发送弹幕频率限制更改事件
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.MsgRuleChangedEvent event) {
        if (event == null || !isVisitor()) {
            MyLog.w(TAG, "this is anchor, don't limit");
            return;
        }
        if (event.getRoomId() != null && mMyRoomData.getRoomId().equals(event.getRoomId())) {
            MessageRule msgRule = new MessageRule();
            msgRule.setMessageRuleType(MessageRule.MessageRuleType.NORMAL);
            msgRule.setSpeakPeriod(event.getSpeakPeriod());
            msgRule.setUnrepeatable(event.isUnrepeatable());
            mMyRoomData.setMsgRule(msgRule);

            LastBarrage lastBarrage = mLastBarrageMap.get(getBarrageCacheKey(mMyRoomData.getRoomId()));
            if (!event.isUnrepeatable()) {
                if (lastBarrage != null) {
                    lastBarrage.setLastSendContent(null);
                }
            }
            if (event.getSpeakPeriod() == 0) {
                if (lastBarrage != null && lastBarrage.getLastSendTime() > 0) {
                    lastBarrage.setLastSendTime(0);
                }
            } else if (event.getSpeakPeriod() > event.getOriSpeakPeriod()) {
                if (lastBarrage != null && lastBarrage.getLastSendTime() > 0 &&
                        (System.currentTimeMillis() - lastBarrage.getLastSendTime()) < event.getSpeakPeriod() * 1000) {
                    String text = mView.getInputView().getText().toString();
                    if (!TextUtils.isEmpty(text)) {
                        mInputContent = text;
                        mView.getInputView().setText("");
                    }
                }
            }
            if (mViewIsShow) {
                checkShowCountdownTimer();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(BarrageMsgEvent.SendBarrageResponseEvent event) {
        BarrageMsg msg = BarrageMessageManager.mSendingMsgCache.get(event.getCid());
        if (msg == null) {//表示之前发送的消息不是从这个房间发送的
            return;
        }
        BarrageMessageManager.mSendingMsgCache.remove(event.getCid());
        if (!msg.getRoomId().equals(mMyRoomData.getRoomId())) {
            return;
        }
        if (Integer.MAX_VALUE != event.getGuardCnt() && mFansPrivilegeModel.getHasSendFlyBarrageTimes() < event.getGuardCnt()) {
            mFansPrivilegeModel.setHasSendFlyBarrageTimes(event.getGuardCnt());
        }
        if (Integer.MAX_VALUE != event.getVipCnt() && mVipCurCnt < event.getVipCnt()) {
            mVipCurCnt = event.getVipCnt();
        }
        updateInputHint(mBarrageState);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(KeyboardEvent event) {
        MyLog.w(TAG, "KeyboardEvent eventType=" + event.eventType);
        if (mView == null) {
            MyLog.e(TAG, "KeyboardEvent but mView is null");
            return;
        }
        //TODO 这里时间不对称，在跳转到其他页面的时候会有问题
        switch (event.eventType) {
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_VISIBLE_ALWAYS_SEND:
                int keyboardHeight = Integer.parseInt(String.valueOf(event.obj1));
                mView.onKeyboardShowed(keyboardHeight);
                break;
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_HIDDEN:
                mView.onKeyboardHidden();
                break;
        }
    }

    public interface IPresenter {
        /**
         * 发送消息
         */
        void sendBarrage(String msg, int state);
    }

    public interface IView extends IViewProxy {
        /**
         * 获取输入框
         */
        EditText getInputView();

        /**
         * 键盘弹起
         */
        void onKeyboardShowed(int keyboardHeight);

        /**
         * 键盘隐藏
         */
        void onKeyboardHidden();
    }

    private static class MyUIHandler extends Handler {
        private WeakReference<InputPresenter<? extends IView>> mPresenter;

        public MyUIHandler(InputPresenter presenter) {
            mPresenter = new WeakReference<InputPresenter<? extends IView>>(presenter);
        }

        @Override
        public void handleMessage(Message msg) {
            InputPresenter<? extends IView> inputPresenter = mPresenter.get();
            if (inputPresenter == null) {
                return;
            }
            switch (msg.what) {
                case MSG_SEND_BARRAGE_COUNT_DOWN:
                    int interval = inputPresenter.getSendBarrageInterval();
                    if (interval > 0) {
                        inputPresenter.mView.getInputView().setHint(GlobalData.app().getString(R.string.send_barrage_interval, interval));
                        inputPresenter.mView.getInputView().setText("");
                        inputPresenter.mUIHandler.sendEmptyMessageDelayed(MSG_SEND_BARRAGE_COUNT_DOWN, 1000);
                        inputPresenter.mCanInput = false;
                    } else {
                        inputPresenter.mCanInput = true;
                        inputPresenter.updateInputHint(inputPresenter.mBarrageState);
                        if (!TextUtils.isEmpty(inputPresenter.mInputContent)) {
                            inputPresenter.mView.getInputView().setText(inputPresenter.mInputContent);
                            inputPresenter.mView.getInputView().setSelection(inputPresenter.mInputContent.length());
                            inputPresenter.mInputContent = "";
                        }
                    }
            }
        }
    }
}
