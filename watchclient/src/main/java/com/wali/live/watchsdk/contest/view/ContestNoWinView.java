package com.wali.live.watchsdk.contest.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.base.mvp.specific.RxRelativeLayout;
import com.wali.live.watchsdk.R;

/**
 * Created by lan on 2018/1/15.
 */
public class ContestNoWinView extends RxRelativeLayout implements View.OnClickListener {
    private View mBgView;
    private TextView mContinueBtn;

    private boolean mIsShown = false;

    public ContestNoWinView(Context context) {
        super(context);
        init(context);
    }

    public ContestNoWinView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ContestNoWinView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.contest_no_win_view, this);

        mBgView = $(R.id.background_view);
        $click(mBgView, this);

        mContinueBtn = $(R.id.continue_btn);
        mContinueBtn.setOnClickListener(this);
    }

    public boolean isShown() {
        return mIsShown;
    }

    public void hide() {
        if (!mIsShown) {
            return;
        }
        mIsShown = false;
        setVisibility(View.GONE);
    }

    public void show() {
        if (mIsShown) {
            return;
        }
        mIsShown = true;
        setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.background_view || id == R.id.continue_btn) {
            hide();
        }
    }
}
