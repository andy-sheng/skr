package com.wali.live.watchsdk.component.view;

import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v7.content.res.AppCompatResources;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.mi.live.data.gift.redenvelope.RedEnvelopeModel;
import com.wali.live.component.view.panel.BaseBottomPanel;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.watchsdk.R;

/**
 * Created by yangli on 2017/07/12.
 *
 * @module 抢红包视图
 */
public class EnvelopeView extends BaseBottomPanel<RelativeLayout, RelativeLayout> {
    private static final String TAG = "EnvelopeView";

    public final static int ENVELOPE_TYPE_SMALL = 1;
    public final static int ENVELOPE_TYPE_MIDDLE = 2;
    public final static int ENVELOPE_TYPE_LARGE = 3;

    private RedEnvelopeModel mRedEnvelopeModel;

    private View mTopView;
    private View mBottomView;
    private BaseImageView mSenderAvatarIv;
    private ImageView mUserBadgeIv;
    private TextView mNameTv;
    private TextView mInfoTv;
    private TextView mGrabBtn;

    private Drawable getDrawable(@DrawableRes int res) {
        return AppCompatResources.getDrawable(mContentView.getContext(), res);
    }

    public EnvelopeView(@NonNull RelativeLayout parentView) {
        super(parentView);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.red_envelope_view;
    }

    public void setEnvelopeModel(RedEnvelopeModel model) {
        mRedEnvelopeModel = model;
        if (mRedEnvelopeModel == null || mContentView == null) {
            return;
        }
        switch (model.getType()) {
            case ENVELOPE_TYPE_SMALL:
                mTopView.setBackground(getDrawable(R.drawable.red_packet_bg_top_1));
                mBottomView.setBackground(getDrawable(R.drawable.red_packet_bg_bottom_1));
                break;
            case ENVELOPE_TYPE_MIDDLE:
                mTopView.setBackground(getDrawable(R.drawable.red_packet_top_2));
                mBottomView.setBackground(getDrawable(R.drawable.red_packet_bg_23));
                break;
            default:
                mTopView.setBackground(getDrawable(R.drawable.red_packet_top_3));
                mBottomView.setBackground(getDrawable(R.drawable.red_packet_bg_23));
                break;
        }
        AvatarUtils.loadAvatarByUidTs(mSenderAvatarIv, model.getUserId(), model.getAvatarTimestamp(), true);
        mUserBadgeIv.setImageDrawable(ItemDataFormatUtils.getLevelSmallImgSource(model.getLevel()));
        mNameTv.setText(model.getNickName());
        mInfoTv.setText(model.getMsg());
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

        setEnvelopeModel(mRedEnvelopeModel);
    }
}
