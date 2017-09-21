package com.wali.live.watchsdk.longtext.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.base.global.GlobalData;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.lit.recycler.adapter.EmptyRecyclerAdapter;
import com.wali.live.watchsdk.lit.recycler.holder.BaseHolder;
import com.wali.live.watchsdk.longtext.holder.BaseFeedItemHolder;
import com.wali.live.watchsdk.longtext.holder.CoverFeedItemHolder;
import com.wali.live.watchsdk.longtext.holder.OwnerFeedItemHolder;
import com.wali.live.watchsdk.longtext.holder.PictureFeedItemHolder;
import com.wali.live.watchsdk.longtext.holder.TextFeedItemHolder;
import com.wali.live.watchsdk.longtext.holder.TitleFeedItemHolder;
import com.wali.live.watchsdk.longtext.holder.ViewerFeedItemHolder;
import com.wali.live.watchsdk.longtext.model.interior.item.BaseFeedItemModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 2017/9/19.
 */
public class LongTextAdapter extends EmptyRecyclerAdapter {
    private List<BaseFeedItemModel> mItemList;

    public LongTextAdapter() {
        mItemList = new ArrayList();
    }

    public void setDataList(List<BaseFeedItemModel> dataList) {
        if (dataList == null || dataList.size() == 0) {
            return;
        }
        mItemList.clear();
        mItemList.addAll(dataList);
        notifyDataSetChanged();
    }

    @Override
    protected int getDataCount() {
        return mItemList == null ? 0 : mItemList.size();
    }

    @Override
    protected BaseHolder onCreateHolder(ViewGroup parent, int viewType) {
        BaseFeedItemHolder holder = null;
        View view;
        switch (viewType) {
            case FeedItemUiType.UI_TYPE_COVER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_feed_cover, parent, false);
                holder = new CoverFeedItemHolder(view);
                break;
            case FeedItemUiType.UI_TYPE_OWNER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_feed_owner, parent, false);
                holder = new OwnerFeedItemHolder(view);
                break;
            case FeedItemUiType.UI_TYPE_TITLE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_feed_title, parent, false);
                holder = new TitleFeedItemHolder(view);
                break;
            case FeedItemUiType.UI_TYPE_VIEWER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_feed_viewer, parent, false);
                holder = new ViewerFeedItemHolder(view);
                break;
            case FeedItemUiType.UI_TYPE_PIC:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_feed_picture, parent, false);
                holder = new PictureFeedItemHolder(view);
                break;
            case FeedItemUiType.UI_TYPE_TEXT:
                view = LayoutInflater.from(GlobalData.app()).inflate(R.layout.recycler_item_feed_text, parent, false);
                holder = new TextFeedItemHolder(view);
                break;
        }
        return holder;
    }

    @Override
    protected void onBindHolder(BaseHolder holder, int position) {
        holder.bindModel(mItemList.get(position), position);
    }

    @Override
    protected int getItemType(int position) {
        return mItemList.get(position).getUiType();
    }
}
