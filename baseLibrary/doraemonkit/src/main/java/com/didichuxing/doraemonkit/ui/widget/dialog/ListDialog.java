package com.didichuxing.doraemonkit.ui.widget.dialog;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.common.utils.U;
import com.didichuxing.doraemonkit.R;
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
                            R.layout.dk_dialog_list_item, parent, false);
                    DialogListViewHolder vh = new DialogListViewHolder(v);
                    vh.titleTv.setTextColor(U.getColor(R.color.dk_color_48BB31));
                    v.setTag(vh);
                } else {
                    v = convertView;
                }
                DialogListViewHolder vh = (DialogListViewHolder) v.getTag();
                DialogListItem debugData = mDataList.get(position);
                vh.bindData(debugData);
                return v;
            }
        };

        mChannelListDialog = DialogPlus.newDialog(context)
                .setContentHolder(new ListHolder())
                .setAdapter(mBaseAdapter)
                .setGravity(Gravity.CENTER)
                .setCancelable(true)
                .setContentBackgroundResource(R.color.dk_list_dialog_content_bg)
                .setOverlayBackgroundResource(R.color.dk_list_dialog_overlay_bg)
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
