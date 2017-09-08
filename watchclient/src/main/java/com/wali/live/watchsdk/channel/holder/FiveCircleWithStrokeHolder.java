package com.wali.live.watchsdk.channel.holder;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.view.RoundRectangleTextView;
import com.wali.live.watchsdk.channel.viewmodel.ChannelUserViewModel;

import java.util.Arrays;

public class FiveCircleWithStrokeHolder extends FiveCircleHolder {

    private int[] mCircleTvIds;
    private RoundRectangleTextView[] mCircleRectangleTvs;

    private int[] mCircleIvIds;
    private ImageView[] mCircleIvs;
    private GradientDrawable mGradientDrawable;//创建drawable

    public FiveCircleWithStrokeHolder(View itemView) {
        super(itemView);
        mGradientDrawable = new GradientDrawable();
        mGradientDrawable.setCornerRadius(DisplayUtils.dip2px(26f));
        mGradientDrawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
    }

    @Override
    protected void initContentViewId() {
        super.initContentViewId();
        mCircleTvIds = new int[mViewSize];
        mCircleIvIds = new int[mViewSize];
        mCircleIvs = new ImageView[mViewSize];
        Arrays.fill(mCircleTvIds, R.id.circle_rectangle_tv);
        Arrays.fill(mCircleIvIds, R.id.single_bg_iv);
    }

    @Override
    protected void initContentView() {
        super.initContentView();
        mCircleRectangleTvs = new RoundRectangleTextView[mViewSize];
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
                    mGradientDrawable.setStroke(DisplayUtils.dip2px(1), Color.parseColor(avatarColor));
                } else {
                    mGradientDrawable.setStroke(DisplayUtils.dip2px(1), itemView.getResources().getColor(R.color.color_d6b383));
                }
                mCircleIvs[i].setImageDrawable(mGradientDrawable);

                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mCircleIvs[i].getLayoutParams();
                lp.width = DisplayUtils.dip2px(52f);
                lp.height = DisplayUtils.dip2px(52f);
                mCircleIvs[i].setLayoutParams(lp);

                if (!TextUtils.isEmpty(descColor)) {
                    mCircleRectangleTvs[i].setRectangleColor(Color.parseColor(descColor));
                } else {
                    mCircleRectangleTvs[i].setRectangleColor(itemView.getResources().getColor(R.color.color_d52e71));
                }
            } catch (Exception e) {
                MyLog.e(TAG, e);
                mCircleRectangleTvs[i].setRectangleColor(itemView.getResources().getColor(R.color.color_d52e71));

                mGradientDrawable.setStroke(DisplayUtils.dip2px(1), itemView.getResources().getColor(R.color.color_d6b383));
                mCircleIvs[i].setImageDrawable(mGradientDrawable);
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mCircleIvs[i].getLayoutParams();
                lp.width = DisplayUtils.dip2px(52f);
                lp.height = DisplayUtils.dip2px(52f);
                mCircleIvs[i].setLayoutParams(lp);
            }
        }
    }
}
