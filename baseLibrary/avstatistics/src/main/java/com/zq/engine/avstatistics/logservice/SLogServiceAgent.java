package com.zq.engine.avstatistics.logservice;


import android.content.Context;

/**
 *
 */
public class SLogServiceAgent {
    private static final int LS_PROVIDER_NONE = 0;
    public  static final int LS_PROVIDER_ALIYUN         = LS_PROVIDER_NONE + 1;
    public  static final int LS_PROVIDER_TO_BE_DESIGNED = LS_PROVIDER_NONE + 2;


    public static SLogServiceAliyun gAliyun = null;

    private SLogServiceAgent() {}


    /**
     * @param providerID refer to {@link this#LS_PROVIDER_ALIYUN and other ID for more info}
     */
    public static SLogServiceBase getService(int providerID){

        SLogServiceBase ls = null;

        switch (providerID) {
            case LS_PROVIDER_ALIYUN:
                {
                    if (null == gAliyun) {
                        gAliyun = new SLogServiceAliyun();
                    }
                    ls = gAliyun;
                }
                break;
            default:
                ls = null;
                break;
        }
        return ls;
    }


    /**
     * this is for {@link SLogServiceBase#init(Object)} which is the type of {@link this#LS_PROVIDER_ALIYUN};
     */
    public static class AliYunSLInitParam{
        public Context appCtx;
        public long skrUid;

        //TODO: to be designed

    }


}
