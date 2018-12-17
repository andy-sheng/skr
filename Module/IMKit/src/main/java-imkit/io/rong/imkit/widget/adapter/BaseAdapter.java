//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class BaseAdapter<T> extends android.widget.BaseAdapter {
    private Context mContext;
    protected List<T> mList;

    public BaseAdapter() {
        this.mList = new ArrayList();
    }

    public BaseAdapter(Context context) {
        this.mContext = context;
        this.mList = new ArrayList();
    }

    protected <T extends View> T findViewById(View view, int id) {
        return view.findViewById(id);
    }

    public int findPosition(T message) {
        int index = this.getCount();
        int position = -1;

        while (index-- > 0) {
            if (message.equals(this.getItem(index))) {
                position = index;
                break;
            }
        }

        return position;
    }

    public int findPosition(long id) {
        int index = this.getCount();
        int position = -1;

        while (index-- > 0) {
            if (this.getItemId(index) == id) {
                position = index;
                break;
            }
        }

        return position;
    }

    public void addCollection(Collection<T> collection) {
        this.mList.addAll(collection);
    }

    public void addCollection(T... collection) {
        T[] var2 = collection;
        int var3 = collection.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            T t = var2[var4];
            this.mList.add(t);
        }

    }

    public void add(T t) {
        this.mList.add(t);
    }

    public void add(T t, int position) {
        this.mList.add(position, t);
    }

    public void remove(int position) {
        this.mList.remove(position);
    }

    public void removeAll() {
        this.mList.clear();
    }

    public void clear() {
        this.mList.clear();
    }

    public int getCount() {
        return this.mList == null ? 0 : this.mList.size();
    }

    public T getItem(int position) {
        if (this.mList == null) {
            return null;
        } else {
            return position >= this.mList.size() ? null : this.mList.get(position);
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = this.newView(this.mContext, position, parent);
        }

        this.bindView(view, position, this.getItem(position));
        return view;
    }

    protected abstract View newView(Context var1, int var2, ViewGroup var3);

    protected abstract void bindView(View var1, int var2, T var3);
}
