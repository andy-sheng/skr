package com.wali.live.modulechannel.util;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.image.fresco.BaseImageView;
import com.common.image.fresco.FrescoWorker;
import com.common.image.fresco.IFrescoCallBack;
import com.common.image.model.ImageFactory;
import com.common.utils.U;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.modulechannel.R;
import com.wali.live.modulechannel.adapter.holder.FixedHolder;
import com.wali.live.modulechannel.adapter.holder.JumpImpl;
import com.wali.live.modulechannel.model.viewmodel.ChannelLiveViewModel;
import com.wali.live.proto.CommonChannel.ListWidgetInfo;

/**
 * Created by liuting on 18-6-15.
 */

public class HolderUtils {
    private static int mImageCornerRadius = 8;
    private static int mImageBorderWidth = 0;

    public static void bindImage(BaseImageView iv, String url, boolean isCircle, int width, int height, ScalingUtils.ScaleType scaleType) {
        bindImageWithCallback(iv, url, isCircle, width, height, scaleType, null);
    }

    public static void bindImageWithCallback(BaseImageView iv, String url, boolean isCircle, int width, int height, ScalingUtils.ScaleType scaleType, IFrescoCallBack callBack) {
        if (iv == null) {
            return;
        }
        FrescoWorker.loadImage(iv,
                ImageFactory.newHttpImage(url)
                        .setIsCircle(isCircle)
                        .setWidth(width).setHeight(height)
                        .setScaleType(scaleType)
                        .setLoadingDrawable(U.app().getResources().getDrawable(R.color.channel_color_f2f2f2))
                        .setFailureDrawable(U.app().getResources().getDrawable(R.color.channel_color_f2f2f2))
                        .setCallBack(callBack)
                        .build());
    }

    public static void bindImageWithBorder(BaseImageView iv, String url, boolean isCircle, int width, int height, ScalingUtils.ScaleType scaleType) {
        if (iv == null) {
            return;
        }
        FrescoWorker.loadImage(iv,
                ImageFactory.newHttpImage(url)
                        .setIsCircle(isCircle)
                        .setWidth(width).setHeight(height)
                        .setScaleType(scaleType)
                        .setLoadingDrawable(U.app().getResources().getDrawable(R.color.channel_color_f2f2f2))
                        .setFailureDrawable(U.app().getResources().getDrawable(R.color.channel_color_f2f2f2))
                        .setCornerRadius(mImageCornerRadius)
                        .setBorderWidth(mImageBorderWidth)
                        .setBorderColor(U.app().getResources().getColor(R.color.channel_color_e5e5e5))
                        .build());
    }

    public static void bindImageWithCorner(BaseImageView iv, String url, boolean isCircle, int width, int height, ScalingUtils.ScaleType scaleType, IFrescoCallBack callBack) {
        if (iv == null) {
            return;
        }

        FrescoWorker.loadImage(iv,
                ImageFactory.newHttpImage(url)
                        .setIsCircle(isCircle)
                        .setWidth(width).setHeight(height)
                        .setScaleType(scaleType)
                        .setLoadingDrawable(U.app().getResources().getDrawable(R.color.channel_color_f2f2f2))
                        .setFailureDrawable(U.app().getResources().getDrawable(R.color.channel_color_f2f2f2))
                        .setCornerRadius(8)
                        .setCallBack(callBack)
                        .build());
    }

    public static void bindText(TextView tv, String... texts) {
        if (tv == null) {
            return;
        }
        for (String text : texts) {
            if (!TextUtils.isEmpty(text)) {
                tv.setVisibility(View.VISIBLE);
                tv.setText(text);
                return;
            }
        }
        tv.setVisibility(View.GONE);
    }

    public static void bindLiveStatusImage(ChannelLiveViewModel.LiveItem item, ImageView mLiveStatusIvs) {
        if (mLiveStatusIvs == null) {
            return;
        }
        if (item.isPK()) {
            //bind PK image
            mLiveStatusIvs.setImageResource(R.drawable.milive_homepage_icon_pk);
            mLiveStatusIvs.setVisibility(View.VISIBLE);
        } else if (item.isMic()) {
            if (item.getMicType() == 0) {
                //bind 观众连麦 image
                mLiveStatusIvs.setImageResource(R.drawable.milive_homepage_icon_lianmai);
                mLiveStatusIvs.setVisibility(View.VISIBLE);
            } else if (item.getMicType() == 1) {
                //bind 主播连麦 image
                mLiveStatusIvs.setImageResource(R.drawable.milive_homepage_icon_manypelple);
                mLiveStatusIvs.setVisibility(View.VISIBLE);
            }
        }
    }

