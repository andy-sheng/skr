package com.imagebrowse.big;

import android.widget.TextView;

import com.common.callback.Callback;
import com.imagebrowse.ImageBrowseView;

import java.util.List;

public class DefaultImageBrowserLoader<T> implements Loader<T> {
    @Override
    public void init() {

    }

    @Override
    public void load(ImageBrowseView imageBrowseView, int position, T item) {

    }

    @Override
    public void loadUpdater(TextView textView, int position, T item) {

    }

    @Override
    public int getInitCurrentItemPostion() {
        return 0;
    }

    @Override
    public List<T> getInitList() {
        return null;
    }

    @Override
    public void loadMore(boolean backward, int position, T data, Callback<List<T>> callback) {

    }

    @Override
    public boolean hasMore(boolean backward, int position, T data) {
        return false;
    }

    @Override
    public boolean hasDeleteMenu() {
        return false;
    }

    @Override
    public boolean hasSaveMenu() {
        return false;
    }

    @Override
    public Callback<T> getDeleteListener() {
        return null;
    }

}
