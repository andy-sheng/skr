package com.wali.live.watchsdk.fans.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.base.mvp.specific.RxRelativeLayout;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;
import com.wali.live.watchsdk.fans.presenter.FansTaskPresenter;
import com.wali.live.watchsdk.fans.presenter.IFansTaskView;
import com.wali.live.watchsdk.fans.task.adapter.FansTaskAdapter;
import com.wali.live.watchsdk.fans.task.listener.FansGroupTaskListener;
import com.wali.live.watchsdk.fans.task.model.GroupJobListModel;

/**
 * Created by anping on 17/6/6.
 */

public class FansTaskView extends RxRelativeLayout implements IFansTaskView, FansGroupTaskListener {
    protected FansTaskPresenter mTaskPresenter;

    private RecyclerView mTaskRv;
    private FansTaskAdapter mAdapter;

    // 目前只需要detailModel里的vipLevel
    private FansGroupDetailModel mGroupDetailModel;

    public FansTaskView(Context context) {
        super(context);
        init();
    }

    public FansTaskView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FansTaskView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.fans_task_view, this);

        mTaskRv = $(R.id.task_rv);
        mTaskRv.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new FansTaskAdapter(this);
        mTaskRv.setAdapter(mAdapter);

        mTaskPresenter = new FansTaskPresenter(this);
    }

    public void setGroupDetailModel(FansGroupDetailModel model) {
        mGroupDetailModel = model;
        mTaskPresenter.getTaskList(mGroupDetailModel.getZuid());
    }

    @Override
    public void setGroupTaskList(GroupJobListModel model) {
        mAdapter.setDataList(model, mGroupDetailModel);
    }

    @Override
    public void finishTask(VFansCommonProto.GroupJobType jobType) {
        mTaskPresenter.finishJob(mGroupDetailModel.getZuid(), jobType, null);
    }

    @Override
    public void notifyFinishTaskSuccess() {
        mTaskPresenter.getTaskList(mGroupDetailModel.getZuid());
    }
}
