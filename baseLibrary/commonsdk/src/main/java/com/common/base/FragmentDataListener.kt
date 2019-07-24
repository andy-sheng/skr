package com.common.base

import android.os.Bundle

interface FragmentDataListener {
    fun onFragmentResult(requestCode: Int, resultCode: Int, bundle: Bundle?, obj: Any?)
}
