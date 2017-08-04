package com.wali.live.watchsdk.component.presenter;

import android.support.annotation.NonNull;

import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.component.view.ImagePagerView;

/**
 * Created by lan on 2017/07/17.
 *
 * @module 模仿viewpager增加上下滑动的效果
 */
public class ImagePagerPresenter extends ComponentPresenter<ImagePagerView.IView>
        implements ImagePagerView.IPresenter {
    private static final String TAG = "ImagePagerPresenter";

    @Override
    protected String getTAG() {
        return TAG;
    }

    public ImagePagerPresenter(@NonNull IEventController controller) {
        super(controller);
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        return false;
    }
}
