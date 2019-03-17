package com.common.core.scheme.processor;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.ArrayList;

public class ZqSchemeProcessorManager {
    private static class ZqSchemeProcessorManagerHolder {
        public static ZqSchemeProcessorManager sZqSchemeProcessorManager = new ZqSchemeProcessorManager();
    }

    private ArrayList<ISchemeProcessor> mISchemeProcessors = new ArrayList<>();

    public static ZqSchemeProcessorManager getInstance() {
        return ZqSchemeProcessorManagerHolder.sZqSchemeProcessorManager;
    }

    private ZqSchemeProcessorManager() {
        mISchemeProcessors.add(new InframeProcessor());
        mISchemeProcessors.add(new DefaultProcessor());
    }

    public ProcessResult process(final Uri uri, @NonNull Activity activity,boolean beforeHomeExistJudge) {
        for (ISchemeProcessor processor : mISchemeProcessors) {
            ProcessResult processResult = processor.process(uri,beforeHomeExistJudge);
            if(processResult == ProcessResult.AcceptedAndReturn){
                return processResult;
            }
        }
        return ProcessResult.NotAccepted;
    }

}
