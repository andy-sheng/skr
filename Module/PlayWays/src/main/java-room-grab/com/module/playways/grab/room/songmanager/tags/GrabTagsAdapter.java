package com.module.playways.grab.room.songmanager.tags;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.ex.drawable.DrawableCreator;
import com.common.view.recyclerview.DiffAdapter;
import com.dialog.view.TipsDialogView;
import com.module.playways.grab.createroom.model.SpecialModel;
import com.module.rank.R;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;

public class GrabTagsAdapter extends DiffAdapter<SpecialModel, RecyclerView.ViewHolder> {
    OnTagClickListener mOnTagClickListener;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grab_song_tag_item_layout, parent, false);
        ItemHolder viewHolder = new ItemHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SpecialModel model = mDataList.get(position);

        ItemHolder reportItemHolder = (ItemHolder) holder;
        reportItemHolder.bind(model);
    }

    public void setOnTagClickListener(OnTagClickListener onTagClickListener) {
        mOnTagClickListener = onTagClickListener;
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        ExTextView mTvSelectedTag;
        SpecialModel mSpecialModel;

        public ItemHolder(View itemView) {
            super(itemView);
            mTvSelectedTag = (ExTextView) itemView.findViewById(R.id.tv_selected_tag);
            mTvSelectedTag.setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    mOnTagClickListener.dismissDialog();
                    TipsDialogView tipsDialogView = new TipsDialogView.Builder(itemView.getContext())
                            .setMessageTip("确认切换为 " + mSpecialModel.getTagName() + " 专场歌单吗？\n当前待下发歌单内的所有歌曲将会被重置")
                            .setConfirmTip("确认切换")
                            .setCancelTip("取消")
                            .build();

                    DialogPlus.newDialog(itemView.getContext())
                            .setContentHolder(new ViewHolder(tipsDialogView))
                            .setGravity(Gravity.BOTTOM)
                            .setContentBackgroundResource(R.color.transparent)
                            .setOverlayBackgroundResource(R.color.black_trans_80)
                            .setExpanded(false)
                            .setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(@NonNull DialogPlus dialog, @NonNull View view) {
                                    if (view instanceof ExTextView) {
                                        if (view.getId() == R.id.confirm_tv) {
                                            dialog.dismiss();
                                            if (mOnTagClickListener != null) {
                                                mOnTagClickListener.onClick(mSpecialModel);
                                            }
                                        }

                                        if (view.getId() == R.id.cancel_tv) {
                                            dialog.dismiss();
                                        }
                                    }
                                }
                            })
                            .setOnDismissListener(new OnDismissListener() {
                                @Override
                                public void onDismiss(@NonNull DialogPlus dialog) {

                                }
                            })
                            .create().show();
                }
            });
        }

        public void bind(SpecialModel model) {
            this.mSpecialModel = model;

            int color = Color.parseColor("#68ABD3");
            if (!TextUtils.isEmpty(model.getBgColor())) {
                color = Color.parseColor(model.getBgColor());
            }

            Drawable drawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(45))
                    .setStrokeColor(Color.parseColor("#202239"))
                    .setStrokeWidth(U.getDisplayUtils().dip2px(2))
                    .setSolidColor(color)
                    .setCornersRadius(U.getDisplayUtils().dip2px(8))
                    .build();
            mTvSelectedTag.setBackground(drawable);
            mTvSelectedTag.setText(model.getTagName());
        }
    }

    public interface OnTagClickListener {
        void onClick(SpecialModel specialModel);

        void dismissDialog();
    }
}