package com.wali.live.watchsdk.bigturntable.presenter;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.base.log.MyLog;
import com.base.presenter.RxLifeCyclePresenter;
import com.mi.live.data.repository.GiftRepository;
import com.mi.live.data.repository.model.turntable.PrizeItemModel;
import com.wali.live.dao.Gift;
import com.wali.live.proto.BigTurnTableProto;
import com.wali.live.watchsdk.bigturntable.contact.BigTurnTableViewContact;
import com.wali.live.watchsdk.bigturntable.manager.LoadDrawableManager;

import java.util.HashMap;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.wali.live.watchsdk.bigturntable.TurnTableType.MODE_SMALL;

/**
 * Created by zhujianning on 18-7-11.
 */

public class BigTurnTableViewPresenter extends RxLifeCyclePresenter implements BigTurnTableViewContact.IPresenter {
    private static final String TAG = "BigTurnTableViewPresenter";

    private BigTurnTableViewContact.IView mIview;
    private HashMap<String, Bitmap> mBBmpMap = new HashMap<>();
    private HashMap<String, Bitmap> mSBmpMap = new HashMap<>();
    private BigTurnTableProto.TurntableType mMode = MODE_SMALL;
    private Subscription mLoadBmpsSubscribe;

    public BigTurnTableViewPresenter(BigTurnTableViewContact.IView iView) {
        this.mIview = iView;
    }


    @Override
    public void loadBmps(final List<PrizeItemModel> datas) {
        if(datas == null || datas.isEmpty()) {
            MyLog.d(TAG, "data is null");
            return;
        }

        if(mMode == MODE_SMALL) {
            if(!mSBmpMap.isEmpty()) {
                mIview.loadBmpsSuccess();
                return;
            }
        } else {
            if(!mBBmpMap.isEmpty()) {
                mIview.loadBmpsSuccess();
                return;
            }
        }

        //key由url+giftName或者银钻倍数组成
        mLoadBmpsSubscribe = Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                for (int i = 0; i < datas.size(); i++) {
                    PrizeItemModel itemModel = datas.get(i);
                    if (itemModel != null) {
                        Gift gift = GiftRepository.findGiftById(itemModel.getGiftId());
                        if (gift != null) {
                            Drawable drawable = LoadDrawableManager.getInstance().getDrawableByUrl(gift.getPicture());
                            if (drawable != null) {
                                Bitmap rotateBmp = getRotateBmp(drawable, datas.size(), i);
                                if (rotateBmp != null) {
                                    //key由url+giftName或者银钻倍数组成
                                    String key = gift.getPicture() +
                                            (itemModel.getGiftType() == BigTurnTableProto.GiftType.VIRTUAL_DIAMOND_VALUE ? itemModel.getTimes() : gift.getName());
                                    if(mMode == MODE_SMALL) {
                                        mSBmpMap.put(key, rotateBmp);
                                    } else {
                                        mBBmpMap.put(key, rotateBmp);
                                    }
                                }
                            }
                        }
                    }
                }

                HashMap map = (mMode == MODE_SMALL ? mSBmpMap : mBBmpMap);
                if (!map.isEmpty()) {
                    subscriber.onNext(true);
                } else {
                    subscriber.onError(new Exception("load drawable fail"));
                }

                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Boolean>bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.w(TAG, e);
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        mIview.loadBmpsSuccess();
                    }
                });
    }

    private Bitmap getRotateBmp(Drawable drawable, int size, int pos) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        int ww = bitmap.getWidth();
        int hh = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(1f, 1f);
        matrix.postRotate(360 / size * pos + 10);
        return Bitmap.createBitmap(bitmap, 0, 0, ww, hh,
                matrix, true);
    }

    public HashMap<String, Bitmap> getBmpMap() {
        return mMode == MODE_SMALL ? mSBmpMap : mBBmpMap;
    }

    public void changeMode(BigTurnTableProto.TurntableType mode) {
        this.mMode = mode;
    }

    @Override
    public void destroy() {
        super.destroy();
        if(mLoadBmpsSubscribe != null && !mLoadBmpsSubscribe.isUnsubscribed()) {
            mLoadBmpsSubscribe.unsubscribe();
        }
    }
}
