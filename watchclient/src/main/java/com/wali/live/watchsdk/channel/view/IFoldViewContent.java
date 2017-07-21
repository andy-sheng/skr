package com.wali.live.watchsdk.channel.view;

/**
 * Created by zhaomin on 17-3-9.
 */
public interface IFoldViewContent {

    /**
     * 折叠最短的高度
     */
    int getShortHeight();

    /**
     * 内容展开的高度
     */
    int getFullHeight();

    /**
     * 是否需要折叠
     */
    boolean needFold();

    /**
     * 绑定数据
     */
    void bindData(Object object);
}
