package com.wali.live.watchsdk.fans.presenter;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.base.mvp.IRxView;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.fans.model.notification.ApplyJoinFansModel;
import com.wali.live.watchsdk.fans.model.notification.GroupNotifyBaseModel;
import com.wali.live.watchsdk.fans.push.GroupNotifyLocalStore;
import com.wali.live.watchsdk.fans.push.data.FansNotifyRepository;
import com.wali.live.watchsdk.fans.push.event.GroupNotifyUpdateEvent;

import org.greenrobot.eventbus.EventBus;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by zyh on 2017/11/26.
 */

public class GroupNotifyDetailPresenter extends BaseRxPresenter<GroupNotifyDetailPresenter.IView> {

    public GroupNotifyDetailPresenter(IView view) {
        super(view);
    }

    public void handleJoinGroup(final ApplyJoinFansModel model, final boolean agree) {
        final VFansCommonProto.ApplyJoinResult resultType = agree ? VFansCommonProto.ApplyJoinResult.PASS :
                VFansCommonProto.ApplyJoinResult.REFUSE;
        Observable.just(0)
                .map(new Func1<Integer, VFansProto.HandleJoinGroupRsp>() {
                    @Override
                    public VFansProto.HandleJoinGroupRsp call(Integer integer) {
                        return FansNotifyRepository.handleJoinGroup(model.getGroupId(),
                                UserAccountManager.getInstance().getUuidAsLong(),
                                model.getCandidate(), resultType, false, model.getId());
                    }
                }).subscribeOn(Schedulers.io())
                .compose(mView.<VFansProto.HandleJoinGroupRsp>bindUntilEvent())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<VFansProto.HandleJoinGroupRsp>() {
                    @Override
                    public void call(VFansProto.HandleJoinGroupRsp rsp) {
                        if (rsp != null) {
                            if (rsp.getErrCode() == ErrorCode.CODE_SUCCESS) {
                                if (mView != null && GroupNotifyLocalStore.getInstance().
                                        deleteAllApplyJoinAndInsert(model.getCandidate(),
                                                model.getGroupId(), resultType)) {
                                    mView.onHandleSuccess(model.toHandleJoinFansGroupNotifyModel(resultType));
                                    //TODO 先简单点，一发现有数据更新，抛出事件，这个事件带着所有通知。
                                    GroupNotifyUpdateEvent event = GroupNotifyLocalStore.getInstance()
                                            .getGroupNotifyBaseModelListEventFromDB();
                                    EventBus.getDefault().post(event);
                                }
                            } else if (rsp.getErrCode() == ErrorCode.CODE_HANDEL_JOIN_NOTIFY) {
                                //已经处理过的消息直接删除
                                GroupNotifyLocalStore.getInstance().delete(model);
                            } else if (!TextUtils.isEmpty(rsp.getErrMsg())) {
                                ToastUtils.showToast(rsp.getErrMsg());
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "handleJoinGroup agree=" + agree + " failed " + throwable);
                    }
                });
    }

    public interface IView extends IRxView {
        void onHandleSuccess(GroupNotifyBaseModel agree);
    }
}
