package com.moudule.playways.beauty.view;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.playways.R;

import java.util.List;

import static com.moudule.playways.beauty.view.BeautyControlPanelView.TYPE_PATER;

/**
 * 美颜和滤镜、贴纸的view
 */
public class BeautyFiterStickerView extends FrameLayout {

    public final static String TAG = "BeautyFiterStickerView";

    RecyclerView mRecyclerView;
    SeekBar mProgressBar;
    BeautyFiterPaterAdapter mAdapter;

    BeautyControlPanelView.Listener mListener;
    BeautyControlPanelView.BeautyViewModel mSelectModel;
    int mType;
    List<BeautyControlPanelView.BeautyViewModel> mDataList;

    public BeautyFiterStickerView(Context context, int type, List<BeautyControlPanelView.BeautyViewModel> list, BeautyControlPanelView.Listener mListener) {
        super(context);
        this.mType = type;
        this.mDataList = list;
        this.mListener = mListener;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.beauty_fiter_pater_view_layout, this);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mProgressBar = (SeekBar) findViewById(R.id.progress_bar);

        if (mType == TYPE_PATER) {
            mProgressBar.setVisibility(GONE);
        } else {
            mProgressBar.setVisibility(VISIBLE);
        }

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        if (mDataList != null && mDataList.size() <= 5) {
            layoutManager = new GridLayoutManager(getContext(), mDataList.size());
            if (mType == TYPE_PATER) {
                // 贴纸不需要给默认的
            } else {
                // 美颜和滤镜默认第一个
                mSelectModel = mDataList.get(0);
            }
        }
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new BeautyFiterPaterAdapter(new RecyclerOnItemClickListener<BeautyControlPanelView.BeautyViewModel>() {
            @Override
            public void onItemClicked(View view, int position, BeautyControlPanelView.BeautyViewModel model) {
                mSelectModel = model;
                mAdapter.setSelectPosition(position);
                // TODO: 2019-06-15 得根据选中，
                if (mType == TYPE_PATER) {
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
                    if (mListener != null && mSelectModel != null) {
                        if (mType == BeautyControlPanelView.TYPE_BEAUTY) {
                            mListener.onChangeBeauty(mSelectModel.getId(), progress);
                        } else if (mType == BeautyControlPanelView.TYPE_FITER) {
                            mListener.onChangeFiter(mSelectModel.getId(), progress);
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
