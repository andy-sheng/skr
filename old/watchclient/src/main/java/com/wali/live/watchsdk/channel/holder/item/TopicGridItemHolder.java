package com.wali.live.watchsdk.channel.holder.item;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.helper.HolderHelper;
import com.wali.live.watchsdk.channel.viewmodel.ChannelNavigateViewModel.NavigateItem;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 提供可滑动的方形item
 */
public class TopicGridItemHolder extends RecyclerView.ViewHolder {
    private TextView mLabelTv;

    private NavigateItem mItemData;

    public TopicGridItemHolder(View itemView) {
        super(itemView);
        initContentViews();
    }

    protected void initContentViews() {
        mLabelTv = $(R.id.label_tv);
    }

    public void bindModel(NavigateItem itemData) {
        mItemData = itemData;
        bindView();
    }

    protected void bindView() {
        if (mItemData != null) {
            if (!TextUtils.isEmpty(mItemData.getText())) {
                mLabelTv.setVisibility(View.VISIBLE);
                mLabelTv.setText(mItemData.getText());
                mLabelTv.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //scheme jump
                            }
                        }
                );

                mLabelTv.setTextColor(createSelector(mItemData));
            } else {
                mLabelTv.setVisibility(View.GONE);
            }
            HolderHelper.sendExposureCommand(mItemData);
        }
    }

    private ColorStateList createSelector(NavigateItem mItemData) {

        int statePressed = android.R.attr.state_pressed;
        int[][] state = {{statePressed}, {-statePressed}};
        int color1;
        int color2;

        if (matchColor(mItemData.getHexColorCode())) {
            String color = new String(mItemData.getHexColorCode());
            String pressColor = new String(mItemData.getHexColorCode());
            StringBuffer stringBuffer = new StringBuffer(pressColor);
            pressColor = stringBuffer.insert(1, "80").toString();
            color1 = Color.parseColor(pressColor);
            color2 = Color.parseColor(color);
        } else {
            color1 = Color.parseColor("#80000000");
            color2 = Color.parseColor("#000000");
        }
        int[] color = {color1, color2};
        ColorStateList colorStateList = new ColorStateList(state, color);
        return colorStateList;
    }

    private boolean matchColor(String color) {
        if (TextUtils.isEmpty(color)) {
            return false;
        }

        Pattern pattern = Pattern.compile("^#[0-9a-fA-F]{6}$");
        Matcher matcher = pattern.matcher(color);
        return matcher.matches();
    }

    protected ScalingUtils.ScaleType getScaleType() {
        return ScalingUtils.ScaleType.CENTER_CROP;
    }

    protected boolean isCircle() {
        return false;
    }

    protected <V extends View> V $(int id) {
        return (V) itemView.findViewById(id);
    }
}
