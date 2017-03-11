package com.wali.live.livesdk.live.liveshow.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.live.engine.base.GalileoConstants;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;
import com.wali.live.livesdk.live.component.utils.MagicParamUtils;
import com.wali.live.livesdk.live.component.utils.PlusParamUtils;
import com.wali.live.proto.CloudParamsProto;
import com.wali.live.statistics.StatisticsKey;

import org.json.JSONObject;

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

    private void syncMagicParams() {
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
        private int[] mBeauty = new int[]{GalileoConstants.BEAUTY_LEVEL_HIGH};
        private boolean mFilter = true;
        private int mFilterIntensity = 100;
        private boolean mExpression = false;

        public void loadParams(@NonNull Context context) {
            String codeStr = PreferenceUtils.getSettingString(context, KEY_MAGIC_PARAM_SUPPORT_CODE, "");
            try {
                JSONObject jsonObject = new JSONObject(codeStr);
                mBeauty = (int[]) jsonObject.opt("beauty");
                mFilter = jsonObject.optBoolean("filter");
                mFilterIntensity = MagicParamUtils.getFilterIntensity();
                mExpression = !PlusParamUtils.isHideExpression(true);
            } catch (Exception e) {
                MyLog.e(TAG, "loadParams failed, exception=" + e);
            }
        }

        private void saveParams(@NonNull Context context) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("beauty", mBeauty);
                jsonObject.put("filter", mFilter);
                PreferenceUtils.setSettingString(context, KEY_MAGIC_PARAM_SUPPORT_CODE, jsonObject.toString());
                PreferenceUtils.setSettingLong(context, KEY_MAGIC_PARAM_SYNC_TIMESTAMP, System.currentTimeMillis());
            } catch (Exception e) {
                MyLog.e(TAG, "saveParams failed, exception=" + e);
            }
        }

        private void parseParams(@NonNull CloudParamsProto.GetCameraResponse rsp) {
            if (rsp.hasOpenFilter()) {
                mFilter = rsp.getOpenFilter();
            }
            int count = rsp.getCameraLevelsCount();
            if (count > 1) { // 多级美颜
                mBeauty = new int[count + 1];
                mBeauty[0] = 0;
                for (int i = 0; i < count; ++i) {
                    mBeauty[i + 1] = rsp.getCameraLevels(i);
                }
            } else if (count == 1) { // 单级美颜
                mBeauty[0] = rsp.getCameraLevels(0);
            }
        }

        /**
         * 是否允许开启滤镜
         */
        public boolean isFilter() {
            return mFilter;
        }

        /**
         * 查询滤镜强度
         */
        public int getFilterIntensity() {
            return mFilterIntensity;
        }

        /**
         * 是否允许开启表情
         */
        public boolean isExpression() {
            return mExpression;
        }

        /**
         * 是否允许开启美颜
         */
        public boolean isBeauty() {
            return mBeauty[0] != 0 || mBeauty.length > 1;
        }

        /**
         * 是否是单极美颜
         */
        public boolean isSingleBeauty() {
            return mBeauty.length == 1 && mBeauty[0] != 0;
        }

        /**
         * 是否是多级美颜
         */
        public boolean isMultiBeauty() {
            return mBeauty.length > 1;
        }

        /**
         * 查询多级美颜级别
         */
        public int[] getBeautyLevels() {
            return mBeauty;
        }

    }
}
