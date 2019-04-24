package com.module.playways.room.gift.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import com.common.utils.U;
import com.common.view.ex.ExFrameLayout;
import com.common.view.ex.drawable.DrawableCreator;
import com.module.playways.R;
import com.module.playways.room.gift.adapter.GiftAdapter;
import com.module.playways.room.gift.adapter.GiftViewPagerAdapter;
import com.module.playways.room.gift.inter.IGiftView;
import com.module.playways.room.gift.model.BaseGift;
import com.module.playways.room.gift.presenter.GiftViewPresenter;

import java.util.HashMap;
import java.util.List;

public class GiftView extends ExFrameLayout implements IGiftView {
    ViewPager mViewpager;

    GiftViewPagerAdapter mGiftViewPagerAdapter;

    GiftViewPresenter mGiftViewPresenter;

    BaseGift mSelectedGift;

    GiftAdapter.GiftUpdateListner mGiftUpdateListner;

    public GiftView(Context context) {
        super(context);
        init();
    }

    public GiftView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GiftView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    IGiftOpListener mIGiftOpListener = new IGiftOpListener() {
        @Override
        public BaseGift getCurSelectedGift() {
            return mSelectedGift;
        }

        @Override
        public void select(BaseGift baseGift, GiftAdapter.GiftUpdateListner giftUpdateListner) {
            if (mSelectedGift != null) {
                mGiftUpdateListner.updateGift(mSelectedGift);
            }

            mSelectedGift = baseGift;
            mGiftUpdateListner = giftUpdateListner;
        }
    };

    public BaseGift getSelectedGift() {
        return mSelectedGift;
    }

    private void init() {
        inflate(getContext(), R.layout.gift_view_layout, this);
        Drawable drawableBg = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(20))
                .setShape(DrawableCreator.Shape.Rectangle)
                .setSolidColor(U.getColor(R.color.black_trans_20))
                .setCornersRadius(U.getDisplayUtils().dip2px(16))
                .build();

        setBackground(drawableBg);
        mViewpager = (ViewPager) findViewById(R.id.viewpager);
        mGiftViewPagerAdapter = new GiftViewPagerAdapter(getContext(), mIGiftOpListener);
        mViewpager.setAdapter(mGiftViewPagerAdapter);
        mGiftViewPresenter = new GiftViewPresenter(this);
        mGiftViewPresenter.loadData();
    }

    @Override
    public void showGift(HashMap<Integer, List<BaseGift>> baseGiftCollection) {
        mGiftViewPagerAdapter.setData(baseGiftCollection);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void destroy() {
        mGiftViewPresenter.destroy();
    }

    public interface IGiftOpListener {
        //获取当前被选中礼物
        BaseGift getCurSelectedGift();

        //选中礼物
        void select(BaseGift baseGift, GiftAdapter.GiftUpdateListner giftUpdateListner);
    }
}
