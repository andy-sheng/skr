package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;

/**
 * Created by vera on 2018/8/7.
 * 直播顶部浮层 包括返回按钮 分享 横屏时的关注按钮、下载等等
 * 内部处理横竖屏样式
 */

public class WatchGameZTopView extends RelativeLayout implements View.OnClickListener,
        IComponentView<WatchGameZTopView.IPresenter, WatchGameZTopView.IView> {

    public WatchGameZTopView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public IView getViewProxy() {
        return null;
    }

    @Override
    public void setPresenter(IPresenter iPresenter) {

    }


    public interface  IPresenter {

    }

    public interface IView extends IViewProxy {

    }
}
