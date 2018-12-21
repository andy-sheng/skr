package com.common.log.screenlog;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.common.base.BaseActivity;
import com.common.base.R;
import com.common.utils.U;
import com.common.view.ex.ExTextView;

import java.util.HashSet;
import java.util.LinkedHashSet;

public class ScreenLogView extends RelativeLayout {

    ScrollView mScrollview;
    ExTextView mLogTv;
    ExTextView mCloseTv;
    ExTextView mAutoScrollTv;
    ExTextView mTagSelectTv;

    boolean mNeedScroll = true;

    static PopupWindow sQuickMsgPopWindow;

    HashSet<String> mTags = new HashSet<>();

    Handler mUiHanlder = new Handler();

    public ScreenLogView(Context context) {
        super(context);
        init();
    }

    public ScreenLogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScreenLogView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.screen_log_view_layout, this);
        setBackgroundResource(R.color.black_trans_30);
        mScrollview = (ScrollView) this.findViewById(R.id.scrollview);
        mLogTv = (ExTextView) this.findViewById(R.id.log_tv);
        mCloseTv = (ExTextView) this.findViewById(R.id.close_tv);
        mAutoScrollTv = (ExTextView) this.findViewById(R.id.auto_scroll_tv);
        mTagSelectTv = (ExTextView) this.findViewById(R.id.tag_select_tv);

        mCloseTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sQuickMsgPopWindow != null) {
                    sQuickMsgPopWindow.dismiss();
                    sQuickMsgPopWindow = null;
                }
            }
        });

        mLogTv.setText(ScreenLogPrinter.getInstance().getLogByTag(null));

        mAutoScrollTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mNeedScroll = !mNeedScroll;
                if (mNeedScroll) {
                    mAutoScrollTv.setText("暂停");
                } else {
                    mAutoScrollTv.setText("滚动");
                }
            }
        });
        ScreenLogPrinter.getInstance().setListener(new LogListContainer.Listener() {
            @Override
            public int getNotifyInterval() {
                return 200;
            }

            @Override
            public void notifyLogUpdate(String logs) {
                mUiHanlder.post(new Runnable() {
                    @Override
                    public void run() {
                        mLogTv.setText(mLogTv.getText() + logs);
                        if (mNeedScroll) {
                            mUiHanlder.post(new Runnable() {
                                @Override
                                public void run() {
                                    mScrollview.smoothScrollTo(0, mLogTv.getBottom());
                                }
                            });
                        }
                    }
                });
            }
        });

        if (sQuickMsgPopWindow != null) {
            sQuickMsgPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    sQuickMsgPopWindow = null;
                    destroy();
                }
            });
        }

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        destroy();
    }

    private void destroy() {
        ScreenLogPrinter.getInstance().setListener(null);
        mUiHanlder.removeCallbacksAndMessages(null);
        if (sQuickMsgPopWindow != null) {
            sQuickMsgPopWindow.dismiss();
            sQuickMsgPopWindow = null;
        }
    }

    public static void showScreenLogView() {
        Log.d("ScreenLogView", "showScreenLogView");

        U.getToastUtil().showShort("showScreenLogView");
        if (sQuickMsgPopWindow != null) {
            sQuickMsgPopWindow.dismiss();
            sQuickMsgPopWindow = null;
        }

        Activity activity = U.getActivityUtils().getTopActivity();
        if (activity instanceof BaseActivity) {
            BaseActivity baseActivity = (BaseActivity) activity;
            if (sQuickMsgPopWindow == null) {
                ScreenLogView screenLogView = new ScreenLogView(baseActivity);
                sQuickMsgPopWindow = new PopupWindow(screenLogView, U.getDisplayUtils().getScreenWidth(), U.getDisplayUtils().getScreenHeight());
                sQuickMsgPopWindow.setFocusable(false);
                // 去除动画
//              mQuickMsgPopWindow.setAnimationStyle(R.style.anim_quickmsg_dialog);
                sQuickMsgPopWindow.setBackgroundDrawable(new BitmapDrawable());
                sQuickMsgPopWindow.setOutsideTouchable(true);
            }
            if (!sQuickMsgPopWindow.isShowing()) {
                sQuickMsgPopWindow.showAtLocation(baseActivity.getWindow().getDecorView(), Gravity.START | Gravity.TOP, 0, 0);
            }
        }

    }
}
