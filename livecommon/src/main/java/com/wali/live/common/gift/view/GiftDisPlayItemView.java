package com.wali.live.common.gift.view;

import android.content.Context;
import android.graphics.Paint;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.BaseImage;
import com.base.image.fresco.image.ImageFactory;
import com.base.log.MyLog;
import com.base.utils.language.LocaleUtil;
import com.live.module.common.R;
import com.mi.live.data.gift.model.BuyGiftType;
import com.mi.live.data.gift.model.GiftCard;
import com.mi.live.data.gift.model.GiftType;
import com.wali.live.common.gift.presenter.GiftMallPresenter;
import com.wali.live.common.gift.utils.NumTranformation;
import com.wali.live.common.view.StrokeTextView;
import com.wali.live.dao.Gift;

/**
 * Created by zjn on 16-7-25.
 *
 * @module 礼物橱窗优化
 */
public class GiftDisPlayItemView extends RelativeLayout {

    public static String TAG = "GiftDisPlayItemView";

    private static final int BALANCE_TEXT_SIZE = 10;
    public static final float INIT_PROGRESS = 100;

    private BaseImageView mGiftIv;

    private TextView mPriceTv;

    private TextView mTextTv;

    private TextView mOriginalPrice;

    private TextView mFreeGiftTv;

//    private TextView mExepTv;
//
//    private ImageView mLianIv;

    private BaseImageView mCornerIv;

    private TextView mContinueSendBtn;

    private StrokeTextView mContinueSendGiftNum;

    private CircularProgressBar mGiftProgressBar;

    private TextView mContinueSendText;

    private TextView mGiftName;

    public void bindView() {
        mGiftIv = (BaseImageView) findViewById(R.id.gift_iv);
        mPriceTv = (TextView) findViewById(R.id.price_tv);
        mTextTv = (TextView) findViewById(R.id.text_tv);
        mOriginalPrice = (TextView) findViewById(R.id.origin_price_tv);
        mFreeGiftTv = (TextView) findViewById(R.id.free_tv);
//        mExepTv = (TextView) findViewById(R.id.exep_tv);
//        mLianIv = (ImageView) findViewById(R.id.lian_iv);
        mCornerIv = (BaseImageView) findViewById(R.id.superscript_iv);
        mContinueSendBtn = (TextView) findViewById(R.id.continue_tv);
        mContinueSendGiftNum = (StrokeTextView) findViewById(R.id.continue_gift_num);
        mGiftProgressBar = (CircularProgressBar) findViewById(R.id.progress_bar);
        mContinueSendText = (TextView) findViewById(R.id.send_tv);
        mGiftName = (TextView) findViewById(R.id.gift_name);
    }

    private int[] mNumberColors = {
            R.color.gift_number_first,
            R.color.gift_number_second,
            R.color.gift_number_third,
            R.color.gift_number_forth,
            R.color.gift_number_fifth,
            R.color.gift_number_sixth,
            R.color.gift_number_seventh
    };

    private int mIndex = 0;// 当前索引级别

    public GiftDisPlayItemView(Context context) {
        super(context);
        init(context);
    }

    public GiftDisPlayItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GiftDisPlayItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    protected void init(Context context) {
        inflate(context, R.layout.gift_display_item_view, this);
        bindView();

        mGiftProgressBar.setProgress(INIT_PROGRESS);
    }

