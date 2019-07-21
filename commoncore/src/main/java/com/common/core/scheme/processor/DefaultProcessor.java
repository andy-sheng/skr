package com.common.core.scheme.processor;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.common.base.BaseActivity;
import com.common.core.scheme.SchemeConstants;
import com.common.log.MyLog;

public class DefaultProcessor implements ISchemeProcessor {
    public final String TAG = SchemeConstants.LOG_PREFIX + "DefaultProcessor";


    @Override
    public ProcessResult process(Uri uri, boolean beforeHomeExistJudge) {
        return ProcessResult.NotAccepted;
    }
}
