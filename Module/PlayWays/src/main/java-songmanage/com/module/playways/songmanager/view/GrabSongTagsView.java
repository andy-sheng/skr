package com.module.playways.songmanager.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.utils.U;
import com.common.view.ex.drawable.DrawableCreator;
import com.component.busilib.friends.SpecialModel;
import com.module.playways.R;
import com.module.playways.songmanager.adapter.GrabTagsAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GrabSongTagsView extends RelativeLayout {
    public final static String TAG = "GrabSongTagsView";
    RecyclerView mRecyclerView;
    GrabTagsAdapter mGrabTagsAdapter;
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
        mGrabTagsAdapter.setOnTagClickListener(onTagClickListener);
    }

    public void setCurSpecialModel(int specialModelId) {
        mCurSpecialModelId = specialModelId;
    }

    private void init() {
        inflate(getContext(), R.layout.grab_song_tags_view_layout, this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mGrabTagsAdapter = new GrabTagsAdapter();
        mRecyclerView.setAdapter(mGrabTagsAdapter);
        Drawable drawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(45))
                .setSolidColor(Color.parseColor("#404A9A"))
                .setCornersRadius(U.getDisplayUtils().dip2px(13))
                .build();

        setBackground(drawable);
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

            mGrabTagsAdapter.setDataList(models);
        }
    }
}
