package com.component.person.view;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.common.core.userinfo.ResponseCallBack;
import com.common.core.userinfo.UserInfoManager;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.component.busilib.R;

public class PersonMoreOpView extends RelativeLayout {

    int mUserID;
    boolean mHasKick;
    boolean isInBlacked;

    LinearLayout mMenuContainer;
    RelativeLayout mModifyRemarkArea;
    ExTextView mModifyRemarkTv;
    RelativeLayout mSpFollowArea;
    ExTextView mSpFollowTv;
    RelativeLayout mKickArea;
    ExTextView mKickTv;
    RelativeLayout mBlackArea;
    ExTextView mBlackTv;
    RelativeLayout mReportArea;
    ExTextView mReportTv;

    Listener mListener;
    PopupWindow mPopupWindow;

    boolean isFollow;
    boolean isSpFollow;

    public PersonMoreOpView(Context context, int mUserID, boolean isFollow, boolean isSpFollow, boolean hasKick) {
        super(context);
        this.mUserID = mUserID;
        this.isFollow = isFollow;
        this.isSpFollow = isSpFollow;
        this.mHasKick = hasKick;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.person_more_op_view_layout, this);
        setBackgroundResource(R.drawable.common_menu_bg);

        mMenuContainer = findViewById(R.id.menu_container);
        mModifyRemarkArea = findViewById(R.id.modify_remark_area);
        mModifyRemarkTv = findViewById(R.id.modify_remark_tv);
        mSpFollowArea = findViewById(R.id.sp_follow_area);
        mSpFollowTv = findViewById(R.id.sp_follow_tv);
        mKickArea = findViewById(R.id.kick_area);
        mKickTv = findViewById(R.id.kick_tv);
        mBlackArea = findViewById(R.id.black_area);
        mBlackTv = findViewById(R.id.black_tv);
        mReportArea = findViewById(R.id.report_area);
        mReportTv = findViewById(R.id.report_tv);

        mKickArea.setVisibility(mHasKick ? VISIBLE : GONE);

        if (isFollow) {
            mSpFollowArea.setVisibility(VISIBLE);
            if (isSpFollow) {
                Drawable drawable = U.getDrawable(R.drawable.person_sp_unfollow_icon);
                drawable.setBounds(0, 0, U.getDisplayUtils().dip2px(21), U.getDisplayUtils().dip2px(18));
                mSpFollowTv.setCompoundDrawables(drawable, null, null, null);
                mSpFollowTv.setText("取消特关");
            } else {
                Drawable drawable = U.getDrawable(R.drawable.person_sp_follow_icon);
                drawable.setBounds(0, 0, U.getDisplayUtils().dip2px(21), U.getDisplayUtils().dip2px(18));
                mSpFollowTv.setCompoundDrawables(drawable, null, null, null);
                mSpFollowTv.setText("特别关注Ta");
            }
        } else {
            mSpFollowArea.setVisibility(GONE);
        }

        mModifyRemarkArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    mListener.onClickRemark();
                }
            }
        });

        mSpFollowArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    mListener.onClickSpFollow();
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

        UserInfoManager.getInstance().getBlacklistStatus(Integer.valueOf(mUserID), new ResponseCallBack() {
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
            mPopupWindow.showAtLocation(view, Gravity.START | Gravity.TOP, l[0] - U.getDisplayUtils().dip2px(70), l[1] + U.getDisplayUtils().dip2px(50));
        }
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        void onClickRemark();

        void onClickSpFollow();

        void onClickReport();

        void onClickKick();

        void onClickBlack(boolean isInBlack);
    }
}
