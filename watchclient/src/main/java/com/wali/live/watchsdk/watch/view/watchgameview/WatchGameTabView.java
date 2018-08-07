package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;

/**
 * Created by vera on 2018/8/7.
 * 游戏直播下面的几个tab
 */

public class WatchGameTabView extends RelativeLayout implements
        IComponentView<WatchGameTabView.IPresenter, WatchGameTabView.IView> {

    public WatchGameTabView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public WatchGameTabView.IView getViewProxy() {
        return null;
    }

    @Override
    public void setPresenter(WatchGameTabView.IPresenter iPresenter) {

    }

    public interface  IPresenter {

    }

    public interface IView extends IViewProxy {

    }
}
