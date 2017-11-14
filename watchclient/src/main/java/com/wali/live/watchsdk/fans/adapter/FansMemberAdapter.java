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
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.viewmodel.BaseViewModel;
import com.wali.live.watchsdk.component.adapter.ClickItemAdapter;
import com.wali.live.watchsdk.fans.model.member.FansMemberModel;
import com.wali.live.watchsdk.fans.utils.FansInfoUtils;

import static com.wali.live.component.view.Utils.$click;
import static com.wali.live.watchsdk.fans.model.member.FansMemberModel.VIP_TYPE_MONTH;
import static com.wali.live.watchsdk.fans.model.member.FansMemberModel.VIP_TYPE_YEAR;

/**
 * Created by yangli on 2017/11/13.
 *
 * @module 粉丝团成员列表适配器
 */
public class FansMemberAdapter extends ClickItemAdapter<BaseViewModel,
        ClickItemAdapter.BaseHolder, FansMemberAdapter.IMemberClickListener> {

    private static final int ITEM_TYPE_NORMAL = 0;
    private static final int ITEM_TYPE_FOOTER = 1;

    private int mGroupCharmLevel; // 群经验值

    protected FooterHolder mFooterHolder;

    @Override
    protected BaseHolder newViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_TYPE_NORMAL: {
                View view = mInflater.inflate(R.layout.fans_member_list_item, null);
                return new MemberHolder(view);
            }
            case ITEM_TYPE_FOOTER: {
                if (mFooterHolder == null) {
                    View view = mInflater.inflate(R.layout.fans_member_foot_item, null);
                    mFooterHolder = new FooterHolder(view);
                }
                return mFooterHolder;
            }
            default:
                return null;
        }
    }

    @Override
    public final int getItemViewType(int position) {
        return mItems.size() == position ? ITEM_TYPE_FOOTER : ITEM_TYPE_NORMAL;
    }

    @Override
    public int getItemCount() {
        return mItems.size() + 1;
    }

    @Override
    public void onBindViewHolder(BaseHolder holder, int position) {
        if (holder instanceof FooterHolder) {
            return;
        }
        super.onBindViewHolder(holder, position);
    }

    protected class MemberHolder extends BaseHolder<FansMemberModel, IMemberClickListener>
            implements View.OnClickListener {

        private FansMemberModel mItem;

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
                mListener.onClickSixin(mItem);
            } else {
                mListener.onItemClick(mItem);
            }
        }

        public MemberHolder(View view) {
            super(view);
            mMemberAvatar = (BaseImageView) itemView.findViewById(R.id.member_avatar);
            mMemberName = (TextView) itemView.findViewById(R.id.member_name);
            mMyExpTitle = (TextView) itemView.findViewById(R.id.my_exp_title);
            mRoleName = (TextView) itemView.findViewById(R.id.role_name);
            mPetExpValueTv = (TextView) itemView.findViewById(R.id.pet_exp_value_tv);
            mFocusBtn = (TextView) itemView.findViewById(R.id.focus_btn);
            mSixinBtn = (TextView) itemView.findViewById(R.id.sixin_btn);
            mSplitLine = itemView.findViewById(R.id.split_line);
            mPetExpTitleTv = (TextView) itemView.findViewById(R.id.pet_exp_title);
            mVipTypeBtn = (ImageView) itemView.findViewById(R.id.vip_type);
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

    protected static class FooterHolder extends ClickItemAdapter.BaseHolder {

        public FooterHolder(View view) {
            super(view);
        }

        @Override
        public void bindView(Object item, Object listener) {
        }
    }

    public interface IMemberClickListener {
        void onItemClick(FansMemberModel item);

        void onClickFocus(FansMemberModel mItem);

        void onClickSixin(FansMemberModel mItem);
    }
}
