package com.module.playways.room.gift.view;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.common.utils.U;
import com.common.view.ex.ExFrameLayout;
import com.module.playways.R;
import com.module.playways.room.gift.adapter.GiftAdapter;
import com.module.playways.room.gift.model.BaseGift;
import com.respicker.view.GridSpacingItemDecoration;

import java.util.List;

public class GiftOnePageView extends ExFrameLayout {
    RecyclerView mRecyclerView;

    GiftAdapter mGiftAdapter;

    GiftView.IGiftOpListener mIGiftOpListener;

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
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mGiftAdapter = new GiftAdapter();
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(4, U.getDisplayUtils().dip2px(0), false));
        mRecyclerView.setAdapter(mGiftAdapter);
    }

    public void setIGiftOpListener(GiftView.IGiftOpListener IGiftOpListener) {
        mIGiftOpListener = IGiftOpListener;
        mGiftAdapter.setIGiftOpListener(mIGiftOpListener);
    }

    public void setData(List<BaseGift> baseGiftList) {
        mGiftAdapter.setDataList(baseGiftList);
    }

    public interface OnClickGiftListener {
        void onClick(BaseGift baseGift);
    }
}
