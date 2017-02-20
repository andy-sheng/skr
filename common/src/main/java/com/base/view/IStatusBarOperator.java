package com.base.view;

import java.util.LinkedList;

/**
 * Created by yangli on 16-6-24.
 */
public interface IStatusBarOperator {

    void addSelfToStatusList();

    void removeSelfFromStatusList();

    void adjustStatusBar();

    void restoreStatusBar(boolean isPrevDark, boolean isFromActivity);

    boolean isStatusBarDark();

    boolean isOverrideStatusBar();

    LinkedList<IStatusBarOperator> sStatusBarOperators = new LinkedList<>();

}
