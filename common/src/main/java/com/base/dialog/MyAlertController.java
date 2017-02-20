package com.base.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


import com.base.common.R;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;

import java.lang.ref.WeakReference;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by lan on 15-12-14.
 */
public class MyAlertController {
    private final static String TAG = MyAlertController.class.getSimpleName();
    private static final int BIT_BUTTON_POSITIVE = 1;
    private static final int BIT_BUTTON_NEGATIVE = 2;
    private static final int BIT_BUTTON_NEUTRAL = 4;

    private final Context mContext;

    private final DialogInterface mDialogInterface;

    private final Window mWindow;

    private CharSequence mTitle;

    private boolean isListStyle;

    private CharSequence mMessage;

    private int mMessageGravity = Gravity.CENTER;

    private ListView mListView;

    private View mView;

    private int mViewSpacingLeft;

    private int mViewSpacingTop;

    private int mViewSpacingRight;

    private int mViewSpacingBottom;

    private boolean mViewSpacingSpecified = false;

    private TextView mButtonPositive;

    private CharSequence mButtonPositiveText;
    private int mPositiveButtonTextColor = 0;

    private Message mButtonPositiveMessage;

    private TextView mButtonNegative;

    private CharSequence mButtonNegativeText;

    private Message mButtonNegativeMessage;

    private TextView mButtonNeutral;

    private CharSequence mButtonNeutralText;

    private Message mButtonNeutralMessage;

    private ViewGroup mParentContainer;

    private ScrollView mScrollView;

    private int mIconId = -1;

    private Drawable mIcon;

    private ImageView mIconView;

    private TextView mTitleView;

    private TextView mMessageView;

    private View mCustomTitleView;

    private boolean mForceInverseBackground;

    private ListAdapter mAdapter;

    private int mCheckedItem = -1;

    private int mAlertDialogLayout;

    private int mListLayout;

    private int mMultiChoiceItemLayout;

    private int mSingleChoiceItemLayout;

    private int mListItemLayout;

    private Handler mHandler;

    private boolean mAutoDismiss = true; // 对话框在点击按钮之后是否自动消失

    View.OnClickListener mButtonHandler = new View.OnClickListener() {
        public void onClick(View v) {
            Message m = null;
            if (v == mButtonPositive && mButtonPositiveMessage != null) {
                m = Message.obtain(mButtonPositiveMessage);
            } else if (v == mButtonNegative && mButtonNegativeMessage != null) {
                m = Message.obtain(mButtonNegativeMessage);
            } else if (v == mButtonNeutral && mButtonNeutralMessage != null) {
                m = Message.obtain(mButtonNeutralMessage);
            }
            if (m != null) {
                m.sendToTarget();
            }

            if (mAutoDismiss) {
                mHandler.obtainMessage(ButtonHandler.MSG_DISMISS_DIALOG, mDialogInterface)
                        .sendToTarget();
            }
        }
    };


    private static final class ButtonHandler extends Handler {
        private static final int MSG_DISMISS_DIALOG = 1;

        private WeakReference<DialogInterface> mDialog;

