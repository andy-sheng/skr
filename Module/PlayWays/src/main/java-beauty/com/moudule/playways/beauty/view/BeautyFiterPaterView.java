package com.moudule.playways.beauty.view;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import com.common.log.MyLog;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.playways.R;

import java.util.List;

/**
 * 美颜和滤镜、贴纸的view
 */
public class BeautyFiterPaterView extends FrameLayout {

    public final static String TAG = "BeautyFiterPaterView";

    RecyclerView mRecyclerView;
    SeekBar mProgressBar;
    BeautyFiterPaterAdapter mAdapter;

    BeautyControlPanelView.Listener mListener;
    BeautyControlPanelView.BeautyViewModel mSelectModel;

    int type;
    List<BeautyControlPanelView.BeautyViewModel> mDataList;

    public BeautyFiterPaterView(Context context, int type, List<BeautyControlPanelView.BeautyViewModel> list, BeautyControlPanelView.Listener mListener) {
        super(context);
        this.type = type;
        this.mDataList = list;
        this.mListener = mListener;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.beauty_fiter_pater_view_layout, this);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mProgressBar = (SeekBar) findViewById(R.id.progress_bar);

        if (type == BeautyControlPanelView.TYPE_PATER) {
            mProgressBar.setVisibility(GONE);
        } else {
            mProgressBar.setVisibility(VISIBLE);
        }

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        if (mDataList != null && mDataList.size() <= 5) {
            layoutManager = new GridLayoutManager(getContext(), mDataList.size());
        }
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new BeautyFiterPaterAdapter(new RecyclerOnItemClickListener<BeautyControlPanelView.BeautyViewModel>() {
            @Override
            public void onItemClicked(View view, int position, BeautyControlPanelView.BeautyViewModel model) {
                mSelectModel = model;
                mAdapter.setSelectPosition(position);
                // TODO: 2019-06-15 得根据选中，
                if (type == BeautyControlPanelView.TYPE_PATER) {
                    if (mListener != null) {
                        mListener.onChangePater(mSelectModel.getId());
                    }
                }
            }
        }, layoutManager);
        mAdapter.setDataList(mDataList);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        mProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    // TODO: 2019-06-16 回调给前台页面
                    if (mListener != null) {
                        if (type == BeautyControlPanelView.TYPE_BEAUTY) {
                            if (mListener != null) {
                                mListener.onChangeBeauty(mSelectModel.getId(), progress);
                            }
                        } else if (type == BeautyControlPanelView.TYPE_FITER) {
                            if (mListener != null) {
                                mListener.onChangeFiter(mSelectModel.getId(), progress);
                            }
                        }
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
