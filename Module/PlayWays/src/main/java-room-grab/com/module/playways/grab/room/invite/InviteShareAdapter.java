package com.module.playways.grab.room.invite;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.view.AnimateClickListener;
import com.common.view.recyclerview.DiffAdapter;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.playways.R;

public class InviteShareAdapter extends DiffAdapter<ShareModel, InviteShareAdapter.ShareViewHolder> {

    RecyclerOnItemClickListener<ShareModel> mRecyclerOnItemClickListener;

    public InviteShareAdapter(RecyclerOnItemClickListener<ShareModel> listener) {
        this.mRecyclerOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public ShareViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.invite_share_item_layout, parent, false);
        ShareViewHolder viewHolder = new ShareViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ShareViewHolder holder, int position) {
        ShareModel shareModel = mDataList.get(position);
        holder.bindData(position, shareModel);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }


    class ShareViewHolder extends RecyclerView.ViewHolder {

        int position;
        ShareModel mShareModel;

        ImageView mShareIconIv;
        TextView mShareText;

        public ShareViewHolder(View itemView) {
            super(itemView);
            mShareIconIv = (ImageView) itemView.findViewById(R.id.share_icon_iv);
            mShareText = (TextView) itemView.findViewById(R.id.share_text);

            itemView.setOnClickListener(new AnimateClickListener() {
                @Override
                public void click(View view) {
                    if (mRecyclerOnItemClickListener != null) {
                        mRecyclerOnItemClickListener.onItemClicked(view, position, mShareModel);
                    }
                }
            });
        }

        public void bindData(int position, ShareModel shareModel) {
            this.position = position;
            this.mShareModel = shareModel;

            if (mShareModel.getResId() != 0) {
                mShareIconIv.setBackgroundResource(shareModel.getResId());
            }
            mShareText.setText(shareModel.getDesc());
        }
    }
}
