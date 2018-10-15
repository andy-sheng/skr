package request;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.LiveSummitProto;

/**
 * Created by zyh on 2018/1/15.
 *
 * @module 冲顶大会房间人数
 */
public class GetContestViewerInfoRequest extends BaseRequest {

    public GetContestViewerInfoRequest(long zuid, String roomId) {
        super(MiLinkCommand.COMMAND_CONTEST_VIEWER_INFO, "GetContestViewerInfoRequest");
        build(zuid, roomId);
    }

    private void build(long zuid, String roomId) {
        mRequest = LiveSummitProto.GetContestViewerInfoReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setHostId(zuid)
                .setLiveId(roomId).build();
    }

    @Override
    protected GeneratedMessage parse(byte[] bytes) throws InvalidProtocolBufferException {
        return LiveSummitProto.GetContestViewerInfoRsp.parseFrom(bytes);
    }
}