    /**
     * 显示打折时礼物原始价格
     */
    protected void showGiftOriginalPriceArea(int giftType, int originalPrice) {
        mOriginalPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);//设置中划线
        mTextTv.setVisibility(VISIBLE);
        if (LocaleUtil.getLanguageCode().matches("^en.*")) {
            mTextTv.setText(" ");
        } else {
            mTextTv.setText(getResources().getString(R.string.origin_for_gift_item));
        }
        mOriginalPrice.setVisibility(View.VISIBLE);
        if (giftType == GiftType.Mi_COIN_GIFT) {
//            mOriginalPrice.setText(String.valueOf((float) originalPrice / 100.0));
            mOriginalPrice.setText(NumTranformation.getShowValues((float) originalPrice / 100));
        } else {
            mOriginalPrice.setText(String.valueOf(originalPrice));
        }
    }

    /**
     * 隐藏打折时礼物原始价格
     */
    protected void hideGiftOriginalPriceArea() {
        mTextTv.setVisibility(GONE);
        mOriginalPrice.setVisibility(View.GONE);
    }

    private String mGiftPicture;

    public void setDataSource(GiftMallPresenter.GiftWithCard infoWithCard) {
        MyLog.d(TAG, "infoWithCard:" + infoWithCard);

        Gift gift = infoWithCard.gift;
        mGiftName.setText(infoWithCard.gift.getInternationalName());
        // 礼物
        mGiftPicture = gift.getPicture();
        FrescoWorker.loadImage(mGiftIv, ImageFactory.newHttpImage(gift.getPicture()).build());
        // 经验
//        mExepTv.setText(getContext().getString(R.string.experience, gift.getEmpiricValue()));

        GiftCard card = infoWithCard.card;

        long now = System.currentTimeMillis();
        // 免费卡
        if (card != null && card.getGiftCardCount() > 0) {
            MyLog.d(TAG, "免费卡");
            // 显示免费卡
            mFreeGiftTv.setVisibility(VISIBLE);
            mFreeGiftTv.setText(String.valueOf(getResources().getString(R.string.gift_card_num) + card.getGiftCardCount()));

            // 该隐藏的隐藏
            mPriceTv.setVisibility(GONE);
            hideGiftOriginalPriceArea();
        } else {
            //没有免费卡
            mFreeGiftTv.setVisibility(GONE);
            // 显示价格
            mPriceTv.setVisibility(View.VISIBLE);
//            mPriceTv.setText(String.valueOf(gift.getPrice()));
            if (gift.getCatagory() == GiftType.RED_ENVELOPE_GIFT) {
                mPriceTv.setCompoundDrawables(null, null, null, null);
                mPriceTv.setText(R.string.richer_hongbao);
//                mExepTv.setText(R.string.get_lucky);
                mTextTv.setVisibility(GONE);
                mOriginalPrice.setVisibility(GONE);
            } else {
                int price = gift.getPrice();
//                if (LocaleUtil.getLanguageCode().matches("^en.*")) {
//                    if (gift.getCatagory() == GiftType.Mi_COIN_GIFT) {
//                        mPriceTv.setText(NumTranformation.getShowValues((float) price / 10));
//                    } else {
//                        mPriceTv.setText(String.valueOf(price));
//                    }
//                    mPriceTv.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.live_icon_golden_diamond_small), null);
//                } else {
//                    mPriceTv.setCompoundDrawables(null, null, null, null);
//                    String priceLength;
//                    if (gift.getCatagory() == GiftType.Mi_COIN_GIFT) {
//                        priceLength = NumTranformation.getShowValues((float) price / 10);
//                    } else {
//                        priceLength = String.valueOf(price);
//
//                    }
//                    String st = String.valueOf(price) + " " + getResources().getString(R.string.gift_item_diamond_text);
//                    SpannableString spannableString = new SpannableString(st);
//                    spannableString.setSpan(new AbsoluteSizeSpan(DisplayUtils.dip2px(BALANCE_TEXT_SIZE)), priceLength.length(), st.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
//                    mPriceTv.setText(spannableString);
//                }

                if (gift.getCatagory() == GiftType.Mi_COIN_GIFT || gift.getBuyType() == BuyGiftType.BUY_GAME_ROOM_GIFT) {
                    mPriceTv.setText(NumTranformation.getShowValues((float) price / 10));
                } else {
                    mPriceTv.setText(String.valueOf(price));
                }
                mPriceTv.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.live_icon_golden_diamond_small), null);

                if (gift.getOriginalPrice() > 0) {
                    // 显示原价
                    showGiftOriginalPriceArea(gift.getCatagory(), gift.getOriginalPrice());
                } else {
                    hideGiftOriginalPriceArea();
                }
            }
        }

        // 连---后面产品要求去除
//        if (!gift.getCanContinuous() || !CommonUtils.isChinese()) {
//            mLianIv.setVisibility(View.GONE);
//        } else {
//            mLianIv.setVisibility(View.VISIBLE);
//        }

        // 角标
        String connerIcon;
        if (!LocaleUtil.getLanguageCode().equals(LocaleUtil.LOCALE_SIMPLIFIED_CHINESE.toString())) {
            connerIcon = gift.getInternationalIcon();
        } else {
            connerIcon = gift.getIcon();
        }

