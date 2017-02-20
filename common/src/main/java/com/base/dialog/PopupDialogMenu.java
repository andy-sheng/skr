package com.base.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.base.common.R;
import com.base.global.GlobalData;


/**
 * Created by yurui on 3/2/16.
 */
public class PopupDialogMenu extends Dialog implements DialogInterface {

    private Window mWindow;
    private Context mContext;
    private OnItemClickListener mListener;
    private LinearLayout contentView;

    public PopupDialogMenu(Context context) {
        super(context, R.style.MyAlertDialog);
        setOwnerActivity((Activity) context);
        mContext = context;
        contentView = new LinearLayout(mContext);
        contentView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        contentView.setOrientation(LinearLayout.VERTICAL);
        contentView.setGravity(Gravity.BOTTOM);
        mWindow = getWindow();
        mWindow.requestFeature(Window.FEATURE_NO_TITLE);
        mWindow.setGravity(Gravity.BOTTOM);
        mWindow.setContentView(contentView);
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindow.setAttributes(lp);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
    }

    @Override
    public void show() {
        if (mContext instanceof Activity && ((Activity) mContext).isFinishing()) {
            return;
        }
        if (contentView.getChildCount() > 0) {
            super.show();
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int itemId, Object obj);
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        mListener = l;
    }

    public void addItem(final int itemId, int resId, final Object data) {
        View item = LayoutInflater.from(mContext).inflate(R.layout.select_dialog_item, null);
        TextView view = (TextView) item.findViewById(R.id.text1);
        view.setText(resId);
        view.setGravity(Gravity.CENTER);
        item.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (mListener != null) {
                    mListener.onItemClick(itemId, data);
                }
                dismiss();
            }
        });
        contentView.addView(view);
    }

    public void addDisableItem(final int itemId, int resId, final Object data) {
        View item = LayoutInflater.from(mContext).inflate(R.layout.select_dialog_item, null);
        TextView view = (TextView) item.findViewById(R.id.text1);
        view.setText(resId);
        view.setTextColor(GlobalData.app().getResources().getColor(R.color.color_black_trans_30));
        view.setEnabled(false);
        contentView.addView(view);
    }


    public int getMenuCount() {
        if (contentView != null) {
            return contentView.getChildCount();
        }
        return 0;
    }

    public void addItem(final int itemId, final String txt, final Object data) {
        View item = LayoutInflater.from(mContext).inflate(R.layout.select_dialog_item, null);
        TextView view = (TextView) item.findViewById(R.id.text1);
        view.setText(txt);
        item.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                dismiss();
                if (mListener != null) {
                    mListener.onItemClick(itemId, data);
                }
            }
        });
        contentView.addView(view);
    }
}