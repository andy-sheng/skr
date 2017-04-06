package com.wali.live.livesdk.live.presenter.cache;

import com.wali.live.livesdk.live.presenter.viewmodel.TitleViewModel;

/**
 * Created by lan on 17/4/5.
 */
public class TitleCache {
    private static final TitleCache sInstance = new TitleCache();

    private TitleViewModel[] mTitleArray = new TitleViewModel[3];

    private TitleCache() {
    }

    public static TitleCache getInstance() {
        return sInstance;
    }

    public TitleViewModel getTitleModel(int source) {
        if (source < 0 || source >= mTitleArray.length) {
            return null;
        }
        return mTitleArray[source];
    }

    public boolean setTitleModel(TitleViewModel titleModel) {
        if (titleModel == null) {
            return false;
        }
        mTitleArray[titleModel.getSource()] = titleModel;
        return true;
    }
}
