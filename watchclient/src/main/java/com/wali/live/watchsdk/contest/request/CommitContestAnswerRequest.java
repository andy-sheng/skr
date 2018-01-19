package com.wali.live.watchsdk.contest.request;

import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.LiveSummitProto;

/**
 * Created by liuyanyan on 2018/1/11.
 */
public class CommitContestAnswerRequest extends BaseRequest {
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
    protected LiveSummitProto.CommitContestAnswerRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return LiveSummitProto.CommitContestAnswerRsp.parseFrom(bytes);
    }
}
