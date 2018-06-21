package com.wali.live.watchsdk.personalcenter.relation.holder;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.config.GetConfigManager;
import com.mi.live.data.data.UserListData;
import com.mi.live.data.user.User;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.personalcenter.relation.contact.IFollowOptListener;

import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

/**
 * Created by zhujianning on 18-6-21.
 */

public class FollowFansHolder extends RecyclerView.ViewHolder{

    private final BaseImageView mAvatorIv;
    private final ImageView mBadgeIv;
    private final ImageView mVipBadgeIv;
    private final TextView mNameTv;
    private final ImageView mGenderIv;
    private final TextView mLevelTv;
    private final TextView mContentTv;
    private final RelativeLayout mFollowArea;
    private final ImageView mFollowBg;
    private final TextView mFollowTv;

    private UserListData mData;

    private IFollowOptListener mIFollowOptListener;

    public void setFollowOptListener(IFollowOptListener listener) {
        mIFollowOptListener = listener;
    }

    public FollowFansHolder(View itemView) {
        super(itemView);
        mAvatorIv = (BaseImageView) itemView.findViewById(R.id.user_list_avatar);
        mBadgeIv = (ImageView) itemView.findViewById(R.id.img_badge);
        mVipBadgeIv = (ImageView) itemView.findViewById(R.id.img_badge_vip);
        mNameTv = (TextView) itemView.findViewById(R.id.txt_username);
        mGenderIv = (ImageView) itemView.findViewById(R.id.img_gender);
        mLevelTv = (TextView) itemView.findViewById(R.id.level_tv);
        mContentTv = (TextView) itemView.findViewById(R.id.txt_tip);
        mFollowArea = (RelativeLayout) itemView.findViewById(R.id.btn_area);
        mFollowBg = (ImageView) itemView.findViewById(R.id.img_follow_state);
        mFollowTv = (TextView) itemView.findViewById(R.id.tv_follow_state);

        RxView.clicks(mFollowArea).throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        if(mData != null
                                && !mData.isFollowing
                                && mIFollowOptListener != null) {
                            mIFollowOptListener.follow(mData.userId);
                        }
                    }
                });
    }

    public void bind(UserListData data) {
        if(data == null) {
            return;
        }

        mData = data;

        //avator
        AvatarUtils.loadAvatarByUidTs(mAvatorIv, data.userId, data.avatar, true);

        //name
        if (!TextUtils.isEmpty(data.userNickname)) {
            mNameTv.setText(data.userNickname);
        } else {
            mNameTv.setText(data.userId + "");
        }

        //sign
        if (!TextUtils.isEmpty(data.signature)) {
            mContentTv.setText(data.signature);
            mContentTv.setVisibility(View.VISIBLE);
        } else {
            mContentTv.setVisibility(View.GONE);
        }

        //level icon
        GetConfigManager.LevelItem levelItem = ItemDataFormatUtils.getLevelItem(data.level);
        mLevelTv.setText(String.valueOf(data.level + ""));
        mLevelTv.setBackgroundDrawable(levelItem.drawableBG);
        mLevelTv.setCompoundDrawables(levelItem.drawableLevel, null, null, null);

        //badge
        if (data.certificationType > 0) {
            mBadgeIv.setVisibility(View.GONE);
            mVipBadgeIv.setVisibility(View.VISIBLE);
            mVipBadgeIv.setImageDrawable(ItemDataFormatUtils.getCertificationImgSource(data.certificationType));
        } else {
            mBadgeIv.setVisibility(View.GONE);
            mVipBadgeIv.setVisibility(View.GONE);
        }

        //gender
        mGenderIv.setVisibility(View.VISIBLE);
        if (data.gender == User.GENDER_MAN) {
            mGenderIv.setImageDrawable(GlobalData.app().getResources().getDrawable(R.drawable.all_man));
        } else if (data.gender == User.GENDER_WOMAN) {
            mGenderIv.setImageDrawable(GlobalData.app().getResources().getDrawable(R.drawable.all_women));
        } else {
            mGenderIv.setVisibility(View.GONE);
        }

        if(UserAccountManager.getInstance().getUuidAsLong() == data.userId) {
            mFollowTv.setVisibility(View.GONE);
        } else {
            mFollowTv.setVisibility(View.VISIBLE);
            if(data.isBothway) {
                mFollowArea.setEnabled(false);
                mFollowTv.setText(GlobalData.app().getResources().getString(R.string.follow_both));
            } else if(data.isFollowing) {
                mFollowArea.setEnabled(false);
                mFollowTv.setText(R.string.already_followed);
            } else {
                mFollowArea.setEnabled(true);
                mFollowTv.setText(R.string.follow);
            }
        }
    }
}
