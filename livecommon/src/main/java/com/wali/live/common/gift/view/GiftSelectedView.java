package com.wali.live.common.gift.view;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.BaseImage;
import com.base.image.fresco.image.ImageFactory;
import com.base.log.MyLog;
import com.live.module.common.R;
import com.mi.live.data.gift.model.BuyGiftType;
import com.mi.live.data.gift.model.GiftType;
import com.wali.live.common.gift.utils.NumTranformation;
import com.wali.live.dao.Gift;
import com.jakewharton.rxbinding.view.RxView;

import rx.functions.Action1;

/**
 * Created by jiang on 18-5-18.
 */

public class GiftSelectedView extends RelativeLayout{
    public static String TAG = "GiftSelectedView";

    TextView giftName = null;

    TextView giftPrice = null;

    BaseImageView giftIcon = null;

    TextView sendGift = null;

    Gift mGift = null;

    View mBackground;

    Animation animationStar;

    SendGiftCallBack mCallBack = null;

    public GiftSelectedView(Context context) {
        super(context);
        init(context);
    }

    public GiftSelectedView(Context context, SendGiftCallBack callBack) {
        super(context);
        mCallBack = callBack;
        init(context);
    }

    public GiftSelectedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GiftSelectedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    void init(Context context) {
        View.inflate(context, R.layout.gift_selected_view, this);
        animationStar = AnimationUtils.loadAnimation(getContext(), R.anim.gift_selected_view_anim);
        animationStar.setInterpolator(new OvershootInterpolator(1.6f));
        bindView();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        clearAnimation();
        startAnimation(animationStar);
    }

    void bindView() {
        giftName = (TextView) findViewById(R.id.animate_gift_name);
        giftPrice = (TextView) findViewById(R.id.animate_gift_price);
        giftIcon = (BaseImageView) findViewById(R.id.animate_gift_iv);
        sendGift = (TextView) findViewById(R.id.animate_send);
        mBackground = findViewById(R.id.iv_background);

        RxView.clicks(sendGift)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        mCallBack.onClickSend(mGift, GiftSelectedView.this);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.w(TAG, throwable);
                    }
                });

        RxView.clicks(mBackground)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        mCallBack.onClickSend(mGift, GiftSelectedView.this);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.w(TAG, throwable);
                    }
                });
    }

    public void setGiftInfo(Gift gift, Boolean shake) {
        mGift = gift;
        giftName.setText(gift.getName());

        if (gift.getCatagory() == GiftType.Mi_COIN_GIFT || gift.getBuyType() == BuyGiftType.BUY_GIFT_BY_MI_COIN) {
            giftPrice.setText(NumTranformation.getShowValues((float) gift.getPrice() / 10));
        } else {
            giftPrice.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.live_icon_golden_diamond_small), null, null, null);
            giftPrice.setText(String.valueOf(gift.getPrice()));
        }

        if (shake) {
            if (!TextUtils.isEmpty(gift.getGifUrl())) {
                playSelectedGiftItemAnimator(gift.getGifUrl(), true);
            } else {
                playSelectedGiftItemAnimator(gift.getPicture(), true);
            }

        }
    }

    void playSelectedGiftItemAnimator(String webpPath, Boolean isKeepShow) {
        MyLog.d(TAG, "playSelectedGiftItemAnimator" + webpPath);

        BaseImage baseImage = ImageFactory.newHttpImage(webpPath).setAutoPlayAnimation(true).build();
        baseImage.mLowImageUri = Uri.parse(mGift.getPicture());
        FrescoWorker.loadImage(giftIcon, baseImage);

        if (isKeepShow) {
            return;
        }

        FrescoWorker.loadImage(giftIcon, baseImage);
    }

    public interface SendGiftCallBack {
        void onClickSend(Gift sendGift, View giftSelectedView);
    }
}
