package com.common.emoji;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.KeyboardEvent;
import com.common.utils.U;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashSet;

/**
 * CSDN_LQR
 * 表情键盘协调工具
 */
public class EmotionKeyboard {

    public final static String TAG = "EmotionKeyboard";

    private Activity mActivity;
    private InputMethodManager mInputManager;//软键盘管理类
    private View mEmotionLayout;//表情布局
    private EditText mEditText;
    private View mContentView;//内容布局view,即除了表情布局或者软键盘布局以外的布局，用于固定bar的高度，防止跳闪
    private View mPlaceHolderView;//也可以用这个防止空白站位的view来实现。当自己要求控制键盘布局时

    private OnEmotionButtonOnClickListener mOnEmotionButtonOnClickListener;
    private BoardStatusListener mBoardStatusListener;

    public EmotionKeyboard() {
    }

    public static EmotionKeyboard with(Activity activity) {
        EmotionKeyboard emotionInputDetector = new EmotionKeyboard();
        emotionInputDetector.mActivity = activity;
        emotionInputDetector.mInputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        //隐藏软件盘
//        emotionInputDetector.hideSoftInput();
        return emotionInputDetector;
    }

    /**
     * 绑定内容view，此view用于固定bar的高度，防止跳闪
     */
    @Deprecated
    public EmotionKeyboard bindToContent(View contentView) {
        mContentView = contentView;
        return this;
    }

    /**
     * 绑定内容view，此view用于固定bar的高度，防止跳闪
     */
    public EmotionKeyboard bindToPlaceHodlerView(View holderView) {
        mPlaceHolderView = holderView;
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        return this;
    }

    /**
     * 绑定编辑框
     */
    public EmotionKeyboard bindToEditText(EditText editText) {
        mEditText = editText;
        mEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                /**
                 * 点击编辑框  从表情面板 跳到 键盘面板
                 */
                if (event.getAction() == MotionEvent.ACTION_UP && mEmotionLayout.isShown()) {
                    //mPlaceHolderView 大小无需改变
                    hideEmotionLayout(true);//隐藏表情布局，显示软件盘
                }
                return false;
            }
        });
        return this;
    }

    /**
     * 绑定表情按钮（可以有多个表情按钮）
     *
     * @param emotionButton
     * @return
     */
    public EmotionKeyboard bindToEmotionButton(View... emotionButton) {
        for (View view : emotionButton) {
            view.setOnClickListener(getOnEmotionButtonOnClickListener());
        }
        return this;
    }

    private View.OnClickListener getOnEmotionButtonOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnEmotionButtonOnClickListener != null) {
                    if (mOnEmotionButtonOnClickListener.onEmotionButtonOnClickListener(v)) {
                        return;
                    }
                }

                if (mEmotionLayout.isShown()) {
                    hideEmotionLayout(true);//隐藏表情布局，显示软件盘
                } else {
                    //同上
                    if (isSoftInputShown()) {
                        showEmotionLayout();
                    } else {
                        showEmotionLayout();//两者都没显示，直接显示表情布局
                    }
                }
            }
        };
    }

    /*================== 表情按钮点击事件回调 begin ==================*/
    public interface OnEmotionButtonOnClickListener {
        /**
         * 主要是为了适用仿微信的情况，微信有一个表情按钮和一个功能按钮，这2个按钮都是控制了底部区域的显隐
         *
         * @param view
         * @return true:拦截切换输入法，false:让输入法正常切换
         */
        boolean onEmotionButtonOnClickListener(View view);
    }

    public void setOnEmotionButtonOnClickListener(OnEmotionButtonOnClickListener onEmotionButtonOnClickListener) {
        mOnEmotionButtonOnClickListener = onEmotionButtonOnClickListener;
    }

    public void setBoardStatusListener(BoardStatusListener boardStatusListener){
        mBoardStatusListener = boardStatusListener;
    }
    /*================== 表情按钮点击事件回调 end ==================*/

    /**
     * 设置表情内容布局
     *
     * @param emotionLayout
     * @return
     */
    public EmotionKeyboard setEmotionLayout(View emotionLayout) {
        mEmotionLayout = emotionLayout;
        return this;
    }

    /**
     * 点击返回键时先隐藏表情布局
     *
     * @return
     */
    public boolean interceptBackPress() {
        if (mEmotionLayout.isShown()) {
            hideEmotionLayout(false);
            return true;
        }
        return false;
    }

    public void showEmotionLayout() {
        int softInputHeight = U.getKeyBoardUtils().getKeyBoardHeightNow(mActivity);
        if (softInputHeight == 0) {
            softInputHeight = U.getKeyBoardUtils().getKeyBoardHeight();
        }
        hideSoftInput();
        /**
         * 将表情面板弄成和软键盘一样高
         */
        mEmotionLayout.getLayoutParams().height = softInputHeight;
        mEmotionLayout.setVisibility(View.VISIBLE);

        mPlaceHolderView.getLayoutParams().height = softInputHeight;
        mPlaceHolderView.setLayoutParams(mPlaceHolderView.getLayoutParams());
        if (mBoardStatusListener != null) {
            mBoardStatusListener.onBoradShow();
        }
    }

    /**
     * 隐藏表情布局
     *
     * @param showSoftInput 是否显示软件盘
     */
    public void hideEmotionLayout(boolean showSoftInput) {
        if (mEmotionLayout.isShown()) {
            mEmotionLayout.setVisibility(View.GONE);
            if (showSoftInput) {
                showSoftInput();
            }else{
                // 隐藏表情面板且不显示软键盘
                mPlaceHolderView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                mPlaceHolderView.setLayoutParams(mPlaceHolderView.getLayoutParams());
                if (mBoardStatusListener != null) {
                    mBoardStatusListener.onBoradHide();
                }
            }
        }
    }

