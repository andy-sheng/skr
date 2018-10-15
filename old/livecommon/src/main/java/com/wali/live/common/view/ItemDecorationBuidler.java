package com.wali.live.common.view;

/**
 * Created by caoxiangyu on 17-3-15.
 */
public class ItemDecorationBuidler {
    private SpaceItemDecoration decoration = new SpaceItemDecoration();

    public SpaceItemDecoration build() {
        return decoration;
    }

    public ItemDecorationBuidler setLeft(int left) {
        decoration.setLeft(left);
        return this;
    }

    public ItemDecorationBuidler setRight(int right) {
        decoration.setRight(right);
        return this;
    }

    public ItemDecorationBuidler setBottom(int bottom) {
        decoration.setBottom(bottom);
        return this;
    }

    public ItemDecorationBuidler setTop(int top) {
        decoration.setTop(top);
        return this;
    }
}
