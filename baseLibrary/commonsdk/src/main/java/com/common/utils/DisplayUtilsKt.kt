package com.common.utils

/**
 * dp转px
 */
fun Float.dp(): Int =
        U.getDisplayUtils().dip2px(this)


/**
 * dp转px
 */
fun Int.dp(): Int =
        U.getDisplayUtils().dip2px(this.toFloat())

