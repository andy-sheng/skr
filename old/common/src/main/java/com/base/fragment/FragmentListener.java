package com.base.fragment;

/**
 * Created by lan on 15-4-20.
 */
public interface FragmentListener {
    /**
     * true表示该Fragment自己来处理返回键按下事件
     * @return
     */
    boolean onBackPressed();

    void onSelfPopped();

    boolean onHomePressed();
}
