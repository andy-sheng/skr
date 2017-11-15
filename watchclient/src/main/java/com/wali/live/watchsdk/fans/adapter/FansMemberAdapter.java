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
import com.wali.live.watchsdk.component.adapter.ClickItemAdapter;
import com.wali.live.watchsdk.fans.model.member.FansMemberModel;
import com.wali.live.watchsdk.fans.utils.FansInfoUtils;

import static com.wali.live.component.view.Utils.$click;
import static com.wali.live.watchsdk.fans.adapter.FansMemberAdapter.FooterItem.STATE_DONE;
import static com.wali.live.watchsdk.fans.model.member.FansMemberModel.VIP_TYPE_MONTH;
import static com.wali.live.watchsdk.fans.model.member.FansMemberModel.VIP_TYPE_YEAR;

/**
 * Created by yangli on 2017/11/13.
 *
 * @module 粉丝团成员列表适配器
 */
public class FansMemberAdapter extends ClickItemAdapter<FansMemberModel,
        ClickItemAdapter.BaseHolder, FansMemberAdapter.IMemberClickListener> {

    private static final int ITEM_TYPE_NORMAL = 0;
    private static final int ITEM_TYPE_FOOTER = 1;

    private int mGroupCharmLevel; // 群经验值

    protected final FooterItem mFooterItem = new FooterItem();

    @Override
    protected BaseHolder newViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_TYPE_NORMAL: {
                View view = mInflater.inflate(R.layout.fans_member_list_item, parent, false);
                return new MemberHolder(view);
            }
            case ITEM_TYPE_FOOTER: {
                View view = mInflater.inflate(R.layout.fans_member_foot_item, parent, false);
                return new FooterHolder(view);
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
            holder.bindView(mFooterItem, null);
        } else {
            super.onBindViewHolder(holder, position);
        }
    }

    protected final void updateLoadingState(int state) {
        if (mFooterItem.getState() == state) {
            return;
        }
        mFooterItem.setState(state);
        notifyItemChanged(mItems.size());
    }

    public final void hideLoading() {
        updateLoadingState(FooterItem.STATE_HIDDEN);
    }

    public final void showLoading() {
        updateLoadingState(FooterItem.STATE_LOADING);
    }

    public final void onLoadingDone(boolean hasMore) {
        if (hasMore) {
            updateLoadingState(FooterItem.STATE_DONE);
        } else {
            updateLoadingState(FooterItem.STATE_NO_MORE);
        }
    }

    public final void onLoadingFailed() {
        updateLoadingState(FooterItem.STATE_FAILED);
    }

    protected static class FooterItem {
        public static final int STATE_HIDDEN = 0;
        public static final int STATE_LOADING = 1;
        public static final int STATE_DONE = 2;
        public static final int STATE_NO_MORE = 3;
        public static final int STATE_FAILED = 4;

        private int state;

        public final int getState() {
            return state;
        }

        public final void setState(int state) {
            this.state = state;
        }
    }

    protected static class FooterHolder extends ClickItemAdapter.BaseHolder<FooterItem, Object> {
        private TextView mStatusView;

        public FooterHolder(View view) {
            super(view);
            mStatusView = $(R.id.status_view);
        }

        @Override
        public void bindView(FooterItem item, Object listener) {
            switch (item.state) {
                case FooterItem.STATE_LOADING:
                    mStatusView.setText(R.string.vfan_member_loading);
                    break;
                case STATE_DONE:
                    mStatusView.setText(R.string.loading_tips_done);
                    break;
                case FooterItem.STATE_NO_MORE:
                    mStatusView.setText(R.string.loading_tips_no_more);
                    break;
                case FooterItem.STATE_FAILED:
                    mStatusView.setText(R.string.loading_tips_failed);
                    break;
                default:
                    break;
            }
        }
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

        void onClickFocus(FansMemberModel mItem);

        void onClickSixin(FansMemberModel mItem);
    }
}
