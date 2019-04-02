package com.module.home.game.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.base.BaseFragment;
import com.component.busilib.friends.RecommendModel;
import com.component.busilib.friends.SpecialModel;
import com.module.home.R;
import com.module.home.game.model.BannerModel;
import com.module.home.game.model.QuickJoinRoomModel;
import com.module.home.game.model.RecommendRoomModel;
import com.module.home.game.viewholder.BannerViewHolder;
import com.module.home.game.viewholder.QuickRoomViewHolder;
import com.module.home.game.viewholder.RecommendRoomViewHolder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameAdapter extends RecyclerView.Adapter {

    BaseFragment mBaseFragment;

    GameAdapterListener mListener;

    List<Object> mDataList = new ArrayList<>();

    public static final int TYPE_BANNER_HOLDER = 1;       // 广告
    public static final int TYPE_RECOMMEND_HOLDER = 2;    // 推荐房
    public static final int TYPE_QUICK_ROOM_HOLDER = 3;   // 快速房

    public List<Object> getDataList() {
        return mDataList;
    }

    public void setDataList(List<Object> dataList) {
        mDataList = dataList;
    }

    public Object getPositionObject(int position) {
        if (mDataList != null && mDataList.size() > 0) {
            if (position < mDataList.size()) {
                return mDataList.get(position);
            }
        }
        return null;
    }

    public GameAdapter(BaseFragment baseFragment, GameAdapterListener gameAdapterListener) {
        this.mBaseFragment = baseFragment;
        mListener = gameAdapterListener;
    }

    public void updateQuickJoinRoomInfo(QuickJoinRoomModel quickJoinRoomModel) {
        if (mDataList != null && mDataList.size() > 0) {
            Iterator<Object> iterator = mDataList.iterator();
            while (iterator.hasNext()) {
                if (iterator.next() instanceof QuickJoinRoomModel) {
                    iterator.remove();
                }
            }
            if (mDataList.size() >= 2) {
                mDataList.add(2, quickJoinRoomModel);
            } else {
                mDataList.add(quickJoinRoomModel);
            }
        } else {
            mDataList = new ArrayList<>();
            mDataList.add(quickJoinRoomModel);
        }
    }

    public void clearQuickJoinRoomInfo() {
        if (mDataList != null && mDataList.size() > 0) {
            Iterator<Object> iterator = mDataList.iterator();
            while (iterator.hasNext()) {
                if (iterator.next() instanceof QuickJoinRoomModel) {
                    iterator.remove();
                }
            }
        }
    }

    public void updateRecommendRoomInfo(RecommendRoomModel recommendRoomModel) {
        if (mDataList != null && mDataList.size() > 0) {
            Iterator<Object> iterator = mDataList.iterator();
            while (iterator.hasNext()) {
                if (iterator.next() instanceof RecommendRoomModel) {
                    iterator.remove();
                }
            }
            if (mDataList.size() >= 1) {
                mDataList.add(1, recommendRoomModel);
            } else {
                mDataList.add(recommendRoomModel);
            }
        } else {
            mDataList = new ArrayList<>();
            mDataList.add(recommendRoomModel);
        }
    }

    public void clearRecommendRoomInfo() {
        if (mDataList != null && mDataList.size() > 0) {
            Iterator<Object> iterator = mDataList.iterator();
            while (iterator.hasNext()) {
                if (iterator.next() instanceof RecommendRoomModel) {
                    iterator.remove();
                }
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_BANNER_HOLDER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_banner_item_view, parent, false);
            BannerViewHolder bannerViewHolder = new BannerViewHolder(view);
            return bannerViewHolder;
        } else if (viewType == TYPE_RECOMMEND_HOLDER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_recommend_room_item_view, parent, false);
            RecommendRoomViewHolder recommendRoomViewHolder = new RecommendRoomViewHolder(view, mBaseFragment, mListener);
            return recommendRoomViewHolder;
        } else if (viewType == TYPE_QUICK_ROOM_HOLDER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_quick_room_item_view, parent, false);
            QuickRoomViewHolder quickRoomViewHolder = new QuickRoomViewHolder(view, mBaseFragment, mListener);
            return quickRoomViewHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (mDataList != null && mDataList.get(position) != null) {
            if (mDataList.get(position) instanceof BannerModel) {
                ((BannerViewHolder) holder).bindData((BannerModel) (mDataList.get(position)));
            } else if (mDataList.get(position) instanceof RecommendRoomModel) {
                ((RecommendRoomViewHolder) holder).bindData((RecommendRoomModel) (mDataList.get(position)));
            } else if (mDataList.get(position) instanceof QuickJoinRoomModel) {
                ((QuickRoomViewHolder) holder).bindData((QuickJoinRoomModel) (mDataList.get(position)));
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mDataList.get(position) instanceof BannerModel) {
            return TYPE_BANNER_HOLDER;
        } else if (mDataList.get(position) instanceof RecommendRoomModel) {
            return TYPE_RECOMMEND_HOLDER;
        } else if (mDataList.get(position) instanceof QuickJoinRoomModel) {
            return TYPE_QUICK_ROOM_HOLDER;
        }
        return 0;
    }

    public interface GameAdapterListener {
        void createRoom();     //创建房间

        void selectSpecial(SpecialModel specialModel);   //选择专场

        void enterRoom(RecommendModel friendRoomModel);   //进入房间

        void moreRoom();  //更多房间
    }
}
