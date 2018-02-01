package com.wali.live.watchsdk.contest.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ImageFactory;
import com.base.mvp.specific.RxRelativeLayout;
import com.wali.live.watchsdk.R;

/**
 * Created by wanglinzhang on 2018/2/1.
 */
public class AdvertisingView extends RxRelativeLayout implements View.OnClickListener {
    private TextView mTitleTv;
    private TextView mStatus;
    private BaseImageView mIconIv;

    public AdvertisingView(Context context) {
        super(context, null);
        init(context);
    }

    public AdvertisingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
        init(context);
    }

    public AdvertisingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.advertising_show_view, this);

        mTitleTv  = $(R.id.advertising_title_tv);
        mIconIv = $(R.id.advertising_icon_iv);
        mStatus = $(R.id.status_tv);
    }

    public void setTitle(String title) {
        mTitleTv.setText(title);
    }

    public void setIcon(String iconUrl) {
        FrescoWorker.loadImage(mIconIv, ImageFactory.newHttpImage(iconUrl).build());
    }

    public void updateStatus(String status){
        mStatus.setText(status);
    }

    @Override
    public void onClick(View v) {

    }
}
