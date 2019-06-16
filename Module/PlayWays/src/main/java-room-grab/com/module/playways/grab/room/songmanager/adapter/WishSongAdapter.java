package com.module.playways.grab.room.songmanager.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.ex.drawable.DrawableCreator;
import com.common.view.recyclerview.DiffAdapter;
import com.module.playways.R;
import com.module.playways.room.song.model.SongModel;
import com.zq.live.proto.Common.StandPlayType;

public class WishSongAdapter extends DiffAdapter<SongModel, RecyclerView.ViewHolder> {

    Drawable mChorusDrawable;
    Drawable mPKDrawable;
    Drawable mMiniGameDrawable;

    Listener mListener;

    public WishSongAdapter(Listener listener) {
        mListener = listener;
        mChorusDrawable = new DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#7088FF"))
                .setCornersRadius(U.getDisplayUtils().dip2px(10))
                .setStrokeWidth(U.getDisplayUtils().dip2px(1.5f))
                .setStrokeColor(U.getColor(R.color.white_trans_70))
                .build();

        mPKDrawable = new DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#E55088"))
                .setCornersRadius(U.getDisplayUtils().dip2px(10))
                .setStrokeWidth(U.getDisplayUtils().dip2px(1.5f))
                .setStrokeColor(U.getColor(R.color.white_trans_70))
                .build();

        mMiniGameDrawable = new DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#61B14F"))
                .setCornersRadius(U.getDisplayUtils().dip2px(10))
                .setStrokeWidth(U.getDisplayUtils().dip2px(1.5f))
                .setStrokeColor(U.getColor(R.color.white_trans_70))
                .build();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wish_song_item, parent, false);
        ItemHolder viewHolder = new ItemHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SongModel model = mDataList.get(position);
        ItemHolder itemHolder = (ItemHolder) holder;
        itemHolder.bind(model, position);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public interface Listener {
        void onClickDeleteWish(View view, int position, SongModel songModel);

        void onClickSelectWish(View view, int position, SongModel songModel);
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        ExTextView mSongNameTv;
        TextView mSingNameTv;
        ExTextView mSongTagTv;
        ExTextView mSelectTv;
        ExTextView mDeleteTv;

        SongModel mSongModel;
        int mPosition;

        public ItemHolder(View itemView) {
            super(itemView);
            mSongNameTv = (ExTextView) itemView.findViewById(R.id.song_name_tv);
            mSingNameTv = (TextView) itemView.findViewById(R.id.sing_name_tv);
            mSongTagTv = (ExTextView) itemView.findViewById(R.id.song_tag_tv);
            mSelectTv = (ExTextView) itemView.findViewById(R.id.select_tv);
            mDeleteTv = (ExTextView) itemView.findViewById(R.id.delete_tv);

            mSelectTv.setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    if (mListener != null) {
                        mListener.onClickSelectWish(v, mPosition, mSongModel);
                    }
                }
            });

            mDeleteTv.setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    if (mListener != null) {
                        mListener.onClickDeleteWish(v, mPosition, mSongModel);
                    }
                }
            });
        }

        public void bind(SongModel model, int position) {
            this.mSongModel = model;
            this.mPosition = position;

            if (model.getPlayType() == StandPlayType.PT_SPK_TYPE.getValue()) {
                mSongNameTv.setPadding(0, 0, U.getDisplayUtils().dip2px(34 + 84), 0);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mSongTagTv.getLayoutParams();
                layoutParams.width = U.getDisplayUtils().dip2px(34);
                layoutParams.leftMargin = -U.getDisplayUtils().dip2px(34 + 84);
                mSongTagTv.setLayoutParams(layoutParams);
                mSongTagTv.setText("PK");
                mSongTagTv.setVisibility(View.VISIBLE);
                mSongTagTv.setBackground(mPKDrawable);
                mSongNameTv.setText("《" + model.getDisplaySongName() + "》");
            } else if (model.getPlayType() == StandPlayType.PT_CHO_TYPE.getValue()) {
                mSongNameTv.setPadding(0, 0, U.getDisplayUtils().dip2px(34 + 84), 0);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mSongTagTv.getLayoutParams();
                layoutParams.width = U.getDisplayUtils().dip2px(34);
                layoutParams.leftMargin = -U.getDisplayUtils().dip2px(34 + 84);
                mSongTagTv.setLayoutParams(layoutParams);
                mSongTagTv.setText("合唱");
                mSongTagTv.setVisibility(View.VISIBLE);
                mSongTagTv.setBackground(mChorusDrawable);
                mSongNameTv.setText("《" + model.getDisplaySongName() + "》");
            } else if (model.getPlayType() == StandPlayType.PT_MINI_GAME_TYPE.getValue()) {
                mSongNameTv.setPadding(0, 0, U.getDisplayUtils().dip2px(58 + 84), 0);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mSongTagTv.getLayoutParams();
                layoutParams.width = U.getDisplayUtils().dip2px(58);
                layoutParams.leftMargin = -U.getDisplayUtils().dip2px(58 + 84);
                mSongTagTv.setLayoutParams(layoutParams);
                mSongTagTv.setText("双人游戏");
                mSongTagTv.setVisibility(View.VISIBLE);
                mSongTagTv.setBackground(mMiniGameDrawable);
                mSongNameTv.setText("【" + model.getItemName() + "】");
            } else {
                mSongNameTv.setPadding(0, 0, U.getDisplayUtils().dip2px(84), 0);
                mSongTagTv.setVisibility(View.GONE);
                mSongNameTv.setText("《" + model.getDisplaySongName() + "》");
            }
            mSingNameTv.setText(model.getOwner());
        }
    }
}
