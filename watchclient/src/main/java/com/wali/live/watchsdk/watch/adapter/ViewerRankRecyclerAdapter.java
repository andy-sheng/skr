package com.wali.live.watchsdk.watch.adapter;

import android.support.annotation.DrawableRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.mi.live.data.config.GetConfigManager;
import com.mi.live.data.query.model.ViewerModel;
import com.mi.live.data.user.User;
import com.wali.live.common.barrage.view.utils.NobleConfigUtils;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.utils.level.VipLevelUtil;
import com.wali.live.watchsdk.R;

import java.util.LinkedList;
import java.util.List;

public class ViewerRankRecyclerAdapter extends RecyclerView.Adapter {

    public static final String TAG = "ViewerRankRecyclerAdapter";

    private LinkedList<ViewerModel> mUsersList = new LinkedList<>();

    private OnItemClickListener mClickListener;

    private static final int TYPE_EMPTY = 9999; //空先不做
    private static final int TYPE_HEADER_TOP_3 = -1; //前三
    private static final int TYPE_ITEM = 1; //普通

    public ViewerRankRecyclerAdapter(OnItemClickListener listener) {
        this.mClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_HEADER_TOP_3) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewer_rank_list_total_top_3, parent, false);
            return new ViewerRankTopHolder(view);
        } else if (viewType == TYPE_ITEM) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewer_rank_list_item, parent, false);
            return new ViewerRankViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MyLog.d(TAG, " position " + position);
        if (holder instanceof ViewerRankTopHolder) {
            final ViewerRankTopHolder topHolder = (ViewerRankTopHolder) holder;
            setTopThreeData(topHolder, position);
        } else if (holder instanceof ViewerRankViewHolder) {
            ((ViewerRankViewHolder) holder).bind(getRankUser(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && getBasicItemCount() >= 3) {
            return TYPE_HEADER_TOP_3;
        }
        return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        if (getBasicItemCount() > 3) {
            return getBasicItemCount() - 2;// 将前三条聚合成一条
        } else {
            return getBasicItemCount();
        }
    }

    public ViewerModel getRankUser(int position) {
        if (getBasicItemCount() > 3) {
            position += 2;
        }
        if (position < 0 || position >= mUsersList.size()) {
            return null;
        }
        return mUsersList.get(position);
    }

    public void setUserList(List<ViewerModel> rankUserList) {
        if (rankUserList == null) {
            return;
        }
        mUsersList.clear();
        mUsersList.addAll(rankUserList);
        notifyDataSetChanged();
    }

    protected int getBasicItemCount() {
        return mUsersList.size();
    }

    public void setTopThreeData(ViewerRankTopHolder topThreeData, int position) {
        if (getBasicItemCount() < 3) {
            return;
        }

        for (int i = 0; i < ViewerRankTopHolder.CARD_NUM; i++) {
            final ViewerModel viewerModel = mUsersList.get(i);
            if (viewerModel == null) {
                return;
            }

            AvatarUtils.loadAvatarByUidTs(topThreeData.imgAvatars[i], viewerModel.getUid(), viewerModel.getAvatar(), true);
            topThreeData.txtNames[i].setText(viewerModel.getNickName());
            if (viewerModel.getNobleLevel() > 0) {
                bindNobelIcon(topThreeData.imgLevels[i], viewerModel.getNobleLevel());
            } else if (viewerModel.getVipLevel() > 0) {
                bindVipLevel(topThreeData.imgLevels[i], viewerModel.getVipLevel());
            } else {
                topThreeData.imgLevels[i].setVisibility(View.GONE);
                topThreeData.levels[i].setVisibility(View.VISIBLE);
                bindLevel(topThreeData.levels[i], viewerModel.getLevel());
            }

            topThreeData.rlytRoots[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickListener.onItemClick(viewerModel.getUid());
                }
            });
        }
    }

    private void bindNobelIcon(ImageView imageView, int nobleLevel) {
        imageView.setImageResource(NobleConfigUtils.getImageResoucesByNobelLevelInAvatar(nobleLevel));
    }

    private void bindVipLevel(ImageView imageView, int vipLevel) {
        int vipLevelIconIndex = vipLevel > VipLevelUtil.MAX_LEVEL_IMAGE_NO ? VipLevelUtil.MAX_LEVEL_IMAGE_NO : vipLevel;
        String iconName = "vipicon_" + vipLevelIconIndex;
        @DrawableRes int vipLevelIconId;
        try {
            vipLevelIconId = (int) R.drawable.class.getField(iconName).get(null);
            imageView.setImageResource(vipLevelIconId);
        } catch (NoSuchFieldException e) {
            MyLog.e(TAG, "not found drawable:" + iconName, e);
        } catch (IllegalAccessException e) {
            MyLog.e(TAG, "IllegalAccess:" + iconName, e);
        }
    }

    private void bindLevel(TextView textView, int level) {
        //显示等级
        int leve = level <= 1 ? 1 : level;
        GetConfigManager.LevelItem levelItem = ItemDataFormatUtils.getLevelItem(leve);
        textView.setText(String.valueOf(level));
        textView.setBackgroundDrawable(levelItem.drawableBG);
        textView.setCompoundDrawables(levelItem.drawableLevel, null, null, null);
    }

    private class ViewerRankTopHolder extends RecyclerView.ViewHolder {

        private static final int CARD_NUM = 3;

        public BaseImageView[] imgAvatars;
        public RelativeLayout[] rlytRoots;
        public TextView[] txtNames;
        public ImageView[] imgLevels;
        public TextView[] levels;


        public ViewerRankTopHolder(View itemView) {
            super(itemView);

            rlytRoots = new RelativeLayout[CARD_NUM];
            imgAvatars = new BaseImageView[CARD_NUM];
            txtNames = new TextView[CARD_NUM];
            imgLevels = new ImageView[CARD_NUM];
            levels = new TextView[CARD_NUM];

            imgAvatars[0] = (BaseImageView) itemView.findViewById(R.id.current_rank_avatar_imgFirst);
            imgAvatars[1] = (BaseImageView) itemView.findViewById(R.id.current_rank_avatar_imgSecond);
            imgAvatars[2] = (BaseImageView) itemView.findViewById(R.id.current_rank_avatar_imgThird);

            rlytRoots[0] = (RelativeLayout) (itemView.findViewById(R.id.current_rank_rlytFirstRoot));
            rlytRoots[1] = (RelativeLayout) (itemView.findViewById(R.id.current_rank_rlytSecondRoot));
            rlytRoots[2] = (RelativeLayout) (itemView.findViewById(R.id.current_rank_rlytThirdRoot));

            txtNames[0] = (TextView) (itemView.findViewById(R.id.current_rank_nameFirst));
            txtNames[1] = (TextView) (itemView.findViewById(R.id.current_rank_nameSecond));
            txtNames[2] = (TextView) (itemView.findViewById(R.id.current_rank_nameThird));

            imgLevels[0] = (ImageView) (itemView.findViewById(R.id.current_rank_levelFirst));
            imgLevels[1] = (ImageView) (itemView.findViewById(R.id.current_rank_levelSecond));
            imgLevels[2] = (ImageView) (itemView.findViewById(R.id.current_rank_levelThird));

            levels[0] = (TextView) (itemView.findViewById(R.id.level_tvFirst));
            levels[1] = (TextView) (itemView.findViewById(R.id.level_tvSecond));
            levels[2] = (TextView) (itemView.findViewById(R.id.level_tvThird));
        }
    }

    private class ViewerRankViewHolder extends RecyclerView.ViewHolder {

        ImageView imageLevel;
        TextView mTextView;
        TextView mLevelView;

        public ViewerRankViewHolder(View itemView) {
            super(itemView);
            imageLevel = (ImageView) itemView.findViewById(R.id.current_rank_level);
            mTextView = (TextView) itemView.findViewById(R.id.current_rank_name);
            mLevelView = (TextView) itemView.findViewById(R.id.current_rank_level_tv);
        }

        public void bind(final ViewerModel model) {
            if (null == model) {
                return;
            }

            mTextView.setText(model.getNickName());
            if (model.getNobleLevel() > 0) {
                bindNobelIcon(imageLevel, model.getNobleLevel());
            } else if (model.getVipLevel() > 0) {
                bindVipLevel(imageLevel, model.getVipLevel());
            } else {
                imageLevel.setVisibility(View.GONE);
                mLevelView.setVisibility(View.VISIBLE);
                bindLevel(mLevelView, model.getLevel());
            }

            mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickListener.onItemClick(model.getUid());
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(long uid);
    }
}
