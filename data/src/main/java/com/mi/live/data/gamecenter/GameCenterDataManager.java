package com.mi.live.data.gamecenter;

import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.gamecenter.model.GameInfoModel;
import com.wali.live.proto.GameCenterProto;

public class GameCenterDataManager {

    public static GameInfoModel getGameInfo(long gameId,String gamePackageName) {
        gameId = 62231086;
        GameCenterProto.GetGameInfoRsp rsp = GameCenterServerApi.getGameInfoRsp(UserAccountManager.getInstance().getUuidAsLong(), gameId,gamePackageName);
        if (rsp != null && rsp.getRet() == 0) {
            GameInfoModel gameInfoModel = new GameInfoModel();
            gameInfoModel.parse(rsp.getGameInfo());
            return gameInfoModel;
        }
        return null;
    }

}
