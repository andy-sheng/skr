package com.zq.person.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.log.MyLog;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.zq.person.holder.PhotoAddHolder;
import com.zq.person.holder.PhotoViewHolder;
import com.zq.person.model.PhotoModel;

import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter {
    public final static String TAG = "PhotoAdapter";

    public final static int TYPE_PERSON_CARD = 1;
    public final static int TYPE_PERSON_CENTER = 2;
    public final static int TYPE_OTHER_PERSON_CENTER = 3;

    List<PhotoModel> mDataList = new ArrayList<>();
    RecyclerOnItemClickListener mListener;

    boolean mHasUpdate;
    int type;

    private int PHOTO_ADD_TYPE = 0;
    private int PHOTO_ITEM_TYPE = 1;

    public PhotoAdapter(RecyclerOnItemClickListener mListener, int type) {
        this.mListener = mListener;
        this.type = type;
        if (type == TYPE_PERSON_CENTER) {
            mHasUpdate = true;
        } else {
            mHasUpdate = false;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == PHOTO_ITEM_TYPE) {
            View view;
            if (type == TYPE_PERSON_CARD) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_card_item_view_layout, parent, false);
            } else {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_item_view_layout, parent, false);
            }
            PhotoViewHolder viewHolder = new PhotoViewHolder(view, mListener);
            return viewHolder;
        } else if (viewType == PHOTO_ADD_TYPE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_add_view_layout, parent, false);
            PhotoAddHolder viewHolder = new PhotoAddHolder(view, mListener);
            return viewHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (mHasUpdate) {
            if (position == 0) {
                ((PhotoAddHolder) holder).bindData(position);
            } else {
                PhotoModel photoModel = mDataList.get(position - 1);
                ((PhotoViewHolder) holder).bindData(photoModel, position);
            }
        } else {
            ((PhotoViewHolder) holder).bindData(mDataList.get(position), position);
        }
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0 && mHasUpdate) {
            return PHOTO_ADD_TYPE;
        }
        return PHOTO_ITEM_TYPE;
    }

    @Override
    public int getItemCount() {
        if (mHasUpdate) {
            return mDataList.size() + 1;
        }

        return mDataList.size();
    }

    public List<PhotoModel> getDataList() {
        return mDataList;
    }

    /**
     * 列表中上传成功的item个数
     *
     * @return
     */
    public int getSuccessNum() {
        int success = 0;
        for (PhotoModel photoModel : mDataList) {
            if (photoModel.getStatus() == PhotoModel.STATUS_SUCCESS) {
                success++;
            }
        }
        return success;
    }

    public void setDataList(List<PhotoModel> dataList) {
        if (dataList != null) {
            // 把未成功的挑选出来
            List<PhotoModel> unSuccessList = new ArrayList<>();
            for (PhotoModel photoModel : mDataList) {
                if (photoModel.getStatus() != PhotoModel.STATUS_SUCCESS) {
                    unSuccessList.add(photoModel);
                }
            }
            mDataList.clear();
            mDataList.addAll(unSuccessList);
            mDataList.addAll(dataList);
            notifyDataSetChanged();
        }
    }

    /**
     * 头部插入某条数据
     *
     * @param data
     */
    public void insertFirst(PhotoModel data) {
        mDataList.add(0, data);
        if (mHasUpdate) {
            notifyItemInserted(1);
        } else {
            notifyItemInserted(0);
        }
    }

    /**
     * 尾部插入一堆数据
     *
     * @param list
     */
    public void insertLast(List<PhotoModel> list) {
        int origin = mDataList.size();
        mDataList.addAll(list);
        if (mHasUpdate) {
            notifyItemRangeInserted(origin + 1, mDataList.size() - origin + 1);
        } else {
            notifyItemRangeInserted(origin, mDataList.size() - origin);
        }
    }

    public void delete(PhotoModel photoModel) {
        for (int i = 0; i < mDataList.size(); i++) {
            PhotoModel m = mDataList.get(i);
            if (m.equals(photoModel)) {
                MyLog.d(TAG, "delete" + " find i=" + i);
                mDataList.remove(i);
                if (mHasUpdate) {
                    notifyItemRemoved(i + 1);
                } else {
                    notifyItemRemoved(i);
                }
                return;
            }
        }
    }

    public void update(PhotoModel photoModel) {
        MyLog.d(TAG, "update" + " photoModel=" + photoModel);
        for (int i = 0; i < mDataList.size(); i++) {
            PhotoModel m = mDataList.get(i);
            if (m.equals(photoModel)) {
                MyLog.d(TAG, "update" + " find i=" + i);
                m.setStatus(photoModel.getStatus());
                if (mHasUpdate) {
                    notifyItemChanged(i + 1);
                } else {
                    notifyItemChanged(i);
                }
                return;
            }
        }
    }

    public int getPostionOfItem(PhotoModel model) {
        for (int i = 0; i < mDataList.size(); i++) {
            PhotoModel m = mDataList.get(i);
            if (m.equals(model)) {
                return i;
            }
        }
        return 0;
    }
}
