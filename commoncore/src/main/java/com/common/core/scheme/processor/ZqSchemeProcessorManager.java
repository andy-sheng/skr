package com.common.core.scheme.processor;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.common.base.BaseActivity;

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
        mISchemeProcessors.add(new SchemeProcessor());
        mISchemeProcessors.add(new DefaultProcessor());
    }

    public void process(final Uri uri, @NonNull BaseActivity activity) {
        for (ISchemeProcessor processor : mISchemeProcessors) {
            if(processor.accept(uri)){
                processor.process(uri, activity);
                break;
            }
        }
    }
}
