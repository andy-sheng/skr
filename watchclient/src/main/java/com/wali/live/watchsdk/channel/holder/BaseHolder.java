package com.wali.live.watchsdk.channel.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.base.utils.display.DisplayUtils;
import com.wali.live.watchsdk.channel.holder.listener.JumpListener;
import com.wali.live.watchsdk.channel.viewmodel.BaseViewModel;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description ViewHolder抽象基类：提供bindModel，bindModel，jumpListener等基本设置功能
 */
public abstract class BaseHolder<VM extends BaseViewModel> extends RecyclerView.ViewHolder {
    protected final String TAG = getTAG() + this.hashCode();

    // 整体view的两边边距
    protected static final int SIDE_MARGIN = DisplayUtils.dip2px(3.33f);
    // view内的中间边距
    protected static final int MIDDLE_MARGIN = DisplayUtils.dip2px(1.33f);

    // 一排两个时， view内的中间边距
    protected static final int MIDDLE_MARGIN_TWO = DisplayUtils.dip2px(6.67f);
    // 一排三个时， view内的中间边距
    protected static final int MIDDLE_MARGIN_THREE = DisplayUtils.dip2px(5f);
    // 长图图片的宽高比
    protected static final float IMAGE_RATIO = 9f / 16;
    // 高图图片的宽高比
    protected static final float HIGH_IMAGE_RATIO = 9f / 14;
    // 高图图片的宽高比
    protected static final float NEW_IMAGE_RATIO = 3f / 4;

    protected VM mViewModel;

    protected int mPosition;
    protected JumpListener mJumpListener;

    protected long mChannelId;

    public BaseHolder(View itemView) {
        super(itemView);

        initView();
    }

    protected String getTAG() {
        return getClass().getSimpleName();
    }

    public <H extends BaseHolder> H get() {
        return (H) this;
    }

    protected abstract void initView();

    public void bindNull() {
        bindView();
    }

    public void bindModel(VM viewModel) {
        mViewModel = viewModel;
        bindView();
    }

    public void bindModel(VM viewModel, int position) {
        mPosition = position;
        bindModel(viewModel);
    }

    protected abstract void bindView();

    public void setJumpListener(JumpListener listener) {
        mJumpListener = listener;
    }

    protected <V extends View> V $(int id) {
        return (V) itemView.findViewById(id);
    }

    protected <V extends View> V $(ViewGroup parent, int id) {
        return (V) (parent.findViewById(id));
    }

    public void setChannelData(long channelId) {
        mChannelId = channelId;
        postChannelData();
    }

    protected void postChannelData() {
    }
}
