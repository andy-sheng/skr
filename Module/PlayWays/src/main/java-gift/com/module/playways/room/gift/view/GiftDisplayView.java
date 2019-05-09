package com.module.playways.room.gift.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import com.common.utils.U;
import com.common.view.ex.ExFrameLayout;
import com.common.view.ex.drawable.DrawableCreator;
import com.kingja.loadsir.callback.Callback;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.module.playways.R;
import com.module.playways.room.gift.adapter.GiftDisplayAdapter;
import com.module.playways.room.gift.adapter.GiftViewPagerAdapter;
import com.module.playways.room.gift.inter.IGiftDisplayView;
import com.module.playways.room.gift.loadsir.GiftEmptyCallback;
import com.module.playways.room.gift.model.BaseGift;
import com.module.playways.room.gift.presenter.GiftViewPresenter;

import java.util.HashMap;
import java.util.List;

/**
 * 礼物橱窗view
 */
public class GiftDisplayView extends ExFrameLayout implements IGiftDisplayView {
    ViewPager mViewpager;

    GiftViewPagerAdapter mGiftViewPagerAdapter;

    GiftViewPresenter mGiftViewPresenter;

    BaseGift mSelectedGift;

    GiftDisplayAdapter.GiftUpdateListner mGiftUpdateListner;

    LoadService mLoadService;

    public GiftDisplayView(Context context) {
        super(context);
        init();
    }

    public GiftDisplayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GiftDisplayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    IGiftOpListener mIGiftOpListener = new IGiftOpListener() {
        @Override
        public BaseGift getCurSelectedGift() {
            return mSelectedGift;
        }

        @Override
        public void select(BaseGift baseGift, GiftDisplayAdapter.GiftUpdateListner giftUpdateListner) {
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
        Drawable drawableBg = new DrawableCreator.Builder()
                .setShape(DrawableCreator.Shape.Rectangle)
                .setSolidColor(U.getColor(R.color.black_trans_20))
                .build();

        setBackground(drawableBg);
        mViewpager = (ViewPager) findViewById(R.id.viewpager);
        mGiftViewPagerAdapter = new GiftViewPagerAdapter(getContext(), mIGiftOpListener);
        mViewpager.setAdapter(mGiftViewPagerAdapter);
        mGiftViewPresenter = new GiftViewPresenter(this);
        mGiftViewPresenter.loadData();

        LoadSir mLoadSir = new LoadSir.Builder()
                .addCallback(new GiftEmptyCallback())
                .build();

        mLoadService = mLoadSir.register(mViewpager, new Callback.OnReloadListener() {
            @Override
            public void onReload(View v) {
                mGiftViewPresenter.loadData();
            }
        });
    }

    @Override
    public void showGift(HashMap<Integer, List<BaseGift>> baseGiftCollection) {
        mLoadService.showSuccess();
        mGiftViewPagerAdapter.setData(baseGiftCollection);
    }

    @Override
    public void getGiftListFaild() {
        mLoadService.showCallback(GiftEmptyCallback.class);
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
        void select(BaseGift baseGift, GiftDisplayAdapter.GiftUpdateListner giftUpdateListner);
    }
}
