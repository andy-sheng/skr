package com.wali.live.watchsdk.longtext.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.mi.live.data.config.GetConfigManager;
import com.mi.live.data.user.User;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.longtext.model.interior.item.OwnerFeedItemModel;

/**
 * Created by lan on 2017/9/20.
 */
public class OwnerFeedItemHolder extends BaseFeedItemHolder<OwnerFeedItemModel> {
    public BaseImageView mAvatarIv;
    public TextView mNameTv;

    public ImageView mGenderIv;
    public TextView mLevelTv;

    public OwnerFeedItemHolder(View view) {
        super(view);
    }

    @Override
    protected void initView() {
        mAvatarIv = $(R.id.avatar_iv);
        mNameTv = $(R.id.name_tv);

        mGenderIv = $(R.id.gender_iv);
        mLevelTv = $(R.id.level_tv);
    }

    @Override
    protected void bindView() {
        User user = mViewModel.getOwner();

        AvatarUtils.loadAvatarByUidTs(mAvatarIv, user.getUid(), user.getAvatar(), true);
        bindText(mNameTv, user.getNickname());

        if (user.getGender() == User.GENDER_MAN) {
            mGenderIv.setImageDrawable(itemView.getResources().getDrawable(R.drawable.all_man));
        } else if (user.getGender() == User.GENDER_WOMAN) {
            mGenderIv.setImageDrawable(itemView.getResources().getDrawable(R.drawable.all_women));
        } else {
            mGenderIv.setVisibility(View.GONE);
        }

        GetConfigManager.LevelItem levelItem = ItemDataFormatUtils.getLevelItem(user.getLevel());
        mLevelTv.setText(String.valueOf(user.getLevel()));
        mLevelTv.setBackground(levelItem.drawableBG);
        mLevelTv.setCompoundDrawables(levelItem.drawableLevel, null, null, null);
    }
}
