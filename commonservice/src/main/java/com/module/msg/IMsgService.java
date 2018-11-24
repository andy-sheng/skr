package com.module.msg;

import com.alibaba.android.arouter.facade.template.IProvider;
import com.module.common.ICallback;

public interface IMsgService extends IProvider {
    void joinChatRoom(String roomId, ICallback callback);
    void leaveChatRoom(String roomId);
}
