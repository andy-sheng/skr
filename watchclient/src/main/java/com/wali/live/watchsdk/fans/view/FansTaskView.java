package com.wali.live.watchsdk.fans.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.adapter.FansTaskAdapter;
import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;
import com.wali.live.watchsdk.fans.model.task.GroupJobModel;
import com.wali.live.watchsdk.fans.model.task.LimitGroupJobModel;
import com.wali.live.watchsdk.fans.presenter.FansTaskPresenter;

import java.util.ArrayList;

import rx.Observable;

/**
 * Created by zyh on 2017/11/13.
 *
 * @module 粉丝任务页面
 */

public class FansTaskView extends RelativeLayout implements FansTaskPresenter.IView {
    private final String TAG = "FansTaskView";
    private RecyclerView mRecyclerView;
    private FansTaskAdapter mFansTaskAdapter;
    private FansTaskPresenter mPresenter;

    private FansGroupDetailModel mFansGroupDetailModel;

    private <V extends View> V $(@IdRes int resId) {
        return (V) findViewById(resId);
    }

    public FansTaskView(Context context) {
        this(context, null);
    }

    public FansTaskView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FansTaskView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void setData(FansGroupDetailModel groupDetailModel) {
        mFansGroupDetailModel = groupDetailModel;
        initPresenter();
    }

    private void initView() {
        inflate(getContext(), R.layout.vfans_task, this);
        mRecyclerView = $(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayout.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mFansTaskAdapter = new FansTaskAdapter();
        mRecyclerView.setAdapter(mFansTaskAdapter);
    }

    private void initPresenter() {
        if (mPresenter == null) {
            mPresenter = new FansTaskPresenter(this);
        }
        mPresenter.getTaskFromServer(mFansGroupDetailModel.getZuid());
    }

    @Override
    public <T> Observable.Transformer<T, T> bindLifecycle() {
        return null;
    }

    @Override
    public void setDataList(Pair pair) {
        mFansTaskAdapter.setList((ArrayList<GroupJobModel>) pair.first,
                (ArrayList<LimitGroupJobModel>) pair.second,
                mFansGroupDetailModel.getVipLevel()
        );
    }
}
