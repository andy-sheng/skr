package com.wali.live.livesdk.live.liveshow.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.live.engine.base.GalileoConstants;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;
import com.wali.live.livesdk.live.component.utils.PlusParamUtils;
import com.wali.live.livesdk.live.view.BeautyView;
import com.wali.live.proto.CloudParamsProto;
import com.wali.live.statistics.StatisticsKey;

import java.util.Arrays;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by yangli on 2017/03/07.
 *
 * @module 美妆数据
 */
public class MagicParamPresenter extends BaseParamPresenter {
    private static final String TAG = "MagicParamPresenter";

    private static final String KEY_MAGIC_PARAM_SYNC_TIMESTAMP = "key_magic_param_sync_timestamp";

    private static final String KEY_MAGIC_PARAM_SUPPORT_CODE = "key_magic_param_support_code";

    private final MagicParams mMagicParams = new MagicParams();

    private Subscription mFaceBeautySub;

    public MagicParamPresenter(
            @NonNull IComponentController componentController,
            @NonNull Context context) {
        super(componentController, context);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mFaceBeautySub != null && !mFaceBeautySub.isUnsubscribed()) {
            mFaceBeautySub.unsubscribe();
            mFaceBeautySub = null;
        }
    }

    public void syncMagicParams(final BeautyView.IMagicParamsCallBack magicParamsCallBack) {
        if (mFaceBeautySub != null && !mFaceBeautySub.isUnsubscribed()) {
            return;
        }
        mFaceBeautySub = Observable.just(0)
                .map(new Func1<Integer, Integer>() {
                    @Override
                    public Integer call(Integer integer) {
                        mMagicParams.loadParams(mContext); // 本地加载
                        // 防止频繁拉取
                        final long lastTime = PreferenceUtils.getSettingLong(mContext, KEY_MAGIC_PARAM_SYNC_TIMESTAMP, 0);
                        if (System.currentTimeMillis() - lastTime <= 60 * 1000) {
                            MyLog.w(TAG, "syncMagicParams, but too frequently, just ignore");
                            return 1;
                        }
                        String model = getModelInfo(), version = getAppVersion();
                        if (TextUtils.isEmpty(model)) {
                            MyLog.e(TAG, "syncMagicParams, but get model failed");
                            return -1;
                        }
                        MyLog.w(TAG, "syncMagicParams model=" + model + ", version=" + version);
                        // 从服务器拉取参数
                        CloudParamsProto.GetCameraResponse rsp = getMagicParamsResult(model, version);
                        if (rsp == null || rsp.getErrCode() != ErrorCode.CODE_SUCCESS) {
                            MyLog.e(TAG, "syncMagicParams, but get rsp failed, errCode=" +
                                    (rsp != null ? "" + rsp.getErrCode() : "null"));
                            return -1;
                        }
                        // 解析参数
                        MyLog.w(TAG, "syncMagicParams rsp=" + rsp);
                        mMagicParams.parseParams(rsp);
                        mMagicParams.saveParams(mContext);
                        return 0;
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(this.<Integer>bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer errCode) {
                        magicParamsCallBack.onComplete();
                        if (errCode == 0) {
                            StatisticsAlmightyWorker.getsInstance().recordDelay(StatisticsKey.AC_APP,
                                    StatisticsKey.KEY, StatisticsKey.KEY_FETCH_BEAUTY_FILTER_SUCCESS,
                                    StatisticsKey.TIMES, "1");
                        } else if (errCode == -1) {
                            StatisticsAlmightyWorker.getsInstance().recordDelay(StatisticsKey.AC_APP,
                                    StatisticsKey.KEY, StatisticsKey.KEY_FETCH_BEAUTY_FILTER_FAILED,
                                    StatisticsKey.TIMES, "1");
                        }

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "syncMagicParams failed, exception=" + throwable);
                        magicParamsCallBack.onComplete();
                    }
                });
    }

    private static CloudParamsProto.GetCameraResponse getMagicParamsResult(String model, String version) {
        CloudParamsProto.GetCameraRequest req = CloudParamsProto.GetCameraRequest.newBuilder()
                .setModel(model)
                .setVersion(version)
                .build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_FACE_BEAUTY_PARAMS);
        data.setData(req.toByteArray());
        MyLog.w(TAG, "getMagicParamsResult request:\n" + req.toString());

        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (rspData != null) {
            try {
                CloudParamsProto.GetCameraResponse rsp =
                        CloudParamsProto.GetCameraResponse.parseFrom(rspData.getData());
                MyLog.w(TAG, "getMagicParamsResult response:\n" + rsp.toString());
                return rsp;
            } catch (InvalidProtocolBufferException e) {
                MyLog.e(e.toString());
            }
        } else {
            MyLog.w(TAG, "getMagicParamsResult = null");
        }
        return null;
    }

    @Nullable
    @Override
    protected IAction createAction() {
        return new Action();
    }

    public class Action implements IAction {
        @Override
        public boolean onAction(int source, @Nullable Params params) {
            switch (source) {
                default:
                    break;
            }
            return false;
        }
    }

    public static class MagicParams {
        @Expose
        private int[] beauty = new int[]{ // 可用的美颜级别
                GalileoConstants.BEAUTY_LEVEL_OFF,
                GalileoConstants.BEAUTY_LEVEL_HIGH};
        @Expose
        private boolean isFilter = true; // 滤镜是否可用
        private boolean isExpression = false; // 魔法表情是否可用

        public void loadParams(@NonNull Context context) {
            String codeStr = PreferenceUtils.getSettingString(context, KEY_MAGIC_PARAM_SUPPORT_CODE, "");
            try {
                MagicParams magicParams = new Gson().fromJson(codeStr, MagicParams.class);
                beauty = magicParams.beauty;
                isFilter = magicParams.isFilter;
                isExpression = !PlusParamUtils.isHideExpression(true);
            } catch (Exception e) {
                MyLog.e(TAG, "loadParams failed, exception=" + e);
            }
        }

        private void saveParams(@NonNull Context context) {
            try {
                PreferenceUtils.setSettingString(context, KEY_MAGIC_PARAM_SUPPORT_CODE, new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(this));
                PreferenceUtils.setSettingLong(context, KEY_MAGIC_PARAM_SYNC_TIMESTAMP, System.currentTimeMillis());
            } catch (Exception e) {
                MyLog.e(TAG, "saveParams failed, exception=" + e);
            }
        }

        private void parseParams(@NonNull CloudParamsProto.GetCameraResponse rsp) {
            if (rsp.hasOpenFilter()) {
                isFilter = rsp.getOpenFilter();
            }
            int count = rsp.getCameraLevelsCount();
            if (count > 1) { // 多级美颜
                beauty = new int[count + 1];
                beauty[0] = GalileoConstants.BEAUTY_LEVEL_OFF;
                for (int i = 0; i < count; ++i) {
                    beauty[i + 1] = rsp.getCameraLevels(i);
                }
            } else if (count == 1) { // 单级美颜
                beauty[1] = rsp.getCameraLevels(0);
            }
        }

        /**
         * 美颜是否可用
         */
        public boolean isBeauty() {
            return beauty[1] != GalileoConstants.BEAUTY_LEVEL_OFF;
        }

        /**
         * 是否是多级美颜
         */
        public boolean isMultiBeauty() {
            return beauty.length > 2;
        }

        /**
         * 查找level的索引
         */
        public int findBeautyPos(int level) {
            int index = Arrays.binarySearch(beauty, level);
            return index >= 0 ? index : (beauty.length - 1);
        }

        public int getBeautyLevel(int index) {
            return beauty[index];
        }

        /**
         * 滤镜是否可用
         */
        public boolean isFilter() {
            return isFilter;
        }

        /**
         * 表情是否可用
         */
        public boolean isExpression() {
            // return isExpression; TODO 完善表情模块
            return false;
        }
    }
}
