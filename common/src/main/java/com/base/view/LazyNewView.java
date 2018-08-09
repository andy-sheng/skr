package com.base.view;

import android.view.View;


public abstract class LazyNewView<V extends View> {
    V view;

    public abstract V newView();

    public V getView() {
        if (view == null) {
            view = newView();
        }
        return view;
    }
}
