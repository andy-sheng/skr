package com.wali.live.watchsdk.recipient.adapter;

import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.mi.live.data.config.GetConfigManager;
import com.mi.live.data.data.UserListData;
import com.mi.live.data.user.User;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.recipient.RecipientsSelectFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yurui on 3/7/16.
 *
 * @module 选人
 */
public class RecipientsSelectRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = "RecipientsSelectRecyclerAdapter";
    public static final int ITEM_TYPE_FOOTER = 101;    //列表的footer  展示loadmore正在加载 和 列表为空的状态
    public static final int ITEM_TYPE_FOLLOWING = 0;   //关注的item
    public static final int ITEM_TYPE_MANAGER = 1;     //管理员

    private int mItemNormalType = ITEM_TYPE_FOLLOWING;
    private int mMode = RecipientsSelectFragment.SELECT_MODE_SINGLE_CLICK;
    private List<Object> mDataList = new ArrayList<>();
    private Object mSelectItem;
    private boolean mShowLvlSx = true;
    private View mFooter;
    private OnItemClickListener mItemClickListener;

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    public void setShowLevelSex(boolean b) {
        mShowLvlSx = b;
    }

    public Object getSelectItem() {
        return mSelectItem;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_TYPE_FOOTER:
                if (mFooter == null) {
                    mFooter = LayoutInflater.from(GlobalData.app()).inflate
                            (R.layout.private_live_invite_people_loading, parent, false);
                }
                return new UserListDataHolder(mFooter);
            case ITEM_TYPE_MANAGER:
            case ITEM_TYPE_FOLLOWING:
                break;
        }
        View view = LayoutInflater.from(GlobalData.app()).inflate(R.layout.user_list_cell,
                parent, false);
        return new UserListDataHolder(view);
    }

    public RecipientsSelectRecyclerAdapter(int type, int mode) {
        mItemNormalType = type;
        mMode = mode;
    }

    public void setData(List<Object> list) {
        if (null != list) {
            mDataList = list;
            notifyDataSetChanged();
        }
    }

    private Object getData(int position) {
        if (position >= 0 && position < getItemCount()) {
            return mDataList.get(position);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount()) {
            return ITEM_TYPE_FOOTER;
        }
        return mItemNormalType;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        if (getItemViewType(position) == ITEM_TYPE_FOOTER) {
            return;
        }
        if (!(viewHolder instanceof UserListDataHolder)) {
            return;
        }
        final UserListDataHolder holder = (UserListDataHolder) viewHolder;
        final UserListData item = (UserListData) getData(position);
        if (item == null) {
            return;
        }
        holder.setUserListData(item);
        AvatarUtils.loadAvatarByUidTs(holder.avatarIv, item.userId, item.avatar, true);
        holder.userNameTv.setText(!TextUtils.isEmpty(item.userNickname) ? item.userNickname : String.valueOf(item.userId));
        holder.signTv.setVisibility(View.GONE);
        if (mShowLvlSx) {
            GetConfigManager.LevelItem levelItem = ItemDataFormatUtils.getLevelItem(item.level);
            holder.levelTv.setText(String.valueOf(item.level + ""));
            holder.levelTv.setBackgroundDrawable(levelItem.drawableBG);
            holder.levelTv.setCompoundDrawables(levelItem.drawableLevel, null, null, null);

            holder.imgGenderIv.setVisibility(View.VISIBLE);
            if (item.gender == User.GENDER_MAN) {
                holder.imgGenderIv.setImageDrawable(GlobalData.app().getResources().getDrawable(R.drawable.all_man));
            } else if (item.gender == User.GENDER_WOMAN) {
                holder.imgGenderIv.setImageDrawable(GlobalData.app().getResources().getDrawable(R.drawable.all_women));
            } else {
                holder.imgGenderIv.setVisibility(View.GONE);
            }
            if (item.certificationType > 0) {
                holder.badgeIv.setVisibility(View.GONE);
                holder.badgeVipIv.setVisibility(View.VISIBLE);
                holder.badgeVipIv.setImageDrawable(ItemDataFormatUtils.getCertificationImgSource(item.certificationType));
            } else {
                holder.badgeIv.setVisibility(View.GONE);
                holder.badgeVipIv.setVisibility(View.GONE);
            }
        } else {
            holder.levelTv.setVisibility(View.GONE);
            holder.imgGenderIv.setVisibility(View.GONE);
            if (item.certificationType > 0) {
                holder.badgeIv.setVisibility(View.GONE);
                holder.badgeVipIv.setVisibility(View.VISIBLE);
                holder.badgeVipIv.setImageDrawable(ItemDataFormatUtils.getCertificationImgSource(item.certificationType));
            } else {
                holder.badgeIv.setVisibility(View.VISIBLE);
                holder.badgeVipIv.setVisibility(View.GONE);
                holder.badgeIv.setImageDrawable(ItemDataFormatUtils.getLevelSmallImgSource(item.level));
            }
        }
        if (mMode == RecipientsSelectFragment.SELECT_MODE_SINGLE_SELECT) {
            holder.checkbox.setVisibility(View.VISIBLE);
            if (mSelectItem != null && item.userId == ((UserListData) mSelectItem).userId) {
                holder.checkbox.setChecked(true);
            } else {
                holder.checkbox.setChecked(false);
            }
        }
    }

    @Nullable
    final protected <V extends View> void $click(V view, View.OnClickListener listener) {
        if (view == null) {
            return;
        }
        view.setOnClickListener(listener);
    }

    @Nullable
    final protected <V extends View> V $(View parent, @IdRes int resId) {
        if (parent == null) {
            return null;
        }
        return (V) parent.findViewById(resId);
    }

    class UserListDataHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        BaseImageView avatarIv;
        TextView userNameTv;
        TextView signTv;
        TextView levelTv;
        CheckBox checkbox;
        ImageView stateBtn;
        ImageView badgeIv;
        ImageView badgeVipIv;
        ImageView imgGenderIv;
        View clickArea;

        UserListData mUserListData;

        UserListDataHolder(View view) {
            super(view);
            if (mFooter == view) {
                return;
            }
            avatarIv = $(itemView, R.id.user_list_avatar);
            userNameTv = $(itemView, R.id.txt_username);
            signTv = $(itemView, R.id.txt_tip);
            levelTv = $(itemView, R.id.level_tv);
            checkbox = $(itemView, R.id.checkbox);
            stateBtn = $(itemView, R.id.img_follow_state);
            badgeIv = $(itemView, R.id.img_badge);
            badgeVipIv = $(itemView, R.id.img_badge_vip);
            imgGenderIv = $(itemView, R.id.img_gender);
            clickArea = $(itemView, R.id.btn_area);
            $click(itemView, this);
        }

        public void setUserListData(UserListData userListData) {
            mUserListData = userListData;
        }

        @Override
        public void onClick(View v) {
            if (mUserListData == null) {
                return;
            }
            switch (mItemNormalType) {
                case ITEM_TYPE_FOLLOWING:
                case ITEM_TYPE_MANAGER:
                    if (mMode == RecipientsSelectFragment.SELECT_MODE_SINGLE_SELECT) {
                        if (mSelectItem == null || mUserListData.userId != ((UserListData) mSelectItem).userId) {
                            mSelectItem = mUserListData;
                        }
                        notifyDataSetChanged();
                    }
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClickListener(mUserListData);
                    }
                    break;
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClickListener(UserListData listData);
    }
}
