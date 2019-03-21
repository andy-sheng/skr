package com.module.playways.grab.room.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.module.playways.grab.room.adapter.GrabTagsAdapter;
import com.module.playways.grab.room.model.GrabRoomSongModel;
import com.module.playways.grab.songselect.model.SpecialModel;
import com.module.rank.R;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GrabSongTagsView extends RelativeLayout {
    public final static String TAG = "GrabSongTagsView";
    SmartRefreshLayout mRefreshLayout;
    RecyclerView mRecyclerView;
    GrabTagsAdapter mManageSongAdapter;
    int mCurSpecialModelId;

    public GrabSongTagsView(Context context) {
        super(context);
        init();
    }

    public GrabSongTagsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GrabSongTagsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setOnTagClickListener(GrabTagsAdapter.OnTagClickListener onTagClickListener) {
        mManageSongAdapter.setOnTagClickListener(onTagClickListener);
    }

    public void setCurSpecialModel(int specialModelId) {
        mCurSpecialModelId = specialModelId;
    }

    private void init() {
        inflate(getContext(), R.layout.grab_song_tags_view_layout, this);
        mRefreshLayout = (SmartRefreshLayout) findViewById(R.id.refreshLayout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mManageSongAdapter = new GrabTagsAdapter();
        mRecyclerView.setAdapter(mManageSongAdapter);

        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(false);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
        mRefreshLayout.setEnableOverScrollDrag(false);
    }

    public void setSpecialModelList(List<SpecialModel> specialModelList) {
        if (specialModelList != null && specialModelList.size() > 0) {
            List<SpecialModel> models = new ArrayList<>(specialModelList);
            Iterator<SpecialModel> iterator = models.iterator();
            while (iterator.hasNext()) {
                SpecialModel grabRoomSongModel = iterator.next();
                if (grabRoomSongModel.getTagID() == mCurSpecialModelId) {
                    iterator.remove();
                }
            }

            mManageSongAdapter.setDataList(models);
        }
    }
}
