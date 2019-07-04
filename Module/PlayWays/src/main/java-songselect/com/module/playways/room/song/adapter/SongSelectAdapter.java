package com.module.playways.room.song.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.utils.U;
import com.common.view.ex.drawable.DrawableCreator;
import com.common.view.recyclerview.DiffAdapter;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.playways.room.song.holder.GrabSongInfoHolder;
import com.module.playways.room.song.holder.SongInfoHolder;
import com.module.playways.room.song.holder.SongSearchFooter;
import com.module.playways.room.song.model.SongModel;
import com.module.playways.R;

public class SongSelectAdapter extends DiffAdapter<SongModel, RecyclerView.ViewHolder> {

    public static int DEFAULT_MODE = 0;          //默认模式
    public static int GRAB_MODE = 1;             //一唱到底搜索
    public static int DOUBLE_MODE = 2;           //双人房模式

    Boolean mHasFooterBack = false;     //是否含底部搜索反馈

    RecyclerOnItemClickListener mRecyclerOnItemClickListener;

    public static int NORMAL_ITEM_TYPE = 0;      // 正常view
    public static int SEARCH_ITEM_TYPE = 1;      // 底部搜索反馈

    int mode = DEFAULT_MODE;
    boolean isOwner = false;

    public static Drawable pk = new DrawableCreator.Builder()
            .setStrokeColor(U.getColor(R.color.white_trans_70))
            .setStrokeWidth(U.getDisplayUtils().dip2px(1.5f))
            .setCornersRadius(U.getDisplayUtils().dip2px(10))
            .setSolidColor(Color.parseColor("#CB5883"))
            .build();

    public static Drawable togather = new DrawableCreator.Builder()
            .setStrokeColor(U.getColor(R.color.white_trans_70))
            .setStrokeWidth(U.getDisplayUtils().dip2px(1.5f))
            .setCornersRadius(U.getDisplayUtils().dip2px(10))
            .setSolidColor(Color.parseColor("#7088FF"))
            .build();

    public static Drawable game = new DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#61B14F"))
            .setCornersRadius(U.getDisplayUtils().dip2px(10))
            .setStrokeWidth(U.getDisplayUtils().dip2px(1.5f))
            .setStrokeColor(U.getColor(R.color.white_trans_70))
            .build();

    public SongSelectAdapter(RecyclerOnItemClickListener onItemClickListener) {
        this.mRecyclerOnItemClickListener = onItemClickListener;
    }

    public SongSelectAdapter(RecyclerOnItemClickListener onItemClickListener, boolean mHasFooterBack) {
        this.mRecyclerOnItemClickListener = onItemClickListener;
        this.mHasFooterBack = mHasFooterBack;
    }

    public SongSelectAdapter(RecyclerOnItemClickListener onItemClickListener, boolean mHasFooterBack, int mode, boolean isOwner) {
        this.mRecyclerOnItemClickListener = onItemClickListener;
        this.mHasFooterBack = mHasFooterBack;
        this.mode = mode;
        this.isOwner = isOwner;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SEARCH_ITEM_TYPE) {
            if (mode == GRAB_MODE || mode == DOUBLE_MODE) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grab_song_search_footer_view, parent, false);
                SongSearchFooter songSearchFooter = new SongSearchFooter(view, mRecyclerOnItemClickListener);
                return songSearchFooter;
            } else {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_search_footer_view, parent, false);
                SongSearchFooter songSearchFooter = new SongSearchFooter(view, mRecyclerOnItemClickListener);
                return songSearchFooter;
            }
        } else {
            if (mode == GRAB_MODE) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grab_song_view_holder_item, parent, false);
                GrabSongInfoHolder viewHolder = new GrabSongInfoHolder(view, mRecyclerOnItemClickListener, isOwner);
                return viewHolder;
            } else if (mode == DOUBLE_MODE) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grab_song_view_holder_item, parent, false);
                GrabSongInfoHolder viewHolder = new GrabSongInfoHolder(view, mRecyclerOnItemClickListener, true);
                return viewHolder;
            } else {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_view_holder_item, parent, false);
                SongInfoHolder viewHolder = new SongInfoHolder(view, mRecyclerOnItemClickListener);
                return viewHolder;
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SongInfoHolder) {
            SongInfoHolder songInfoHolder = (SongInfoHolder) holder;
            SongModel songModel = mDataList.get(position);
            songInfoHolder.bind(position, songModel);
        } else if (holder instanceof GrabSongInfoHolder) {
            GrabSongInfoHolder grabSongInfoHolder = (GrabSongInfoHolder) holder;
            SongModel songModel = mDataList.get(position);
            grabSongInfoHolder.bind(position, songModel);
        } else if (holder instanceof SongSearchFooter) {
            SongSearchFooter songSearchFooter = (SongSearchFooter) holder;
            songSearchFooter.bind(position);
        }
    }

    @Override
    public int getItemCount() {
        if (mHasFooterBack) {
            if (mDataList == null) {
                return 1;
            }
            return mDataList.size() + 1;
        } else {
            if (mDataList == null) {
                return 0;
            }
            return mDataList.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mHasFooterBack) {
            int counts = getItemCount();
            if (position == counts - 1) {
                return SEARCH_ITEM_TYPE;
            }
        }

        return NORMAL_ITEM_TYPE;
    }
}
