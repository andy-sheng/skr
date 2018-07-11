package com.wali.live.watchsdk.bigturntable.presenter;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.presenter.RxLifeCyclePresenter;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.repository.GiftRepository;
import com.mi.live.data.repository.model.turntable.PrizeItemModel;
import com.mi.live.data.repository.model.turntable.TurnTableConfigModel;
import com.mi.live.data.repository.model.turntable.TurnTablePreConfigModel;
import com.wali.live.dao.Gift;
import com.wali.live.proto.BigTurnTableProto;
import com.wali.live.proto.LiveProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.bigturntable.api.BigTurnTableApi;
import com.wali.live.watchsdk.bigturntable.contact.BigTurnTableContact;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by zhujianning on 18-4-14.
 * 大转盘主播端presenter
 * http://wiki.n.miui.com/pages/viewpage.action?pageId=79762978
 * http://wiki.n.miui.com/pages/viewpage.action?pageId=25044158
 */

public class BigTurnTablePresenter extends RxLifeCyclePresenter implements BigTurnTableContact.IPresenter{
    private static final String TAG = "BigTurnTablePresenter";

    private BigTurnTableContact.IView mIview;
    private List<TurnTableConfigModel> mDataSources;
    private BigTurnTableProto.TurntableType mType = BigTurnTableProto.TurntableType.TYPE_128;

    public BigTurnTablePresenter(BigTurnTableContact.IView iView) {
        this.mIview = iView;
    }

    public BigTurnTableProto.TurntableType getType() {
        return mType;
    }

    public void setType(BigTurnTableProto.TurntableType type) {
        this.mType = type;
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void loadTurnTableDataByType(final long zuid, final String roomId) {
        MyLog.d(TAG, "loadTurnTableDataByType");
        Observable.create(new Observable.OnSubscribe<List<TurnTableConfigModel>>() {
            @Override
            public void call(Subscriber<? super List<TurnTableConfigModel>> subscriber) {
                LiveProto.GetRoomAttachmentRsp rsp = BigTurnTableApi.getTurnTableInfoReq(zuid, roomId);
                if(rsp != null && rsp.getRetCode() == ErrorCode.CODE_SUCCESS) {
                    List<BigTurnTableProto.TurntableConfig> turntableConfigList = rsp.getTurntableConfigList();
                    if(turntableConfigList != null && !turntableConfigList.isEmpty()) {
                        List<TurnTableConfigModel> list = transformData(turntableConfigList);
                        subscriber.onNext(list);
                    }
                    subscriber.onCompleted();

                } else {
                    subscriber.onError(new Exception("GetRoomAttachmentRsp == null"));
                }
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<List<TurnTableConfigModel>>bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Observer<List<TurnTableConfigModel>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, e);
                    }

                    @Override
                    public void onNext(List<TurnTableConfigModel> turnTableConfigModels) {
                        mDataSources = turnTableConfigModels;

                        for(TurnTableConfigModel model : mDataSources) {
                            if(model.getType() == mType) {
                                mIview.loadDataSuccess(model);
                                break;
                            }
                        }

                        for(TurnTableConfigModel model : mDataSources) {
                            if(model.getStatus() == 1) {
                                mIview.notifyOpenStatus(model.getType());
                            }
                        }
                    }
                });
    }

    private List<TurnTableConfigModel> transformData(List<BigTurnTableProto.TurntableConfig> turntableConfigList) {
        ArrayList<TurnTableConfigModel> list = new ArrayList<>();
        for(BigTurnTableProto.TurntableConfig data : turntableConfigList) {
            TurnTableConfigModel turnTableConfigModel = new TurnTableConfigModel(data);

            TurnTablePreConfigModel turnTablePreConfigModel = turnTableConfigModel.getTurnTablePreConfigModel();
            if(turnTablePreConfigModel != null) {
                List<PrizeItemModel> prizeItems = turnTablePreConfigModel.getPrizeItems();
                if(prizeItems != null && !prizeItems.isEmpty()) {
                    for(int i = 0; i < prizeItems.size(); i++) {
                        PrizeItemModel prizeItemModel = prizeItems.get(i);
                        if(!prizeItemModel.isCustom()) {
                            Gift giftById = GiftRepository.findGiftById(prizeItemModel.getGiftId());
                            if(giftById != null) {
                                prizeItemModel.setGiftPic(giftById.getPicture());
                            }
                        } else {
                            if(TextUtils.isEmpty(prizeItemModel.getCustomDes())) {
                                prizeItemModel.setCustomDes(GlobalData.app().getResources().getString(R.string.big_turn_host_hint_tips));
                            }
                        }
                    }
                }
            }
            list.add(turnTableConfigModel);
        }
        return list;
    }

    @Override
    public void open(long zuid, String roomId, final BigTurnTableProto.TurntableType type, String customDes) {
        BigTurnTableApi.startTurnTableReq(zuid, roomId, type, customDes)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<BigTurnTableProto.StartTurntableRsp>bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Observer<BigTurnTableProto.StartTurntableRsp>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, e);
                    }

                    @Override
                    public void onNext(BigTurnTableProto.StartTurntableRsp rsp) {
                        if(rsp != null && rsp.getRetCode() == ErrorCode.CODE_SUCCESS) {
                            mIview.openSuccess(type);
                        } else {
                            MyLog.d(TAG, "open Turn Table fail :" + rsp.getRetCode());
                            mIview.openFail();
                        }
                    }
                });
    }

    @Override
    public void close(long zuid, String roomId, final BigTurnTableProto.TurntableType type, final String inputTxt, final boolean needOpenOtherMode) {
        BigTurnTableApi.stopTurnTableReq(zuid, roomId, type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<BigTurnTableProto.StopTurntableRsp>bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Observer<BigTurnTableProto.StopTurntableRsp>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, e);
                    }

                    @Override
                    public void onNext(BigTurnTableProto.StopTurntableRsp rsp) {
                        if(rsp != null && rsp.getRetCode() == ErrorCode.CODE_SUCCESS) {
                            mIview.closeSuccess(type, inputTxt, needOpenOtherMode);
                        } else {
                            mIview.closeFail();
                        }
                    }
                });
    }

    @Override
    public void switchMode(BigTurnTableProto.TurntableType type) {
        for(TurnTableConfigModel model : mDataSources) {
            if(model.getType() == type) {
                mIview.loadDataSuccess(model);
                break;
            }
        }
    }
}
