package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;

/**
 * Created by vera on 2018/8/7.
 * 竖屏时底部的编辑框、送礼按钮等
 */

public class WatchGameBottomEditView extends RelativeLayout implements
        IComponentView<WatchGameBottomEditView.IPresenter, WatchGameBottomEditView.IView> {

    public WatchGameBottomEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
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
