package com.common.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import java.util.concurrent.atomic.AtomicInteger

/**
 * 通过U.getCommonUtils 获得
 *
 *
 * 一些实在不好分类的 utils 方法,放在这。
 */
class CommonUtils internal constructor() {
    companion object {
         const val FAST_DOUBLE_CLICK_INTERVAL = 500
    }

    private val sNextGeneratedId = AtomicInteger(1)

    private var sLastClickTime: Long = 0

    private val sMainHandler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    fun isMainThread():Boolean = Looper.getMainLooper() == Looper.myLooper()

    fun getUiHandler():Handler{
        return sMainHandler
    }

    /**
     * 判断是否是快速点击
     */
    fun isFastDoubleClick():Boolean = isFastDoubleClick(FAST_DOUBLE_CLICK_INTERVAL.toLong())

    /**
     * 判断是否是快速点击
     *
     * @param time, 时间间隔, 单位为毫秒
     * @return
     */
    fun isFastDoubleClick(time: Long): Boolean {
        if (time <= 0) {
            return true
        }

        val now = System.currentTimeMillis()
        val delta = now - sLastClickTime
        if (delta > 0 && delta < time) {
            return true
        }
        sLastClickTime = now
        return false
    }

    fun generateViewId(): Int {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            while (true) {
                val result = sNextGeneratedId.get()
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                var newValue = result + 1
                if (newValue > 0x00FFFFFF) {
                    newValue = 1
                }
                // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result
                }
            }

        } else {
            return View.generateViewId()
        }
    }

    fun isIntentAvailable(context: Context,
                          intent: Intent): Boolean {
        val packageManager = context.packageManager
        val list = packageManager.queryIntentActivities(
                intent, PackageManager.MATCH_DEFAULT_ONLY)
        return list.size > 0
    }

    /**
     * com.tencent.mm  微信
     * 判断用户手机是否安装了某个app
     * @param pkgName
     * @return
     */
    fun hasInstallApp(pkgName: String?): Boolean {
        if (pkgName == null || pkgName.isEmpty()) {
            return false
        }
        var packageInfo: PackageInfo?
        try {
            packageInfo = U.app().packageManager.getPackageInfo(pkgName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            packageInfo = null
            e.printStackTrace()
        }

        return packageInfo != null
    }


}
