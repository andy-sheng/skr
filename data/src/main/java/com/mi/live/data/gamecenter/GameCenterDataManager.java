package com.mi.live.data.gamecenter;

import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.gamecenter.model.GameInfoModel;
import com.wali.live.proto.GameCenterProto;

public class GameCenterDataManager {

    public static GameInfoModel getGameInfo(long gameId) {
        GameCenterProto.GetGameInfoRsp rsp = GameCenterServerApi.getGameInfoRsp(UserAccountManager.getInstance().getUuidAsLong(), gameId);
        if (rsp != null && rsp.getRet() != 0) {
            GameInfoModel gameInfoModel = new GameInfoModel();
            gameInfoModel.parse(rsp.getGameInfo());
            return gameInfoModel;
        }
        return null;
    }

}
