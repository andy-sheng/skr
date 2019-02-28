package com.common.core.scheme.processor;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;

public enum ProcessResult {
    NotAccepted,// 不处理
    AcceptedAndContinue,// 我已经处理了，但是还可以交给下一个处理
    AcceptedAndReturn,// 我已经处理了，并且不希望其他再处理
}
