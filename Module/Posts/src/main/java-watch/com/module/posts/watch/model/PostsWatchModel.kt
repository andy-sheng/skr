package com.module.posts.watch.model

import java.io.Serializable
import kotlin.random.Random

class PostsWatchModel : Serializable {
    var isExpend = false  // 文字是否展开
    val imageList= ArrayList<String>()
    init {
        val r = Random(System.currentTimeMillis()).nextInt(3)

        for (i in 0 until r) {
            imageList.add("http://res-static.inframe.mobi/app/remen_2.png")
        }
    }
}