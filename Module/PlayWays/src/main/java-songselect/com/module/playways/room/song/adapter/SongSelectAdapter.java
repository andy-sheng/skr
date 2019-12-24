package com.module.playways.room.song.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    public static int MIC_MODE = 3;              //排麦房模式 和 一唱到底保持一致就好
    public static int RACE_MODE = 4;             //排位赛模式 和 一唱到底保持一致就好
    public static int RELAY_MODE = 5;            //接唱模式 和 一唱到底保持一致就好
    public static int PARTY_MODE = 6;            //剧场模式 和一唱到底保持一致就好
    public static int AUDITION_MODE = 7;         //练歌房

    Boolean mHasFooterBack = false;     //是否含底部搜索反馈

    Listener mListener;

    public static int NORMAL_ITEM_TYPE = 0;      // 正常view
    public static int SEARCH_ITEM_TYPE = 1;      // 底部搜索反馈

    int mode = DEFAULT_MODE;
    String selectText = "点歌";

    public SongSelectAdapter(Listener onItemClickListener, boolean mHasFooterBack, int mode, String selectText) {
        this.mListener = onItemClickListener;
        this.mHasFooterBack = mHasFooterBack;
        this.mode = mode;
        this.selectText = selectText;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SEARCH_ITEM_TYPE) {
            // 搜索类型
            if (mode == GRAB_MODE || mode == DOUBLE_MODE || mode == MIC_MODE || mode == RACE_MODE || mode == RELAY_MODE || mode == PARTY_MODE || mode == AUDITION_MODE) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grab_song_search_footer_view, parent, false);
                SongSearchFooter songSearchFooter = new SongSearchFooter(view, mListener);
                return songSearchFooter;
            } else {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_search_footer_view, parent, false);
                SongSearchFooter songSearchFooter = new SongSearchFooter(view, mListener);
                return songSearchFooter;
            }
        } else {
            // 展示的哥类型
            if (mode == GRAB_MODE || mode == MIC_MODE || mode == RACE_MODE || mode == PARTY_MODE) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grab_song_view_holder_item, parent, false);
                GrabSongInfoHolder viewHolder = new GrabSongInfoHolder(view, mListener, selectText);
                return viewHolder;
            } else if (mode == DOUBLE_MODE) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grab_song_view_holder_item, parent, false);
                GrabSongInfoHolder viewHolder = new GrabSongInfoHolder(view, mListener, selectText);
                return viewHolder;
            } else {
                // 练歌房(得区分搜索和普通展示)
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_view_holder_item, parent, false);
                SongInfoHolder viewHolder = new SongInfoHolder(view, mListener, selectText);
                return viewHolder;
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SongInfoHolder) {
            SongInfoHolder songInfoHolder = (SongInfoHolder) holder;
            SongModel songModel = mDataList.get(position);
            songInfoHolder.bind(position, songModel, mDataList.size());
            if ((mode == RELAY_MODE || mode == AUDITION_MODE) && mHasFooterBack) {
                // 搜索的颜色变成白色
                songInfoHolder.setTextColorWhite(true);
            }
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

    public interface Listener {
        void onClickSelect(int position, SongModel model);
        void onClickSongName(int position, SongModel model);
    }
}
