//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit;

import android.content.Context;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.rong.common.RLog;
import io.rong.imkit.IExtensionModule;
import io.rong.imkit.emoticon.AndroidEmoji;
import io.rong.imkit.utilities.RongUtils;
import io.rong.imlib.model.Message;

public class RongExtensionManager {
    private static final String TAG = "RongExtensionManager";
    private static String mAppKey;
    private static List<IExtensionModule> mExtModules = new ArrayList();
    private static final String DEFAULT_REDPACKET = "com.jrmf360.rylib.modules.JrmfExtensionModule";
    private static final String DEFAULT_BQMM = "com.melink.bqmmplugin.rc.BQMMExtensionModule";
    private static final String DEFAULT_RC_STICKER = "cn.rongcloud.sticker.StickerExtensionModule";

    private RongExtensionManager() {
        if (mExtModules != null) {
            try {
                Class<?> cls = Class.forName("com.jrmf360.rylib.modules.JrmfExtensionModule");
                Constructor<?> constructor = cls.getConstructor();
                IExtensionModule jrmf = (IExtensionModule) constructor.newInstance();
                RLog.i("RongExtensionManager", "add module " + jrmf.getClass().getSimpleName());
                mExtModules.add(jrmf);
                jrmf.onInit(mAppKey);
            } catch (Exception var4) {
                ;
            }

            this.checkBQMM();
            this.checkRCBQ();
        }

    }

    public static io.rong.imkit.RongExtensionManager getInstance() {
        return io.rong.imkit.RongExtensionManager.SingletonHolder.sInstance;
    }

    static void init(Context context, String appKey) {
        RLog.d("RongExtensionManager", "init");
        AndroidEmoji.init(context);
        RongUtils.init(context);
        mAppKey = appKey;
    }

    public void registerExtensionModule(IExtensionModule extensionModule) {
        if (mExtModules == null) {
            RLog.e("RongExtensionManager", "Not init in the main process.");
        } else if (extensionModule != null && !mExtModules.contains(extensionModule)) {
            RLog.i("RongExtensionManager", "registerExtensionModule " + extensionModule.getClass().getSimpleName());
            if (mExtModules.size() <= 0 || !((IExtensionModule) mExtModules.get(0)).getClass().getCanonicalName().equals("com.jrmf360.rylib.modules.JrmfExtensionModule") && !((IExtensionModule) mExtModules.get(0)).getClass().getCanonicalName().equals("com.melink.bqmmplugin.rc.BQMMExtensionModule")) {
                mExtModules.add(extensionModule);
            } else {
                mExtModules.add(0, extensionModule);
            }

            extensionModule.onInit(mAppKey);
        } else {
            RLog.e("RongExtensionManager", "Illegal extensionModule.");
        }
    }

    public void unregisterExtensionModule(IExtensionModule extensionModule) {
        if (mExtModules == null) {
            RLog.e("RongExtensionManager", "Not init in the main process.");
        } else if (extensionModule != null && mExtModules.contains(extensionModule)) {
            RLog.i("RongExtensionManager", "unregisterExtensionModule " + extensionModule.getClass().getSimpleName());
            Iterator iterator = mExtModules.iterator();

            while (iterator.hasNext()) {
                if (((IExtensionModule) iterator.next()).equals(extensionModule)) {
                    iterator.remove();
                }
            }

        } else {
            RLog.e("RongExtensionManager", "Illegal extensionModule.");
        }
    }

    public List<IExtensionModule> getExtensionModules() {
        return mExtModules;
    }

    void connect(String token) {
        if (mExtModules != null) {
            Iterator var2 = mExtModules.iterator();

            while (var2.hasNext()) {
                IExtensionModule extensionModule = (IExtensionModule) var2.next();
                extensionModule.onConnect(token);
            }

        }
    }

    void disconnect() {
        if (mExtModules != null) {
            Iterator var1 = mExtModules.iterator();

            while (var1.hasNext()) {
                IExtensionModule extensionModule = (IExtensionModule) var1.next();
                extensionModule.onDisconnect();
            }

        }
    }

    void onReceivedMessage(Message message) {
        Iterator var2 = mExtModules.iterator();

        while (var2.hasNext()) {
            IExtensionModule extensionModule = (IExtensionModule) var2.next();
            extensionModule.onReceivedMessage(message);
        }

    }

    private void checkBQMM() {
        try {
            Class<?> cls = Class.forName("com.melink.bqmmplugin.rc.BQMMExtensionModule");
            Constructor<?> constructor = cls.getConstructor();
            IExtensionModule bqmm = (IExtensionModule) constructor.newInstance();
            RLog.i("RongExtensionManager", "add module " + bqmm.getClass().getSimpleName());
            mExtModules.add(bqmm);
            bqmm.onInit(mAppKey);
        } catch (Exception var4) {
            RLog.i("RongExtensionManager", "Can't find com.melink.bqmmplugin.rc.BQMMExtensionModule");
        }

    }

    private void checkRCBQ() {
        try {
            Class<?> cls = Class.forName("cn.rongcloud.sticker.StickerExtensionModule");
            Constructor<?> constructor = cls.getConstructor();
            IExtensionModule rcbq = (IExtensionModule) constructor.newInstance();
            RLog.i("RongExtensionManager", "add module " + rcbq.getClass().getSimpleName());
            mExtModules.add(rcbq);
            rcbq.onInit(mAppKey);
        } catch (Exception var4) {
            RLog.i("RongExtensionManager", "Can't find cn.rongcloud.sticker.StickerExtensionModule");
        }

    }

    private static class SingletonHolder {
        static io.rong.imkit.RongExtensionManager sInstance = new io.rong.imkit.RongExtensionManager();

        private SingletonHolder() {
        }
    }
}
