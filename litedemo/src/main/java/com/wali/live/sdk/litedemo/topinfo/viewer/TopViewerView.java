package com.wali.live.sdk.litedemo.topinfo.viewer;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.mi.liveassistant.data.model.Viewer;
import com.wali.live.sdk.litedemo.R;

import java.util.List;

/**
 * Created by lan on 17/5/4.
 */
public class TopViewerView extends RelativeLayout {
    private RecyclerView mViewerRv;

    private LinearLayoutManager mLayoutManager;
    private TopViewerAdapter mViewerAdapter;

    public TopViewerView(Context context) {
        super(context);
        init();
    }

    public TopViewerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TopViewerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected <V extends View> V $(int id) {
        return (V) findViewById(id);
    }

    private void init() {
        inflate(getContext(), R.layout.top_viewer_view, this);

        mViewerRv = $(R.id.viewer_rv);
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mViewerRv.setLayoutManager(mLayoutManager);

        mViewerAdapter = new TopViewerAdapter();
        mViewerRv.setAdapter(mViewerAdapter);
    }

    public void updateViewerView(List<Viewer> list) {
        mViewerAdapter.setViewerList(list);
    }
}
