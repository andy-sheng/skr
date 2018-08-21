package com.wali.live.watchsdk.watch.adapter;

import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.base.utils.StringUtils;
import com.base.utils.display.DisplayUtils;
import com.mi.live.data.config.GetConfigManager;
import com.mi.live.data.query.model.ViewerModel;
import com.wali.live.common.barrage.view.utils.NobleConfigUtils;
import com.wali.live.common.view.LevelIconsLayout;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.utils.level.VipLevelUtil;
import com.wali.live.watchsdk.R;

import java.util.ArrayList;
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
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

    public int getBasicItemCount() {
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
            topThreeData.txtNames[i].setText(StringUtils.subString(viewerModel.getNickName(), 6));
            updateLevelIcon(topThreeData.mLevelIconsLayouts[i], viewerModel);

            topThreeData.rlytRoots[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickListener.onItemClick(viewerModel.getUid());
                }
            });
        }
    }

    private void updateLevelIcon(LevelIconsLayout levelIconsLayout, ViewerModel viewerModel) {
        List<TextView> list = new ArrayList<>();
        TextView view;
        if (viewerModel.isNoble()) {
            view = LevelIconsLayout.getDefaultTextView(GlobalData.app());
            view.setBackgroundResource(NobleConfigUtils.getImageResoucesByNobelLevelInBarrage(viewerModel.getNobleLevel()));
            list.add(view);
            levelIconsLayout.addIconsWithClear(list);
            return;

        }
        // VIP
        Pair<Boolean, Integer> pair = VipLevelUtil.getLevelBadgeResId(viewerModel.getVipLevel(), viewerModel.isVipFrozen(), false);
        if (true == pair.first) {
            view = LevelIconsLayout.getDefaultTextView(GlobalData.app());
            view.setBackgroundResource(pair.second);
            list.add(view);
            levelIconsLayout.addIconsWithClear(list);
            return;
        }

        // Plain
        GetConfigManager.LevelItem levelItem = ItemDataFormatUtils.getLevelItem(viewerModel.getLevel());
        view = LevelIconsLayout.getDefaultTextView(GlobalData.app());
        view.setText(String.valueOf(viewerModel.getLevel()) + " ");
        view.setBackground(levelItem.drawableBG);
        view.setCompoundDrawables(levelItem.drawableLevel, null, null, null);
        if (viewerModel.getVipLevel() > 4 && !viewerModel.isVipFrozen()) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(view.getLayoutParams());
            params.setMargins(DisplayUtils.dip2px(3), DisplayUtils.dip2px(2), 0, 0);
            view.setLayoutParams(params);
        }
        list.add(view);
        levelIconsLayout.addIconsWithClear(list);
    }

    private class ViewerRankTopHolder extends RecyclerView.ViewHolder {

        private static final int CARD_NUM = 3;

        public BaseImageView[] imgAvatars;
        public RelativeLayout[] rlytRoots;
        public TextView[] txtNames;
        public LevelIconsLayout[] mLevelIconsLayouts;


        public ViewerRankTopHolder(View itemView) {
            super(itemView);

            rlytRoots = new RelativeLayout[CARD_NUM];
            imgAvatars = new BaseImageView[CARD_NUM];
            txtNames = new TextView[CARD_NUM];
            mLevelIconsLayouts = new LevelIconsLayout[CARD_NUM];


            imgAvatars[0] = (BaseImageView) itemView.findViewById(R.id.current_rank_avatar_imgFirst);
            imgAvatars[1] = (BaseImageView) itemView.findViewById(R.id.current_rank_avatar_imgSecond);
            imgAvatars[2] = (BaseImageView) itemView.findViewById(R.id.current_rank_avatar_imgThird);

            rlytRoots[0] = (RelativeLayout) (itemView.findViewById(R.id.current_rank_rlytFirstRoot));
            rlytRoots[1] = (RelativeLayout) (itemView.findViewById(R.id.current_rank_rlytSecondRoot));
            rlytRoots[2] = (RelativeLayout) (itemView.findViewById(R.id.current_rank_rlytThirdRoot));

            txtNames[0] = (TextView) (itemView.findViewById(R.id.current_rank_nameFirst));
            txtNames[1] = (TextView) (itemView.findViewById(R.id.current_rank_nameSecond));
            txtNames[2] = (TextView) (itemView.findViewById(R.id.current_rank_nameThird));

            mLevelIconsLayouts[0] = (LevelIconsLayout) (itemView.findViewById(R.id.level_tvFirst));
            mLevelIconsLayouts[1] = (LevelIconsLayout) (itemView.findViewById(R.id.level_tvSecond));
            mLevelIconsLayouts[2] = (LevelIconsLayout) (itemView.findViewById(R.id.level_tvThird));
        }
    }

    private class ViewerRankViewHolder extends RecyclerView.ViewHolder {

        TextView mTextView;
        LevelIconsLayout mLevelView;

        ViewerModel mViewerModel;

        public ViewerRankViewHolder(View itemView) {
            super(itemView);

            mTextView = (TextView) itemView.findViewById(R.id.current_rank_name);
            mLevelView = (LevelIconsLayout) itemView.findViewById(R.id.current_rank_level_tv);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickListener.onItemClick(mViewerModel.getUid());
                }
            });
        }

        public void bind(final ViewerModel model) {
            if (null == model) {
                return;
            }

            this.mViewerModel = model;

            mTextView.setText(StringUtils.subString(model.getNickName(), 12));
            updateLevelIcon(mLevelView, model);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(long uid);
    }
}
