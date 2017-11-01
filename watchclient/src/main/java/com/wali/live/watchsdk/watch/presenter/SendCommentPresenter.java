package com.wali.live.watchsdk.watch.presenter;

import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.activity.assist.IBindActivityLIfeCycle;
import com.base.event.KeyboardEvent;
import com.base.event.SdkEventClass;
import com.base.global.GlobalData;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.base.utils.toast.ToastUtils;
import com.base.view.MLTextView;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.preference.MLPreferenceUtils;
import com.mi.live.data.push.SendBarrageManager;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.common.smiley.SmileyInputFilter;
import com.wali.live.common.smiley.SmileyParser;
import com.wali.live.common.smiley.SmileyPicker;
import com.wali.live.common.smiley.SmileyTranslateFilter;
import com.wali.live.common.view.PlaceHolderView;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.watch.model.RoomInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

/**
 * Created by chengsimin on 16/9/9.
 */
public class SendCommentPresenter implements IBindActivityLIfeCycle {
    public final static String TAG = SendCommentPresenter.class.getSimpleName();

    ViewStub mViewStub;
    ViewGroup mRootView;

    EditText mInputView;
    ImageView mShowSmileyBtn;
    MLTextView mSendBtn;

    BaseActivity mActivity;

    ViewStub mSimilePickerViewStub;
    SmileyPicker mSmileyPicker;

    PlaceHolderView mPlaceHolderView;

    View mCommentView;

    RoomInfo mRoomInfo;

    RoomBaseDataModel mMyRoomData;

    Runnable mHideSendCommentAreaCallBack;

    boolean mLandscape = false;

    boolean mHasInflate = false;

    boolean mHasInflateSimle = false;

    boolean mInputViewShow = false;

    public static int sEditTextHeight = DisplayUtils.dip2px(43);

    public SendCommentPresenter(BaseActivity activity, RoomBaseDataModel myRoomData, RoomInfo roomInfo, Runnable hideSendCommentAreaCallBack) {
        this.mActivity = activity;
        this.mMyRoomData = myRoomData;
        this.mRoomInfo = roomInfo;
        this.mHideSendCommentAreaCallBack = hideSendCommentAreaCallBack;
    }

    public void setViewStub(ViewStub viewStub) {
        mViewStub = viewStub;
    }

    public void setmSimilePickerViewStub(ViewStub similePickerViewStub) {
        this.mSimilePickerViewStub = similePickerViewStub;
    }

    public void setPlaceHolderView(PlaceHolderView mPlaceHolderView) {
        this.mPlaceHolderView = mPlaceHolderView;
    }

    public void setLiveCommentView(View commentView) {
        this.mCommentView = commentView;
    }

    private TextWatcher mTextWatcher;