        public ButtonHandler(DialogInterface dialog) {
            mDialog = new WeakReference<>(dialog);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DialogInterface.BUTTON_POSITIVE:
                case DialogInterface.BUTTON_NEGATIVE:
                case DialogInterface.BUTTON_NEUTRAL:
                    ((DialogInterface.OnClickListener) msg.obj).onClick(mDialog.get(), msg.what);
                    break;
                case MSG_DISMISS_DIALOG:
                    ((DialogInterface) msg.obj).dismiss();
            }
        }
    }

    public void sendDismissMessage() {
        mHandler.obtainMessage(ButtonHandler.MSG_DISMISS_DIALOG, mDialogInterface).sendToTarget();
    }

    public MyAlertController(Context context, DialogInterface dialogInterface, Window window) {
        mContext = context;
        mDialogInterface = dialogInterface;
        mWindow = window;
        mHandler = new ButtonHandler(dialogInterface);

        mAlertDialogLayout = R.layout.my_alert_dialog;
        mListLayout = R.layout.my_select_dialog;
        mMultiChoiceItemLayout = R.layout.my_select_dialog_multichoice;
        mSingleChoiceItemLayout = R.layout.my_select_dialog_singlechoice;
        mListItemLayout = R.layout.my_select_dialog_item;
    }

    protected static boolean canTextInput(View v) {
        if (v.onCheckIsTextEditor()) {
            return true;
        }
        if (!(v instanceof ViewGroup)) {
            return false;
        }
        ViewGroup vg = (ViewGroup) v;
        int i = vg.getChildCount();
        while (i > 0) {
            i--;
            v = vg.getChildAt(i);
            if (canTextInput(v)) {
                return true;
            }
        }
        return false;
    }

    public void setParentMargin(int left, int top, int right, int bottom) {
        if (mParentContainer != null){
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mParentContainer.getLayoutParams();
            params.leftMargin = left;
            params.rightMargin = right;
            params.topMargin = top;
            params.bottomMargin = bottom;
            mParentContainer.setLayoutParams(params);
            mParentContainer.requestLayout();
        }
    }

    public void installContent() {
        mWindow.requestFeature(Window.FEATURE_NO_TITLE);

//        mWindow.setGravity(Gravity.CENTER);
        if (mView == null || !canTextInput(mView)) {
            mWindow.setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        }
        mWindow.setContentView(mAlertDialogLayout);
        mParentContainer = (ViewGroup) mWindow.findViewById(R.id.parentPanel);
        setupView();
    }

    public void setWindowLocation(int gravity) {
        WindowManager.LayoutParams wlp = mWindow.getAttributes();
        wlp.gravity = gravity;
        // 设置背景是否变暗
//        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        mWindow.setAttributes(wlp);
    }

    public void setWindowAnimation(int animStyle) {
        mWindow.getAttributes().windowAnimations = animStyle;
    }


    public void setTitle(CharSequence title) {
        mTitle = title;
        if (mTitleView != null) {
            mTitleView.setText(title);
        }
    }
    public void setTitleSize(float titleSize) {
        if (mTitleView != null) {
            mTitleView.setTextSize(titleSize);
        }
    }
    public void setCustomTitle(View customTitleView) {
        mCustomTitleView = customTitleView;
    }

    public void setAutoDismiss(boolean autoDismiss) {
        mAutoDismiss = autoDismiss;
    }

    public void setMessage(CharSequence message) {
        setMessage(message, Gravity.CENTER);
    }

    public void setMessage(CharSequence message, int gravity) {
        mMessage = message;
        mMessageGravity = gravity;
        if (mMessageView != null) {
            mMessageView.setText(message);
            mMessageView.setGravity(gravity);
        }
    }

    public void setView(View view) {
        mView = view;
        mViewSpacingSpecified = false;
    }

    public void setView(View view, int viewSpacingLeft, int viewSpacingTop,
                        int viewSpacingRight, int viewSpacingBottom) {
        mView = view;
        mViewSpacingSpecified = true;
        mViewSpacingLeft = viewSpacingLeft;
        mViewSpacingTop = viewSpacingTop;
        mViewSpacingRight = viewSpacingRight;
        mViewSpacingBottom = viewSpacingBottom;
    }

    public void setButton(int whichButton, CharSequence text,
                          DialogInterface.OnClickListener listener, Message msg) {
        if (msg == null && listener != null) {
            msg = mHandler.obtainMessage(whichButton, listener);
        }
        switch (whichButton) {
            case DialogInterface.BUTTON_POSITIVE:
                mButtonPositiveText = text;
                mButtonPositiveMessage = msg;
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                mButtonNegativeText = text;
                mButtonNegativeMessage = msg;
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                mButtonNeutralText = text;
                mButtonNeutralMessage = msg;
                break;
            default:
                throw new IllegalArgumentException("Button does not exist");
        }
    }

    public void setPositiveButtonTextColor(int color) {
        if (color != 0) {
            mPositiveButtonTextColor = color;
        }
    }

    public void setIcon(int resId) {
        mIconId = resId;
        if (mIconView != null) {
            if (resId > 0) {
                mIconView.setImageResource(mIconId);
            } else if (resId == 0) {
                mIconView.setVisibility(View.GONE);
            }
        }
    }

    public void setIcon(Drawable icon) {
        mIcon = icon;
        if ((mIconView != null) && (mIcon != null)) {
            mIconView.setImageDrawable(icon);
        }
    }

    public void setInverseBackgroundForced(boolean forceInverseBackground) {
        mForceInverseBackground = forceInverseBackground;
    }

    public ListView getListView() {
        return mListView;
    }

    public View getView() {
        return mView;
    }

    public TextView getButton(int whichButton) {
        switch (whichButton) {
            case DialogInterface.BUTTON_POSITIVE:
                return mButtonPositive;
            case DialogInterface.BUTTON_NEGATIVE:
                return mButtonNegative;
            case DialogInterface.BUTTON_NEUTRAL:
                return mButtonNeutral;
            default:
                return null;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            this.mDialogInterface.dismiss();
        }
        return mScrollView != null && mScrollView.executeKeyEvent(event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return mScrollView != null && mScrollView.executeKeyEvent(event);
    }

    public void setupView() {
        LinearLayout contentPanel = (LinearLayout) mWindow.findViewById(R.id.contentPanel);
        setupContent(contentPanel);
        boolean hasButtons = setupButtons();

        LinearLayout topPanel = (LinearLayout) mWindow.findViewById(R.id.topPanel);
        boolean hasTitle = setupTitle(topPanel);

        View buttonPanel = mWindow.findViewById(R.id.buttonPanel);
        if (!hasButtons) {
            buttonPanel.setVisibility(View.GONE);
        }

        FrameLayout customPanel = null;
        if (mView != null) {
            customPanel = (FrameLayout) mWindow.findViewById(R.id.customPanel);
            FrameLayout custom = (FrameLayout) mWindow.findViewById(R.id.custom);
            custom.addView(mView, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
            if (mViewSpacingSpecified) {
                custom.setPadding(mViewSpacingLeft, mViewSpacingTop, mViewSpacingRight,
                        mViewSpacingBottom);
            }
            if (mListView != null) {
                ((LinearLayout.LayoutParams) customPanel.getLayoutParams()).weight = 0;
            }
        } else {
            mWindow.findViewById(R.id.customPanel).setVisibility(View.GONE);
        }

        /*
         * Only display the divider if we have a title and a custom view or a
         * message.
         */
        if (hasTitle || mMessage != null) {
            if (mListView != null) {
                mWindow.findViewById(R.id.separate_line).setVisibility(View.VISIBLE);
            }
        }
        setBackground(topPanel, contentPanel, customPanel, hasButtons, hasTitle, buttonPanel);
        if (TextUtils.isEmpty(mTitle) && TextUtils.isEmpty(mMessage)) {
            mWindow.findViewById(R.id.empty_view).setVisibility(View.GONE);
        }
    }

    private boolean setupTitle(LinearLayout topPanel) {
        boolean hasTitle = true;

        if (mCustomTitleView != null) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            topPanel.addView(mCustomTitleView, 0, lp);

            View titleTemplate = mWindow.findViewById(R.id.title_template);
            titleTemplate.setVisibility(View.GONE);
        } else {
            final boolean hasTextTitle = !TextUtils.isEmpty(mTitle);

            mIconView = (ImageView) mWindow.findViewById(R.id.icon);
            if (hasTextTitle) {
                mTitleView = (TextView) mWindow.findViewById(R.id.alertTitle);
                mTitleView.setText(mTitle);
                if (isListStyle) {
                    // 有列表时的title样式
//                    mTitleView.setTextColor(GlobalData.app().getResources().getColor(R.color.colorPrimary));
//                    mTitleView.setTextSize(GlobalData.app().getResources().getDimensionPixelSize(R.dimen.text_size_55) / 3);
//                    mTitleView.setGravity(Gravity.CENTER_VERTICAL);
                }
                //登录设置用户信息（ProfileSettingActivity）页面选择设置头像的dialog需要标题居中
                mTitleView.setGravity(Gravity.CENTER);

                if (mIconId > 0) {
                    mIconView.setImageResource(mIconId);
                } else if (mIcon != null) {
                    mIconView.setImageDrawable(mIcon);
                } else if (mIconId == 0) {
                    mIconView.setVisibility(View.GONE);
                }
            } else {
                View titleTemplate = mWindow.findViewById(R.id.title_template);
                titleTemplate.setVisibility(View.GONE);
                mIconView.setVisibility(View.GONE);
                topPanel.setVisibility(View.GONE);
                hasTitle = false;
            }
        }
        return hasTitle;
    }

    private void setupContent(LinearLayout contentPanel) {
        mScrollView = (ScrollView) mWindow.findViewById(R.id.scrollView);
        mScrollView.setFocusable(false);

        mMessageView = (TextView) mWindow.findViewById(R.id.message);
        if (mMessageView == null) {
            return;
        }
        if (mMessage != null) {
            mMessageView.setText(mMessage);
            mMessageView.setGravity(mMessageGravity);
        } else {
            mMessageView.setVisibility(View.GONE);
            mScrollView.removeView(mMessageView);
            if (mListView != null) {
//                mListView.setBackgroundColor(0xffff0000);
                contentPanel.removeView(mWindow.findViewById(R.id.scrollView));
                contentPanel.addView(mListView, new LinearLayout.LayoutParams(MATCH_PARENT,
                        WRAP_CONTENT));
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) contentPanel.getLayoutParams();
                lp.bottomMargin = 0;
                if (!TextUtils.isEmpty(mTitle)) {
                    View emptyView = mWindow.findViewById(R.id.empty_view);
                    if (emptyView != null) {
                        emptyView.getLayoutParams().height = DisplayUtils.dip2px(22);
                    }
                    View titleView = mWindow.findViewById(R.id.topPanel);
                    if (titleView != null) {
                        LinearLayout.LayoutParams lpTitle = (LinearLayout.LayoutParams) titleView.getLayoutParams();
                        lpTitle.bottomMargin = DisplayUtils.dip2px(18);
                    }
                }
//                contentPanel.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, 0, 1.0f));
            } else {
                contentPanel.setVisibility(View.GONE);
            }
        }
    }

    private boolean setupButtons() {
        int whichButtons = 0;
        mButtonPositive = (TextView) mWindow.findViewById(R.id.button1);
        mButtonPositive.setOnClickListener(mButtonHandler);
        if (TextUtils.isEmpty(mButtonPositiveText)) {
            mButtonPositive.setVisibility(View.GONE);
        } else {
            mButtonPositive.setText(mButtonPositiveText);
            mButtonPositive.setVisibility(View.VISIBLE);
            whichButtons = whichButtons | BIT_BUTTON_POSITIVE;
        }

        if (mPositiveButtonTextColor != 0) {
            mButtonPositive.setTextColor(mPositiveButtonTextColor);
        }


        mButtonNegative = (TextView) mWindow.findViewById(R.id.button2);
        mButtonNegative.setOnClickListener(mButtonHandler);
        if (TextUtils.isEmpty(mButtonNegativeText)) {
            mButtonNegative.setVisibility(View.GONE);
        } else {
            mButtonNegative.setText(mButtonNegativeText);
            mButtonNegative.setVisibility(View.VISIBLE);
            whichButtons = whichButtons | BIT_BUTTON_NEGATIVE;
        }

        mButtonNeutral = (TextView) mWindow.findViewById(R.id.button3);
        mButtonNeutral.setOnClickListener(mButtonHandler);
        if (TextUtils.isEmpty(mButtonNeutralText)) {
            mButtonNeutral.setVisibility(View.GONE);
        } else {
            mButtonNeutral.setText(mButtonNeutralText);
            mButtonNeutral.setVisibility(View.VISIBLE);
            whichButtons = whichButtons | BIT_BUTTON_NEUTRAL;
        }

        if (shouldCenterSingleButton(whichButtons)) {
            if (whichButtons == BIT_BUTTON_POSITIVE) {
                centerButton(mButtonPositive);
            } else if (whichButtons == BIT_BUTTON_NEGATIVE) {
                centerButton(mButtonNegative);
            } else if (whichButtons == BIT_BUTTON_NEUTRAL) {
                centerButton(mButtonNeutral);
            }
        }
        return whichButtons != 0;
    }

    private static boolean shouldCenterSingleButton(int whichButton) {
        return whichButton == BIT_BUTTON_POSITIVE || whichButton == BIT_BUTTON_NEGATIVE
                || whichButton == BIT_BUTTON_NEUTRAL;
    }

    private void centerButton(TextView button) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) button.getLayoutParams();
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.weight = 0.5f;
        button.setLayoutParams(params);
        button.setBackgroundResource(R.drawable.bottom_button_single);
    }

    private void setBackground(LinearLayout topPanel, LinearLayout contentPanel, View customPanel,
                               boolean hasButtons, boolean hasTitle, View buttonPanel) {
        int fullDark = mContext.getResources().getColor(android.R.color.transparent);
        int topDark = mContext.getResources().getColor(android.R.color.transparent);
        int centerDark = mContext.getResources().getColor(android.R.color.transparent);
        int bottomDark = mContext.getResources().getColor(android.R.color.transparent);
        int fullBright = mContext.getResources().getColor(android.R.color.transparent);
        int topBright = mContext.getResources().getColor(android.R.color.transparent);
        int centerBright = mContext.getResources().getColor(android.R.color.transparent);
        int bottomBright = mContext.getResources().getColor(android.R.color.transparent);
        int bottomMedium = mContext.getResources().getColor(android.R.color.transparent);

        /*
         * We now set the background of all of the sections of the alert. First
         * collect together each section that is being displayed along with
         * whether it is on a light or dark background, then run through them
         * setting their backgrounds. This is complicated because we need to
         * correctly use the full, top, middle, and bottom graphics depending on
         * how many views they are and where they appear.
         */

        View[] views = new View[4];
        boolean[] light = new boolean[4];
        View lastView = null;
        boolean lastLight = false;
        int pos = 0;
        if (hasTitle) {
            views[pos] = topPanel;
            light[pos] = false;
            pos++;
        }
        /*
         * The contentPanel displays either a custom text message or a ListView.
         * If it's text we should use the dark background for ListView we should
         * use the light background. If neither are there the contentPanel will
         * be hidden so set it as null.
         */
        views[pos] = (contentPanel.getVisibility() == View.GONE) ? null : contentPanel;
        light[pos] = mListView != null;
        pos++;
        if (customPanel != null) {
            views[pos] = customPanel;
            light[pos] = mForceInverseBackground;
            pos++;
        }
        if (hasButtons) {
            views[pos] = buttonPanel;
            light[pos] = true;
        }
        boolean setView = false;
        for (pos = 0; pos < views.length; pos++) {
            View v = views[pos];
            if (v == null) {
                continue;
            }
            if (lastView != null) {
                if (!setView) {
                    lastView.setBackgroundResource(lastLight ? topBright : topDark);
                } else {
                    lastView.setBackgroundResource(lastLight ? centerBright : centerDark);
                }
                setView = true;
            }
            lastView = v;
            lastLight = light[pos];
        }
        if (lastView != null) {
            if (setView) {
                lastView.setBackgroundResource(lastLight ? (hasButtons ? bottomMedium
                        : bottomBright) : bottomDark);
            } else {
                lastView.setBackgroundResource(lastLight ? fullBright : fullDark);
            }
        }
        if ((mListView != null) && (mAdapter != null)) {
            mListView.setAdapter(mAdapter);
            if (mCheckedItem > -1) {
                mListView.setItemChecked(mCheckedItem, true);
                mListView.setSelection(mCheckedItem);
            }
        }
    }

    public static class RecycleListView extends ListView {
        boolean mRecycleOnMeasure = true;

        public RecycleListView(Context context) {
            super(context);
        }

        public RecycleListView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public RecycleListView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        protected boolean recycleOnMeasure() {
            return mRecycleOnMeasure;
        }
    }

    public static class AlertParams {
        public final Context mContext;

        public final LayoutInflater mInflater;

        public int mIconId = 0;

        public Drawable mIcon;

        public CharSequence mTitle;

        public View mCustomTitleView;

        public CharSequence mMessage;

        public CharSequence mPositiveButtonText;
        public int mPositiveButtonTextColor;

        public DialogInterface.OnClickListener mPositiveButtonListener;

        public CharSequence mNegativeButtonText;

        public DialogInterface.OnClickListener mNegativeButtonListener;

        public CharSequence mNeutralButtonText;

        public DialogInterface.OnClickListener mNeutralButtonListener;

        public boolean mCancelable;

        public boolean mCancelableOnTouchOutSide;

        public DialogInterface.OnCancelListener mOnCancelListener;

        public DialogInterface.OnKeyListener mOnKeyListener;

        public CharSequence[] mItems;

        public float[] mItemTextSizesInPx;

        public ListAdapter mAdapter;

        public DialogInterface.OnClickListener mOnClickListener;

        public View mView;

        public int mViewSpacingLeft;

        public int mViewSpacingTop;

        public int mViewSpacingRight;

        public int mViewSpacingBottom;

        public boolean mViewSpacingSpecified = false;

        public boolean[] mCheckedItems;

        public boolean mIsMultiChoice;

        public boolean mIsSingleChoice;

        public int mCheckedItem = -1;

        public DialogInterface.OnMultiChoiceClickListener mOnCheckboxClickListener;

        public Cursor mCursor;

        public String mLabelColumn;

        public String mIsCheckedColumn;

        public boolean mForceInverseBackground;

        public AdapterView.OnItemSelectedListener mOnItemSelectedListener;

        public OnPrepareListViewListener mOnPrepareListViewListener;

        public boolean mRecycleOnMeasure = true;

        public boolean mAutoDismiss = true;

        public MyAlertDialog.DismissCallBack mDismissCallBack;

        public boolean mIsListStyle = false;

        public interface OnPrepareListViewListener {
            void onPrepareListView(ListView listView);
        }

        public AlertParams(Context context) {
            mContext = context;
            mCancelable = true;
            mCancelableOnTouchOutSide = true;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void apply(MyAlertController dialog) {
            if (mCustomTitleView != null) {
                dialog.setCustomTitle(mCustomTitleView);
            } else {
                if (mTitle != null) {
                    dialog.setTitle(mTitle);
                }
                if (mIcon != null) {
                    dialog.setIcon(mIcon);
                }
                if (mIconId >= 0) {
                    dialog.setIcon(mIconId);
                }
            }
            if (mMessage != null) {
                dialog.setMessage(mMessage);
            }
            if (mPositiveButtonText != null) {
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, mPositiveButtonText,
                        mPositiveButtonListener, null);
                if (mPositiveButtonTextColor != 0) {
                    dialog.setPositiveButtonTextColor(mPositiveButtonTextColor);
                }
            }
            if (mNegativeButtonText != null) {
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, mNegativeButtonText,
                        mNegativeButtonListener, null);
            }
            if (mNeutralButtonText != null) {
                dialog.setButton(DialogInterface.BUTTON_NEUTRAL, mNeutralButtonText,
                        mNeutralButtonListener, null);
            }
            if (mForceInverseBackground) {
                dialog.setInverseBackgroundForced(true);
            }

            // 设置list
            if ((mItems != null) || (mCursor != null) || (mAdapter != null)) {
                createListView(dialog);
            }
            // 自定义的view
            if (mView != null) {
                if (mViewSpacingSpecified) {
                    dialog.setView(mView, mViewSpacingLeft, mViewSpacingTop, mViewSpacingRight,
                            mViewSpacingBottom);
                } else {
                    dialog.setView(mView);
                }
            }
            dialog.setAutoDismiss(mAutoDismiss);
            dialog.isListStyle = mIsListStyle;
        }

        private void createListView(final MyAlertController dialog) {
            final RecycleListView listView = (RecycleListView) mInflater.inflate(dialog.mListLayout, null);
            ListAdapter adapter;

            if (mIsMultiChoice) {
                if (mCursor == null) {
                    adapter = new ArrayAdapter<CharSequence>(mContext,
                            dialog.mMultiChoiceItemLayout, R.id.text1, mItems) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            if (mItemTextSizesInPx != null) {
                                TextView tv = (TextView) view.findViewById(R.id.text1);
                                if (position < mItemTextSizesInPx.length) {
                                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                                            mItemTextSizesInPx[position]);
                                } else {
                                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                                            mItemTextSizesInPx[mItemTextSizesInPx.length - 1]);
                                }
                            }
                            if (mCheckedItems != null) {
                                boolean isItemChecked = mCheckedItems[position];
                                if (isItemChecked) {
                                    listView.setItemChecked(position, true);
                                }
                            }
                            return view;
                        }
                    };
                } else {
                    adapter = new CursorAdapter(mContext, mCursor, false) {
                        private final int mLabelIndex;

                        private final int mIsCheckedIndex;

                        {
                            final Cursor cursor = getCursor();
                            mLabelIndex = cursor.getColumnIndexOrThrow(mLabelColumn);
                            mIsCheckedIndex = cursor.getColumnIndexOrThrow(mIsCheckedColumn);
                        }

                        @Override
                        public void bindView(View view, Context context, Cursor cursor) {
                            CheckedTextView text = (CheckedTextView) view.findViewById(R.id.text1);
                            text.setText(cursor.getString(mLabelIndex));
                            if (mItemTextSizesInPx != null) {
                                int position = cursor.getPosition();
                                if (position < mItemTextSizesInPx.length) {
                                    text.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                                            mItemTextSizesInPx[position]);
                                } else {
                                    text.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                                            mItemTextSizesInPx[mItemTextSizesInPx.length - 1]);
                                }
                            }
                            listView.setItemChecked(cursor.getPosition(),
                                    cursor.getInt(mIsCheckedIndex) == 1);
                        }

                        @Override
                        public View newView(Context context, Cursor cursor, ViewGroup parent) {
                            return mInflater.inflate(dialog.mMultiChoiceItemLayout, parent, false);
                        }

                    };
                }
            } else {
                int layout = mIsSingleChoice ? dialog.mSingleChoiceItemLayout
                        : dialog.mListItemLayout;
                if (mCursor == null) {
                    adapter = (mAdapter != null) ? mAdapter : new ArrayAdapter<CharSequence>(
                            mContext, layout, R.id.text1, mItems) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            // 设置不同position，不同的背景
                            if (getCount() == 1) {
                                view.setBackgroundResource(R.drawable.dialog_list_item_bg);
                            } else {
                                if (position == 0 && TextUtils.isEmpty(mTitle)) {
                                    view.setBackgroundResource(R.drawable.dialog_list_item_first_bg);
                                } else if (position < getCount() - 1) {
                                    view.setBackgroundResource(R.drawable.dialog_list_item_bg);
                                } else {
                                    view.setBackgroundResource(R.drawable.dialog_list_item_last_bg);
                                }
                            }

                            if (mItemTextSizesInPx != null) {
                                TextView tv = (TextView) view.findViewById(R.id.text1);

                                if (position < mItemTextSizesInPx.length) {
                                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                                            mItemTextSizesInPx[position]);
                                } else {
                                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                                            mItemTextSizesInPx[mItemTextSizesInPx.length - 1]);
                                }
                            }
