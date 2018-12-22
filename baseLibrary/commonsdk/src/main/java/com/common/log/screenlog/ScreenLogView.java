package com.common.log.screenlog;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.common.base.BaseActivity;
import com.common.base.R;
import com.common.utils.DateTimeUtils;
import com.common.utils.U;
import com.common.view.ex.ExTextView;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;

import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

public class ScreenLogView extends RelativeLayout {

    ScrollView mScrollview;
    ExTextView mLogTv;
    ExTextView mCloseTv;
    ExTextView mAutoScrollTv;
    ExTextView mTagSelectTv;
    ExTextView mSaveBtn;

    boolean mNeedScroll = true;

    static PopupWindow sQuickMsgPopWindow;

    Handler mUiHanlder = new Handler();

    TagSelectView mTagSelectView;

    HashSet<String> mSelectTagSet = new HashSet<>();

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
        mSaveBtn = (ExTextView) this.findViewById(R.id.save_btn);

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
        scrollBottom();

        mAutoScrollTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mNeedScroll = !mNeedScroll;
                if (mNeedScroll) {
                    mAutoScrollTv.setText("暂停滚动");
                } else {
                    mAutoScrollTv.setText("开启滚动");
                }
            }
        });

        mTagSelectTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTagSelectView == null) {
                    mTagSelectView = new TagSelectView(getContext());
                }
                addView(mTagSelectView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                mTagSelectView.setListener(new TagSelectView.Listener() {
                                               @Override
                                               public void onResult(HashSet<String> set) {
                                                   mSelectTagSet.clear();
                                                   if (set != null) {
                                                       mSelectTagSet.addAll(set);
                                                   }
                                                   if (mSelectTagSet.isEmpty()) {
                                                       mLogTv.setText(ScreenLogPrinter.getInstance().getLogByTag(null));
                                                       scrollBottom();
                                                   } else {
                                                       mLogTv.setText(ScreenLogPrinter.getInstance().getLogByTag(mSelectTagSet));
                                                       scrollBottom();
                                                   }
                                                   removeView(mTagSelectView);
                                               }
                                           }, ScreenLogPrinter.getInstance().getAllLogTags()
                        , mSelectTagSet);

            }
        });

        mSaveBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = U.getDateTimeUtils().formatTimeStringForDate(System.currentTimeMillis(), "yyyy-MM-dd_HH_mm_ss");

                String filename = U.getAppInfoUtils().getMainDir() + File.separator + "slog_" + name + ".txt";
                File file = new File(filename);
                BufferedSink bufferedSink = null;
                try {
                    Sink sink = Okio.sink(file);
                    bufferedSink = Okio.buffer(sink);
                    bufferedSink.writeString(mLogTv.getText().toString(), Charset.forName("GBK"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (null != bufferedSink) {
                        bufferedSink.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                U.getToastUtil().showLong("日志文件存在：" + filename);
            }
        });
        ScreenLogPrinter.getInstance().setListener(new LogListContainer.Listener() {
            @Override
            public boolean accept(String tag) {
                if (mSelectTagSet.isEmpty()) {
                    return true;
                }
                return mSelectTagSet.contains(tag);
            }

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
                            scrollBottom();
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

    void scrollBottom() {
        mUiHanlder.post(new Runnable() {
            @Override
            public void run() {
                mScrollview.smoothScrollTo(0, mLogTv.getBottom());
            }
        });
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

    static long mLastShowTs = 0;

    public static void showScreenLogView() {
        Log.d("ScreenLogView", "showScreenLogView");

        if (sQuickMsgPopWindow != null) {
            if (System.currentTimeMillis() - mLastShowTs > 5000) {
                U.getToastUtil().showShort("showScreenLogView already,dismiss first");
                sQuickMsgPopWindow.dismiss();
            } else {
                U.getToastUtil().showShort("showScreenLogView already show");
                return;
            }
        }
        mLastShowTs = System.currentTimeMillis();
        Activity activity = U.getActivityUtils().getTopActivity();
        if (activity instanceof BaseActivity) {
            BaseActivity baseActivity = (BaseActivity) activity;
            if (sQuickMsgPopWindow == null) {
                ScreenLogView screenLogView = new ScreenLogView(baseActivity);
                sQuickMsgPopWindow = new PopupWindow(screenLogView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
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
