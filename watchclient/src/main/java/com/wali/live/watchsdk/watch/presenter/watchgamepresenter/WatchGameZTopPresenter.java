package com.wali.live.watchsdk.watch.presenter.watchgamepresenter;

import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.watch.view.watchgameview.WatchGameZTopView;

import static com.wali.live.component.BaseSdkController.MSG_FORCE_ROTATE_SCREEN;

/**
 * Created by vera on 2018/8/8.
 */

public class WatchGameZTopPresenter extends ComponentPresenter<WatchGameZTopView.IView>
        implements WatchGameZTopView.IPresenter {


    public WatchGameZTopPresenter(IEventController controller) {
        super(controller);
    }

    @Override
    protected String getTAG() {
        return null;
    }

    @Override
    public boolean onEvent(int i, IParams iParams) {
        return false;
    }

    @Override
    public void forceRotate() {
        postEvent(MSG_FORCE_ROTATE_SCREEN);
    }
}
