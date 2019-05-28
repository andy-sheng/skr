package com.module;

import com.alibaba.android.arouter.launcher.ARouter;
import com.module.msg.IMsgService;

public class ModuleServiceManager {
    private static class ModuleServiceManagerHolder {
        private static final ModuleServiceManager INSTANCE = new ModuleServiceManager();
    }

    private ModuleServiceManager() {

    }

    public static final ModuleServiceManager getInstance() {
        return ModuleServiceManagerHolder.INSTANCE;
    }

    public IMsgService getMsgService(){
        return (IMsgService) ARouter.getInstance().build(RouterConstants.SERVICE_MSG).navigation();
    }
}
