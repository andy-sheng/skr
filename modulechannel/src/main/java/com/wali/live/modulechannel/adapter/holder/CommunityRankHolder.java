package com.wali.live.modulechannel.adapter.holder;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.common.core.avatar.AvatarUtils;
import com.common.image.fresco.BaseImageView;
import com.common.utils.ImageUtils;
import com.wali.live.modulechannel.R;
import com.wali.live.modulechannel.model.viewmodel.ChannelRankingViewModel;

import java.util.List;

/**
 * Created by yaojian on 16-7-16.
 *
 * @module 频道
 * @description 占位item
 */
public class CommunityRankHolder extends FixedHolder {
    public View mRootView;

    private TextView txtTitle;
    private BaseImageView mAvatarFirst;
    private BaseImageView mAvatarSecond;
    private BaseImageView mAvatarThird;
    private BaseImageView[] avatars = new BaseImageView[]{mAvatarFirst, mAvatarSecond, mAvatarThird};

    public CommunityRankHolder(View view) {
        super(view);
        mRootView = view;
    }

    @Override
    protected void initView() {
        mAvatarFirst = $(R.id.user_avatar_iv_first);
        mAvatarSecond = $(R.id.user_avatar_iv_second);
        mAvatarThird = $(R.id.user_avatar_iv_third);

        txtTitle = $(R.id.anchor_rank_title);
    }

    @Override
    protected void initContentView() {

    }

    @Override
    protected void bindView() {
        final ChannelRankingViewModel viewModel = mViewModel.get();
        List<ChannelRankingViewModel.UserItemData> datas = viewModel.getItemDatas();
        for (int i = 0; i < datas.size(); i++) {
            if (i < avatars.length) {
                AvatarUtils.loadAvatarByUrl(avatars[i], AvatarUtils.newParamsBuilder(datas.get(i).getUser().getUserId()).setTimestamp(datas.get(i).getUser().getAvatar()).setSizeType(ImageUtils.SIZE.SIZE_160).setCircle(true).build());
            }
        }

        txtTitle.setText(viewModel.getTitle());

        mRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(viewModel.getSchemeUri())) {
                    mJumpListener.jumpScheme(viewModel.getSchemeUri());
                }
            }
        });
    }

    @Override
    protected void bindRankingModel(ChannelRankingViewModel viewModel) {
    }
}
