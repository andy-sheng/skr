package com.example.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wali.live.moduletest.R;

public class SimpleAdapter extends BaseAdapter {
    Context context;
    boolean isGrid;
    int count;
    LayoutInflater layoutInflater;

    public SimpleAdapter(Context context, boolean isGrid, int count) {
        this.context = context;
        this.isGrid = isGrid;
        this.count = count;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        View view = convertView;
        if (view == null) {
            view = isGrid ? layoutInflater.inflate(R.layout.simple_grid_item, parent, false)
                    : layoutInflater.inflate(R.layout.simple_list_item, parent, false);
            viewHolder = new ViewHolder(view.findViewById(R.id.text_view), view.findViewById(R.id.image_view));
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) (view.getTag());
        }
        Context mContext = parent.getContext();
        switch (position) {
            case 0:
                viewHolder.textView.setText(mContext.getString(R.string.google_plus_title));
                viewHolder.imageView.setImageResource(R.drawable.ic_google_plus_icon);
                break;
            case 1:
                viewHolder.textView.setText(mContext.getString(R.string.google_maps_title));
                viewHolder.imageView.setImageResource(R.drawable.ic_google_maps_icon);
                break;
            case 2:
                viewHolder.textView.setText(context.getString(R.string.google_messenger_title));
                viewHolder.imageView.setImageResource(R.drawable.ic_google_messenger_icon);
                break;
            default:
                viewHolder.textView.setText(context.getString(R.string.google_messenger_title));
                viewHolder.imageView.setImageResource(R.drawable.ic_google_messenger_icon);
                break;

        }
        return view;
    }


    class ViewHolder {
        public TextView textView;
        public ImageView imageView;

        ViewHolder(TextView textView, ImageView imageView) {
            this.textView = textView;
            this.imageView = imageView;
        }
    }

}
