package com.wali.live.common.gift.adapter;

import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.base.log.MyLog;
import com.jakewharton.rxbinding.view.RxView;
import com.live.module.common.R;
import com.wali.live.common.gift.adapter.viewHolder.GiftItemViewHolder;
import com.wali.live.common.gift.adapter.viewHolder.GiftSelectedHolder;
import com.wali.live.common.gift.presenter.GiftMallPresenter;
import com.wali.live.common.gift.view.GiftDisPlayItemLandView;
import com.wali.live.common.gift.view.GiftDisPlayItemLandWiderView;
import com.wali.live.common.gift.view.GiftDisPlayItemView;
import com.wali.live.common.gift.view.GiftSelectedView;
import com.wali.live.dao.Gift;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import rx.Observer;

/**
 * Created by zjn on 16-7-4.
 *
 * @module 礼物
 */
public class GiftDisplayRecycleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static String TAG = "GiftDisplayRecycleViewAdapter";

    private List<GiftMallPresenter.GiftWithCard> mDataSource = new ArrayList<>();
    private boolean mLandscape;

    private GiftSelectedView.SendGiftCallBack mSendGiftCallBack;

    public GiftDisplayRecycleViewAdapter(boolean landscape, GiftItemListener giftItemListener) {
        this.mLandscape = landscape;
        this.mGiftItemListener = giftItemListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mLandscape) {
            if (GiftMallPresenter.GiftWithCard.TYPE_SELECTED == viewType) {
                return new GiftSelectedHolder(new GiftSelectedView(parent.getContext(),mSendGiftCallBack));
            }else if(GiftMallPresenter.GiftWithCard.TYPE_SEND == viewType){
                GiftDisPlayItemLandWiderView view = new GiftDisPlayItemLandWiderView(parent.getContext());
                view.setGiftItemListener(mGiftItemListener);
                return new GiftItemViewHolder(view);
            }else {
                GiftDisPlayItemLandView view = new GiftDisPlayItemLandView(parent.getContext());
                view.setGiftItemListener(mGiftItemListener);
                return new GiftItemViewHolder(view);
            }
        } else {
            GiftDisPlayItemView view = new GiftDisPlayItemView(parent.getContext());
            view.setGiftItemListener(mGiftItemListener);
            return new GiftItemViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        MyLog.d(TAG, "onBindViewHolder");
        final GiftMallPresenter.GiftWithCard infoWithCard = mDataSource.get(position);

        if(viewHolder instanceof GiftSelectedHolder) {
            GiftSelectedHolder giftSelectedHolder = (GiftSelectedHolder) viewHolder;
            giftSelectedHolder.mGiftSelectedView.setGiftInfo(mDataSource.get(position).gift,true);
        }else if (viewHolder instanceof GiftItemViewHolder) {

            GiftItemViewHolder unSelectedViewHolder = (GiftItemViewHolder) viewHolder;

//            unSelectedViewHolder.giftDisPlayItemView.changeContinueSendBtnBackGroup(false);
//            unSelectedViewHolder.giftDisPlayItemView.hideContinueSendBtn();
            unSelectedViewHolder.giftDisPlayItemView.setDataSource(infoWithCard);


            if (!TextUtils.isEmpty(infoWithCard.gift.getGifUrl())) {
                if (!GiftMallPresenter.GiftWithCard.hashSet.contains(
                        infoWithCard.gift.getGiftId())) {
                    unSelectedViewHolder.giftDisPlayItemView.playSelectedGiftItemAnimator(
                            infoWithCard.gift.getGifUrl(), false);
                    GiftMallPresenter.GiftWithCard.hashSet.add(infoWithCard.gift.getGiftId());
                }
            }
            final View finalConvertView = unSelectedViewHolder.giftDisPlayItemView;
            Gift selectedGift = mGiftItemListener.getSelectedGift();
            if (mGiftItemListener != null && selectedGift != null && infoWithCard.gift.getGiftId()
                    == mGiftItemListener.getSelectedGift().getGiftId()) {
//                unSelectedViewHolder.giftDisPlayItemView.setBackgroundResource(
//                        R.drawable.live_choice_selected);
                if (!infoWithCard.gift.getCanContinuous()) {
//                    unSelectedViewHolder.giftDisPlayItemView.changeContinueSendBtnBackGroup(true);
//                    unSelectedViewHolder.giftDisPlayItemView.showContinueSendBtn(false);
                    unSelectedViewHolder.giftDisPlayItemView.changeCornerStatus(
                            infoWithCard.gift.getIcon(), true);
                }
                if (mGiftItemListener != null && mGiftItemListener.getSelectedGift() != null) {
                    mGiftItemListener.updateSelectedGiftView(finalConvertView, infoWithCard);
                }
            } else {
                //不能在onBindView中进行此操作，即使选中的礼物不变，但只要用户滑动ViewPager,就会调用每个新出现礼物的onBindView
//            if (mGiftItemListener != null) {
//                mGiftItemListener.updateContinueSend();
//            }
                unSelectedViewHolder.giftDisPlayItemView.setBackgroundResource(0);
            }
        }

        /**
         * 花钱礼物需要监听，或者是包裹礼物允许被送的礼物需要监听
         */
        if (mGiftItemListener.getMallType()
                || mGiftItemListener.getAllowedPktGiftId().contains(infoWithCard.gift.getGiftId())) {
            RxView.clicks(viewHolder.itemView).throttleFirst(200, TimeUnit.MILLISECONDS).subscribe(new Observer<Void>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(Void aVoid) {
                    if (mGiftItemListener != null) {
                        mGiftItemListener.clickGiftItem(viewHolder.itemView, infoWithCard, position);
                    }
//                EventBus.getDefault().post(new EventClass.GiftEvent(EventClass.GiftEvent.EVENT_TYPE_CLICK_GIFT_VIEW_IN_MALL, finalConvertView, infoWithCard));
                }
            });
        } else {
            viewHolder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return mDataSource == null ? 0 : mDataSource.size();
    }

    public void setData(List<GiftMallPresenter.GiftWithCard> dataList) {
        MyLog.d(TAG, "setData dataList=" + dataList.size());
        mDataSource = dataList;
        notifyDataSetChanged();
    }

    //更改选中的状态，横屏下使用
    public void changeSelectState(int position,int type,GiftSelectedView.SendGiftCallBack sendGiftCallBack){
        MyLog.d(TAG,"position:" + position + "type:" + type);
        if (position < 0 || position > mDataSource.size() - 1){
            return;
        }
        mDataSource.get(position).selectStatus = type;
        mSendGiftCallBack = sendGiftCallBack;
        specialUpdate();
    }

    private void specialUpdate() {
        Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                notifyItemChanged(getItemCount() - 1);
            }
        };
        handler.post(r);
    }

    @Override
    public int getItemViewType(int position) {
        return mDataSource.get(position).selectStatus;
    }

    public List<GiftMallPresenter.GiftWithCard> getData() {
        return mDataSource;
    }

    private GiftItemListener mGiftItemListener;

    public interface GiftItemListener {

        void clickGiftItem(View finalConvertView, GiftMallPresenter.GiftWithCard infoWithCard, int position);

        Gift getSelectedGift();

        void updateSelectedGiftView(View finalConvertView, GiftMallPresenter.GiftWithCard info);

        boolean getMallType();

        Set getAllowedPktGiftId();

        void updateContinueSend();
    }
}
