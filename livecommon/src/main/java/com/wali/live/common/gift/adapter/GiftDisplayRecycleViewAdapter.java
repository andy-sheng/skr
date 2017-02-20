package com.wali.live.common.gift.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.jakewharton.rxbinding.view.RxView;
import com.live.module.common.R;
import com.wali.live.common.gift.adapter.viewHolder.GiftItemViewHolder;
import com.wali.live.common.gift.presenter.GiftMallPresenter;
import com.wali.live.common.gift.view.GiftDisPlayItemLandView;
import com.wali.live.common.gift.view.GiftDisPlayItemView;
import com.wali.live.dao.Gift;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observer;

/**
 * Created by zjn on 16-7-4.
 *
 * @module 礼物
 */
public class GiftDisplayRecycleViewAdapter extends RecyclerView.Adapter<GiftItemViewHolder> {
    public static String TAG = "GiftDisplayRecycleViewAdapter";

    private List<GiftMallPresenter.GiftWithCard> mDataSource = new ArrayList<>();

    private Context mContext;
    private boolean mLandscape;

    public GiftDisplayRecycleViewAdapter(Context context, boolean landscape, GiftItemListener giftItemListener) {
        this.mContext = context;
        this.mLandscape = landscape;
        this.mGiftItemListener = giftItemListener;
    }

    @Override
    public GiftItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mLandscape) {
            GiftDisPlayItemLandView view = new GiftDisPlayItemLandView(parent.getContext());
            return new GiftItemViewHolder(view);
        } else {
            GiftDisPlayItemView view = new GiftDisPlayItemView(parent.getContext());
            return new GiftItemViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(GiftItemViewHolder viewHolder, final int position) {
        final GiftMallPresenter.GiftWithCard infoWithCard = mDataSource.get(position);
        viewHolder.giftDisPlayItemView.changeContinueSendBtnBackGroup(false);
        viewHolder.giftDisPlayItemView.hideContinueSendBtn();
        viewHolder.giftDisPlayItemView.setDataSource(infoWithCard);

        if(!TextUtils.isEmpty(infoWithCard.gift.getGifUrl())){
            if(!GiftMallPresenter.GiftWithCard.hashSet.contains(infoWithCard.gift.getGiftId())){
                viewHolder.giftDisPlayItemView.playSelectedGiftItemAnimator(infoWithCard.gift.getGifUrl(), false);
                GiftMallPresenter.GiftWithCard.hashSet.add(infoWithCard.gift.getGiftId());
            }
        }


        final View finalConvertView = viewHolder.giftDisPlayItemView;
        if (mGiftItemListener!=null && infoWithCard.gift == mGiftItemListener.getSelectedGift()) {
            viewHolder.giftDisPlayItemView.setBackgroundResource(R.drawable.live_anchor_gift_selected);
            if(!infoWithCard.gift.getCanContinuous()){
                viewHolder.giftDisPlayItemView.changeContinueSendBtnBackGroup(true);
                viewHolder.giftDisPlayItemView.showContinueSendBtn(false);
                viewHolder.giftDisPlayItemView.changeCornerStatus(infoWithCard.gift.getIcon(), true);
            }
            if(mGiftItemListener!=null){
                mGiftItemListener.updateSelectedGiftView(finalConvertView,infoWithCard);
            }
        } else {
            if(mGiftItemListener != null) {
                mGiftItemListener.updateContinueSend();
            }
            viewHolder.giftDisPlayItemView.setBackgroundResource(0);
        }
        RxView.clicks(finalConvertView).throttleFirst(200, TimeUnit.MILLISECONDS).subscribe(new Observer<Void>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Void aVoid) {
                if(mGiftItemListener!=null){
                    mGiftItemListener.clickGiftItem(finalConvertView,infoWithCard, position);
                }
//                EventBus.getDefault().post(new EventClass.GiftCacheChangeEvent(EventClass.GiftCacheChangeEvent.EVENT_TYPE_CLICK_GIFT_VIEW_IN_MALL, finalConvertView, infoWithCard));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataSource == null ? 0 : mDataSource.size();
    }

    public void setData(List<GiftMallPresenter.GiftWithCard> dataList) {
        mDataSource = dataList;
        notifyDataSetChanged();
    }

    public List<GiftMallPresenter.GiftWithCard> getData(){
        return mDataSource;
    }

    private GiftItemListener mGiftItemListener;

    public interface GiftItemListener {

        void clickGiftItem(View finalConvertView, GiftMallPresenter.GiftWithCard infoWithCard, int position);

        Gift getSelectedGift();

        void updateSelectedGiftView(View finalConvertView, GiftMallPresenter.GiftWithCard info);

        void updateContinueSend();
    }
}
