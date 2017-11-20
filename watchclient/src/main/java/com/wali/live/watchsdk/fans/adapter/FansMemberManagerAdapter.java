package com.wali.live.watchsdk.fans.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.global.GlobalData;
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
import com.wali.live.watchsdk.fans.model.member.FansMemberModel;

import static com.wali.live.component.view.Utils.$click;
import static com.wali.live.watchsdk.fans.model.member.FansMemberModel.VIP_TYPE_MONTH;
import static com.wali.live.watchsdk.fans.model.member.FansMemberModel.VIP_TYPE_YEAR;

/**
 * Created by yangli on 2017/11/17.
 *
 * @module 粉丝团成员管理列表适配器
 */
public class FansMemberManagerAdapter extends LoadingItemAdapter<FansMemberModel,
        ClickItemAdapter.BaseHolder, FansMemberManagerAdapter.IMemberClickListener> {

    protected static final int ITEM_TYPE_NORMAL = 0;
    protected static final int ITEM_TYPE_OWNER = 1;
    protected static final int ITEM_TYPE_LABEL = 2;

    private boolean mIsBatchDeleteMode = false;

    private int mGroupCharmLevel; // 群经验值

    public void setIsBatchDeleteMode(boolean isBatchDeleteMode) {
        if (mIsBatchDeleteMode != isBatchDeleteMode) {
            mIsBatchDeleteMode = isBatchDeleteMode;
            notifyDataSetChanged();
        }
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

    public static class LabelItem {
        public static final int TYPE_GROUP_OWNER = 0;
        public static final int TYPE_GROUP_ADMIN = 1;
        public static final int TYPE_GROUP_DEPUTY_ADMIN = 2;
        public static final int TYPE_GROUP_MASS = 3;

        private int type;

        public LabelItem(int type) {
            this.type = type;
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
            itemView.setVisibility(View.VISIBLE);
            switch (item.type) {
                case LabelItem.TYPE_GROUP_OWNER:
                    mTitleView.setText(R.string.vfans_owner);
                    break;
                case LabelItem.TYPE_GROUP_ADMIN:
                    mTitleView.setText(R.string.vfans_admin);
                    break;
                case LabelItem.TYPE_GROUP_DEPUTY_ADMIN:
                    mTitleView.setText(R.string.vfans_deput_admin);
                    break;
                case LabelItem.TYPE_GROUP_MASS:
                    mTitleView.setText(R.string.vfans_mass);
                    break;
                default:
                    itemView.setVisibility(View.GONE);
                    break;
            }
        }
    }

    protected class MemberHolder extends BaseHolder<FansMemberModel, IMemberClickListener>
            implements View.OnClickListener {

        private FansMemberModel mItem;

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
                    mListener.onItemClick(mItem);
                    break;
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
                mLoveTitle.setVisibility(View.VISIBLE);
                mLoveValue.setVisibility(View.VISIBLE);
                mNewLoveTitle.setVisibility(View.VISIBLE);
                mNewLoveValue.setVisibility(View.VISIBLE);
            }
            mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    if (isChecked) {
//                        vfansMemberManagerAdapter.mSelelctMap.put(mGroupMemberInfo.getUuid(), mGroupMemberInfo);
//                    } else {
//                        vfansMemberManagerAdapter.mSelelctMap.remove(mGroupMemberInfo.getUuid());
//                    }
                }
            });

            $click(itemView, this);
//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (mGroupMemberInfo != null) {
//                        if (mCurrentShowMode == FansMemberManagerFragment.MODEL_BATCH_REMOVE) {
//                            if (mVfansMemberManagerViewWeakReference != null && mVfansMemberManagerViewWeakReference.get() != null
//                                    && mVfansMemberManagerViewWeakReference.get().getMyRole() < mGroupMemberInfo.getMemType()) {
//                                mCheckBox.setChecked(!mCheckBox.isChecked());
//                            }
//                        } else if (mCurrentShowMode == FansMemberManagerFragment.MODEL_MANAGER) {
//                            if (mVfansMemberManagerViewWeakReference != null && mVfansMemberManagerViewWeakReference.get() != null
//                                    && mVfansMemberManagerViewWeakReference.get().getMyRole() < mGroupMemberInfo.getMemType()) {
//                                onCliclMoreBtn();
//                            }
//                        } else if (mVfansMemberManagerViewWeakReference != null && mVfansMemberManagerViewWeakReference.get() != null) {
//                            mVfansMemberManagerViewWeakReference.get().openPersonInfo(mGroupMemberInfo.getUuid());
//                        }
//                    }
//                }
//            });
        }

        @Override
        public void bindView(FansMemberModel memberInfo, IMemberClickListener listener) {
            mItem = memberInfo;

            HttpImage httpImage = new HttpImage(AvatarUtils.getAvatarUrlByUid(memberInfo.getUuid(), 0));
            httpImage.setWidth(DisplayUtils.dip2px(34));
            httpImage.setHeight(DisplayUtils.dip2px(34));
            httpImage.setIsCircle(true);
            httpImage.setLoadingDrawable(GlobalData.app().getResources().getDrawable(R.drawable.avatar_default_a));
            httpImage.setFailureDrawable(GlobalData.app().getResources().getDrawable(R.drawable.avatar_default_a));
            FrescoWorker.loadImage(mMemberAvatar, httpImage);

            mMemberName.setText(memberInfo.getNickname());
            mNewLoveValue.setText(String.valueOf(memberInfo.getNewLoveValue()));
            mLoveValue.setText(String.valueOf(memberInfo.getPetExp()));
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
            } else {
                mMyExpTitle.setText(memberInfo.getMedalName());
                mMyExpTitle.setBackgroundResource(FansInfoUtils.getGroupMemberLevelDrawable(memberInfo.getPetLevel()));
            }

//            if (mCurrentShowMode == FansMemberManagerFragment.MODEL_BATCH_REMOVE) {
//                mMoreBtn.setVisibility(View.GONE);
//                if (mVfansMemberManagerViewWeakReference != null && mVfansMemberManagerViewWeakReference.get() != null
//                        && mVfansMemberManagerViewWeakReference.get().getMyRole() < memberInfo.getMemType()) {
//                    mCheckBox.setVisibility(View.VISIBLE);
//                } else {
//                    mCheckBox.setVisibility(View.GONE);
//                }
//            } else if (mCurrentShowMode == FansMemberManagerFragment.MODEL_MANAGER) {
//                if (mVfansMemberManagerViewWeakReference != null && mVfansMemberManagerViewWeakReference.get() != null
//                        && mVfansMemberManagerViewWeakReference.get().getMyRole() < memberInfo.getMemType()) {
//                    mMoreBtn.setVisibility(View.VISIBLE);
//                } else {
//                    mMoreBtn.setVisibility(View.GONE);
//                }
//                mCheckBox.setVisibility(View.GONE);
//            } else {
//                mMoreBtn.setVisibility(View.GONE);
//                mCheckBox.setVisibility(View.GONE);
//            }
        }
    }

    public interface IMemberClickListener {

        void onItemClick(FansMemberModel item);

    }

}
