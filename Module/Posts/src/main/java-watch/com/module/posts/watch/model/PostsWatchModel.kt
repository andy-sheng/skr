package com.module.posts.watch.model

import kotlin.random.Random

class PostsWatchModel {

    val imageList= ArrayList<String>()
    init {
        val r = Random(System.currentTimeMillis()).nextInt(3)
        for (i in 0 until r) {
            imageList.add("http://res-static.inframe.mobi/app/remen_2.png")
        }
    }

}