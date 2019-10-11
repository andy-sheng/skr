package com.component.busilib.friends;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.ex.drawable.DrawableCreator;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;

import java.util.ArrayList;
import java.util.List;

public class FriendRoomVerticalAdapter extends RecyclerView.Adapter {

    public final static String TAG = "FriendRoomVerticalAdapter";

    FriendRoomClickListener mOnItemClickListener;
    List<RecommendModel> mDataList = new ArrayList<>();

    public static Drawable bgDrawable1;
    public static Drawable bgDrawable2;
    public static Drawable bgDrawable3;
    public static Drawable bgDrawable4;

    public static Drawable playDrawable1;
    public static Drawable playDrawable2;
    public static Drawable playDrawable3;
    public static Drawable playDrawable4;

    private int REFRESH_PLAY = 1;

    public RecommendModel mCurrPlayModel = null;  //记录当前播放的
    public int mCurrPlayPosition = -1;

    private Handler uiHanlder = new Handler(Looper.getMainLooper());

    public FriendRoomVerticalAdapter(FriendRoomClickListener listener) {
        this.mOnItemClickListener = listener;

        bgDrawable1 = new DrawableCreator.Builder()
                .setGradientColor(Color.parseColor("#ACB7FF"), Color.parseColor("#7481FF"), Color.parseColor("#7481FF"))
                .setGradientAngle(315)
                .setCornersRadius(U.getDisplayUtils().dip2px(8))
                .build();

        playDrawable1 = new DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#7481FF"))
                .setCornersRadius(U.getDisplayUtils().dip2px(8))
                .build();

        bgDrawable2 = new DrawableCreator.Builder()
                .setGradientColor(Color.parseColor("#FCD5A9"), Color.parseColor("#F8AB70"), Color.parseColor("#F8AB70"))
                .setGradientAngle(315)
                .setCornersRadius(U.getDisplayUtils().dip2px(8))
                .build();

        playDrawable2 = new DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#F8AB70"))
                .setCornersRadius(U.getDisplayUtils().dip2px(8))
                .build();

        bgDrawable3 = new DrawableCreator.Builder()
                .setGradientColor(Color.parseColor("#FFC6C4"), Color.parseColor("#FF9492"), Color.parseColor("#FF9492"))
                .setGradientAngle(315)
                .setCornersRadius(U.getDisplayUtils().dip2px(8))
                .build();

        playDrawable3 = new DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#FF9492"))
                .setCornersRadius(U.getDisplayUtils().dip2px(8))
                .build();

        bgDrawable4 = new DrawableCreator.Builder()
                .setGradientColor(Color.parseColor("#A3E7ED"), Color.parseColor("#6ACAD7"), Color.parseColor("#6ACAD7"))
                .setGradientAngle(315)
                .setCornersRadius(U.getDisplayUtils().dip2px(8))
                .build();

        playDrawable3 = new DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#6ACAD7"))
                .setCornersRadius(U.getDisplayUtils().dip2px(8))
                .build();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_room_verit_item_layout, parent, false);
        FriendRoomVerticalViewHolder itemHolder = new FriendRoomVerticalViewHolder(view);
        itemHolder.setOnItemClickListener(mOnItemClickListener);
        return itemHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List payloads) {
        if (payloads.isEmpty()) {
            RecommendModel friendRoomModel = mDataList.get(position);
            ((FriendRoomVerticalViewHolder) holder).bindData(friendRoomModel, position);
            if (mCurrPlayModel == mDataList.get(position)) {
                ((FriendRoomVerticalViewHolder) holder).startPlay();
            } else {
                ((FriendRoomVerticalViewHolder) holder).stopPlay();
            }
        } else {
            // 只有播放
            if (mCurrPlayModel == mDataList.get(position)) {
                ((FriendRoomVerticalViewHolder) holder).startPlay();
            } else {
                ((FriendRoomVerticalViewHolder) holder).stopPlay();
            }
        }
    }

    public void update(RecommendModel model, int position) {
        if (mDataList != null && mDataList.size() > position && position >= 0) {
            mDataList.set(position, model);
            notifyItemChanged(position);
        }
    }

    public void remove(int position) {
        if (mDataList != null && mDataList.size() > position && position >= 0) {
            mDataList.remove(position);
            notifyDataSetChanged();
        }
    }

    public void startOrPauseAudio(int pos, RecommendModel model) {
        if (mCurrPlayModel != null && mCurrPlayModel == model) {
            // 数据和播放类型一致
            stopPlay();
        } else {
            // 数据改变或者播放的类型不一致了
            int lastPos = -1;
            if (mCurrPlayModel != model) {
                mCurrPlayModel = model;
                lastPos = mCurrPlayPosition;
                mCurrPlayPosition = pos;
            }
            notifyItemChanged(pos, REFRESH_PLAY);
            if (lastPos != -1) {
                final int finalLastPos = lastPos;
                uiHanlder.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyItemChanged(finalLastPos, REFRESH_PLAY);
                    }
                });
            }
        }
    }

    public void stopPlay() {
        update(mCurrPlayPosition, mCurrPlayModel, REFRESH_PLAY);
        // 重置数据
        mCurrPlayPosition = -1;
        mCurrPlayModel = null;
    }

    public void update(int position, RecommendModel model, int refreshType) {
        if (mDataList != null || mDataList.size() > 0) {
            if (position >= 0 && position < mDataList.size() && mDataList.get(position) == model) {
                // 位置是对的
                notifyItemChanged(position, refreshType);
                return;
            } else {
                // 位置是错的
                for (int i = 0; i < mDataList.size(); i++) {
                    if (mDataList.get(i) == model) {
                        notifyItemChanged(i, refreshType);
                        return;
                    }
                }
            }
        } else {
            mCurrPlayModel = null;
            mCurrPlayPosition = -1;
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public List<RecommendModel> getDataList() {
        return mDataList;
    }

    public void setDataList(List<RecommendModel> dataList) {
        mDataList = dataList;
    }

    public interface FriendRoomClickListener {
        void onClickFriendRoom(int position, RecommendModel model);

        void onClickFriendVoice(int position, RecommendModel model);
    }
}
