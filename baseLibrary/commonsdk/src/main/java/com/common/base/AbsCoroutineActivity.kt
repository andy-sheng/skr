package com.common.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

abstract class AbsCoroutineActivity : BaseActivity(), CoroutineScope by MainScope() {
    override fun destroy() {
        super.destroy()
        cancel()
    }
}