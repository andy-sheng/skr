package com.module.home.view;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.common.view.ex.ExTextView;
import com.module.home.R;
import com.module.home.model.GameConfModel;

public class GameTimeTipsView extends RelativeLayout {
    ExTextView mContentTv;
    LinearLayout mOpContainer;
    ExTextView mUpdateBtn;
    GameConfModel mGameConfModel;

    public GameTimeTipsView(Context context) {
        super(context);
        initView();
    }

    public void setGameConfModel(GameConfModel gameConfModel) {
        mGameConfModel = gameConfModel;
        mContentTv.setText(mGameConfModel.getDetail().getContent());
    }

    private void initView() {
        inflate(getContext(), R.layout.game_time_view_layout, this);

        mContentTv = (ExTextView) findViewById(R.id.content_tv);
        mOpContainer = (LinearLayout) findViewById(R.id.op_container);
        mUpdateBtn = (ExTextView) findViewById(R.id.update_btn);
    }
}
