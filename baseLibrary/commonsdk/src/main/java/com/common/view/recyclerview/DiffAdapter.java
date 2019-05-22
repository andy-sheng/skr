package com.common.view.recyclerview;

import android.support.v7.util.DiffUtil;
import android.support.v7.util.ListUpdateCallback;
import android.support.v7.widget.RecyclerView;

import com.common.log.MyLog;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用 DiffUitl 支持局部刷新的
 * 如果是Adapter中有许多类型的 viewHolder 呢
 */
public abstract class DiffAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    public final static String TAG = "DiffAdapter";

    protected List<T> mDataList = new ArrayList<>();

    public List<T> getDataList() {
        return mDataList;
    }

    public void setDataList(List<T> dataList){
        setDataList(dataList,false);
    }

    public void setDataList(List<T> dataList, boolean useDiffUtils) {
        if(dataList==null){
            mDataList.clear();
            notifyDataSetChanged();
            return;
        }
        if (useDiffUtils) {
            /**
             *  想要 dataList 中的数据完全替换 mDataList 中的数据
             *  但是 dataList 中有一些数据 和 mDataList重合，并说是引用相同
             *  而是 equals 方法返回 true，一些别的属性还是有可能不同，这时用局部刷新
             *
             *  测试发现，remove 完全没问题 比如 旧数据源 1 2 3 4 5 6 ，新数据源 2 3 4 5 6 ，完美响应
             *
             *  移动 比如 0移动到4 会刷新 0-4 所有的
             *  增加不太行会刷新所有 ,好像没那么好用,计算的位置不是很符合需求
             */
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new MyDiffUtliCallBack(mDataList, dataList), false);
            mDataList.clear();
            mDataList.addAll(dataList);
            diffResult.dispatchUpdatesTo(new ListUpdateCallback() {
                @Override
                public void onInserted(int position, int count) {
                    MyLog.d(TAG, "onInserted" + " position=" + position + " count=" + count);
                    DiffAdapter.this.notifyItemRangeInserted(position, count);
                }

                /** {@inheritDoc} */
                @Override
                public void onRemoved(int position, int count) {
                    MyLog.d(TAG, "onRemoved" + " position=" + position + " count=" + count);
                    DiffAdapter.this.notifyItemRangeRemoved(position, count);
                }

                /** {@inheritDoc} */
                @Override
                public void onMoved(int fromPosition, int toPosition) {
                    MyLog.d(TAG, "onMoved" + " fromPosition=" + fromPosition + " toPosition=" + toPosition);
                    DiffAdapter.this.notifyItemMoved(fromPosition, toPosition);
                }

                /** {@inheritDoc} */
                @Override
                public void onChanged(int position, int count, Object payload) {
                    MyLog.d(TAG, "onChanged" + " position=" + position + " count=" + count + " payload=" + payload);
                    DiffAdapter.this.notifyItemRangeChanged(position, count, payload);
                }
            });
        }else{
            mDataList.clear();
            mDataList.addAll(dataList);
            notifyDataSetChanged();
        }
    }

    /**
     * 更新某个数据
     *
     * @param data
     */
    public void update(T data) {
        for (int i = 0; i < mDataList.size(); i++) {
            if (mDataList.get(i).equals(data)) {
                mDataList.set(i, data);
                notifyItemChanged(i);
                return;
            }
        }
    }

    /**
     * 插入某条数据
     *
     * @param postion
     * @param data
     */
    public void insert(int postion, T data) {
        mDataList.add(postion, data);
        notifyItemInserted(postion);
    }

    public void insertFirst(T data) {
        insert(0, data);
    }

    public void insertLast(T data) {
        insert(mDataList.size(), data);
    }

    public void insertList(int postion, List<T> dataList) {
        mDataList.addAll(postion, dataList);
        notifyItemRangeInserted(postion, dataList.size());
    }


    public void insertListFirst(List<T> data) {
        insertList(0, data);
    }

    public void insertListLast(List<T> data) {
        insertList(mDataList.size(), data);
    }


    static class MyDiffUtliCallBack<T> extends DiffUtil.Callback {

        List<T> mOldList = new ArrayList<>();
        List<T> mNewList = new ArrayList<>();

        public MyDiffUtliCallBack(List<T> oldList, List<T> newList) {
            mOldList.clear();
            mOldList.addAll(oldList);
            mNewList.clear();
            mNewList.addAll(newList);
        }

        @Override
        public int getOldListSize() {
            return mOldList.size();
        }

        @Override
        public int getNewListSize() {
            return mNewList.size();
        }

        /**
         * 但是后两个方法，主要是为了对应多布局的情况产生的，也就是存在多个 viewType 和多个 ViewHodler 的情况。
         * 首先需要使用 areItemsTheSame() 方法比对是否来自同一个 viewType（也就是同一个 ViewHolder ） ，
         * 然后再通过 areContentsTheSame() 方法比对其内容是否也相等。
         *
         * @param oldItemPosition
         * @param newItemPosition
         * @return
         */
        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            Object obj1 = mOldList.get(oldItemPosition);
            Object obj2 = mNewList.get(newItemPosition);
            boolean b = obj1.getClass().equals(obj2.getClass());
            return b;
        }

        /**
         * 这个方法仅仅在areItemsTheSame()返回true时，才调用
         */
        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Object obj1 = mOldList.get(oldItemPosition);
            Object obj2 = mNewList.get(newItemPosition);
            return obj1 == obj2;
//        boolean b = obj1.equals(obj2);
//        return b;
        }
    }


}
