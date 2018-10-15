package com.wali.live.watchsdk.bigturntable.contact;

import com.mi.live.data.repository.model.turntable.PrizeItemModel;

import java.util.List;

/**
 * Created by zhujianning on 18-7-11.
 */

public class BigTurnTableViewContact {
    public interface IView {
        void loadBmpsSuccess();
    }

    public interface IPresenter {
        void loadBmps(List<PrizeItemModel> datas);
    }
}
