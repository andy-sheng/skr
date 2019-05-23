package com.zq.person.view;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.component.busilib.R;

public class PersonMoreOpView extends RelativeLayout {

    boolean mHasUnFollow;
    boolean mHasKick;

    LinearLayout mMenuContainer;
    RelativeLayout mModifyRemarkArea;
    ExTextView mModifyRemarkTv;
    RelativeLayout mUnfollowArea;
    ExTextView mUnfollowTv;
    RelativeLayout mKickArea;
    ExTextView mKickTv;
    RelativeLayout mReportArea;
    ExTextView mReportTv;

    Listener mListener;
    PopupWindow mPopupWindow;

    public PersonMoreOpView(Context context, boolean hasUnFollow, boolean hasKick) {
        super(context);
        this.mHasUnFollow = hasUnFollow;
        this.mHasKick = hasKick;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.person_more_op_view_layout, this);
        setBackgroundResource(R.drawable.common_menu_bg);

        mMenuContainer = (LinearLayout) findViewById(R.id.menu_container);
        mModifyRemarkArea = (RelativeLayout) findViewById(R.id.modify_remark_area);
        mModifyRemarkTv = (ExTextView) findViewById(R.id.modify_remark_tv);
        mUnfollowArea = (RelativeLayout) findViewById(R.id.unfollow_area);
        mUnfollowTv = (ExTextView) findViewById(R.id.unfollow_tv);
        mKickArea = (RelativeLayout) findViewById(R.id.kick_area);
        mKickTv = (ExTextView) findViewById(R.id.kick_tv);
        mReportArea = (RelativeLayout) findViewById(R.id.report_area);
        mReportTv = (ExTextView) findViewById(R.id.report_tv);

        mUnfollowArea.setVisibility(mHasUnFollow ? VISIBLE : GONE);
        mKickTv.setVisibility(mHasKick ? VISIBLE : GONE);

        mModifyRemarkArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    mListener.onClickRemark();
                }
            }
        });

        mUnfollowArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    mListener.onClickUnFollow();
                }
            }
        });

        mReportArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    mListener.onClickReport();
                }
            }
        });

        mKickArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    mListener.onClickKick();
                }
            }
        });
    }

    public void dismiss() {
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }
    }

    public void showAt(View view) {
        if (mPopupWindow == null) {
            mPopupWindow = new PopupWindow(this, U.getDisplayUtils().dip2px(118), ViewGroup.LayoutParams.WRAP_CONTENT);
            mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
            mPopupWindow.setOutsideTouchable(true);
        }
        if (!mPopupWindow.isShowing()) {
            mPopupWindow.showAsDropDown(view, -U.getDisplayUtils().dip2px(2), U.getDisplayUtils().dip2px(5));
        }
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        void onClickRemark();

        void onClickUnFollow();

        void onClickReport();

        void onClickKick();
    }
}
