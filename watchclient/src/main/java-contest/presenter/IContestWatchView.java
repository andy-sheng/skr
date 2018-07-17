package presenter;

import com.base.mvp.IRxView;
import com.mi.live.data.query.model.EnterRoomInfo;
import com.wali.live.proto.LiveProto;
import model.AwardUser;

import java.util.List;

/**
 * Created by liuyanyan on 2018/1/13.
 */

public interface IContestWatchView extends IRxView {
    void processEnterLive(EnterRoomInfo enterRoomInfo);

    void processRoomInfo(LiveProto.RoomInfoRsp rsp);

    void processViewerNum(int num);

    void showAwardListView(List<AwardUser> userList);
}
