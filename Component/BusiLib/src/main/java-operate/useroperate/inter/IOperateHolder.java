package useroperate.inter;

import android.view.View;

import com.common.base.BaseActivity;

import java.lang.ref.WeakReference;

public interface IOperateHolder<T> {
    void init(WeakReference<BaseActivity> weakReference, View view);

    default void bindData(int pos, T t) {

    }
}
