package com.common.core.global.event;

import com.orhanobut.dialogplus.DialogPlus;

public class ShowDialogInHomeEvent {
    public DialogPlus mDialogPlus;
    public int mSeq;
    public ShowDialogInHomeEvent(DialogPlus dialogPlus,int seq) {
        this.mDialogPlus = dialogPlus;
        this.mSeq = seq;
    }
}
