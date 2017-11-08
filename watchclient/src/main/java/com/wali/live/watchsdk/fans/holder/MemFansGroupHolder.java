package com.wali.live.watchsdk.fans.holder;

import android.view.View;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.model.item.MemFansGroupModel;
import com.wali.live.watchsdk.fans.utils.FansInfoUtils;
import com.wali.live.watchsdk.lit.recycler.holder.BaseHolder;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lan on 2017/11/8.
 */
public class MemFansGroupHolder extends BaseHolder<MemFansGroupModel> {
    private BaseImageView mAvatarIv;

    private TextView mNameTv;
    private TextView mLevelTv;

    private TextView mExpValueTv;

    private TextView mTimeTv;

    private TextView mRenewalTv;

    public MemFansGroupHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initView() {
        mAvatarIv = $(R.id.avatar_iv);

        mNameTv = $(R.id.name_tv);
        mLevelTv = $(R.id.level_title_tv);

        mExpValueTv = $(R.id.exp_value_tv);
        mTimeTv = $(R.id.time_tv);

        mRenewalTv = $(R.id.renewal_tv);
    }

    @Override
    protected void bindView() {
        AvatarUtils.loadAvatarByUidTs(mAvatarIv, mViewModel.getZuid(), mViewModel.getZuidAvatar(), true);

        mNameTv.setText(mViewModel.getGroupName());
        mLevelTv.setText(mViewModel.getMedalValue());
        mLevelTv.setBackgroundResource(FansInfoUtils.getGroupMemberLevelDrawable(mViewModel.getPetLevel()));

        mExpValueTv.setText(itemView.getContext().getString(R.string.vfans_my_value) + ":" + String.valueOf(mViewModel.getPetExp()));

        if (mViewModel.getVipLevel() <= 0) {
            mTimeTv.setText("");
            mTimeTv.setBackground(itemView.getContext().getResources().getDrawable(R.drawable.pet_group_open_privilege));

            mRenewalTv.setText(R.string.vfans_open_privilege);
        } else {
            mRenewalTv.setText(R.string.vfans_renew_pay);
            mTimeTv.setBackground(null);

            Date date = new Date(mViewModel.getVipExpire() * 1000);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String time = String.format(itemView.getContext().getResources().getString(R.string.recharge_will_expire_date), format.format(date));
            mTimeTv.setText(time);
        }
    }
}
