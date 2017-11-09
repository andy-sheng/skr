package com.wali.live.watchsdk.fans.holder;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.UserAccountManager;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.constant.FansConstant;
import com.wali.live.watchsdk.fans.model.item.CreateFansGroupModel;
import com.wali.live.watchsdk.lit.recycler.holder.BaseHolder;
import com.wali.live.watchsdk.webview.WebViewActivity;

/**
 * Created by lan on 2017/11/8.
 */
public class CreateFansGroupHolder extends BaseHolder<CreateFansGroupModel> {
    private BaseImageView mAvatarIv;

    private TextView mNameTv;
    private TextView mRecommendTv;
    private TextView mCreateBtn;

    public CreateFansGroupHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initView() {
        mAvatarIv = $(R.id.avatar_iv);
        mNameTv = $(R.id.name_tv);
        mRecommendTv = $(R.id.recommend_tv);
        mCreateBtn = $(R.id.create_btn);

        initListener();
    }

    private void initListener() {
        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        mRecommendTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * @colorWrong
                 */
                WebViewActivity.open((Activity) itemView.getContext(), FansConstant.VFAN_INTRO_LIVE_URL);
            }
        });
    }

    @Override
    protected void bindView() {
        AvatarUtils.loadAvatarByUidTs(mAvatarIv,
                UserAccountManager.getInstance().getUuidAsLong(),
                MyUserInfoManager.getInstance().getAvatar(),
                true);

        mNameTv.setText(MyUserInfoManager.getInstance().getNickname());
    }
}
