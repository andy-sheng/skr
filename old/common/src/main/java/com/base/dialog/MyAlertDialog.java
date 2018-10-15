package com.base.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.ColorInt;
import android.support.annotation.StringRes;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.base.common.R;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;

/**
 * Created by lan on 15-12-14.
 */
public class MyAlertDialog extends Dialog implements DialogInterface {

    private final static String TAG = "MyAlertDialog";

    private MyAlertController mAlertController;

    public CharSequence[] mItemTexts;

    private Context mContext;

    private DismissCallBack mDismissCallBack;

    protected MyAlertDialog(Context context) {
        this(context, R.style.MyAlertDialog);
    }

    protected MyAlertDialog(Context context, int theme) {
        super(context, theme);
        mAlertController = new MyAlertController(context, this, getWindow());
        mContext = context;
    }

    protected MyAlertDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, R.style.MyAlertDialog);
        setCancelable(cancelable);
        setOnCancelListener(cancelListener);
        mAlertController = new MyAlertController(context, this, getWindow());
        mContext = context;
    }

    public void setParentMargin(int left, int top, int right, int bottom){
        mAlertController.setParentMargin(left, top, right, bottom);
    }

    /**
     * 设置顶部EmptyView的可见性
     *
     * @param visibility One of {@link View#VISIBLE}, {@link View#INVISIBLE}, or {@link View#GONE}.
     */
    public void setTopEmptyViewVisibility(int visibility) {
        mAlertController.setTopEmptyViewVisibility(visibility);
    }

    /**
     * 设置顶部面板的margin
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void setTopPanelMargin(int left, int top, int right, int bottom) {
        mAlertController.setTopPanelMargin(left, top, right, bottom);
    }

    public void setTitleHeight(int height) {
        mAlertController.setTitleHeight(height);
    }

    public void setTitleColor(@ColorInt int color) {
        mAlertController.setTitleColor(color);
    }

    /**
     * 使title位于顶部的中央<br>
     * 在{@link #show()}之后调用
     *
     * @param height
     */
    public void makeTitleInCenter(int height) {
        setTopEmptyViewVisibility(View.GONE);
        setTopPanelMargin(0, 0, 0, 0);
        setTitleHeight(height);
    }

    public TextView getButton(int whichButton) {
        return mAlertController.getButton(whichButton);
    }

    public ListView getListView() {
        return mAlertController.getListView();
    }

    public View getView() {
        return mAlertController.getView();
    }

    public EditText getInputView() {
        return (EditText) mAlertController.getView();
    }

    public CharSequence[] getItemTexts() {
        return this.mItemTexts;
    }

    private void hideSoftInput() {
        if (mAlertController.getView() != null) {
            final InputMethodManager inputMethodManager = (InputMethodManager) mContext
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(mAlertController.getView().getWindowToken(), 0);
        }
    }

    @Override
    public void dismiss() {
        if (mDismissCallBack != null) {
            mDismissCallBack.beforeDismissCallBack();
        }
        hideSoftInput();
        super.dismiss();
        if (mDismissCallBack != null) {
            mDismissCallBack.afterDismissCallBack();
        }
    }

    /**
     * 对话框点击按钮之后默认会自动消失 如果不希望点击按钮之后自动消失，可以使用此方法
     */
    public void setAutoDismiss(boolean autoDismiss) {
        mAlertController.setAutoDismiss(autoDismiss);
        if (autoDismiss) {
            mAlertController.sendDismissMessage();
        }
    }

    /**
     * 设置dialog dismiss时的回调
     */
    public void setDismissCallBack(DismissCallBack callBack) {
        this.mDismissCallBack = callBack;
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        mAlertController.setTitle(title);
    }

    public void setTitleSize(float titleSize) {
        mAlertController.setTitleSize(titleSize);
    }

    @Override
    public void show() {
        if (!(mContext instanceof Activity) || ((Activity) mContext).isFinishing()) {
            return;
        }

        super.show();
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        this.getWindow().setAttributes(lp);
    }

    public void setCustomTitle(View customTitleView) {
        mAlertController.setCustomTitle(customTitleView);
    }

    public void setMessage(CharSequence message) {
        mAlertController.setMessage(message);
    }

    public void setMessage(CharSequence message, int gravity) {
        mAlertController.setMessage(message, gravity);
    }

    public void setView(View view) {
        mAlertController.setView(view);
    }

    public void setView(View view, int viewSpacingLeft, int viewSpacingTop, int viewSpacingRight, int viewSpacingBottom) {
        mAlertController.setView(view, viewSpacingLeft, viewSpacingTop, viewSpacingRight, viewSpacingBottom);
    }

    public void setButton(int whichButton, CharSequence text, Message msg) {
        mAlertController.setButton(whichButton, text, null, msg);
    }

    public void setButton(int whichButton, CharSequence text, OnClickListener listener) {
        mAlertController.setButton(whichButton, text, listener, null);
    }

    @Deprecated
    public void setButton(CharSequence text, Message msg) {
        setButton(BUTTON_POSITIVE, text, msg);
    }

    @Deprecated
    public void setButton2(CharSequence text, Message msg) {
        setButton(BUTTON_NEGATIVE, text, msg);
    }

    @Deprecated
    public void setButton3(CharSequence text, Message msg) {
        setButton(BUTTON_NEUTRAL, text, msg);
    }

    @Deprecated
    public void setButton(CharSequence text, final OnClickListener listener) {
        setButton(BUTTON_POSITIVE, text, listener);
    }

    @Deprecated
    public void setButton2(CharSequence text, final OnClickListener listener) {
        setButton(BUTTON_NEGATIVE, text, listener);
    }

    @Deprecated
    public void setButton3(CharSequence text, final OnClickListener listener) {
        setButton(BUTTON_NEUTRAL, text, listener);
    }

    public void setIcon(int resId) {
        mAlertController.setIcon(resId);
    }

    public void setIcon(Drawable icon) {
        mAlertController.setIcon(icon);
    }

    public void setInverseBackgroundForced(boolean forceInverseBackground) {
        mAlertController.setInverseBackgroundForced(forceInverseBackground);
    }

    /**
     * 设置positive button的字体颜色
     *
     * @param color
     * @return
     */
    public void setPositiveButtonTextColor(int color) {
        if (mAlertController != null) {
            TextView positiveButton = mAlertController.getButton(DialogInterface.BUTTON_POSITIVE);
            if (positiveButton != null) {
                positiveButton.setTextColor(color);
            }
        }
    }

    /**
     * main entrance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAlertController.installContent();
        mAlertController.setWindowLocation(Gravity.BOTTOM);
        mAlertController.setWindowAnimation(R.style.MyDialogAnimation);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mAlertController.onKeyDown(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mAlertController.onKeyUp(keyCode, event)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    public static class Builder {
        private final MyAlertController.AlertParams Params;

        private Context mContext;

        public Builder(Context context) {
            this.mContext = context;
            Params = new MyAlertController.AlertParams(context);
        }

        // speical code : 返回输入框
        public EditText getInputView() {
            return (EditText) Params.mView;
        }

        public Builder setTitle(int titleId) {
            Params.mTitle = Params.mContext.getText(titleId);
            return this;
        }

        public Builder setTitle(CharSequence title) {
            Params.mTitle = title;
            return this;
        }

        public Builder setCustomTitle(View customTitleView) {
            Params.mCustomTitleView = customTitleView;
            return this;
        }

        public Builder setMessage(@StringRes int messageId) {
            Params.mMessage = Params.mContext.getText(messageId);
            return this;
        }

        public Builder setMessage(CharSequence message) {
            Params.mMessage = message;
            return this;
        }

        public Builder setIcon(int iconId) {
            Params.mIconId = iconId;
            return this;
        }

        public Builder setIcon(Drawable icon) {
            Params.mIcon = icon;
            return this;
        }

        public Builder setPositiveButton(int textId, final OnClickListener listener) {
            Params.mPositiveButtonText = Params.mContext.getText(textId);
            Params.mPositiveButtonListener = listener;
            return this;
        }


        public Builder setPositiveButton(CharSequence text, final OnClickListener listener) {
            Params.mPositiveButtonText = text;
            Params.mPositiveButtonListener = listener;
            return this;
        }

        public Builder setNegativeButton(int textId, final OnClickListener listener) {
            Params.mNegativeButtonText = Params.mContext.getText(textId);
            Params.mNegativeButtonListener = listener;
            return this;
        }

        public Builder setNegativeButton(CharSequence text, final OnClickListener listener) {
            Params.mNegativeButtonText = text;
            Params.mNegativeButtonListener = listener;
            return this;
        }

        public Builder setNeutralButton(int textId, final OnClickListener listener) {
            Params.mNeutralButtonText = Params.mContext.getText(textId);
            Params.mNeutralButtonListener = listener;
            return this;
        }

        public Builder setNeutralButton(CharSequence text, final OnClickListener listener) {
            Params.mNeutralButtonText = text;
            Params.mNeutralButtonListener = listener;
            return this;
        }

        /**
         * 设置positive 字体颜色
         *
         * @param color
         * @return
         */
        public Builder setPositiveButtonTextColor(int color) {
            MyLog.v(TAG + " setPositiveButtonTextColor color == " + color);
            if (color != 0) {
                Params.mPositiveButtonTextColor = color;
            }

            return this;
        }


        public Builder setCancelable(boolean cancelable) {
            Params.mCancelable = cancelable;
            return this;
        }

        //添加这个函数的原因是为了实现点击空白区域dailog不隐藏，但是back键dialog退出。
        public Builder setCancelableOnTouchOutSide(boolean cancelable) {
            Params.mCancelableOnTouchOutSide = cancelable;
            return this;
        }

        public Builder setOnCancelListener(OnCancelListener onCancelListener) {
            Params.mOnCancelListener = onCancelListener;
            return this;
        }

        public Builder setOnKeyListener(OnKeyListener onKeyListener) {
            Params.mOnKeyListener = onKeyListener;
            return this;
        }

        public Builder setItems(int itemsId, final OnClickListener listener) {
            Params.mItems = Params.mContext.getResources().getTextArray(itemsId);
            Params.mOnClickListener = listener;
            Params.mIsListStyle = true;
            return this;
        }

        public Builder setItems(CharSequence[] items, final OnClickListener listener) {
            Params.mItems = items;
            Params.mOnClickListener = listener;
            Params.mIsListStyle = true;
            return this;
        }

        public Builder setAdapter(final ListAdapter adapter, final OnClickListener listener) {
            Params.mAdapter = adapter;
            Params.mOnClickListener = listener;
            return this;
        }

        public Builder setCursor(final Cursor cursor, final OnClickListener listener, String labelColumn) {
            Params.mCursor = cursor;
            Params.mLabelColumn = labelColumn;
            Params.mOnClickListener = listener;
            return this;
        }

        public Builder setMultiChoiceItems(int itemsId, boolean[] checkedItems,
                                           final OnMultiChoiceClickListener listener) {
            Params.mItems = Params.mContext.getResources().getTextArray(itemsId);
            Params.mOnCheckboxClickListener = listener;
            Params.mCheckedItems = checkedItems;
            Params.mIsMultiChoice = true;
            return this;
        }

        public Builder setMultiChoiceItems(CharSequence[] items, boolean[] checkedItems,
                                           final OnMultiChoiceClickListener listener) {
            Params.mItems = items;
            Params.mOnCheckboxClickListener = listener;
            Params.mCheckedItems = checkedItems;
            Params.mIsMultiChoice = true;
            return this;
        }

        public Builder setMultiChoiceItems(Cursor cursor, String isCheckedColumn, String labelColumn,
                                           final OnMultiChoiceClickListener listener) {
            Params.mCursor = cursor;
            Params.mOnCheckboxClickListener = listener;
            Params.mIsCheckedColumn = isCheckedColumn;
            Params.mLabelColumn = labelColumn;
            Params.mIsMultiChoice = true;
            return this;
        }

        public Builder setSingleChoiceItems(int itemsId, int checkedItem,
                                            final OnClickListener listener) {
            Params.mItems = Params.mContext.getResources().getTextArray(itemsId);
            Params.mOnClickListener = listener;
            Params.mCheckedItem = checkedItem;
            Params.mIsSingleChoice = true;
            return this;
        }

        public Builder setSingleChoiceItems(CharSequence[] items, int checkedItem,
                                            final OnClickListener listener) {
            Params.mItems = items;
            Params.mOnClickListener = listener;
            Params.mCheckedItem = checkedItem;
            Params.mIsSingleChoice = true;
            return this;
        }

        public Builder setSingleChoiceItems(Cursor cursor, int checkedItem, String labelColumn,
                                            final OnClickListener listener) {
            Params.mCursor = cursor;
            Params.mOnClickListener = listener;
            Params.mCheckedItem = checkedItem;
            Params.mLabelColumn = labelColumn;
            Params.mIsSingleChoice = true;
            return this;
        }

        public Builder setSingleChoiceItems(ListAdapter adapter, int checkedItem,
                                            final OnClickListener listener) {
            Params.mAdapter = adapter;
            Params.mOnClickListener = listener;
            Params.mCheckedItem = checkedItem;
            Params.mIsSingleChoice = true;
            return this;
        }

        /**
         * 设置list item中textview的字体大小, 单位是dip
         * <p>
         * 长度要与adapter的size一致，如果长度小于adapter的size，将使用sizes最后一个元素作为后续的item的size
         */
        public Builder setItemTextSizesInDip(float[] itemTextSizes) {
            if (itemTextSizes != null) {
                float[] pxSizes = new float[itemTextSizes.length];
                for (int i = 0; i < itemTextSizes.length; i++) {
                    pxSizes[i] = DisplayUtils.dip2px(itemTextSizes[i]);
                }
                Params.mItemTextSizesInPx = pxSizes;
            }
            return this;
        }

        /**
         * 设置list item中textview的字体大小, 单位是px
         * <p>
         * 长度要与adapter的size一直，如果长度小于adapter的size，将使用sizes最后一个元素作为后续的item的size
         */
        public Builder setItemTextSizesInPx(float[] itemTextSizes) {
            Params.mItemTextSizesInPx = itemTextSizes;
            return this;
        }

        public Builder setOnItemSelectedListener(final AdapterView.OnItemSelectedListener listener) {
            Params.mOnItemSelectedListener = listener;
            return this;
        }

        public Builder setView(View view) {
            Params.mView = view;
            Params.mViewSpacingSpecified = false;
            return this;
        }

        /**
         * 该方法不能与setView()方法同时使用
         */
        public Builder setInputView() {
            View view = View.inflate(mContext, R.layout.my_alert_dialog_input_view, null);
            setView(view, DisplayUtils.dip2px(15), 0, DisplayUtils.dip2px(15), 0);
            return this;
        }

        /**
         * 该方法不能与setView()方法同时使用
         */
        public Builder setInputView(int type) {
            View view = View.inflate(mContext, R.layout.my_alert_dialog_input_view, null);
            ((EditText) view).setInputType(type);
            setView(view, DisplayUtils.dip2px(15), 0, DisplayUtils.dip2px(15), 0);
            return this;
        }

        /**
         * 该方法不能与setView()方法同时使用
         */
        public Builder setInputView(CharSequence defaultText) {
            final View view = View.inflate(mContext, R.layout.my_alert_dialog_input_view, null);
            ((EditText) view).setText(defaultText);
            ((EditText) view).setSelection(defaultText.length());
            ((EditText) view).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String text = s.toString();
                    if (text == null || text.length() == 0) {
                        view.setSelected(false);
                    } else {
                        view.setSelected(true);
                    }
                }
            });
            setView(view, DisplayUtils.dip2px(15), 0, DisplayUtils.dip2px(15), 0);
            return this;
        }

        /**
         * 设置dialog dismiss时的回调
         */
        public Builder setDismissCallBack(DismissCallBack callBack) {
            Params.mDismissCallBack = callBack;
            return this;
        }

        public Builder setView(View view, int viewSpacingLeft, int viewSpacingTop,
                               int viewSpacingRight, int viewSpacingBottom) {
            Params.mView = view;
            Params.mViewSpacingSpecified = true;
            Params.mViewSpacingLeft = viewSpacingLeft;
            Params.mViewSpacingTop = viewSpacingTop;
            Params.mViewSpacingRight = viewSpacingRight;
            Params.mViewSpacingBottom = viewSpacingBottom;
            return this;
        }

        public Builder setInverseBackgroundForced(boolean inverseBackground) {
            Params.mForceInverseBackground = inverseBackground;
            return this;
        }

        public Builder setRecycleOnMeasureEnabled(boolean recycleOnMeasure) {
            Params.mRecycleOnMeasure = recycleOnMeasure;
            return this;
        }

        /**
         * 对话框点击按钮之后默认会自动消失 如果不希望点击按钮之后自动消失，可以使用此方法
         */
        public Builder setAutoDismiss(boolean autoDismiss) {
            Params.mAutoDismiss = autoDismiss;
            return this;
        }

        public MyAlertDialog create() {
            final MyAlertDialog dialog = new MyAlertDialog(Params.mContext);
            dialog.mItemTexts = Params.mItems;
            Params.apply(dialog.mAlertController);
            dialog.setCancelable(Params.mCancelable);
            if (!Params.mCancelable) {
                dialog.setCanceledOnTouchOutside(false);
            } else {
                dialog.setCanceledOnTouchOutside(Params.mCancelableOnTouchOutSide);
            }
            dialog.setOnCancelListener(Params.mOnCancelListener);
            if (Params.mOnKeyListener != null) {
                dialog.setOnKeyListener(Params.mOnKeyListener);
            }
            dialog.setDismissCallBack(Params.mDismissCallBack);
            return dialog;
        }

        public MyAlertDialog show() {
            MyAlertDialog dialog = create();
            dialog.show();

            WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
            return dialog;
        }

        public MyAlertDialog showGlobal() {
            MyAlertDialog dialog = create();
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            dialog.show();

            WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
            return dialog;
        }
    }

    public interface DismissCallBack {
        void beforeDismissCallBack();

        void afterDismissCallBack();
    }
}
