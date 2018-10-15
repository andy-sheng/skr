package com.wali.live.watchsdk.personalcenter.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.ReplacementTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.BaseSdkActivity;
import com.base.event.KeyboardEvent;
import com.base.fragment.BaseFragment;
import com.base.fragment.FragmentDataListener;
import com.base.fragment.RxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.base.utils.toast.ToastUtils;
import com.base.view.BackTitleBar;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.user.User;
import com.thornbirds.component.ComponentController;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.presenter.adapter.ConversationAdapter;
import com.wali.live.watchsdk.component.presenter.panel.MessagePresenter;
import com.wali.live.watchsdk.component.view.panel.MessagePanel;
import com.wali.live.watchsdk.editinfo.EditInfoActivity;
import com.wali.live.watchsdk.editinfo.fragment.presenter.EditNamePresenter;
import com.wali.live.watchsdk.editinfo.fragment.presenter.IEditNameView;
import com.wali.live.watchsdk.income.view.NoLeakEditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

/**
 * Created by zhujianning on 18-6-25.
 * 半屏修改名字页面
 */

public class ChatThreadHalfFragment extends RxFragment implements MessagePanel.IView {
    private static final String TAG = "EditNameHalfFragment";
    public static final int REQUEST_CODE = GlobalData.getRequestCode();

    RelativeLayout mRootContainer;
    View mPlaceHolderView;
    RelativeLayout mContainer;
    BackTitleBar mTitleBar;
    RecyclerView mRecyclerView;

    MessagePresenter mMessagePresenter;

    ConversationAdapter mConversationAdapter;
    private ComponentController mComponentController = new ComponentController() {
        @Override
        protected String getTAG() {
            return "MyInfoChatThreadView.ComponentController";
        }
    };

    @Override
    public int getRequestCode() {
        return REQUEST_CODE;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.frag_chatthread_half, container, false);
    }

    @Override
    protected void bindView() {
        mRootContainer = (RelativeLayout) mRootView.findViewById(R.id.root_container);
        mPlaceHolderView = (View) mRootView.findViewById(R.id.place_holder_view);
        mContainer = (RelativeLayout) mRootView.findViewById(R.id.container);
        mTitleBar = (BackTitleBar) mRootView.findViewById(R.id.title_bar);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);

        mConversationAdapter = new ConversationAdapter();
        mConversationAdapter.setClickListener(new ConversationAdapter.IConversationClickListener() {
            @Override
            public void onItemClick(ConversationAdapter.ConversationItem item) {
                if (mMessagePresenter != null) {
                    mMessagePresenter.onConversationClick(getContext(), item);
                }
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mConversationAdapter);


        mMessagePresenter = new MessagePresenter(mComponentController);
        mMessagePresenter.setView(this);
        mMessagePresenter.startPresenter(MessagePresenter.MODE_UN_FOCUS);
        mTitleBar.getBackBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentNaviUtils.popFragment(getActivity());
            }
        });
        bindData();
    }

    private void bindData() {
        mTitleBar.setTitle("未关注的人");
    }


    public static void openFragment(BaseSdkActivity activity, int containerId, FragmentDataListener listener) {
        Bundle bundle = new Bundle();
        BaseFragment fragment = FragmentNaviUtils.addFragment(activity, containerId, ChatThreadHalfFragment.class,
                bundle, true, true, true);
        fragment.initDataResult(REQUEST_CODE, listener);
    }

    @Override
    public void onEnterFocusMode() {

    }

    @Override
    public void onEnterUnFocusMode() {

    }

    @Override
    public void onNewConversationList(List<ConversationAdapter.ConversationItem> list) {
        mConversationAdapter.setItemData(list);
    }

    @Override
    public <T extends View> T getRealView() {
        return (T) mRootView;
    }
}
