package com.mi.live.data.repository;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.assist.Attachment;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.UserProto;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by zhangzhiyuan on 16-7-5.
 */
public class IdentificationRepository {


    private static final String TAG = IdentificationRepository.class.getSimpleName();
    public static final int LIVE_IDENTIFICATION = 1; //小米直播认证
    public static final int ID_IDENTIFICATION = 2; //实名认证

    public static final int AccountCidType_ID_CARD = 1; //证件照类型：身份证号
    public static final int AccountCidType_PASSPORT = 2; //证件照类型：护照号


    @Inject
    public IdentificationRepository() {

    }

    public int getCaptcha(String phoneNum) {

        int resultCode = -1;
        if (TextUtils.isEmpty(phoneNum)) {
            return resultCode;
        }
        UserProto.GetCaptchaReq getCaptchaReq = UserProto.GetCaptchaReq.newBuilder()
                .setPhoneNum(phoneNum)
                .setType(1).build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_GET_CAPTCHA);
        data.setData(getCaptchaReq.toByteArray());

        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (rspData != null) {
            try {
                UserProto.GetCaptchaRsp rsp = UserProto.GetCaptchaRsp.parseFrom(rspData.getData());
                MyLog.w(TAG + "getCaptcha response : \n" + rsp.toString());
                resultCode = rsp.getRetCode();
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }

        return resultCode;
    }

    public Observable<Integer> verifyCaptcha(final String phoneNum, final String captcha, final String idCardNum, final boolean isPassPortEnable, final int identificationType) {


        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                int retCode = -1;
                if (TextUtils.isEmpty(phoneNum) || TextUtils.isEmpty(captcha)) {
                    subscriber.onNext(retCode);
                    subscriber.onCompleted();
                    return;
                }
                UserProto.VerifyCaptchaReq.Builder builder = UserProto.VerifyCaptchaReq.newBuilder();
                if (!isPassPortEnable) {
                    builder.setIdCardNum(idCardNum);
                }
                UserProto.VerifyCaptchaReq getCaptchaReq = builder
                        .setPhoneNum(phoneNum)
                        .setCaptcha(captcha)
                        .setType(identificationType).build();
                PacketData data = new PacketData();
                data.setCommand(MiLinkCommand.COMMAND_VERIFY_CAPTCHA);
                data.setData(getCaptchaReq.toByteArray());

                PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
                if (rspData != null) {
                    try {
                        UserProto.VerifyCaptchaRsp rsp = UserProto.VerifyCaptchaRsp.parseFrom(rspData.getData());
                        MyLog.w(TAG + "verifyCaptcha response : \n" + rsp.toString());
                        retCode = rsp.getRetCode();
                        subscriber.onNext(retCode);
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                }
                //subscriber.onNext(retCode);
                subscriber.onCompleted();
            }
        });
    }


    public Observable<Integer> applyIdentificationTask(final int type, final long uid,
                                                       final List<Attachment> iCCardImageUrls, final List<Attachment> otherImageUrls, final String inputName, final String inputPhoneNum,
                                                       final String accountCid, final int accountCidType, final String certification, final String worksLink, final String birthDay) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {

                int retCode = applyCertificationReq(type, uid, iCCardImageUrls, otherImageUrls,
                        inputName, inputPhoneNum, accountCid, accountCidType, certification, worksLink, birthDay);
                subscriber.onNext(retCode);
                subscriber.onCompleted();
            }
        });
    }

    public static int applyCertificationReq(int type, long uid, List<Attachment> iCCardImageUrls, List<Attachment> otherImageUrls,
                                            String inputName, String inputPhoneNum, String accountCid, int accountCidType,
                                            String certification, String worksLink, String birthDay) {
        int retCode = -1;
        MyLog.w("applyCertificationReq");
        List<UserProto.PrivateImg> iCCardList = null;
        List<UserProto.PrivateImg> otherCardList = null;

        UserProto.CertificationInfo.Builder builder = UserProto.CertificationInfo.newBuilder();
        builder.setType(type);

        if (type == LIVE_IDENTIFICATION) {
            if (otherImageUrls != null && otherImageUrls.size() > 0) {
                otherCardList = new ArrayList<>(otherImageUrls.size());
                for (Attachment att : otherImageUrls) {
                    UserProto.PrivateImg proofJobs = UserProto.PrivateImg.newBuilder()
                            .setBucket(att.bucketName)
                            .setObjectKey(att.objectKey)
                            .build();
                    otherCardList.add(proofJobs);
                }
            }
            if (!TextUtils.isEmpty(certification)) {
                builder.setCertification(certification);
            }

            if (!TextUtils.isEmpty(worksLink)) {
                builder.setWorks(worksLink);
            }

            if (otherCardList != null) {
                builder.addAllProofJobs(otherCardList);
            }
        }

        if (!TextUtils.isEmpty(birthDay)) {
            builder.setBirthday(birthDay);
        }
        if (!TextUtils.isEmpty(inputPhoneNum)) {
            builder.setPhoneNum(inputPhoneNum);
        }
        if (!TextUtils.isEmpty(accountCid)) {
            builder.setIdCardNum(accountCid);
        }
        if (!TextUtils.isEmpty(inputName)) {
            builder.setName(inputName);
        }

        if (iCCardImageUrls != null && iCCardImageUrls.size() > 0) {
            iCCardList = new ArrayList<>(iCCardImageUrls.size());
            for (Attachment att : iCCardImageUrls) {
                UserProto.PrivateImg idcard = UserProto.PrivateImg.newBuilder()
                        .setBucket(att.bucketName)
                        .setObjectKey(att.objectKey)
                        .build();
                iCCardList.add(idcard);
            }

        }
        if (iCCardList != null) {
            builder.addAllIdcard(iCCardList);
            builder.setCardType(accountCidType);
        }

        builder.setUuid(uid);

        UserProto.CertificationInfo certificationInfo = builder.build();

        UserProto.ApplyCertificationReq applyCertificationReq = UserProto.ApplyCertificationReq.newBuilder()
                .setType(type)
                .setCertificationInfo(certificationInfo.toByteString())
                .build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_APPLY_CERTIFICATION);
        data.setData(applyCertificationReq.toByteArray());

        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (rspData != null) {
            try {
                UserProto.ApplyCertificationRsp rsp = UserProto.ApplyCertificationRsp.parseFrom(rspData.getData());
                MyLog.w(TAG + "  applyCertificationReq response : \n" + rsp.toString());
                retCode = rsp.getRetCode();

            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }

        return retCode;
    }
}
