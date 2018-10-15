package com.wali.live.watchsdk.component.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.utils.CommonUtils;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.watchsdk.R;

import static com.wali.live.watchsdk.component.viewmodel.BarrageState.BARRAGE_MANAGE;
import static com.wali.live.watchsdk.component.viewmodel.BarrageState.BARRAGE_NORMAL;
import static com.wali.live.watchsdk.component.viewmodel.BarrageState.BARRAGE_NOTIFY;

/**
 * Created by zyh on 2017/12/21.
 */
public class BarrageSelectView extends RelativeLayout implements IComponentView<BarrageSelectView.IPresenter,
        BarrageSelectView.IView>, View.OnClickListener {
    private static final String TAG = "BarrageSelectView";

    private int mState = BARRAGE_NORMAL;

    private TextView mSwitchTv;
    private TextView mBarrageTv;
    private TextView mManageTv;
    private TextView mNotifyTv;
    private PopupWindow mPopupWindow;
    private View mContentView;
    private boolean mIsOpen = false;

    @Nullable
    protected IPresenter mPresenter;
    private IBtnChangeListener mListener;

    public void setListener(IBtnChangeListener listener) {
        mListener = listener;
    }

    protected final <T extends View> T $(@IdRes int resId) {
        return (T) findViewById(resId);
    }

    protected final <T extends View> T $(View parent, @IdRes int resId) {
        if (parent != null) {
            return (T) parent.findViewById(resId);
        }
        return null;
    }

    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    public BarrageSelectView(Context context) {
        this(context, null);
    }

    public BarrageSelectView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BarrageSelectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    public boolean isOpen() {
        return mIsOpen;
    }

    private void init(Context context) {
        inflate(context, R.layout.fly_barrage_select_button, this);
        mSwitchTv = $(R.id.barrage_switch_close_iv);
        if (CommonUtils.isChinese()) {
            mSwitchTv.getPaint().setFakeBoldText(true);
        }
        $click(mSwitchTv, new OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(BarrageSelectView.this);
            }
        });
    }

    public void hidePopWindow() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }

    private void showPopupMenu(View view) {
        if (mPopupWindow == null) {
            mContentView = LayoutInflater.from(getContext()).inflate(R.layout.barrage_select_item, null);
            mBarrageTv = $(mContentView, R.id.barrage_select_item_txtBarrage);
            mManageTv = $(mContentView, R.id.barrage_select_item_txtManage);
            mNotifyTv = $(mContentView, R.id.barrage_select_item_txtNotify);
            $click(mBarrageTv, this);
            $click(mManageTv, this);
            $click(mNotifyTv, this);

            mPopupWindow = new PopupWindow(getContext());
            mPopupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
            mPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            mPopupWindow.setContentView(mContentView);
            mPopupWindow.setOutsideTouchable(true);
            mPopupWindow.setBackgroundDrawable(null);
            mPopupWindow.setFocusable(true);
            mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    mIsOpen = false;
                }
            });
        }
        if (mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        } else {
            int[] location = calculatePopWindowPos(this, mContentView);
            mPopupWindow.showAtLocation(view, Gravity.NO_GRAVITY, location[0], location[1] - 20);
            mIsOpen = true;
        }
    }

    private static int[] calculatePopWindowPos(final View anchorView, final View contentView) {
        final int windowPos[] = new int[2];
        final int anchorLoc[] = new int[2];
        anchorView.getLocationOnScreen(anchorLoc);
        contentView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        final int windowHeight = contentView.getMeasuredHeight();
        windowPos[0] = anchorLoc[0];
        windowPos[1] = anchorLoc[1] - windowHeight;
        return windowPos;
    }

    private void updateSwitchTv(int state, @StringRes int stringId) {
        mState = state;
        mSwitchTv.setText(stringId);
        mBarrageTv.setTextColor(getResources().getColor(R.color.ffd267));
        mNotifyTv.setTextColor(getResources().getColor(R.color.color_b7b7b7));
        mManageTv.setTextColor(getResources().getColor(R.color.color_b7b7b7));
    }

    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.barrage_select_item_txtBarrage) {
            mState = BARRAGE_NORMAL;
            mBarrageTv.setTextColor(getResources().getColor(R.color.ffd267));
            mNotifyTv.setTextColor(getResources().getColor(R.color.color_b7b7b7));
            mManageTv.setTextColor(getResources().getColor(R.color.color_b7b7b7));
        } else if (i == R.id.barrage_select_item_txtManage) {
            mState = BARRAGE_MANAGE;
            mManageTv.setTextColor(getResources().getColor(R.color.ffd267));
            mNotifyTv.setTextColor(getResources().getColor(R.color.color_b7b7b7));
            mBarrageTv.setTextColor(getResources().getColor(R.color.color_b7b7b7));
        } else if (i == R.id.barrage_select_item_txtNotify) {
            mState = BARRAGE_NOTIFY;
            mNotifyTv.setTextColor(getResources().getColor(R.color.ffd267));
            mManageTv.setTextColor(getResources().getColor(R.color.color_b7b7b7));
            mBarrageTv.setTextColor(getResources().getColor(R.color.color_b7b7b7));
        }
        mSwitchTv.setText(((TextView) v).getText());
        mListener.onChange(mState);
        mPopupWindow.dismiss();
    }

    @Override
    public IView getViewProxy() {
        class ComponentView implements IView {
            @Override
            public <T extends View> T getRealView() {
                return (T) BarrageSelectView.this;
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
    }

    public interface IView extends IViewProxy {

    }

    public interface IBtnChangeListener {
        void onChange(int status);
    }
}
