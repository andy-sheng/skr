package com.wali.live.modulechannel.adapter.holder;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.log.MyLog;
import com.common.utils.U;
import com.wali.live.modulechannel.R;

import java.util.Arrays;

public class FiveCircleWithStrokeHolder extends FiveCircleHolder {

    private int[] mCircleTvIds;
    private TextView[] mCircleRectangleTvs;

    private int[] mCircleIvIds;
    private ImageView[] mCircleIvs;
    private GradientDrawable mCircleDrawable;
    private GradientDrawable mRoundRectDrawable;

    public FiveCircleWithStrokeHolder(View itemView) {
        super(itemView);
        mCircleDrawable = new GradientDrawable();
        mCircleDrawable.setShape(GradientDrawable.OVAL);
        mCircleDrawable.setCornerRadius(U.getDisplayUtils().dip2px(26f));

        mRoundRectDrawable = new GradientDrawable();
        mRoundRectDrawable.setShape(GradientDrawable.RECTANGLE);
        mRoundRectDrawable.setCornerRadius(U.getDisplayUtils().dip2px(8.33f));
        mRoundRectDrawable.setStroke(U.getDisplayUtils().dip2px(1), Color.WHITE);
    }

    @Override
    protected void initContentViewId() {
        super.initContentViewId();
        mCircleTvIds = new int[mViewSize];
        mCircleIvIds = new int[mViewSize];

        Arrays.fill(mCircleTvIds, R.id.circle_rectangle_tv);
        Arrays.fill(mCircleIvIds, R.id.single_bg_iv);
    }

    @Override
    protected void initContentView() {
        super.initContentView();
        mCircleRectangleTvs = new TextView[mViewSize];
        mCircleIvs = new ImageView[mViewSize];
        for (int i = 0; i < mViewSize; i++) {
            mCircleRectangleTvs[i] = $(mParentViews[i], mCircleTvIds[i]);
            mCircleIvs[i] = $(mParentViews[i], mCircleIvIds[i]);
        }
    }

    @Override
    protected void bindUserModel(ChannelUserViewModel viewModel) {
        super.bindUserModel(viewModel);
        for (int i = 0; i < viewModel.getItemDatas().size(); i++) {
            mCircleRectangleTvs[i].setText(viewModel.getItemDatas().get(i).getDescText());
            try {
                String avatarColor = viewModel.getItemDatas().get(i).getAvatarLayerColor();
                String descColor = viewModel.getItemDatas().get(i).getDescTextColor();
                MyLog.w(TAG, "avatarColor=" + avatarColor + " descColor=" + descColor);
                if (!TextUtils.isEmpty(avatarColor)) {
                    if (!avatarColor.startsWith("#"))
                        avatarColor = "#" + avatarColor;
                    bindCircleBackgroundIv(mCircleIvs[i], Color.parseColor(avatarColor));
                } else {
                    bindCircleBackgroundIv(mCircleIvs[i], itemView.getResources().getColor(R.color.channel_color_d6b383));
                }

                if (!TextUtils.isEmpty(descColor)) {
                    if (!avatarColor.startsWith("#"))
                        descColor = "#" + descColor;
                    bindRoundBackgroundIv(mCircleRectangleTvs[i], Color.parseColor(descColor));
                } else {
                    bindRoundBackgroundIv(mCircleRectangleTvs[i], itemView.getResources().getColor(R.color.channel_color_fb98aa));
                }
            } catch (Exception e) {
                MyLog.e(TAG, e);
                bindRoundBackgroundIv(mCircleRectangleTvs[i], itemView.getResources().getColor(R.color.channel_color_fb98aa));
                bindCircleBackgroundIv(mCircleIvs[i], itemView.getResources().getColor(R.color.channel_color_d6b383));
            }
        }
    }

    /**
     * 设置描边图片以及调整宽高
     *
     * @param imageView
     */
    private void bindCircleBackgroundIv(ImageView imageView, int color) {
        if (imageView == null) {
            return;
        }
        mCircleDrawable.setStroke(U.getDisplayUtils().dip2px(1), color);
        imageView.setImageDrawable(mCircleDrawable);
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) imageView.getLayoutParams();
        lp.width = U.getDisplayUtils().dip2px(52f);
        lp.height = U.getDisplayUtils().dip2px(52f);
        imageView.setLayoutParams(lp);
    }

    private void bindRoundBackgroundIv(TextView textView, int color) {
        if (textView == null) {
            return;
        }
        mRoundRectDrawable.setColor(color);
        textView.setBackground(mRoundRectDrawable);
    }
}
