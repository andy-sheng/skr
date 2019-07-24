package com.component.person.view;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.common.core.userinfo.UserInfoManager;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.component.busilib.R;

public class PersonMoreOpView extends RelativeLayout {

    int mUserID;
    boolean mHasUnFollow;
    boolean mHasKick;
    boolean isInBlacked;

    LinearLayout mMenuContainer;
    RelativeLayout mModifyRemarkArea;
    ExTextView mModifyRemarkTv;
    RelativeLayout mUnfollowArea;
    ExTextView mUnfollowTv;
    RelativeLayout mKickArea;
    ExTextView mKickTv;
    RelativeLayout mBlackArea;
    ExTextView mBlackTv;
    RelativeLayout mReportArea;
    ExTextView mReportTv;

    Listener mListener;
    PopupWindow mPopupWindow;

    public PersonMoreOpView(Context context, int mUserID, boolean hasUnFollow, boolean hasKick) {
        super(context);
        this.mUserID = mUserID;
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
        mBlackArea = (RelativeLayout) findViewById(R.id.black_area);
        mBlackTv = (ExTextView) findViewById(R.id.black_tv);
        mReportArea = (RelativeLayout) findViewById(R.id.report_area);
        mReportTv = (ExTextView) findViewById(R.id.report_tv);

        mUnfollowArea.setVisibility(mHasUnFollow ? VISIBLE : GONE);
        mKickArea.setVisibility(mHasKick ? VISIBLE : GONE);

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

        mBlackArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    mListener.onClickBlack(isInBlacked);
                }
            }
        });

        UserInfoManager.getInstance().getBlacklistStatus(Integer.valueOf(mUserID), new UserInfoManager.ResponseCallBack() {
            @Override
            public void onServerSucess(Object obj) {
                if (obj != null) {
                    isInBlacked = (boolean) obj;
                    if (isInBlacked) {
                        mBlackTv.setText("移出黑名单");
                    } else {
                        mBlackTv.setText("加入黑名单");
                    }
                }
            }

            @Override
            public void onServerFailed() {

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
            mPopupWindow = new PopupWindow(this, U.getDisplayUtils().dip2px(134), ViewGroup.LayoutParams.WRAP_CONTENT);
            mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
            mPopupWindow.setOutsideTouchable(true);
        }
        if (!mPopupWindow.isShowing()) {
            // TODO: 2019-05-26  showAsDropDown(会受到组件位置的影响)和 showAtLocation(屏幕的位置)区别
            int l[] = new int[2];
            view.getLocationInWindow(l);
            mPopupWindow.showAtLocation(view, Gravity.START | Gravity.TOP, l[0] - U.getDisplayUtils().dip2px(70), l[1] + U.getDisplayUtils().dip2px(35));
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

        void onClickBlack(boolean isInBlack);
    }
}
