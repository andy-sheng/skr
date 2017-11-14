package com.wali.live.component.view;

import android.support.annotation.NonNull;
import android.view.View;

import com.thornbirds.component.presenter.IEventPresenter;
import com.thornbirds.component.view.IEventView;

import java.lang.ref.Reference;

/**
 * Created by yangli on 2017/11/13.
 */
public class Utils {

    private Utils() {
    }

    public static <T> T deRef(Reference<T> reference) {
        return reference != null ? reference.get() : null;
    }

    public static void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    public static void $component(@NonNull IEventView view, @NonNull IEventPresenter presenter) {
        presenter.setView(view.getViewProxy());
        view.setPresenter(presenter);
    }
}
