package com.common.utils

/**
 * dp转px
 */
fun Float.dp(): Int =
        U.getDisplayUtils().dip2px(this)

