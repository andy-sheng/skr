package com.wali.live.watchsdk.endlive;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.base.image.fresco.BaseImageView;
import com.base.view.MLTextView;
import com.live.module.common.R;
import com.wali.live.proto.RoomRecommend;
import com.wali.live.utils.AvatarUtils;

import java.util.List;

/**
 * Created by jiyangli on 16-10-12.
 */
public class RecommendAdapter extends RecyclerView.Adapter<RecommendAdapter.RecommendViewHolder>  {
    List<RoomRecommend.RecommendRoom> data;
    private EndLivePresenter presenter;
    Context mContext;
    public RecommendAdapter(Context mContext,List<RoomRecommend.RecommendRoom>data,EndLivePresenter presenter) {
        this.data = data;
        this.mContext = mContext;
        this.presenter = presenter;
    }

    @Override
    public RecommendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.end_live_recommend_item, parent, false);
        return new RecommendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecommendViewHolder holder, int position) {
        final RoomRecommend.RecommendRoom roomData = data.get(position);
        AvatarUtils.loadAvatarByUidTs(holder.img, roomData.getZuid(), roomData.getAvatar(), AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE, false);
        holder.txtName.setText(roomData.getNickname());
        holder.txtCount.setText(roomData.getViewerCnt() + "");
        holder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.startWatchActivity((Activity) mContext, roomData, 3);
                presenter.popFragment();
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class RecommendViewHolder extends RecyclerView.ViewHolder {

        private BaseImageView img;
        private MLTextView txtName;
        private MLTextView txtCount;

        public RecommendViewHolder(View itemView) {
            super(itemView);
            img = (BaseImageView) itemView.findViewById(R.id.end_live_img);
            txtName = (MLTextView) itemView.findViewById(R.id.end_live_txtName);
            txtCount = (MLTextView) itemView.findViewById(R.id.end_live_txtCount);
        }
    }
}
