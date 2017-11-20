package com.wali.live.watchsdk.fans.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.HttpImage;
import com.base.utils.display.DisplayUtils;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.user.User;
import com.wali.live.common.barrage.view.utils.FansInfoUtils;
import com.wali.live.dao.SixinMessage;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.adapter.ClickItemAdapter;
import com.wali.live.watchsdk.component.adapter.LoadingItemAdapter;
import com.wali.live.watchsdk.fans.model.member.FansMemberModel;
import com.wali.live.watchsdk.sixin.pojo.SixinTarget;

import static com.wali.live.component.view.Utils.$click;
import static com.wali.live.watchsdk.fans.model.member.FansMemberModel.VIP_TYPE_MONTH;
import static com.wali.live.watchsdk.fans.model.member.FansMemberModel.VIP_TYPE_YEAR;

/**
 * Created by yangli on 2017/11/13.
 *
 * @module 粉丝团成员列表适配器
 */
public class FansMemberAdapter extends LoadingItemAdapter<FansMemberModel,
        ClickItemAdapter.BaseHolder, FansMemberAdapter.IMemberClickListener> {

    protected static final int ITEM_TYPE_NORMAL = 0;

    private int mGroupCharmLevel; // 群经验值

    public void setGroupCharmLevel(int groupCharmLevel) {
        mGroupCharmLevel = groupCharmLevel;
    }

    @Override
    protected BaseHolder newViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_TYPE_NORMAL: {
                View view = mInflater.inflate(R.layout.fans_member_list_item, parent, false);
                return new MemberHolder(view);
            }
        }
        return super.newViewHolder(parent, viewType);
    }

    protected class MemberHolder extends BaseHolder<FansMemberModel, IMemberClickListener>
            implements View.OnClickListener {

        private FansMemberModel mItem;
        private SixinTarget mSixinTarget;

        private BaseImageView mMemberAvatar;
        private TextView mMemberName;
        private TextView mMyExpTitle;
        private TextView mRoleName;
        private TextView mPetExpValueTv;
        private TextView mFocusBtn;
        private TextView mSixinBtn;
        private ImageView mVipTypeBtn;
        private View mSplitLine;
        private TextView mPetExpTitleTv;

        @Override
        public void onClick(View v) {
            if (mListener == null) {
                return;
            }
            final int i = v.getId();
            if (i == R.id.focus_btn) {
                mListener.onClickFocus(mItem);
            } else if (i == R.id.sixin_btn) {
                if (mSixinTarget == null) {
                    mSixinTarget = new SixinTarget(new User());
                }
                // User信息
                final User user = mSixinTarget.getTargetUser();
                user.setUid(mItem.getUuid());
                user.setNickname(mItem.getNickname());
                user.setAvatar(mItem.getAvatar());
                user.setIsFocused(mItem.isFollow());
                // 关注信息
                if (mItem.isBothWay()) {
                    mSixinTarget.setFocusState(SixinMessage.MSG_STATUE_BOTHFOUCS);
                } else if (mItem.isFollow()) {
                    mSixinTarget.setFocusState(SixinMessage.MSG_STATUS_ONLY_ME_FOUCS);
                } else {
                    mSixinTarget.setFocusState(SixinMessage.MSG_STATUS_UNFOUCS);
                }
                mListener.onClickSixin(mSixinTarget);
            } else {
                mListener.onItemClick(mItem);
            }
        }

        public MemberHolder(View view) {
            super(view);
            mMemberAvatar = $(R.id.member_avatar);
            mMemberName = $(R.id.member_name);
            mMyExpTitle = $(R.id.my_exp_title);
            mRoleName = $(R.id.role_name);
            mPetExpValueTv = $(R.id.pet_exp_value_tv);
            mFocusBtn = $(R.id.focus_btn);
            mSixinBtn = $(R.id.sixin_btn);
            mSplitLine = $(R.id.split_line);
            mPetExpTitleTv = $(R.id.pet_exp_title);
            mVipTypeBtn = $(R.id.vip_type);
            $click(mFocusBtn, this);
            $click(mSixinBtn, this);
            $click(itemView, this);
        }

        @Override
        public void bindView(FansMemberModel memberInfo, IMemberClickListener listener) {
            mItem = memberInfo;

            HttpImage httpImage = new HttpImage(AvatarUtils.getAvatarUrlByUid(memberInfo.getUuid(), 0));
            httpImage.setWidth(DisplayUtils.dip2px(34));
            httpImage.setHeight(DisplayUtils.dip2px(34));
            httpImage.setIsCircle(true);
            httpImage.setLoadingDrawable(getResources().getDrawable(R.drawable.avatar_default_a));
            httpImage.setFailureDrawable(getResources().getDrawable(R.drawable.avatar_default_a));
            FrescoWorker.loadImage(mMemberAvatar, httpImage);

            mMemberName.setText(memberInfo.getNickname());
            switch (memberInfo.getVipType()) {
                case VIP_TYPE_MONTH:
                    mVipTypeBtn.setVisibility(View.VISIBLE);
                    mVipTypeBtn.setImageDrawable(getResources().getDrawable(R.drawable.live_pet_live_member_month));
                    break;
                case VIP_TYPE_YEAR:
                    mVipTypeBtn.setVisibility(View.VISIBLE);
                    mVipTypeBtn.setImageDrawable(getResources().getDrawable(R.drawable.live_pet_live_member_year));
                    break;
                default:
                    mVipTypeBtn.setVisibility(View.GONE);
                    break;
            }

            if (memberInfo.getMemType() == VFansCommonProto.GroupMemType.OWNER_VALUE) {
                mMyExpTitle.setText("");
                mMyExpTitle.setBackgroundResource(FansInfoUtils.getImageResourcesByCharmLevelValue(mGroupCharmLevel));
                mPetExpValueTv.setVisibility(View.GONE);
                mSplitLine.setVisibility(View.GONE);
                mPetExpTitleTv.setVisibility(View.GONE);
            } else {
                mMyExpTitle.setText(memberInfo.getMedalName());
                mMyExpTitle.setBackgroundResource(FansInfoUtils.getGroupMemberLevelDrawable(memberInfo.getPetLevel()));
                mPetExpValueTv.setVisibility(View.VISIBLE);
                mSplitLine.setVisibility(View.VISIBLE);
                mPetExpTitleTv.setVisibility(View.VISIBLE);
            }

            mRoleName.setText(FansInfoUtils.getMemberRoleStringByType(memberInfo.getMemType()));

            mPetExpValueTv.setText(String.valueOf(memberInfo.getPetExp()));

            if (memberInfo.isFollow() || memberInfo.isBothWay()) {
                mFocusBtn.setText(R.string.vfans_has_focus);
                mFocusBtn.setClickable(false);
                mFocusBtn.setTextColor(getResources().getColor(R.color.color_black_trans_20));
            } else {
                mFocusBtn.setText(R.string.vfans_focus);
                mFocusBtn.setClickable(true);
                mFocusBtn.setTextColor(getResources().getColor(R.color.cash_color));
            }

            if (memberInfo.getUuid() == MyUserInfoManager.getInstance().getUuid()) {
                mFocusBtn.setVisibility(View.INVISIBLE);
                mSixinBtn.setVisibility(View.INVISIBLE);
            } else {
                mFocusBtn.setVisibility(View.VISIBLE);
                mSixinBtn.setVisibility(View.VISIBLE);
            }
        }
    }

    public interface IMemberClickListener {

        void onItemClick(FansMemberModel item);

        void onClickFocus(FansMemberModel item);

        void onClickSixin(SixinTarget target);
    }
}
