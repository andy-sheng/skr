package com.module.playways.room.gift.view;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.common.utils.U;
import com.common.view.ex.ExFrameLayout;
import com.module.playways.R;
import com.module.playways.room.gift.adapter.GiftDisplayAdapter;
import com.module.playways.room.gift.event.CancelGiftCountDownEvent;
import com.module.playways.room.gift.event.GIftNotifyEvent;
import com.module.playways.room.gift.event.StartGiftCountDownEvent;
import com.module.playways.room.gift.event.UpdateMeiGuiFreeCountEvent;
import com.module.playways.room.gift.model.BaseGift;
import com.respicker.view.GridSpacingItemDecoration;
import com.zq.live.proto.Common.EGiftType;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class GiftOnePageView extends ExFrameLayout {
    RecyclerView mRecyclerView;

    GiftDisplayAdapter mGiftAdapter;

    GiftDisplayView.IGiftOpListener mIGiftOpListener;

    public GiftOnePageView(Context context) {
        super(context);
        init();
    }

    public GiftOnePageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GiftOnePageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.gift_one_page_view, this);
        EventBus.getDefault().register(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mGiftAdapter = new GiftDisplayAdapter();
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(4, U.getDisplayUtils().dip2px(0), false));
        mRecyclerView.setAdapter(mGiftAdapter);
    }

    public void setIGiftOpListener(GiftDisplayView.IGiftOpListener IGiftOpListener) {
        mIGiftOpListener = IGiftOpListener;
        mGiftAdapter.setIGiftOpListener(mIGiftOpListener);
    }

    public void setData(List<BaseGift> baseGiftList) {
        mGiftAdapter.setDataList(baseGiftList);
    }

    public interface OnClickGiftListener {
        void onClick(BaseGift baseGift);
    }

    public void destroy() {
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GIftNotifyEvent bigGiftMsgEvent) {
        mGiftAdapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(CancelGiftCountDownEvent cancelGiftCountDownEvent) {
        mGiftAdapter.setCountDownCircleShow(false);
        updateSysHandselGift();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(StartGiftCountDownEvent startGiftCountDownEvent) {
        mGiftAdapter.setCountDownCircleShow(true);
        updateSysHandselGift();
    }

    private void updateSysHandselGift() {
        for (int i = 0; i < mGiftAdapter.getDataList().size(); i++) {
            if (mGiftAdapter.getDataList().get(i).getGiftType() == EGiftType.EG_SYS_Handsel.getValue()) {
                mGiftAdapter.notifyItemChanged(i);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateMeiGuiFreeCountEvent updateMeiGuiFreeCountEvent) {
        for (BaseGift baseGift : mGiftAdapter.getDataList()) {
            if (baseGift.getGiftID() == 1) {
                baseGift.setBalance(updateMeiGuiFreeCountEvent.getCount());
                break;
            }
        }
    }
}
