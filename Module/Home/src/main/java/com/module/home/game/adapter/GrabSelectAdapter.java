package com.module.home.game.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.log.MyLog;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.friends.SpecialModel;
import com.module.home.R;
import com.module.home.game.viewholder.GrabCreateViewHolder;
import com.module.home.game.viewholder.GrabSelectViewHolder;

import java.util.ArrayList;
import java.util.List;

public class GrabSelectAdapter extends RecyclerView.Adapter {

    public final static String TAG = "GrabSelectAdapter";

//  public static final int TYPE_CREATE_ROOM = 1;   //创建房间
    public static final int TYPE_SELECT_NORMAL = 2; //正常选择房间

    List<SpecialModel> mDataList = new ArrayList<>();
    RecyclerOnItemClickListener mRecyclerOnItemClickListener;

    public GrabSelectAdapter(RecyclerOnItemClickListener recyclerOnItemClickListener) {
        mRecyclerOnItemClickListener = recyclerOnItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SELECT_NORMAL) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_grab_select_item_view, parent, false);
            GrabSelectViewHolder grabSelectViewHolder = new GrabSelectViewHolder(view, mRecyclerOnItemClickListener);
            return grabSelectViewHolder;
        }
//        else if (viewType == TYPE_CREATE_ROOM) {
//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_garb_create_item_view, parent, false);
//            GrabCreateViewHolder grabCreateViewHolder = new GrabCreateViewHolder(view, mRecyclerOnItemClickListener);
//            return grabCreateViewHolder;
//        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof GrabSelectViewHolder) {
            ((GrabSelectViewHolder) holder).bindData(mDataList.get(position), position);
        }

//        if (holder instanceof GrabCreateViewHolder) {
//            ((GrabCreateViewHolder) holder).bindData(position);
//        }
    }


    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public int getItemViewType(int position) {
//        if (position == 0) {
//            return TYPE_CREATE_ROOM;
//        }
        return TYPE_SELECT_NORMAL;
    }

    public List<SpecialModel> getDataList() {
        return mDataList;
    }

    public void setDataList(List<SpecialModel> dataList) {
        if (dataList != null && dataList.size() != 0) {
            mDataList = dataList;
        }
    }
}
