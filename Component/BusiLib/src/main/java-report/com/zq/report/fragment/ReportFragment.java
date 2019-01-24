package com.zq.report.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.KeyboardEvent;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.ex.NoLeakEditText;
import com.component.busilib.R;
import com.jakewharton.rxbinding2.view.RxView;
import com.zq.report.adapter.ReportAdapter;
import com.zq.report.model.ReportModel;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class ReportFragment extends BaseFragment {

    public static final String REPORT_FROM_KEY = "report_from_key";
    public static final String REPORT_USER_ID = "report_user_id";

    public static final int FORM_GAME = 1;
    public static final int FORM_PERSON = 2;

    List<ReportModel> mReportModels = new ArrayList<>(); //数据源
    List<ReportModel> mSelectModels = new ArrayList<>(); //已选项

    ExTextView mSubmitTv;
    NoLeakEditText mReportContent;
    RecyclerView mRecyclerView;
    RelativeLayout mContainer;
    View mPlaceView;
    View mPlaceHolderView;

    ReportAdapter mReportAdapter;

    int mUserID;
    int mMode;

    @Override
    public int initView() {
        return R.layout.report_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mContainer = (RelativeLayout) mRootView.findViewById(R.id.container);
        mSubmitTv = (ExTextView) mRootView.findViewById(R.id.submit_tv);
        mReportContent = (NoLeakEditText) mRootView.findViewById(R.id.report_content);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
        mPlaceView = (View) mRootView.findViewById(R.id.place_view);
        mPlaceHolderView = (View) mRootView.findViewById(R.id.place_holder_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mReportAdapter = new ReportAdapter(new ReportAdapter.RecyclerOnItemCheckListener() {
            @Override
            public void onCheckedChanged(boolean isCheck, ReportModel model) {
                if (isCheck) {
                    mSelectModels.add(model);
                } else {
                    mSelectModels.remove(model);
                }
            }
        });

        Bundle bundle = getArguments();
        if (bundle != null) {
            mMode = bundle.getInt(REPORT_FROM_KEY);
            mUserID = bundle.getInt(REPORT_USER_ID);
        }

        if (mMode == FORM_GAME) {
            mReportModels = getGameReportList();
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(U.getDisplayUtils().dip2px(344), U.getDisplayUtils().dip2px(467));
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            params.setMargins(0, 0, 0, U.getDisplayUtils().dip2px(17));
            mContainer.setLayoutParams(params);
        } else if (mMode == FORM_PERSON) {
            mReportModels = getPersonReportList();
        }

        mReportAdapter.setDataList(mReportModels);
        mRecyclerView.stopScroll();
        mRecyclerView.setAdapter(mReportAdapter);
        mReportAdapter.notifyDataSetChanged();

        RxView.clicks(mPlaceView)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getFragmentUtils().popFragment(ReportFragment.this);
                    }
                });

        RxView.clicks(mSubmitTv)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        submitReport();
                    }
                });

    }

    private void submitReport() {
        String content = mReportContent.getText().toString().trim();

        HashMap<String, Object> map = new HashMap<>();
        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));

        UserInfoServerApi userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
        ApiMethods.subscribe(userInfoServerApi.report(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                // TODO: 2019/1/24  需要服务器接口完善
            }
        }, this);
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    private List<ReportModel> getGameReportList() {
        List<ReportModel> rankList = new ArrayList<>();
        rankList.add(new ReportModel(5, "违规作弊"));
        rankList.add(new ReportModel(6, "恶意乱唱"));
        rankList.add(new ReportModel(7, "冒充官方"));
        rankList.add(new ReportModel(2, "侮辱谩骂"));
        rankList.add(new ReportModel(3, "色情低俗"));
        return rankList;
    }

    private List<ReportModel> getPersonReportList() {
        List<ReportModel> personList = new ArrayList<>();
        personList.add(new ReportModel(1, "骗子，有欺诈行为"));
        personList.add(new ReportModel(2, "侮辱谩骂"));
        personList.add(new ReportModel(3, "色情低俗"));
        personList.add(new ReportModel(4, "头像、昵称违规"));
        return personList;
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(KeyboardEvent event) {
        MyLog.d(TAG, "onEvent" + " event=" + event);
        switch (event.eventType) {
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_HIDDEN: {
                mPlaceHolderView.getLayoutParams().height = event.keybordHeight;
                mPlaceHolderView.setLayoutParams(mPlaceHolderView.getLayoutParams());
                break;
            }
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_VISIBLE: {
                mPlaceHolderView.getLayoutParams().height = event.keybordHeight - U.getDisplayUtils().dip2px(118);
                mPlaceHolderView.setLayoutParams(mPlaceHolderView.getLayoutParams());
                break;
            }
        }
    }


}
