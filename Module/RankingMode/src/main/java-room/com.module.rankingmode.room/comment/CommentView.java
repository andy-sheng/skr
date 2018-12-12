package com.module.rankingmode.room.comment;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.rankingmode.R;
import com.module.rankingmode.msg.event.CommentMsgEvent;
import com.module.rankingmode.room.event.InputBoardEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class CommentView extends RelativeLayout {
    RecyclerView mCommentRv;

    LinearLayoutManager mLinearLayoutManager;

    CommentAdapter mCommentAdapter;

    public CommentView(Context context) {
        super(context);
        init();
    }

    public CommentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.comment_view_layout, this);
        mCommentRv = (RecyclerView) this.findViewById(R.id.comment_rv);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        mLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true);
        mCommentRv.setLayoutManager(mLinearLayoutManager);
        mCommentAdapter = new CommentAdapter(new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, Object model) {

            }
        });
        mCommentRv.setAdapter(mCommentAdapter);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(CommentMsgEvent event) {
        CommentModel commentModel = new CommentModel();
        commentModel.setText(event.text);
        commentModel.setCommentType(CommentModel.TYPE_TEXT);
        mCommentAdapter.insertFirst(commentModel);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(InputBoardEvent event) {
        RelativeLayout.LayoutParams lp  = (LayoutParams) this.getLayoutParams();
        if (event.show) {
            lp.addRule(RelativeLayout.ABOVE,R.id.input_container_view);
        } else {
            lp.addRule(RelativeLayout.ABOVE,R.id.bottom_container_view);
        }
        setLayoutParams(lp);
    }

}