//                            view.setBackgroundColor(0xff0000ff);
                            return view;
                        }
                    };
                } else {
                    adapter = new SimpleCursorAdapter(mContext, layout, mCursor, new String[]{
                            mLabelColumn
                    }, new int[]{
                            R.id.text1
                    }) {

                        @Override
                        public void bindView(View view, Context context, Cursor cursor) {
                            super.bindView(view, context, cursor);
                            CheckedTextView text = (CheckedTextView) view.findViewById(R.id.text1);
                            if (mItemTextSizesInPx != null) {
                                int position = cursor.getPosition();
                                if (position < mItemTextSizesInPx.length) {
                                    text.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                                            mItemTextSizesInPx[position]);
                                    view.setBackgroundResource(R.drawable.bottom_button_single);
                                } else {
                                    text.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                                            mItemTextSizesInPx[mItemTextSizesInPx.length - 1]);
                                }
                            }
                        }

                    };
                }
            }

            if (mOnPrepareListViewListener != null) {
                mOnPrepareListViewListener.onPrepareListView(listView);
            }

            dialog.mAdapter = adapter;
            dialog.mCheckedItem = mCheckedItem;

            if (mOnClickListener != null) {
                listView.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView parent, View v, int position, long id) {
                        mOnClickListener.onClick(dialog.mDialogInterface, position);
                        if (!mIsSingleChoice) {
                            dialog.mDialogInterface.dismiss();
                        }
                    }
                });
            } else if (mOnCheckboxClickListener != null) {
                listView.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView parent, View v, int position, long id) {
                        if (mCheckedItems != null) {
                            mCheckedItems[position] = listView.isItemChecked(position);
                        }
                        mOnCheckboxClickListener.onClick(dialog.mDialogInterface, position,
                                listView.isItemChecked(position));
                    }
                });
            }

            if (mOnItemSelectedListener != null) {
                listView.setOnItemSelectedListener(mOnItemSelectedListener);
            }

            if (mIsSingleChoice) {
                listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            } else if (mIsMultiChoice) {
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            }
            listView.mRecycleOnMeasure = mRecycleOnMeasure;
            dialog.mListView = listView;
        }
    }
}
