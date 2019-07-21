package com.common.mvp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

abstract class AbsCoroutinePresenter : RxLifeCyclePresenter(), CoroutineScope by MainScope() {

    override fun destroy() {
        super.destroy()
        cancel()
    }
}