package com.component.relation.view;

import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.component.busilib.view.AvatarView;
import com.component.busilib.view.NickNameView;
import com.component.relation.adapter.RelationAdapter;

public class RelationHolderView extends RecyclerView.ViewHolder {
    ConstraintLayout mContent;
    AvatarView mAvatarIv;
    NickNameView mNickNameTv;
    ExTextView mFollowTv;
    ExTextView mSendTv;
    ExTextView mStatusTv;
    ExTextView mIntimacyTv;

    int mMode;
    int position;
    UserInfoModel userInfoModel;

    public RelationHolderView(View itemView, int mode, final RecyclerOnItemClickListener recyclerOnItemClickListener) {
        super(itemView);

        this.mMode = mode;
        mContent = itemView.findViewById(R.id.content);
        mAvatarIv = itemView.findViewById(R.id.avatar_iv);
        mNickNameTv = itemView.findViewById(R.id.nickname_tv);
        mFollowTv = itemView.findViewById(R.id.follow_tv);
        mSendTv = itemView.findViewById(R.id.send_tv);
        mStatusTv = itemView.findViewById(R.id.status_tv);
        mIntimacyTv = itemView.findViewById(R.id.intimacy_tv);

        mFollowTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (recyclerOnItemClickListener != null) {
                    recyclerOnItemClickListener.onItemClicked(mFollowTv, position, userInfoModel);
                }
            }
        });

        mContent.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (recyclerOnItemClickListener != null) {
                    recyclerOnItemClickListener.onItemClicked(mContent, position, userInfoModel);
                }
            }
        });
    }

    public void bind(int position, UserInfoModel userInfoModel) {
        this.position = position;
        this.userInfoModel = userInfoModel;

        mNickNameTv.setAllStateText(userInfoModel);
        mAvatarIv.bindData(userInfoModel);

        if (mMode == UserInfoManager.RELATION_BLACKLIST) {
            mFollowTv.setVisibility(View.VISIBLE);
            mFollowTv.setClickable(true);
            mFollowTv.setText("移出");
            mFollowTv.setTextColor(Color.parseColor("#3B4E79"));
            mFollowTv.setBackground(RelationAdapter.mFollowDrawable);
        } else {
            if (userInfoModel.getUserId() == MyUserInfoManager.INSTANCE.getUid()) {
                mFollowTv.setVisibility(View.GONE);
                return;
            } else {
                if (mMode == UserInfoManager.RELATION.FANS.getValue() || mMode == 0) {
                    if (userInfoModel.isFriend()) {
                        mFollowTv.setVisibility(View.VISIBLE);
                        mFollowTv.setText("已互关");
                        mFollowTv.setClickable(false);
                        mFollowTv.setTextColor(Color.parseColor("#AD6C00"));
                        mFollowTv.setBackground(RelationAdapter.mFriendDrawable);
                    } else if (userInfoModel.isFollow()) {
                        mFollowTv.setVisibility(View.VISIBLE);
                        mFollowTv.setText("已关注");
                        mFollowTv.setClickable(false);
                        mFollowTv.setTextColor(Color.parseColor("#3B4E79"));
                        mFollowTv.setBackground(RelationAdapter.mFollowDrawable);
                    } else {
                        mFollowTv.setVisibility(View.VISIBLE);
                        mFollowTv.setText("+关注");
                        mFollowTv.setClickable(true);
                        mFollowTv.setTextColor(Color.parseColor("#AD6C00"));
                        mFollowTv.setBackground(RelationAdapter.mUnFollowDrawable);
                    }
                } else {
                    mFollowTv.setVisibility(View.GONE);
                }
            }
        }

        // 只是关心在线和离线
        if (mMode != UserInfoManager.RELATION.FANS.getValue()) {
            if (userInfoModel.getStatus() >= UserInfoModel.EF_ONLINE) {
                mStatusTv.setCompoundDrawablePadding(U.getDisplayUtils().dip2px(3));
                mStatusTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.greendot, 0, 0, 0);
                mStatusTv.setVisibility(View.VISIBLE);
                mStatusTv.setText(userInfoModel.getStatusDesc());
            } else if (userInfoModel.getStatus() == UserInfoModel.EF_OFFLINE) {
                mStatusTv.setCompoundDrawablePadding(U.getDisplayUtils().dip2px(3));
                mStatusTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.graydot, 0, 0, 0);
                mStatusTv.setVisibility(View.VISIBLE);
                mStatusTv.setText(userInfoModel.getStatusDesc());
            } else {
                mStatusTv.setVisibility(View.GONE);
            }
        } else {
            mStatusTv.setVisibility(View.GONE);
        }

        if (mMode == UserInfoManager.RELATION.FRIENDS.getValue() && userInfoModel.hasIntimacy()) {
            mIntimacyTv.setVisibility(View.VISIBLE);
            mIntimacyTv.setText("亲密度 " + userInfoModel.getIntimacy());
        } else {
            mIntimacyTv.setVisibility(View.GONE);
        }
    }
}