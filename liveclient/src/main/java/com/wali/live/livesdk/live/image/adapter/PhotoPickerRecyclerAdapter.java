package com.wali.live.livesdk.live.image.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.base.global.GlobalData;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ImageFactory;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.wali.live.common.listener.OnItemClickListener;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.image.PhotoPickerFragment;
import com.wali.live.livesdk.live.viewmodel.PhotoItem;

import java.util.HashMap;
import java.util.List;

/**
 * Created by zyh on 15-12-23.
 */
public class PhotoPickerRecyclerAdapter extends RecyclerView.Adapter<PhotoPickerRecyclerAdapter.PhotoRecyclerViewHolder> {
    private static final String TAG = PhotoPickerRecyclerAdapter.class.getSimpleName();

    private List<PhotoItem> mPhotoList;
    public static final int MAX_SELECT_COUNT = 6;
    private int mMaxCount = MAX_SELECT_COUNT;

    private OnItemClickListener mClickListener;
    private UpdatePhotoListener mUpdateListener;

    private int mUiType;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mClickListener = listener;
    }

    public void setUpdateListener(UpdatePhotoListener updateListener) {
        this.mUpdateListener = updateListener;
    }

    public PhotoPickerRecyclerAdapter() {
    }

    public PhotoPickerRecyclerAdapter(int maxCount) {
        mMaxCount = maxCount;
    }

    public PhotoPickerRecyclerAdapter(int maxCount, int uiType) {
        mMaxCount = maxCount;
        mUiType = uiType;
    }

    public void setPhotoList(List<PhotoItem> photoList) {
        mPhotoList = photoList;
        notifyDataSetChanged();
    }

    public void clearPhotoList() {
        if (null != mPhotoList) {
            mPhotoList.clear();
            notifyDataSetChanged();
        }
    }

    public void cancelSelectedItem(int position) {
        if (null != mPhotoList && position < mPhotoList.size()) {
            mPhotoList.get(position).setSelected(false);
            notifyItemChanged(position);
        }
    }

    @Override
    public int getItemCount() {
        return mPhotoList == null ? 0 : mPhotoList.size();
    }

    @Override
    public PhotoRecyclerViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.photo_picker_item, viewGroup, false);
        return new PhotoRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PhotoRecyclerViewHolder holder, final int position) {
        final PhotoItem item = mPhotoList.get(position);
        FrescoWorker.loadImage(holder.photoDv, ImageFactory.newLocalImage(item.getLocalPath())
                .setWidth(PhotoItem.RESIZE)
                .setHeight(PhotoItem.RESIZE)
                .build());
        //图片格式错误　选择项不显示
        if ((item.getSrcWidth() <= 0) || (item.getSrcHeight() <= 0)) {
            MyLog.d(TAG, " item.getLocalPath = " + item.getLocalPath());
            holder.checkIv.setVisibility(View.GONE);
        } else {
            final HashMap<String, PhotoItem> selectedMap = mUpdateListener.getSelectItem();
            final boolean isSelect = selectedMap.containsKey(item.getLocalPath());

            if (mUiType == PhotoPickerFragment.UI_TYPE_ADD_PHOTO) {
                holder.checkIv.setVisibility(View.GONE);
            } else {
                if (isSelect) {
                    holder.checkIv.setImageDrawable(GlobalData.app().getResources().getDrawable(R.drawable.image_photo_choice_press));
                    item.setSelected(true);
                } else {
                    holder.checkIv.setImageDrawable(GlobalData.app().getResources().getDrawable(R.drawable.all_checkbox_round_normal_night));
                    item.setSelected(false);
                }
            }

            //选择图片
            holder.foregroundIv.setVisibility(isSelect ? View.VISIBLE : View.GONE);
            holder.checkIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectItem(item, holder.foregroundIv, holder.checkIv);
                }
            });
        }
        //查看大图
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUiType == PhotoPickerFragment.UI_TYPE_ADD_PHOTO) {
                    selectItem(item, holder.foregroundIv, holder.checkIv);
                }
                if (mClickListener != null) {
                    mClickListener.onItemClick(view, position);
                }
            }
        });
    }

    private void selectItem(PhotoItem item, ImageView foregroundIv, ImageView checkIv) {
        int selectSize = mUpdateListener.getSelectedSize();
        if (!item.isSelected() && selectSize >= mMaxCount) {
            ToastUtils.showToast(GlobalData.app(), GlobalData.app().getString(R.string.photo_select_over_limit, mMaxCount));
            return;
        }
        item.setSelected(!item.isSelected());
        if (item.isSelected()) {
            foregroundIv.setVisibility(View.VISIBLE);
            checkIv.setImageDrawable(GlobalData.app().getResources().getDrawable(R.drawable.image_photo_choice_press));
            mUpdateListener.addPhotoItem(item);
        } else {
            foregroundIv.setVisibility(View.GONE);
            checkIv.setImageDrawable(GlobalData.app().getResources().getDrawable(R.drawable.all_checkbox_round_normal_night));
            mUpdateListener.removePhotoItem(item);
        }
    }

    public static class PhotoRecyclerViewHolder extends RecyclerView.ViewHolder {
        public SimpleDraweeView photoDv;
        public ImageView foregroundIv;
        public ImageView checkIv;

        public PhotoRecyclerViewHolder(View itemView) {
            super(itemView);
            ViewGroup.MarginLayoutParams vlp = (ViewGroup.MarginLayoutParams) itemView.getLayoutParams();
            vlp.height = vlp.width = (GlobalData.screenWidth - 3 *2 * (PhotoPickerFragment.PHOTO_COLUMN - 1)) / PhotoPickerFragment.PHOTO_COLUMN;
            vlp.setMargins(3, 3, 3, 3);
            itemView.setLayoutParams(vlp);
            photoDv = (SimpleDraweeView) itemView.findViewById(R.id.photo_dv);
            foregroundIv = (ImageView) itemView.findViewById(R.id.foreground_iv);
            checkIv = (ImageView) itemView.findViewById(R.id.check_iv);
        }
    }

    //回调接口
    public interface UpdatePhotoListener {
        void addPhotoItem(PhotoItem photoItem);

        void removePhotoItem(PhotoItem photoItem);

        int getSelectedSize();

        HashMap<String, PhotoItem> getSelectItem();
    }
}
