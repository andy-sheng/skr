package com.common.core.scheme.processor;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.common.base.BaseActivity;

public interface ISchemeProcessor {

    /**
     *
     * @param uri
     * @param beforeHomeExistJudge 在home是否存在的判断之前，true的话就不确定home是否存在
     *                             false 的话 home肯定存在
     * @return
     */
    ProcessResult process(Uri uri,boolean beforeHomeExistJudge);
}
