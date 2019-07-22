package com.common.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

abstract class AbsCoroutineFragment : BaseFragment(), CoroutineScope by MainScope() {
    override fun destroy() {
        super.destroy()
        cancel()
    }
}