//    /**
//     * 锁定内容高度，防止跳闪
//     */
//    private void lockContentHeight() {
//        /**
//         * 这个高度为键盘起来后，被挤压的内容区的高度
//         */
//        if (mContentView != null && mContentView.getLayoutParams() instanceof LinearLayout.LayoutParams) {
//            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mContentView.getLayoutParams();
//            params.height = mContentView.getHeight();
//            params.weight = 0.0F;
//        }
//
//        if (mPlaceHolderView != null) {
//            mPlaceHolderView.getLayoutParams().height = U.getKeyBoardUtils().getKeyBoardHeight();
//            mPlaceHolderView.setLayoutParams(mPlaceHolderView.getLayoutParams());
//        }
//    }
//
//    /**
//     * 释放被锁定的内容高度
//     */
//    public void unlockContentHeightDelayed() {
//        if (mContentView != null) {
//            mEditText.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (mContentView.getLayoutParams() instanceof LinearLayout.LayoutParams) {
//                        ((LinearLayout.LayoutParams) mContentView.getLayoutParams()).weight = 1.0F;
//                    } else {
//                    }
//
//                }
//            }, 200L);
//        }
//
//        if (mPlaceHolderView != null) {
//            mEditText.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mPlaceHolderView.getLayoutParams().height =;
//                    mPlaceHolderView.setLayoutParams(mPlaceHolderView.getLayoutParams());
//                }
//            }, 200L);
//        }
//
//    }

    /**
     * 编辑框获取焦点，并显示软件盘
     */
    public void showSoftInput() {
        if (mBoardStatusListener != null) {
            mBoardStatusListener.onBoradShow();
        }
        mEditText.requestFocus();
        mEditText.post(new Runnable() {
            @Override
            public void run() {
                mInputManager.showSoftInput(mEditText, 0);
            }
        });
    }

    /**
     * 隐藏软件盘
     */
    public void hideSoftInput() {
        mInputManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    /**
     * 是否显示软件盘
     *
     * @return
     */
    public boolean isSoftInputShown() {
        int keyboradHeight = U.getKeyBoardUtils().getKeyBoardHeightNow(mActivity);
        MyLog.d(TAG, "isSoftInputShown keyboradHeight:" + keyboradHeight);
        return keyboradHeight != 0;
    }

    public boolean isEmotionShown() {
        return mEmotionLayout.isShown();
    }

    public void destroy() {
        EventBus.getDefault().unregister(this);
    }

    /*
     只要有软键盘或者表情面板，其中一个在显示，那placeHolder就是和软键盘一样的高度
     只要两个同时都不显示了，才还原
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(KeyboardEvent event) {
        switch (event.eventType) {
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_HIDDEN:
                if (!isEmotionShown()) {
                    mPlaceHolderView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    mPlaceHolderView.setLayoutParams(mPlaceHolderView.getLayoutParams());

                    // 隐藏键盘且不显示表情面板
                    if (mBoardStatusListener != null) {
                        mBoardStatusListener.onBoradHide();
                    }
                } else {
                    // 表情面板仍然需要显示， 延迟修改布局，防止闪一下
//                    HandlerTaskTimer.newBuilder().delay(200).start(new HandlerTaskTimer.ObserverW() {
//                        @Override
//                        public void onNext(Integer integer) {
//                            mPlaceHolderView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
//                            mPlaceHolderView.setLayoutParams(mPlaceHolderView.getLayoutParams());
//                        }
//                    });
                }
                break;
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_VISIBLE:
                mPlaceHolderView.getLayoutParams().height = event.keybordHeight;
                mPlaceHolderView.setLayoutParams(mPlaceHolderView.getLayoutParams());
                break;
        }
    }

    public interface BoardStatusListener{
        void onBoradShow();
        void onBoradHide();
    }
}