    public void inflate() {
        if (mHasInflate) {
            return;
        }
        mHasInflate = true;
        mRootView = (ViewGroup) mViewStub.inflate();
        mInputView = $(mRootView, R.id.text_editor);
        mShowSmileyBtn = $(mRootView, R.id.show_smiley_btn);
        mSendBtn = $(mRootView, R.id.send_btn);


        // 初始化EditText各项参数
        mTextWatcher = new TextWatcher() {
            boolean mIsEmpty = true;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mInputView.getText().toString().equals("")) {
                    if (!mIsEmpty) {
                        mSendBtn.setEnabled(false);
//                        mSendBtn.setBackground(GlobalData.app().getResources().getDrawable(R.drawable.live_barrage_send_button_bg_null));
//                        mSendBtn.setTextColor(GlobalData.app().getResources().getColor(R.color.color_black_trans_18));
                        mInputView.setHintTextColor(GlobalData.app().getResources().getColor(R.color.color_black_trans_30));
//                        mInputView.setHint("hint");
                        mIsEmpty = true;
                    }
                } else {
                    if (mIsEmpty) {
                        mSendBtn.setEnabled(true);
//                        mSendBtn.setBackground(GlobalData.app().getResources().getDrawable(R.drawable.live_send_btn_bg));
//                        mSendBtn.setTextColor(GlobalData.app().getResources().getColor(R.color.color_black_trans_80));
                        mIsEmpty = false;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        mInputView.addTextChangedListener(mTextWatcher);

        mInputView.setFilters(new InputFilter[]{
                new SmileyTranslateFilter(mInputView.getTextSize()),
                new SmileyInputFilter(mInputView, 120),
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        return source;
                    }
                }
        });

        mInputView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (null == event) {
                    return false;
                }
                return (event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
        mInputView.setOnFocusChangeListener(new TextView.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            }
        });
        mInputView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN: {
                        showInputView();
                    }
                    break;
                }
                return false;
            }
        });
        mInputView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputView();
            }
        });
        mSoftKeyboardHeight = MLPreferenceUtils.getKeyboardHeight(true);

        // 表情按钮
        RxView.clicks(mShowSmileyBtn)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        MyLog.d(TAG, "showSmileyBtn");
                        if (mSmileyPicker == null) {
                            inflateSimleyPicker();
                        }
                        if (mSmileyPicker != null) {
                            if (mSmileyPicker.isPickerShowed()) {
                                hideSmileyPickerAndShowInputSoft();
                            } else {
                                showSmileyPicker();
                            }
                        }
                    }
                });


        RxView.clicks(mSendBtn)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(new Action1<Void>() {
                               @Override
                               public void call(Void aVoid) {
                                   String msg = mInputView.getText().toString();
                                   String body = SmileyParser.getInstance()
                                           .convertString(msg, SmileyParser.TYPE_LOCAL_TO_GLOBAL).toString();
                                   if (mMyRoomData != null && !mMyRoomData.canSpeak()) {
                                       ToastUtils.showToast(GlobalData.app(), R.string.can_not_speak);
                                       return;
                                   }
                                   if (TextUtils.isEmpty(body.trim())) {
                                       return;
                                   }
                                   BarrageMsg barrageMsg = SendBarrageManager.createBarrage(BarrageMsgType.B_MSG_TYPE_TEXT, body, mRoomInfo.getLiveId(), mRoomInfo.getPlayerId(), System.currentTimeMillis(), null);
                                   SendBarrageManager
                                           .sendBarrageMessageAsync(barrageMsg)
                                           .subscribe();
                                   SendBarrageManager.pretendPushBarrage(barrageMsg);
                                   mInputView.setText("");
                               }
                           }
                );
        mInputView.clearFocus();
        mRootView = null;
    }

    private void inflateSimleyPicker() {
        View root = mSimilePickerViewStub.inflate();
        mHasInflateSimle = true;
        mSmileyPicker = (SmileyPicker) root.findViewById(R.id.smiley_picker);
        mSmileyPicker.setEditText(mInputView);
        mSimilePickerViewStub = null;
    }

    boolean mIsShowSmilyPicker = false;

    public void hideSmileyPickerAndShowInputSoft() {
        mShowSmileyBtn.setImageResource(R.drawable.chat_bottom_enter_expression_btn_2);
        mIsShowSmilyPicker = false;
        KeyboardUtils.showKeyboard(mActivity);
        if (mSmileyPicker != null) {
            mSmileyPicker.hide(null);
        }
    }

    public void showSmileyPicker() {
        mShowSmileyBtn.setImageResource(R.drawable.chat_bottom_enter_expression_keyboard_btn);
        mIsShowSmilyPicker = true;
        KeyboardUtils.hideKeyboardImmediately(mActivity);
        if (mSmileyPicker != null) {
            mSmileyPicker.show();
            ViewGroup.LayoutParams lp = mSmileyPicker.getLayoutParams();
            lp.height = mSoftKeyboardHeight;
            mSmileyPicker.setLayoutParams(lp);
        }
    }

    private int mSoftKeyboardHeight = 0;

    boolean mShowInputView = false;

    protected void showInputView() {
        MyLog.d(TAG, "showInputView");

        MyLog.d(TAG, "showInputView mSoftKeyboardHeight:" + mSoftKeyboardHeight);
        if (!mHasInflate) {
            return;
        }
        if (mShowInputView) {
            return;
        }
        mShowInputView = true;
        mViewStub.setVisibility(View.VISIBLE);
        KeyboardUtils.showKeyboard(mActivity);
        if (mShowSmileyBtn != null) {
            mShowSmileyBtn.setVisibility(View.VISIBLE);
            mShowSmileyBtn.setImageResource(R.drawable.chat_bottom_enter_expression_btn_2);
        }
        if (mInputView != null) {
            mInputView.setVisibility(View.VISIBLE);
            mInputView.requestFocus();
        }
        //如果是管理员,动态修改edittext的布局
        mPlaceHolderView.onShowInputView(mSoftKeyboardHeight);
        if (mLandscape) {
            if (mCommentView != null) {
                mCommentView.setVisibility(View.GONE);
            }
        }

    }

    Handler mUiHanlder = new Handler();

    protected void hideInputView() {
        MyLog.d(TAG, "hideInputView");
        if (!mShowInputView) {
            return;
        }
        KeyboardUtils.hideKeyboard(mActivity, mInputView);
        mUiHanlder.post(new Runnable() {
            @Override
            public void run() {
                if (mHasInflate) {
                    mViewStub.setVisibility(View.GONE);
                }
                if (mInputView != null) {
//            KeyboardUtils.hideKeyboardImmediately(mActivity);

                    mInputView.setVisibility(View.GONE);
                    mInputView.clearFocus();
                }
                if (mShowSmileyBtn != null) {
                    mShowSmileyBtn.setImageResource(R.drawable.chat_bottom_enter_expression_btn_2);
                }
                mIsShowSmilyPicker = false;
                if (mSmileyPicker != null) {
                    mSmileyPicker.hide(null);
                }
                mPlaceHolderView.onHideInputView();
                if (mHideSendCommentAreaCallBack != null) {
                    mHideSendCommentAreaCallBack.run();
                }
                if (mCommentView != null) {
                    mCommentView.setVisibility(View.VISIBLE);
                }
                mShowInputView = false;
            }
        });
    }


    public boolean ismInputViewShow() {
        return mInputViewShow;
    }

    @Subscribe
    public void onEvent(SdkEventClass.OrientEvent event) {
        mLandscape = event.isLandscape();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(KeyboardEvent event) {
        MyLog.d(TAG, "KeyboardEvent eventType = " + event.eventType);
        if (!mShowInputView) {
            // 键盘不可见，其实可能是别的地方触发了键盘事件
            return;
        }
        switch (event.eventType) {
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_VISIBLE:
                try {
                    int keyboardHeight = Integer.parseInt(String.valueOf(event.obj1));
                    if (mPlaceHolderView.getVisibility() == View.VISIBLE) {
                        if (mPlaceHolderView.getHeight() != keyboardHeight) {
                            mSoftKeyboardHeight = keyboardHeight;
                            MyLog.v(TAG, " keyboardHeight=" + keyboardHeight + ", mPlaceHolderView.getHeight()=" + mPlaceHolderView.getHeight());
                            mPlaceHolderView.onShowInputView(mSoftKeyboardHeight);
                            MLPreferenceUtils.setKeyboardHeight(mSoftKeyboardHeight, true);
                        }
                    }
                } catch (NumberFormatException e) {
                    MyLog.e(TAG, e);
                }
                if (mIsShowSmilyPicker) {
                    hideSmileyPickerAndShowInputSoft();
                }
                break;
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_HIDDEN:
                if (!mIsShowSmilyPicker) {
                    hideInputView();
                }
                break;
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_VISIBLE_ALWAYS_SEND: {
                int keyboardHeight = Integer.parseInt(String.valueOf(event.obj1));
                if (mSoftKeyboardHeight != keyboardHeight) {
                    mSoftKeyboardHeight = keyboardHeight;
                    MLPreferenceUtils.setKeyboardHeight(mSoftKeyboardHeight, true);
                }
                if (mIsShowSmilyPicker) {
                    hideSmileyPickerAndShowInputSoft();
                }
            }
            break;
        }
    }

    public void showInputArea() {
        MyLog.d(TAG, "showInputArea");
        inflate();
        mInputViewShow = true;
        showInputView();
    }

    public void hideInputArea() {
        MyLog.d(TAG, "hideInputArea");
        mInputViewShow = false;
        hideInputView();
    }

    <T extends View> T $(View view, int id) {
        return (T) view.findViewById(id);
    }

    @Override
    public void onActivityDestroy() {
        if (mInputView != null && mTextWatcher != null) {
            mInputView.removeTextChangedListener(mTextWatcher);
            mInputView.setOnEditorActionListener(null);
        }
        hideInputArea();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onActivityCreate() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }


}
