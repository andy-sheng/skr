package com.common.flutter

import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON

class FlutterRoute {
    companion object {
        /**
         * 跳转到一个 flutter 页面
         */
        fun open(router: String, params: HashMap<String, Any>?) {
            val sb = StringBuilder()
            sb.append("flutter://")
            sb.append(router)
            if (params != null) {
                val ps = JSON.toJSONString(params)
                sb.append("?params=").append(ps)
            }
            ARouter.getInstance().build("/flutter/FlutterActivity")
                    .withString("initial_route", sb.toString())
                    .navigation()
        }
    }
}