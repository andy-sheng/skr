package com.common.flutter.plugin

import com.common.log.MyLog
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.PluginRegistry.Registrar

/**
 * SqflitePlugin Android implementation
 */
object CommonFlutterPlugin : MethodCallHandler {

    private var listeners = ArrayList<MethodHandler>()
    var channel:MethodChannel? = null

    fun registerWith(registrar: Registrar) {
        channel = MethodChannel(registrar.messenger(), "com.commonsdk.SkrCommonFlutterPlugin")
        channel?.setMethodCallHandler(this)
    }

    fun addListener(l: MethodHandler) {
        if (!listeners.contains(l)) {
            listeners.add(l)
        }
    }

    fun removeListener(l: MethodHandler) {
        listeners.remove(l)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        for (h in listeners) {
            val rsp = h.handle(call, result)
            if (rsp) {
                MyLog.d("CommonFlutterPlugin", "${call.method}被${h.name}处理")
                return
            }
        }
        result.notImplemented()
    }

}

abstract class MethodHandler {
    val name: String

    constructor(name: String) {
        this.name = name
    }

    abstract fun handle(call: MethodCall, result: MethodChannel.Result): Boolean

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MethodHandler

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}
