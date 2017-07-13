package com.wali.live.watchsdk.watch.view;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.wali.live.component.view.panel.BaseBottomPanel;
import com.wali.live.watchsdk.R;

/**
 * Created by simon on 2017/07/12.
 * <p>
 * Generated using create_component_view.py
 *
 * @module 抢红包视图
 */
public class EnvelopeView extends BaseBottomPanel<RelativeLayout, RelativeLayout> {
    private static final String TAG = "EnvelopeView";

    private View mTopView;
    private View mBottomView;
    private BaseImageView mSenderAvatarIv;
    private ImageView mUserBadgeIv;
    private TextView mNameTv;
    private TextView mInfoTv;
    private TextView mGrabBtn;

    public EnvelopeView(@NonNull RelativeLayout parentView) {
        super(parentView);
    }

    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.red_envelope_view;
    }

    @Override
    protected void inflateContentView() {
        super.inflateContentView();
        mTopView = $(R.id.bg_top);
        mBottomView = $(R.id.bg_bottom);
        mSenderAvatarIv = $(R.id.sender_avatar_iv);
        mUserBadgeIv = $(R.id.user_badge_iv);
        mNameTv = $(R.id.name_tv);
        mInfoTv = $(R.id.info_tv);
        mGrabBtn = $(R.id.grab_btn);

        $click(R.id.close_iv, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 加入关闭按钮点击响应 YangLi
            }
        });
    }
}
