package com.wali.live.watchsdk.fans.adapter;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.HttpImage;
import com.base.utils.display.DisplayUtils;
import com.wali.live.common.barrage.view.utils.FansInfoUtils;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.adapter.ClickItemAdapter;
import com.wali.live.watchsdk.component.adapter.LoadingItemAdapter;
import com.wali.live.watchsdk.fans.adapter.FansMemberAdapter.MemberItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.wali.live.watchsdk.fans.model.member.FansMemberModel.VIP_TYPE_MONTH;
import static com.wali.live.watchsdk.fans.model.member.FansMemberModel.VIP_TYPE_YEAR;

/**
 * Created by yangli on 2017/11/17.
 *
 * @module 粉丝团成员管理列表适配器
 */
public class FansMemberManagerAdapter extends LoadingItemAdapter<ClickItemAdapter.TypeItem,
        ClickItemAdapter.BaseHolder, FansMemberManagerAdapter.IMemberClickListener> {

    protected static final int ITEM_TYPE_NORMAL = 0;
    protected static final int ITEM_TYPE_OWNER = 1;
    protected static final int ITEM_TYPE_LABEL = 2;

    private boolean mIsBatchDeleteMode = false;
    private final Set<MemberItem> mSelectedSet = new HashSet<>(10); // 已选择

    private int mGroupCharmLevel; // 群经验值
    private int mMyMemType = VFansCommonProto.GroupMemType.DEPUTY_ADMIN_VALUE;

    public final List<MemberItem> getSelectedItem() {
        return mSelectedSet.isEmpty() ? null : new ArrayList<>(mSelectedSet);
    }

    public final void removeSelection(@NonNull List<MemberItem> memberList) {
        mSelectedSet.removeAll(memberList);
    }

    public final void setGroupCharmLevel(int groupCharmLevel) {
        mGroupCharmLevel = groupCharmLevel;
    }

    public final void setMyMemType(int memType) {
        mMyMemType = memType;
    }

    public final void setIsBatchDeleteMode(boolean isBatchDeleteMode) {
        if (mIsBatchDeleteMode != isBatchDeleteMode) {
            mIsBatchDeleteMode = isBatchDeleteMode;
            mSelectedSet.clear();
            notifyDataSetChanged();
        }
    }

    public void setItemDataEx(@NonNull List<MemberItem> dataSet) {
        mItems.clear();
        if (!dataSet.isEmpty()) {
            final int size = dataSet.size();
            ((ArrayList) mItems).ensureCapacity(size + 4); // with four label
            int memType = -1, i = 0;
            for (MemberItem elem : dataSet) {
                if (memType != elem.getMemType()) {
                    memType = elem.getMemType();
                    mItems.add(new LabelItem(FansInfoUtils.getMemberRoleStringByType(memType))); // TODO-YangLi 使用对象池优化LabelItem
                    if (memType == VFansCommonProto.GroupMemType.MASS_VALUE) {
                        ((ArrayList) mItems).addAll(dataSet.subList(i, size));
                        break;
                    }
                }
                ++i;
                mItems.add(elem);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    protected BaseHolder newViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_TYPE_NORMAL: {
                View view = mInflater.inflate(R.layout.fans_member_manager_list_item, parent, false);
                return new MemberHolder(view, false);
            }
            case ITEM_TYPE_LABEL: {
                View view = mInflater.inflate(R.layout.fans_member_manager_label_item, parent, false);
                return new LabelHolder(view);
            }
            case ITEM_TYPE_OWNER: {
                View view = mInflater.inflate(R.layout.fans_member_manager_list_item, parent, false);
                return new MemberHolder(view, true);
            }
        }
        return super.newViewHolder(parent, viewType);
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.size() == position ? ITEM_TYPE_FOOTER :
                (position == 1 ? ITEM_TYPE_OWNER : mItems.get(position).getItemType());
    }

    public static class LabelItem implements TypeItem {

        private int strResId;

        public LabelItem(int strResId) {
            this.strResId = strResId;
        }

        @Override
        public final int getItemType() {
            return ITEM_TYPE_LABEL;
        }
    }

    protected static class LabelHolder extends ClickItemAdapter.BaseHolder<LabelItem, Object> {
        private TextView mTitleView;

        public LabelHolder(View view) {
            super(view);
            mTitleView = $(R.id.label_title);
        }

        @Override
        public void bindView(LabelItem item, Object listener) {
            if (item.strResId > 0) {
                itemView.setVisibility(View.VISIBLE);
                mTitleView.setText(item.strResId);
            } else {
                itemView.setVisibility(View.GONE);
            }
        }
    }

    protected class MemberHolder extends BaseHolder<MemberItem, IMemberClickListener>
            implements View.OnClickListener {

        private MemberItem mItem;

        private CheckBox mCheckBox;
        private BaseImageView mMemberAvatar;
        private TextView mMemberName;
        private TextView mMyExpTitle;
        private ImageView mVipTypeBtn;
        private TextView mLoveValue;
        private TextView mNewLoveValue;
        private TextView mNewLoveTitle;
        private TextView mLoveTitle;

        @Override
        public void onClick(View v) {
            if (mListener == null) {
                return;
            }
            switch (v.getId()) {
                default:
                    break;
            }
            if (mMyMemType < mItem.getMemType()) {
                if (mIsBatchDeleteMode) {
                    mCheckBox.setChecked(!mCheckBox.isChecked());
                } else {
                    mListener.onItemClick(mItem);
                }
            }
        }

        public MemberHolder(View view, final boolean isOwner) {
            super(view);
            mLoveTitle = $(R.id.love_value_title);
            mCheckBox = $(R.id.checkbox);
            mMemberAvatar = $(R.id.member_avatar);
            mMemberName = $(R.id.member_name);
            mMyExpTitle = $(R.id.my_exp_title);
            mVipTypeBtn = $(R.id.vip_type);
            mLoveValue = $(R.id.love_value);
            mNewLoveValue = $(R.id.new_love_value);
            mNewLoveTitle = $(R.id.sevendays_love_title);
            if (isOwner) {
                mLoveTitle.setVisibility(View.GONE);
                mLoveValue.setVisibility(View.GONE);
                mNewLoveTitle.setVisibility(View.GONE);
                mNewLoveValue.setVisibility(View.GONE);
            }
            mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (mListener == null) {
                        return;
                    }
                    if (isChecked) {
                        mSelectedSet.add(mItem);
                    } else {
                        mSelectedSet.remove(mItem);
                    }
                    mListener.onItemSelectionChange(mSelectedSet.size());
                }
            });
            itemView.setOnClickListener(this);
        }

        @Override
        public void bindView(MemberItem memberItem, IMemberClickListener listener) {
            mItem = memberItem;

            HttpImage httpImage = new HttpImage(AvatarUtils.getAvatarUrlByUid(memberItem.getUuid(), 0));
            httpImage.setWidth(DisplayUtils.dip2px(34));
            httpImage.setHeight(DisplayUtils.dip2px(34));
            httpImage.setIsCircle(true);
            httpImage.setLoadingDrawable(getResources().getDrawable(R.drawable.avatar_default_a));
            httpImage.setFailureDrawable(getResources().getDrawable(R.drawable.avatar_default_a));
            FrescoWorker.loadImage(mMemberAvatar, httpImage);

            mMemberName.setText(memberItem.getNickname());
            mNewLoveValue.setText(String.valueOf(memberItem.getNewLoveValue()));
            mLoveValue.setText(String.valueOf(memberItem.getPetExp()));
            switch (memberItem.getVipType()) {
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

            if (memberItem.getMemType() == VFansCommonProto.GroupMemType.OWNER_VALUE) {
                mMyExpTitle.setText("");
                mMyExpTitle.setBackgroundResource(FansInfoUtils.getImageResourcesByCharmLevelValue(mGroupCharmLevel));
            } else {
                mMyExpTitle.setText(memberItem.getMedalName());
                mMyExpTitle.setBackgroundResource(FansInfoUtils.getGroupMemberLevelDrawable(memberItem.getPetLevel()));
            }
            if (mMyMemType < mItem.getMemType()) {
                itemView.setClickable(true);
                if (mIsBatchDeleteMode) {
                    mCheckBox.setVisibility(View.VISIBLE);
                    mCheckBox.setChecked(mSelectedSet.contains(mItem));
                } else {
                    mCheckBox.setVisibility(View.GONE);
                }
            } else {
                itemView.setClickable(false);
                mCheckBox.setVisibility(View.GONE);
            }

        }
    }

    public interface IMemberClickListener {

        void onItemClick(MemberItem item);

        void onItemSelectionChange(int cnt);

    }

}
