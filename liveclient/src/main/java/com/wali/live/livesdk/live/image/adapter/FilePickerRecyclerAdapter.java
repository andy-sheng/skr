package com.wali.live.livesdk.live.image.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ImageFactory;
import com.base.image.fresco.image.LocalImage;
import com.base.utils.display.DisplayUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.mi.live.data.repository.DataType.PhotoFolder;
import com.wali.live.common.listener.OnItemClickListener;
import com.wali.live.livesdk.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zyh on 15-12-23.
 */
public class FilePickerRecyclerAdapter extends RecyclerView.Adapter<FilePickerRecyclerAdapter.PhotoRecyclerViewHolder> {
    private static final String TAG = FilePickerRecyclerAdapter.class.getSimpleName();

    private List<PhotoFolder> mFolderList = new ArrayList<>();
    private static final int cornerRadius = DisplayUtils.dip2px(3f);

    private OnItemClickListener mClickListener;
    public void setOnItemClickListener(OnItemClickListener listener) {
        mClickListener = listener;
    }

    public FilePickerRecyclerAdapter() {
    }

    public void setFolderList(List<PhotoFolder> folderList) {
        this.mFolderList = folderList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mFolderList == null ? 0 : mFolderList.size();
    }

    @Override
    public PhotoRecyclerViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.file_picker_item, viewGroup, false);
        return new PhotoRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PhotoRecyclerViewHolder holder, final int position) {
        PhotoFolder photoFolder = mFolderList.get(position);
        if (photoFolder == null) {
            return;
        }
        if (holder.mBaseImage == null) {
            holder.mBaseImage = (LocalImage) ImageFactory.newLocalImage(photoFolder.getPhotoPath()).build();
        }
        holder.mBaseImage.setPath(photoFolder.getPhotoPath());
        holder.mBaseImage.setCornerRadius(cornerRadius);
        FrescoWorker.loadImage(holder.photoDv, holder.mBaseImage);
        holder.titleTv.setText(photoFolder.getFolderName() + "(" + photoFolder.getPhotoCnt() + ")");
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mClickListener != null) {
                    mClickListener.onItemClick(view, position);
                }
            }
        });
    }

    public static class PhotoRecyclerViewHolder extends RecyclerView.ViewHolder {
        public SimpleDraweeView photoDv;
        public TextView titleTv;
        public LocalImage mBaseImage;

        public PhotoRecyclerViewHolder(View itemView) {
            super(itemView);
            photoDv = (SimpleDraweeView) itemView.findViewById(R.id.image_iv);
            titleTv = (TextView) itemView.findViewById(R.id.title_tv);
        }
    }
}