package com.wali.live.watchsdk.bigturntable.contact;

import com.mi.live.data.repository.model.turntable.TurnTableConfigModel;
import com.wali.live.proto.BigTurnTableProto;

/**
 * Created by zhujianning on 18-4-14.
 */

public class BigTurnTableContact {

    public interface IView {
        void loadDataSuccess(TurnTableConfigModel data);

        void notifyOpenStatus(BigTurnTableProto.TurntableType type);

        void loadDataFail();

        void openSuccess(BigTurnTableProto.TurntableType type);

        void openFail();

        void closeSuccess(BigTurnTableProto.TurntableType type, String inputTxt, boolean needOpenOtherMode);

        void closeFail();
    }

    public interface IPresenter {
        void loadTurnTableDataByType(long zuid, String roomId);

        void open(long zuid, String roomId, BigTurnTableProto.TurntableType type, String customDes);

        void close(long zuid, String roomId, BigTurnTableProto.TurntableType type, String inputTxt, boolean needCloseOtherMode);

        void switchMode(BigTurnTableProto.TurntableType type);
    }
}
