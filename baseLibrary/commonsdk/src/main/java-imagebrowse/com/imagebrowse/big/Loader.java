package com.imagebrowse.big;

import com.common.callback.Callback;
import com.dialog.list.DialogListItem;
import com.imagebrowse.ImageBrowseView;

import java.util.List;

public interface Loader<T> {
    void init();

    void load(ImageBrowseView imageBrowseView, int position, T item);

    int getInitCurrentItemPostion();

    List<T> getInitList();

    /**
     * 执行在IO线程
     * 一定要判断是向前还是向后
     * backward 为 true，表示要向后加载更多，position 为当前最后一个元素的索引
     * backward 为 false，表示要向前加载更多，position 为当前最前一个元素的索引
     *
     * @param backward
     * @param position 返回新增了多少个元素，主要用在向前load more 时，更正当前元素的索引
     */
    void loadMore(boolean backward, int position, T data, Callback<List<T>> callback);

    /**
     * 一定要判断是向前还是向后
     * backward 为 true，表示要向后加载更多
     * backward 为 false，表示要向前加载更多
     *
     * @param backward
     */
    boolean hasMore(boolean backward, int position, T data);

    /**
     * 是否有更多菜单栏
     * @return
     */
    boolean hasMenu();

    /**
     * 更多菜单栏中是否有删除按钮
     * @return
     */
    boolean hasDeleteMenu();

    Callback<T> getDeleteListener();
}
