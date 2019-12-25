package com.module.playways.room.song.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.playways.room.song.holder.SongCardHolder;
import com.module.playways.room.song.model.SongCardModel;
import com.module.playways.R;

import java.util.ArrayList;
import java.util.Collection;

public class SongCardSwipAdapter extends BaseAdapter {

    ArrayList<SongCardModel> mSongCardHolderArrayList = new ArrayList<>();

    SongSelectAdapter.Listener mListener;
    int defaultCount;   // 每张卡片上多少个元素
    int selectModel;
    String selectText;

    public SongCardSwipAdapter(SongSelectAdapter.Listener listener, int defaultCount, int selectModel, String selectText) {
        this.mListener = listener;
        this.defaultCount = defaultCount;
        this.selectModel = selectModel;
        this.selectText = selectText;
    }

    public void addAll(Collection<SongCardModel> collection) {
        if (isEmpty()) {
            mSongCardHolderArrayList.addAll(collection);
            notifyDataSetChanged();
        } else {
            mSongCardHolderArrayList.addAll(collection);
        }
    }

    public void addData(int position, SongCardModel talent) {
        mSongCardHolderArrayList.add(position, talent);
        notifyDataSetChanged();
    }

    public void addData(SongCardModel talent) {
        mSongCardHolderArrayList.add(talent);
    }

    public ArrayList<SongCardModel> getSongCardHolderArrayList() {
        return mSongCardHolderArrayList;
    }

    public void setSongCardHolderArrayList(ArrayList<SongCardModel> songCardHolderArrayList) {
        mSongCardHolderArrayList = songCardHolderArrayList;
    }

    public void remove(int position) {
        if (position > -1 && position < mSongCardHolderArrayList.size()) {
            mSongCardHolderArrayList.remove(position);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        if (mSongCardHolderArrayList != null) {
            return mSongCardHolderArrayList.size();
        }
        return 0;
    }

    @Override
    public SongCardModel getItem(int position) {
        if (mSongCardHolderArrayList != null) {
            return mSongCardHolderArrayList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SongCardHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_card_item_view, parent, false);
            holder = new SongCardHolder(convertView, mListener, defaultCount, selectModel, selectText);
            convertView.setTag(holder);
        } else {
            holder = (SongCardHolder) convertView.getTag();
        }
        holder.bind(position, getItem(position));
        return convertView;
    }
}
