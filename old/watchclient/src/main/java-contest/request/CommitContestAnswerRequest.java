package request;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.LiveSummitProto;

/**
 * Created by liuyanyan on 2018/1/11.
 */
public class CommitContestAnswerRequest extends BaseRequest {
    private final int COMMIT_TIME_OUT = 5_000;
    /**
     * seq 题目号
     * id 题目的选项号
     */
    public CommitContestAnswerRequest(String seq, String id, long zuId, String roomId) {
        super(MiLinkCommand.COMMAND_COMMIT_CONTEST_ANSWER, "CommitContestAnswerRequest");
        build(seq, id, zuId, roomId);
    }

    private void build(String seq, String id, long zuId, String roomId) {
        if (TextUtils.isEmpty(id)) {
            mRequest = LiveSummitProto.CommitContestAnswerReq.newBuilder()
                    .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                    .setSeq(seq)
                    .setZuid(zuId)
                    .setLiveid(roomId)
                    .build();
        } else {
            mRequest = LiveSummitProto.CommitContestAnswerReq.newBuilder()
                    .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                    .setSeq(seq)
                    .setId(id)
                    .setZuid(zuId)
                    .setLiveid(roomId)
                    .build();
        }
    }

    @Override
    protected GeneratedMessage sendSync() {
        if (mRequest == null) {
            MyLog.w(TAG, mAction + " request is null");
            return null;
        }
        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(generateReqData(), COMMIT_TIME_OUT);
        if (rspData != null) {
            try {
                mResponse = parse(rspData.getData());
                MyLog.d(TAG, mAction + " response : \n" + mResponse);
            } catch (Exception e) {
                MyLog.e(TAG, e);
            }
        } else {
            MyLog.w(TAG, mAction + " response is null");
        }
        return mResponse;
    }

    @Override
    protected boolean sendAsync() {
        if (mRequest == null) {
            MyLog.w(TAG, mAction + " request is null");
            return false;
        }
        MiLinkClientAdapter.getsInstance().sendAsync(generateReqData(), COMMIT_TIME_OUT);
        return true;
    }

    @Override
    protected LiveSummitProto.CommitContestAnswerRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return LiveSummitProto.CommitContestAnswerRsp.parseFrom(bytes);
    }
}
