package com.wali.live.watchsdk.component.view;

import com.wali.live.proto.ShareProto;

import java.util.List;

/**
 * Created by zyh on 2017/4/26.
 */

public interface IShareView {
    void notifyShareControlPanel(List<ShareProto.TagTail> tagTail);
}
