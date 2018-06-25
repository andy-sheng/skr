package com.wali.live.watchsdk.personalcenter.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.thornbirds.component.ComponentController;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.presenter.adapter.ConversationAdapter;
import com.wali.live.watchsdk.component.presenter.panel.MessagePresenter;
import com.wali.live.watchsdk.component.view.panel.MessagePanel;

import java.util.List;

public class MyInfoChatThreadView extends RelativeLayout implements MessagePanel.IView {
    public final static String TAG = "MyInfoChatThreadView";

    RecyclerView mChatthreadRv;
    ConversationAdapter mConversationAdapter;
    MessagePresenter mMessagePresenter;

    private ComponentController mComponentController = new ComponentController() {
        @Override
        protected String getTAG() {
            return "MyInfoChatThreadView.ComponentController";
        }
    };

    public MyInfoChatThreadView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.my_info_half_chatthread_layout, this);
        mChatthreadRv = (RecyclerView) this.findViewById(R.id.chatthread_rv);
        mConversationAdapter = new ConversationAdapter();
        mConversationAdapter.setClickListener(new ConversationAdapter.IConversationClickListener() {
            @Override
            public void onItemClick(ConversationAdapter.ConversationItem item) {
                if (mMessagePresenter != null) {
                    mMessagePresenter.onConversationClick(getContext(), item);
                }
            }
        });
        mChatthreadRv.setLayoutManager(new LinearLayoutManager(context,
                LinearLayoutManager.VERTICAL, false));
        mChatthreadRv.setAdapter(mConversationAdapter);

        mMessagePresenter = new MessagePresenter(mComponentController);
        mMessagePresenter.setView(this);

        bindData();
    }

    private void bindData() {

    }


    @Override
    public void onEnterFocusMode() {

    }

    @Override
    public void onEnterUnFocusMode() {

    }

    @Override
    public void onNewConversationList(List<ConversationAdapter.ConversationItem> list) {
        if (mConversationAdapter != null) {
            mConversationAdapter.setItemData(list);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        MyLog.d(TAG, "onAttachedToWindow");

        super.onAttachedToWindow();
        mMessagePresenter.startPresenter();
    }

    @Override
    protected void onDetachedFromWindow() {
        MyLog.d(TAG, "onDetachedFromWindow");
        super.onDetachedFromWindow();
        mMessagePresenter.stopPresenter();
    }

    @Override
    public <T extends View> T getRealView() {
        return (T) this;
    }
}