    private static int mLeftLabelImageWidth = U.getDisplayUtils().dip2px(80); //左上角配角标的宽度
    private static int mLeftLabelImageHeight = U.getDisplayUtils().dip2px(40); //左上角配角标的高度
    /**
     * 左上角标签 配文字 对应topLeft
     * @param item
     */
    public static void bindLeftLabel(final ChannelLiveViewModel.BaseItem item, final TextView labelTv) {
        if (item.getTopLeft() == null) {
            labelTv.setVisibility(View.GONE);
            return;
        }
        String text = item.getTopLeft().getText();
        if (!TextUtils.isEmpty(text)) {
            if(item.getTopLeft().hasBgColor()){
                GradientDrawable bgDrawable = item.getTopLeft().getBgDrawable();
                //leftTop, rightTop, rightBottom, leftBottom of (X,Y)
                int rightTop = mLeftLabelImageHeight >> 1;
                int rightBottom = mLeftLabelImageHeight >> 1;
                int leftTop = mImageCornerRadius << 1;
                float[] radius = {0, 0, rightTop, rightTop, rightBottom, rightBottom, 0, 0 };
                bgDrawable.setCornerRadii(radius);
                labelTv.setBackground(bgDrawable);

                int leftPadding = U.getDisplayUtils().dip2px(7f);
                int rightPadding = U.getDisplayUtils().dip2px(7f);
                labelTv.setPadding(leftPadding, 0, rightPadding, 0);

                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) labelTv.getLayoutParams();
                layoutParams.leftMargin = 0;
                labelTv.setLayoutParams(layoutParams);
            }else {
                int id = item.getTopLeft().getBgID() - 1;
                if (id < 0 || id > ChannelLiveViewModel.RichText.LEFT_LABEL_BG.length - 1) {
                    id = 0;
                }
                int leftPadding = 0;
                int rightPadding = 0;
                labelTv.setGravity(Gravity.CENTER);
                if (id >= 3 ) {
                    labelTv.setGravity(Gravity.CENTER | Gravity.RIGHT);
                    rightPadding = U.getDisplayUtils().dip2px(6.67f);
                } else if (id == 2) {
                    leftPadding = U.getDisplayUtils().dip2px(6.67f);
                    rightPadding = U.getDisplayUtils().dip2px(8.33f);
                }
                labelTv.setPadding(leftPadding, 0, rightPadding, 0);
                labelTv.setBackground(labelTv.getContext().getResources().getDrawable(ChannelLiveViewModel.RichText.LEFT_LABEL_BG[id]));
                labelTv.setTextColor(Color.WHITE);
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) labelTv.getLayoutParams();
                layoutParams.leftMargin = id != 2 ? U.getDisplayUtils().dip2px(6.67f) : 0;
                labelTv.setLayoutParams(layoutParams);
            }
        }
        labelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(item.getTopLeft().getJumpUrl())) {
                    JumpImpl.jumpSchema((Activity) labelTv.getContext(), item.getTopLeft().getJumpUrl());
                }
            }
        });
        bindBoldText(labelTv, text);
    }

    public static void bindBoldText(TextView tv, String... texts) {
        if (tv == null) {
            return;
        }
        for (String text : texts) {
            if (!TextUtils.isEmpty(text)) {
                SpannableStringBuilder ssb = new SpannableStringBuilder();
                ssb.append(text);
                ssb.setSpan(new FixedHolder.FakeBoldSpan(), 0, text.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                tv.setVisibility(View.VISIBLE);
                tv.setText(ssb);
                return;
            }
        }
        tv.setVisibility(View.GONE);
    }

    /**
     * 左上角标签 配图片 对应ListWidgetInfo
     */
    public static void bindLeftWidgetInfo(final ChannelLiveViewModel.BaseItem item, final BaseImageView labelIv) {
        ListWidgetInfo widgetInfo = item.getWidgetInfo();
        if (widgetInfo != null) {
            String iconUrl = widgetInfo.getIconUrl();
            final String jumpUrl = widgetInfo.getJumpSchemeUri();
            if (!TextUtils.isEmpty(iconUrl)) {
                ViewGroup.MarginLayoutParams layoutParam = (ViewGroup.MarginLayoutParams) labelIv.getLayoutParams();
                if (layoutParam.width != mLeftLabelImageWidth) {
                    layoutParam.width = mLeftLabelImageWidth;
                    layoutParam.height = mLeftLabelImageHeight;
                    labelIv.setLayoutParams(layoutParam);
                }
                labelIv.setVisibility(View.VISIBLE);
                bindImage(labelIv, iconUrl, false, mLeftLabelImageWidth, mLeftLabelImageHeight, ScalingUtils.ScaleType.FIT_START);
                labelIv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        JumpImpl.jumpSchema((Activity) labelIv.getContext(), jumpUrl);
                    }
                });
            }
        }
    }
}
