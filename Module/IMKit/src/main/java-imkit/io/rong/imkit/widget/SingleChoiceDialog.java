//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import io.rong.imkit.R;

public class SingleChoiceDialog extends Dialog {
    protected Context mContext;
    protected View mRootView;
    protected TextView mTVTitle;
    protected TextView mButtonOK;
    protected TextView mButtonCancel;
    protected ListView mListView;
    protected List<String> mList;
    protected OnClickListener mOkClickListener;
    protected OnClickListener mCancelClickListener;
    private SingleChoiceAdapter<String> mSingleChoiceAdapter;

    public SingleChoiceDialog(Context context, List<String> list) {
        super(context);
        this.mContext = context;
        this.mList = list;
        this.initView(this.mContext);
        this.initData();
    }

    protected void initView(Context context) {
        this.requestWindowFeature(1);
        this.setContentView(R.layout.rc_cs_single_choice_layout);
        this.mRootView = this.findViewById(R.id.rc_cs_rootView);
        this.mRootView.setBackgroundDrawable(new ColorDrawable(0));
        this.mTVTitle = (TextView) this.findViewById(R.id.rc_cs_tv_title);
        this.mButtonOK = (Button) this.findViewById(R.id.rc_cs_btn_ok);
        this.mButtonOK.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                io.rong.imkit.widget.SingleChoiceDialog.this.onButtonOK();
            }
        });
        this.mButtonCancel = (Button) this.findViewById(R.id.rc_cs_btn_cancel);
        this.mButtonCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                io.rong.imkit.widget.SingleChoiceDialog.this.onButtonCancel();
            }
        });
        this.mListView = (ListView) this.findViewById(R.id.rc_cs_group_dialog_listView);
        Window dialogWindow = this.getWindow();
        LayoutParams lp = dialogWindow.getAttributes();
        ColorDrawable dw = new ColorDrawable(0);
        dialogWindow.setBackgroundDrawable(dw);
    }

    public void setTitle(String title) {
        this.mTVTitle.setText(title);
    }

    public void setOnOKButtonListener(OnClickListener onClickListener) {
        this.mOkClickListener = onClickListener;
    }

    public void setOnCancelButtonListener(OnClickListener onClickListener) {
        this.mCancelClickListener = onClickListener;
    }

    protected void onButtonOK() {
        this.dismiss();
        if (this.mOkClickListener != null) {
            this.mOkClickListener.onClick(this, 0);
        }

    }

    protected void onButtonCancel() {
        this.dismiss();
        if (this.mCancelClickListener != null) {
            this.mCancelClickListener.onClick(this, 0);
        }

    }

    protected void initData() {
        this.mSingleChoiceAdapter = new SingleChoiceAdapter(this.mContext, this.mList, R.drawable.rc_cs_group_checkbox_selector);
        this.mListView.setAdapter(this.mSingleChoiceAdapter);
        this.mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position != io.rong.imkit.widget.SingleChoiceDialog.this.mSingleChoiceAdapter.getSelectItem()) {
                    if (!io.rong.imkit.widget.SingleChoiceDialog.this.mButtonOK.isEnabled()) {
                        io.rong.imkit.widget.SingleChoiceDialog.this.mButtonOK.setEnabled(true);
                    }

                    io.rong.imkit.widget.SingleChoiceDialog.this.mSingleChoiceAdapter.setSelectItem(position);
                    io.rong.imkit.widget.SingleChoiceDialog.this.mSingleChoiceAdapter.notifyDataSetChanged();
                }

            }
        });
        this.setListViewHeightBasedOnChildren(this.mListView);
    }

    public int getSelectItem() {
        return this.mSingleChoiceAdapter.getSelectItem();
    }

    private void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {
            int totalHeight = 0;

            for (int i = 0; i < listAdapter.getCount(); ++i) {
                View listItem = listAdapter.getView(i, (View) null, listView);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }

            totalHeight += 10;
            android.view.ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalHeight + listView.getDividerHeight() * (listAdapter.getCount() - 1);
            listView.setLayoutParams(params);
        }
    }
}