//        String connerIcon = gift.getInternationalIcon();
        MyLog.d(TAG, "connerIcon:" + connerIcon);
        if (TextUtils.isEmpty(connerIcon)) {
            mCornerIv.setVisibility(GONE);
        } else {
            mCornerIv.setVisibility(VISIBLE);
            FrescoWorker.loadImage(mCornerIv, ImageFactory.newHttpImage(connerIcon).build());
        }
    }

    public void setContinueSendGiftNum(int giftNum) {
        mContinueSendGiftNum.setVisibility(VISIBLE);
        mContinueSendGiftNum.setText(" x" + String.valueOf(giftNum));
        selectNumTextColor(giftNum);
    }

    private int[] mFlag = {
            19, 49, 98, 298, 519, 998, Integer.MAX_VALUE
    };

    private void selectNumTextColor(int number) {
        int index = 0;
        for (int i = 0; i < mFlag.length; i++) {
            if (number > mFlag[i]) {
                index++;
            } else {
                break;
            }
        }
        if (index >= mNumberColors.length) {
            index = mNumberColors.length - 1;
        }
        if (index != mIndex) {
            mIndex = index;
            mContinueSendGiftNum.setTextColor(GlobalData.app().getResources().getColorStateList(mNumberColors[mIndex]));
            //字体描边
            mContinueSendGiftNum.setOutTextColor(R.color.color_white);
        }
    }

    public boolean isContinueSendBtnShow() {
        if (mContinueSendBtn.getVisibility() == VISIBLE) {
            return true;
        }
        return false;
    }

    public void showContinueSendBtn(boolean isShowProgressBar) {
        if (isShowProgressBar) {
            mGiftProgressBar.setProgress(INIT_PROGRESS);
            mGiftProgressBar.setVisibility(VISIBLE);
        } else {
            mGiftProgressBar.setVisibility(GONE);
        }
        mContinueSendBtn.setVisibility(VISIBLE);
        mContinueSendText.setVisibility(VISIBLE);
    }

    public void hideContinueSendBtn() {
        mGiftProgressBar.setVisibility(GONE);
        mContinueSendGiftNum.setVisibility(GONE);
        mContinueSendBtn.setVisibility(GONE);
        mContinueSendText.setVisibility(GONE);
    }

    public void changeCornerStatus(String cornerIcon, boolean isHide) {
        if (!TextUtils.isEmpty(cornerIcon)) {
            if (isHide) {
                mCornerIv.setVisibility(INVISIBLE);
            } else {
                mCornerIv.setVisibility(VISIBLE);
            }
        }
    }

    public void changeContinueSendBtnProgressBarProgress(float progress) {
        if (mGiftProgressBar.getVisibility() == VISIBLE) {
            mGiftProgressBar.setProgress(progress);
        }
    }

    public void changeContinueSendBtnBackGroup(boolean isBigGiftFlag) {
        if (isBigGiftFlag) {
            mContinueSendText.setText(getContext().getString(R.string.continue_big_send_text));
            mContinueSendBtn.setBackgroundResource(R.drawable.live_anchor_presented_idetified_bg);
        } else {
            mContinueSendText.setText(getContext().getString(R.string.continue_normal_send_text));
            mContinueSendBtn.setBackgroundResource(R.drawable.live_anchor_presented_bg);
        }

    }

    public void playSelectedGiftItemAnimator(String webpPath, boolean isKeepShow) {
        MyLog.d(TAG, "playSelectedGiftItemAnimator" + webpPath);

        BaseImage baseImage = ImageFactory.newHttpImage(webpPath).setAutoPlayAnimation(true).build();
        baseImage.mLowImageUri = Uri.parse(mGiftPicture);
        FrescoWorker.loadImage(mGiftIv, baseImage);

        if (isKeepShow) {
            return;
        }

        FrescoWorker.loadImage(mGiftIv, ImageFactory.newHttpImage(mGiftPicture).build());
    }

    public void cancelSelectedGiftItemAnimator() {
        FrescoWorker.loadImage(mGiftIv, ImageFactory.newHttpImage(mGiftPicture).build());
    }
}
