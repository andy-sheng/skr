package com.wali.live.watchsdk.watch.presenter.watchgamepresenter;

import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.watch.view.watchgameview.WatchGameBottomEditView;

/**
 * Created by vera on 2018/8/8.
 */

public class WatchGameBottomEditPresenter extends ComponentPresenter<WatchGameBottomEditView.IView>
        implements WatchGameBottomEditView.IPresenter {


    public WatchGameBottomEditPresenter(IEventController controller) {
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
}
