package com.dialog.list;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.common.base.R;
import com.common.utils.U;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ListHolder;

import java.util.ArrayList;
import java.util.List;

public class ListDialog {
    Context mContext;
    DialogPlus mChannelListDialog;
    BaseAdapter mBaseAdapter;
    List<DialogListItem> mDataList = new ArrayList<>();

    public ListDialog(Context context) {
        mContext = context;
        mBaseAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return mDataList.size();
            }

            @Override
            public Object getItem(int position) {
                return mDataList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = null;
                if (convertView == null) {
                    v = LayoutInflater.from(mContext).inflate(
                            R.layout.dialog_list_item, parent, false);
                    DialogListViewHolder vh = new DialogListViewHolder(v);
                    v.setTag(vh);
                } else {
                    v = convertView;
                }
                DialogListViewHolder vh = (DialogListViewHolder) v.getTag();
                DialogListItem debugData = mDataList.get(position);
                if ((mDataList.size() - 1) == position) {
                    if (vh.mDivider != null) {
                        vh.mDivider.setVisibility(View.GONE);
                    }
                } else {
                    if (vh.mDivider != null) {
                        vh.mDivider.setVisibility(View.VISIBLE);
                    }
                }
                vh.bindData(debugData);
                return v;
            }
        };

        mChannelListDialog = DialogPlus.newDialog(context)
                .setContentHolder(new ListHolder())
                .setAdapter(mBaseAdapter)
                .setGravity(Gravity.CENTER)
                .setCancelable(true)
                .setContentBackgroundResource(R.drawable.img_dialog_bg)
                .setMargin(U.getDisplayUtils().dip2px(45), 0, U.getDisplayUtils().dip2px(45), 0)
                .setOverlayBackgroundResource(R.color.black_trans_60)
                .setExpanded(false)
                .create();
    }

    public void showList(final List<DialogListItem> list) {
        mDataList.clear();
        mDataList.addAll(list);
        mBaseAdapter.notifyDataSetChanged();
        mChannelListDialog.show();
    }

    public void dissmiss() {
        if (mChannelListDialog != null) {
            mChannelListDialog.dismiss();
        }
    }

}
