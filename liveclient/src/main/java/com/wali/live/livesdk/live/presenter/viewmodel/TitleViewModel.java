package com.wali.live.livesdk.live.presenter.viewmodel;

import com.wali.live.proto.Live2Proto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 17/4/5.
 */
public class TitleViewModel {
    public static final int SOURCE_NORMAL = 0;
    public static final int SOURCE_GAME = 1;
    public static final int SOURCE_DEVICE = 2;

    private int mSource;
    private List<String> mTitleList;

    private int mTitleIndex = 0;

    private TitleViewModel() {
    }

    public TitleViewModel(Live2Proto.TitleInfo protoInfo) throws Exception {
        parse(protoInfo);
    }

    public void parse(Live2Proto.TitleInfo protoInfo) throws Exception {
        if (protoInfo == null) {
            throw new NullPointerException("protoInfo is null");
        }
        if (!checkSource(protoInfo.getSource())) {
            throw new IllegalArgumentException("source is illegal" + protoInfo.getSource());
        }
        mSource = protoInfo.getSource();
        if (mTitleList == null) {
            mTitleList = new ArrayList();
        }
        mTitleList.addAll(protoInfo.getTitleListList());
    }

    private boolean checkSource(int source) {
        return source >= SOURCE_NORMAL && source <= SOURCE_DEVICE;
    }

    public int getSource() {
        return mSource;
    }

    public String nextTitle() {
        if (mTitleList != null && mTitleList.size() > 0) {
            String title = mTitleList.get(mTitleIndex);
            mTitleIndex = (mTitleIndex + 1) % mTitleList.size();
            return title;
        }
        return "";
    }
}
